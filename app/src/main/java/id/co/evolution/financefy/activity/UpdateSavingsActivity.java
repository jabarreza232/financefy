package id.co.evolution.financefy.activity;

import static id.co.evolution.financefy.callback.CallbackOnActivityResult.REQUEST_CODE_SAVINGS;
import static id.co.evolution.financefy.helper.Tools.REQUEST_CODE;
import static id.co.evolution.financefy.helper.Tools.getFileFromUri;
import static id.co.evolution.financefy.helper.Tools.getFileName;
import static id.co.evolution.financefy.helper.Tools.getFormattedDateSimple;
import static id.co.evolution.financefy.helper.Tools.getFormattedMonthSimple;
import static id.co.evolution.financefy.helper.Tools.getRealPathFromURI;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.camera.core.ExperimentalGetImage;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.google.ai.edge.litertlm.Conversation;
import com.google.ai.edge.litertlm.ConversationConfig;
import com.google.ai.edge.litertlm.Engine;
import com.google.ai.edge.litertlm.Message;
import com.google.ai.edge.litertlm.MessageCallback;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import id.co.evolution.financefy.R;
import id.co.evolution.financefy.databinding.ActivityCreateSavingsProgressBinding;
import id.co.evolution.financefy.dialog.DialogCalculator;
import id.co.evolution.financefy.dialog.DialogConfirm;
import id.co.evolution.financefy.dialog.DialogPreviewImage;
import id.co.evolution.financefy.helper.HelperResultLLM;
import id.co.evolution.financefy.helper.TinyDb;
import id.co.evolution.financefy.helper.Tools;
import id.co.evolution.financefy.model.ModelPrimaryColor;
import id.co.evolution.financefy.model.ModelSavings;
import id.co.evolution.financefy.model.ModelSavingsProgress;
import id.co.evolution.financefy.repository.SavingsProgressRepository;
import id.co.evolution.financefy.repository.SavingsRepository;
import id.co.evolution.financefy.viewmodel.ViewModelSavings;
import id.co.evolution.financefy.viewmodel.ViewModelSavingsProgress;

@ExperimentalGetImage @AndroidEntryPoint
public class UpdateSavingsActivity extends BaseFinanceActivity implements View.OnClickListener {
    String date = "";
    private String jumlah = "";
    String month = "";
    ActivityCreateSavingsProgressBinding binding;
    Calendar cur_calendar = Calendar.getInstance();
    ViewModelSavingsProgress viewModelSavingsProgress;
    ViewModelSavings viewModelSaving;
    DialogCalculator dialogCalculator;

    @Inject
    TinyDb tinyDb;
    public ModelPrimaryColor modelPrimaryColor = Tools.modelPrimaryColor;

    @Inject
    SavingsProgressRepository savingsProgressRepository;
    @Inject
    SavingsRepository savingsRepository;
    ModelSavings modelSavings;
    ModelSavingsProgress modelSavingsProgress;
    int position;
    Locale locale;
    File filePhoto;
    DialogPreviewImage dialogPreviewImage;
    Uri imageUri;
    boolean isFromScanImage;

    private Engine litertEngine;
    private Conversation litertConversation;
    private String currentOcrText = "";
    private BottomSheetDialog loadingOcrDialog;

    private HelperResultLLM helperResultLLM;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        modelPrimaryColor = tinyDb.getObject("model_primary_color", ModelPrimaryColor.class);
        Tools.setThemeNoActionBarActivity(getTheme(), modelPrimaryColor);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_create_savings_progress);
        dialogPreviewImage = new DialogPreviewImage(this);
        Tools.setBackgroundColorView(binding.rlBackground, modelPrimaryColor);
        Tools.setImageTintView(binding.btnCalculator, modelPrimaryColor);
        Tools.setImageTintView(binding.imgScanStruk, modelPrimaryColor);
        Tools.setImageTintView(binding.imgBack, modelPrimaryColor);
        Tools.setBackgroundTintView(binding.placeSubmit, modelPrimaryColor);

        Tools.setTextColorView(binding.txtHeader, modelPrimaryColor);
        Tools.setTextColorView(binding.txtDetail, modelPrimaryColor);
        Tools.setTextColorView(binding.txtScanStruk, modelPrimaryColor);
        Tools.setTextColorView(binding.txtInformation, modelPrimaryColor);
        Tools.setTextColorView(binding.txtEvidence, modelPrimaryColor);
        Tools.setTextColorView(binding.txtCamera, modelPrimaryColor);
        Tools.setTextColorView(binding.txtGallery, modelPrimaryColor);
        Tools.setImageTintView(binding.imgCamera, modelPrimaryColor);
        Tools.setImageTintView(binding.imgGallery, modelPrimaryColor);
        Tools.setImageTintView(binding.imgCalendar, modelPrimaryColor);

        cur_calendar.get(Calendar.YEAR);
        cur_calendar.get(Calendar.MONTH);
        cur_calendar.get(Calendar.DAY_OF_MONTH);
        long date_ship_milis = cur_calendar.getTimeInMillis();
        viewModelSavingsProgress = new ViewModelProvider(this).get(ViewModelSavingsProgress.class);
        viewModelSaving = new ViewModelProvider(this).get(ViewModelSavings.class);
        viewModelSavingsProgress.init(savingsProgressRepository);
        viewModelSaving.init(savingsRepository);

        binding.txtHeader.setText("Edit Progress Tabungan");

        binding.txtDate.setText(getFormattedDateSimple(date_ship_milis));
        date = getFormattedDateSimple(date_ship_milis);
        month = getFormattedMonthSimple(date_ship_milis);
        modelSavings = (ModelSavings) getIntent().getSerializableExtra("savings");
        locale = modelSavings.getType_currency().equalsIgnoreCase("IDR") ? Tools.getLocaleIDN() : Tools.getLocaleUS();

        helperResultLLM = new HelperResultLLM(map -> {
            binding.etAmount.setText(map.get("total"));
            binding.txtDate.setText(map.get("tanggal"));
            binding.etDescription.setText(map.get("description"));
            if (map.get("title") != null) {
                binding.etTitle.setText(map.get("title"));
            }
        });

        position = getIntent().getIntExtra("position", 0);
        viewModelSavingsProgress.findSavingsById(getIntent().getIntExtra("id", 0), modelSavings.getType_currency()).observe(this, modelSavingsProgress -> {
            this.modelSavingsProgress = modelSavingsProgress;
            loadData();
        });

        new Thread(() -> {
            helperResultLLM.initLlmInference(this);

            runOnUiThread(() -> {
                litertEngine = helperResultLLM.getLitertEngine();

                if (litertEngine != null) {
                    binding.btnScan.setEnabled(true);
                    Toast.makeText(this, "AI Siap Digunakan!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "AI Belum Terunduh", Toast.LENGTH_SHORT).show();
                    binding.btnScan.setEnabled(true);
                }
            });
        }).start();

        binding.etAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().equals(jumlah)) {
                    binding.etAmount.removeTextChangedListener(this);
                    String cleanString = s.toString().replaceAll("[Rp,.$]", "");
                    if (!cleanString.isEmpty()) {
                        double parsed = Double.parseDouble(cleanString);
                        String formatted = Tools.convertToCurrency(parsed, locale);

                        jumlah = formatted;
                        binding.etAmount.setText(formatted);
                        binding.etAmount.setSelection(formatted.length());
                    }

                    binding.etAmount.addTextChangedListener(this);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        binding.placeDate.setOnClickListener(this);

        binding.imgBack.setOnClickListener(this);
        binding.placeSubmit.setOnClickListener(this);
        binding.btnCalculator.setOnClickListener(this);

        // Handler untuk Kamera
        binding.btnCamera.setOnClickListener(this);

        // Handler untuk Galeri
        binding.btnGallery.setOnClickListener(this);
        binding.tvFileName.setOnClickListener(this);
        binding.btnClose.setOnClickListener(this);
        binding.btnScan.setOnClickListener(this);
    }

    private void loadData() {
        cur_calendar.get(Calendar.YEAR);
        cur_calendar.get(Calendar.MONTH);
        cur_calendar.get(Calendar.DAY_OF_MONTH);

        jumlah = Tools.convertToCurrency(modelSavingsProgress.getProcessValue(), locale);
        date = modelSavingsProgress.getDate_progress_savings();
        month = modelSavingsProgress.getMonth();

        binding.txtDate.setText(date);
        binding.etTitle.setText(modelSavingsProgress.getTitle());
        binding.etAmount.setText(jumlah);
        if (modelSavingsProgress.getPhoto() != null) {
            filePhoto = new File(modelSavingsProgress.getPhoto());
            String filePath = new File(filePhoto.getPath()).getAbsolutePath();
            String fileName = new File(filePath).getName();
            binding.rlPreviewImage.setVisibility(View.VISIBLE);
            binding.tvFileName.setText(fileName);
        }
        binding.etDescription.setText(modelSavingsProgress.getDescription());
    }

    public void showDatePickerDialog() {
        if (modelPrimaryColor == null) modelPrimaryColor = Tools.modelPrimaryColor;

        DatePickerDialog datePickerDialog = DatePickerDialog.newInstance((view, year, monthOfYear, dayOfMonth) -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, monthOfYear);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            long date_ship_milis = calendar.getTimeInMillis();
            binding.txtDate.setText(getFormattedDateSimple(date_ship_milis));
            date = getFormattedDateSimple(date_ship_milis);

            Log.e("TAG", "onDateSet: " + date);
            month = getFormattedMonthSimple(date_ship_milis);
        });

        datePickerDialog.setYearRange(cur_calendar.get(Calendar.YEAR), cur_calendar.get(Calendar.YEAR));
        datePickerDialog.setMaxDate(cur_calendar);
        datePickerDialog.setAccentColor(getResources().getColor(modelPrimaryColor.getColorPrimary()));
        datePickerDialog.show(getSupportFragmentManager(), "PickerDialog");
    }

    @Override
    public void onClick(View v) {
        String resourceName = getResources().getResourceEntryName(v.getId());
        switch (resourceName) {
            case "tvFileName":
                dialogPreviewImage.show(filePhoto.getPath());
                break;
            case "btnCamera":
                isFromScanImage = false;
                if (checkCameraPermission()) {
                    openCamera();
                } else {
                    requestCameraPermission.launch(android.Manifest.permission.CAMERA);
                }
                break;
            case "btnClose":
                binding.rlPreviewImage.setVisibility(View.GONE);
                filePhoto = null;
                break;
            case "btn_scan":
                isFromScanImage = true;
                boolean isLlmActive = tinyDb.getBoolean("isSwitchLLM", true);
                if (isLlmActive) {
                    // Mode AI MATI -> Tampilkan Dialog Edukasi & Blokir Kamera
                    showDialogChoosePicture();

                } else {
                    helperResultLLM.showLlmDisabledBottomSheet(this,getLayoutInflater(), modelPrimaryColor);
                }
                break;
            case "btnGallery":
                isFromScanImage = false;
                String permission = getGalleryPermission();
                if (permission != null && ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{permission}, REQUEST_CODE);
                } else {
                    openGallery();
                }
                break;
            case "place_date":
                showDatePickerDialog();
                break;
            case "img_back":
                finish();
                break;
            case "btn_calculator":
                dialogCalculator = new DialogCalculator(this, getLayoutInflater(), result -> {
                    jumlah = result;
                    binding.etAmount.setText(Tools.convertToCurrency(result, locale));
                });
                dialogCalculator.show();
                break;
            case "place_submit":
                if (binding.etTitle.getText().toString().isEmpty()) {
                    binding.tilTitle.setError("Silahkan input judul terlebih dahulu");
                } else {
                    binding.tilTitle.setError(null);
                }

                if (binding.etAmount.getText().toString().isEmpty()) {
                    binding.tilAmount.setError("Silahkan input jumlah mata uang anda terlebih dahulu");
                } else {
                    binding.tilAmount.setError(null);
                }

                if (!binding.etAmount.getText().toString().isEmpty()) {
                    DialogConfirm dialogConfirm = new DialogConfirm(this, getLayoutInflater(), new DialogConfirm.DialogConfirm() {
                        @Override
                        public void onSubmit(@NonNull String result) {
                            if (result.equalsIgnoreCase("yes")) {
                                ModelSavingsProgress model = new ModelSavingsProgress();
                                model.setId(getIntent().getIntExtra("id", 0));
                                model.setDate_progress_savings(date);
                                model.setMonth(month);
                                model.setProcessValue(Long.parseLong(Tools.convertCurrencyToValue(jumlah)));
                                model.setDescription(binding.etDescription.getText().toString().trim());
                                model.setTitle(binding.etTitle.getText().toString().trim());
                                model.setId_savings(modelSavings.getId());
                                model.setType_currency(tinyDb.getString("currency"));
                                if (filePhoto != null)
                                    model.setPhoto(filePhoto.getPath());
                                UpdateSavingsActivity.this.onSubmit(model);
                                Intent intent = new Intent();
                                intent.putExtra("savings_progress", model);
                                intent.putExtra("position", position);
                                setResult(REQUEST_CODE_SAVINGS, intent);
                                finish();
                                Toast.makeText(UpdateSavingsActivity.this, "Catatan progress menabung berhasil di ubah !", Toast.LENGTH_SHORT).show();

                            }
                        }
                    });
                    dialogConfirm.showDialogConfirm("Update", "Apakah anda yakin ingin update data ?");
                }
                break;
        }
    }

    private void onSubmit(ModelSavingsProgress model) {
        viewModelSavingsProgress.inputUpdateSavings("Update", model);

        viewModelSavingsProgress.findAllSavingsByIdSavings(modelSavings.getId(), modelSavings.getType_currency()).observe(this, modelSavingsProgresses -> {
            long processValue = 0;
            for (ModelSavingsProgress modelSavingsProgress : modelSavingsProgresses)
                processValue += modelSavingsProgress.getProcessValue();

            modelSavings.setProcessValue(processValue);
            viewModelSaving.inputUpdateSavings("Update", modelSavings);

        });
    }

    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private String getGalleryPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return null;
        } else {
            return android.Manifest.permission.READ_EXTERNAL_STORAGE;
        }
    }

    private final ActivityResultLauncher<String> requestCameraPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    openCamera();
                } else {
                    Toast.makeText(this, "Izin Kamera Ditolak", Toast.LENGTH_SHORT).show();
                }
            });

    @SuppressLint("SuspiciousIndentation")
    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    if (isFromScanImage) {
                        String ocrResult = result.getData().getStringExtra(CameraOcrActivity.EXTRA_OCR_RESULT);
                        Uri imageUri = result.getData().getParcelableExtra(CameraOcrActivity.IMAGE_RESULT);
                        loadingOcrDialog = helperResultLLM.showLoadingOcrBottomSheet(this,getLayoutInflater(),imageUri,modelPrimaryColor);
                        processReceiptWithLlm(ocrResult);
                    } else {
                        filePhoto = getFileFromUri(this, imageUri);

                        String filePath = new File(filePhoto.getPath()).getAbsolutePath();
                        String fileName = new File(filePath).getName();
                        binding.rlPreviewImage.setVisibility(View.VISIBLE);
                        binding.tvFileName.setText(fileName);
                    }

                }
            });

    private final ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (isFromScanImage) {
                        try {
                            Bitmap originalBitmap = Tools.getBitmapFromUri(this.getContentResolver(), imageUri);

                            Bitmap processedBitmap = helperResultLLM.preprocessReceipt(originalBitmap);

                            InputImage image = InputImage.fromBitmap(processedBitmap, 0);

                            TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

                            recognizer.process(image).addOnSuccessListener(text -> {
                                String rawOcrText = text.getText();
                                binding.etDescription.setText(rawOcrText);
                                processReceiptWithLlm(rawOcrText);
                            }).addOnFailureListener(e -> {
                                Log.e("OCR_ERROR", "Gagal mengekstrak teks", e);
                                binding.etDescription.setText("Gagal membaca teks dari gambar.");
                            });

                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.e("IMAGE_ERROR", "Gagal memuat gambar dari Uri", e);
                            binding.etDescription.setText("Gagal memuat gambar dari galeri.");
                        }

                    } else {
                        binding.rlPreviewImage.setVisibility(View.VISIBLE);
                        String fileName = getFileName(this, imageUri);
                        filePhoto = getFileFromUri(this, imageUri);
                        String filePath = getRealPathFromURI(this, imageUri);
                        binding.tvFileName.setText(fileName);
                    }

                }
            });

    private void processReceiptWithLlm(String ocrText) {
        if (litertConversation == null) {
            binding.etDescription.setText("Error: Engine LiteRT belum siap atau belum diinisialisasi.");
            return;
        }

        currentOcrText = ocrText;

        String cleanedOcr = currentOcrText
                .replaceAll("(?m)^[ \t]*\r?\n", "")
                .replaceAll("Rp\\s*", "")
                .replaceAll("TOTAL", "Total");

        try {
            StringBuilder llmResponseBuilder = new StringBuilder();
            Map<String, Object> extraContext = new HashMap<>();
            litertConversation.sendMessageAsync(getPrompt(ocrText), new MessageCallback() {
                @Override
                public void onMessage(@NonNull Message message) {
                    String chunk = message.toString();
                    llmResponseBuilder.append(chunk);
                }

                @Override
                public void onDone() {
                    String fullTextResponse = llmResponseBuilder.toString();
                    loadingOcrDialog.dismiss();

                    runOnUiThread(() -> {
                        helperResultLLM.showBottomSheetSavingsOcr(UpdateSavingsActivity.this, getResources(), getLayoutInflater(), fullTextResponse,modelPrimaryColor);
                    });
                }

                @Override
                public void onError(@NonNull Throwable throwable) {
                    loadingOcrDialog.dismiss();

                    Log.e("LLM_ERROR", "Gagal memproses AI", throwable);
                    runOnUiThread(() -> {
                        binding.etDescription.setText("Gagal memproses AI: " + throwable.getMessage());
                    });
                }
            }, extraContext);

        } catch (Exception e) {
            loadingOcrDialog.dismiss();

            runOnUiThread(() -> {
                binding.etDescription.setText("Error Kirim Pesan AI: " + e.getMessage());
            });
        }
    }

    private void openCamera() {
        if (isFromScanImage) {
            Intent intent = new Intent(this, CameraOcrActivity.class);
            cameraLauncher.launch(intent);
        } else {
            File photoFile = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "photo_" + System.currentTimeMillis() + ".jpg");

            imageUri = FileProvider.getUriForFile(this, getPackageName() + ".provider", photoFile);

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            cameraLauncher.launch(intent);
        }
    }

    private void openGallery() {
        Intent intent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent = new Intent(MediaStore.ACTION_PICK_IMAGES);
        } else {
            intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        }
        galleryLauncher.launch(intent);
    }

    private void showDialogChoosePicture() {
        if (litertEngine == null) {
            Toast.makeText(this, "AI Belum Terunduh", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            litertConversation = helperResultLLM.getLitertConversation();
            if (litertConversation != null) {
                litertConversation.close();
            }

            litertConversation = litertEngine.createConversation(new ConversationConfig());

        } catch (Exception e) {
            binding.etDescription.setText("Gagal mereset sesi AI: " + e.getMessage());
            return;
        }
        if (checkCameraPermission()) {
            openCamera();
        } else {
            requestCameraPermission.launch(android.Manifest.permission.CAMERA);
        }

    }

    private String getPrompt(String ocrText) {
        return "You are an advanced AI extraction system for financial incomes, investments, and savings. Extract data into pure JSON.\n" +
                "STRICT RULES:\n" +
                "1. PURE JSON ONLY: Output nothing but the JSON object.\n" +
                "2. NO HALLUCINATION: Only use numbers and texts visible in the OCR. If missing, write '-'.\n" +
                "3. AMOUNT MATCHING: Look for 'Nominal', 'Transfer Masuk', 'Nilai Pembelian', 'Total', or the largest currency amount. DO NOT confuse weights or codes (e.g., '5 Gram', '24K', '0.5g') with the actual monetary amount.\n" +
                "4. TITLE (judul): Identify the source or purpose of the fund (e.g., 'Gaji PT ABC', 'Reksa Dana Bibit', 'Beli Emas Antam').\n" +
                "5. DATE (tanggal): Format strictly as dd/MM/yyyy.\n" +
                "6. DESCRIPTION (catatan): Combine the merchant name, platform, and specific item details as a note.\n\n" +
                "=== EXAMPLE 1 (M-Banking / Salary Transfer) ===\n" +
                "OCR INPUT:\n" +
                "Livin by Mandiri\n" +
                "Transfer Masuk Berhasil\n" +
                "25/09/2026 09:00\n" +
                "Dari: PT MAJU BERSAMA JAYA\n" +
                "Keterangan: Gaji Bulan September\n" +
                "Nominal Rp 6.500.000\n" +
                "JSON OUTPUT:\n" +
                "{\n" +
                "  \"judul\": \"Gaji PT MAJU BERSAMA JAYA\",\n" +
                "  \"tanggal\": \"25/09/2026\",\n" +
                "  \"nominal\": \"6500000\",\n" +
                "  \"catatan\": \"Gaji Bulan September via Livin by Mandiri\"\n" +
                "}\n\n" +
                "=== EXAMPLE 2 (Investment / Mutual Fund / Bibit) ===\n" +
                "OCR INPUT:\n" +
                "Bibit\n" +
                "Pembelian Berhasil\n" +
                "10/10/2026\n" +
                "Sucorinvest Money Market Fund\n" +
                "Nilai Pembelian Rp 1.500.000\n" +
                "Biaya Admin Rp 0\n" +
                "Total Bayar Rp 1.500.000\n" +
                "JSON OUTPUT:\n" +
                "{\n" +
                "  \"judul\": \"Investasi Sucorinvest Money Market Fund\",\n" +
                "  \"tanggal\": \"10/10/2026\",\n" +
                "  \"nominal\": \"1500000\",\n" +
                "  \"catatan\": \"Pembelian reksa dana di platform Bibit\"\n" +
                "}\n\n" +
                "=== EXAMPLE 3 (Physical Receipt / Gold Investment) ===\n" +
                "OCR INPUT:\n" +
                "Toko Emas Cahaya Mulia\n" +
                "12/11/2026\n" +
                "LM Antam 24K 5 Gram\n" +
                "Harga Rp 7.250.000\n" +
                "Status: LUNAS\n" +
                "JSON OUTPUT:\n" +
                "{\n" +
                "  \"judul\": \"Beli Emas LM Antam 5 Gram\",\n" +
                "  \"tanggal\": \"12/11/2026\",\n" +
                "  \"nominal\": \"7250000\",\n" +
                "  \"catatan\": \"Pembelian di Toko Emas Cahaya Mulia (24K, 5 Gram)\"\n" +
                "}\n\n" +
                "=== ACTUAL TASK ===\n" +
                "OCR INPUT:\n" +
                ocrText + "\n" +
                "JSON OUTPUT:\n";
    }
}
