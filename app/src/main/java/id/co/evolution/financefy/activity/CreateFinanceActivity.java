package id.co.evolution.financefy.activity;

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

import com.google.ai.edge.litertlm.ConversationConfig;
import com.google.ai.edge.litertlm.Engine;
import com.google.ai.edge.litertlm.Conversation;
import com.google.ai.edge.litertlm.Message;
import com.google.ai.edge.litertlm.MessageCallback;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import org.opencv.android.OpenCVLoader;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import id.co.evolution.financefy.R;
import id.co.evolution.financefy.dialog.DialogCalculator;
import id.co.evolution.financefy.dialog.DialogConfirm;
import id.co.evolution.financefy.dialog.DialogPreviewImage;
import id.co.evolution.financefy.helper.HelperResultLLM;
import id.co.evolution.financefy.helper.TinyDb;
import id.co.evolution.financefy.helper.Tools;
import id.co.evolution.financefy.model.ModelFinance;
import id.co.evolution.financefy.model.ModelPrimaryColor;
import id.co.evolution.financefy.model.ModelUser;
import id.co.evolution.financefy.repository.FinanceRepository;
import id.co.evolution.financefy.viewmodel.ViewModelFinance;

@ExperimentalGetImage @AndroidEntryPoint
public class CreateFinanceActivity extends BaseFinanceActivity implements View.OnClickListener {


    //    @Inject
//    ViewModelFactory viewModelFactory;
    private BottomSheetDialog loadingOcrDialog;
    public  List<ModelFinance> listFinance;
    @Inject
      FinanceRepository financeRepository;
    @Inject
    TinyDb tinyDb;

    Locale locale;
    ModelUser modelUser;
    File filePhoto;
    DialogPreviewImage dialogPreviewImage;
    Uri imageUri;

    private Engine litertEngine;
    private Conversation litertConversation;
    private String currentOcrText = "";
        boolean isFromScanImage;

        private HelperResultLLM helperResultLLM;
    @SuppressLint("ObsoleteSdkInt")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean isCustomActive = tinyDb.getBoolean("is_custom_color_active");

        if (isCustomActive) {
            int customColor = tinyDb.getInt("custom_color_int");
            getWindow().setStatusBarColor(customColor);
        } else {
            if (tinyDb.getObject("model_primary_color", ModelPrimaryColor.class) != null) {
                modelPrimaryColor = tinyDb.getObject("model_primary_color", ModelPrimaryColor.class);
                Tools.setThemeNoActionBarActivity(getTheme(), modelPrimaryColor);
            }
        }

        binding = DataBindingUtil.setContentView(this, R.layout.activity_create_finance);
        dialogPreviewImage = new DialogPreviewImage(this);
        Tools.setBackgroundColorView(binding.rlBackground, modelPrimaryColor);
        Tools.setImageTintView(binding.btnCalculator, modelPrimaryColor);
        Tools.setImageTintView(binding.imgExpandType, modelPrimaryColor);
        Tools.setImageTintView(binding.imgExpandCategory, modelPrimaryColor);
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

       //TODO HIDE STATUS BAR

        cur_calendar.get(Calendar.YEAR);
        cur_calendar.get(Calendar.MONTH);
        cur_calendar.get(Calendar.DAY_OF_MONTH);
        long date_ship_milis = cur_calendar.getTimeInMillis();
        viewModelFinance = new ViewModelProvider(this).get(ViewModelFinance.class);
        viewModelFinance.init(financeRepository);

        binding.txtHeader.setText("Tambah Transaksi");
        binding.txtDate.setText(getFormattedDateSimple(date_ship_milis));
        date = getFormattedDateSimple(date_ship_milis);
        month = getFormattedMonthSimple(date_ship_milis);
        modelUser = (ModelUser) getIntent().getSerializableExtra("user");
        locale =modelUser.getType_currency().equalsIgnoreCase("IDR")? Tools.getLocaleIDN():Tools.getLocaleUS();
        helperResultLLM = new HelperResultLLM(map -> {


            binding.etAmount.setText(map.get("total"));

            binding.txtDate.setText(map.get("tanggal"));

            binding.etDescription.setText(map.get("description"));

        });
        viewModelFinance.getFinanceByUserId(modelUser.getId(),modelUser.getType_currency()).observe(this, modelFinances -> {
            listFinance = modelFinances;
        });

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

                        jumlah = Tools.convertToCurrency(parsed,locale);
                        binding.etAmount.setText(Tools.convertToCurrency(parsed,locale));
                        binding.etAmount.setSelection(Tools.convertToCurrency(parsed,locale).length());
                    }

                    binding.etAmount.addTextChangedListener(this);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        binding.placeDate.setOnClickListener(this);
        binding.placeCategory.setOnClickListener(this);
        binding.placeType.setOnClickListener(this);
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
        new Thread(() -> {
            helperResultLLM.initLlmInference(this);

            runOnUiThread(() -> {
                litertEngine = helperResultLLM.getLitertEngine();

                if (litertEngine != null) {
                    // Model berhasil dimuat
                    binding.etDescription.setText(""); // Kosongkan keterangan
                    binding.btnScan.setEnabled(true);  // Aktifkan kembali tombol scan
                    Toast.makeText(this, "AI Siap Digunakan!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "AI Belum Terunduh", Toast.LENGTH_SHORT).show();
                    binding.btnScan.setEnabled(true);
                }
            });
        }).start();

        if (!OpenCVLoader.initDebug()) {
            Log.e("OpenCV", "Gagal load OpenCV");
        } else {
            Toast.makeText(this, "Berhasil load opencv", Toast.LENGTH_SHORT).show();
        }


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
            case "btnClose":
                binding.rlPreviewImage.setVisibility(View.GONE);
                filePhoto = null;
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
            case "place_category":
                if (!type.isEmpty()) {
                    showDialogCategory();
                } else {
                    Toast.makeText(this, "Pilih tipe terlebih dahulu", Toast.LENGTH_SHORT).show();
                }
                break;
            case "place_type":
                showDialogType();
                break;
            case "place_date":
                showDatePickerDialog();
                break;
            case "img_back":
                finish();
                break;
            case "btn_calculator":
                dialogCalculator = new DialogCalculator(this, getLayoutInflater(), result -> {
                    jumlah = Tools.convertToCurrency(result, locale);
                    binding.etAmount.setText(jumlah);
                });
                dialogCalculator.show();
                break;
            case "place_submit":
                if (type.isEmpty()) {
                    Toast.makeText(this, "Silahkan Pilih tipe terlebih dahulu", Toast.LENGTH_SHORT).show();
                }

                if (binding.etAmount.getText().toString().isEmpty()) {
                    binding.tilAmount.setError("Silahkan input jumlah mata uang anda terlebih dahulu");
                } else {
                    binding.tilAmount.setError(null);
                }

                if (!type.isEmpty() && !binding.etAmount.getText().toString().isEmpty()) {
                    DialogConfirm dialogConfirm = new DialogConfirm(this, getLayoutInflater(), new DialogConfirm.DialogConfirm() {
                        @Override
                        public void onSubmit(@NonNull String result) {
                            if (result.equalsIgnoreCase("yes")) {
                                ModelFinance model = new ModelFinance();
                                model.setDate(date);
                                model.setJumlah(Tools.replaceCurrencyStringToDouble(jumlah));
                                model.setTipe(type);
                                model.setKategori(category);
                                model.setKeterangan(binding.etDescription.getText().toString().trim());
                                model.setMonth(month);
                                model.setId_finance_user(modelUser.getId());
                                model.setType_currency(modelUser.getType_currency());
                                if (filePhoto != null)
                                    model.setPhoto(filePhoto.getPath());

                                CreateFinanceActivity.this.onSubmit(model);
                                Intent intent = new Intent();
                                intent.putExtra("finance", model);
                                setResult(RESULT_OK, intent);
                                Toast.makeText(CreateFinanceActivity.this, "Catatan " + type + " berhasil di tambahkan !", Toast.LENGTH_SHORT).show();

                                finish();

                            }
                        }
                    });
                    dialogConfirm.showDialogConfirm("Submit", "Apakah anda yakin ingin submit data ?");
                }
                break;
        }
    }

    private void onSubmit(ModelFinance model) {
        boolean isUpdate = false;
        //TODO ketika submit terdapat data yang identik sama maka tidak dapat duplikasi.
        // melainkan hanya bisa melakukan penjumlahan value nya saja


        for (ModelFinance modelFinance : listFinance) {
            if (modelFinance.getKategori().contains(model.getKategori()) && modelFinance.getDate().contains(model.getDate())) {
                double jumlahValue = modelFinance.getJumlahValue() + model.getJumlahValue();
                model.setId(modelFinance.getId());
                model.setJumlah(jumlahValue);
                isUpdate = true;
            }
        }

        if (isUpdate)
            viewModelFinance.inputUpdateFinance("Update", model);
        else
            viewModelFinance.inputUpdateFinance("Create", model);

    }

    // 🔹 Cek Izin Kamera
    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }


    // 🔹 Mendapatkan permission sesuai Android Version
    private String getGalleryPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ pakai Photo Picker, tidak perlu permission
            return null;
        } else {
            // Android 12 ke bawah tetap perlu izin
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
                    if(isFromScanImage){

                        String ocrResult = result.getData().getStringExtra(CameraOcrActivity.EXTRA_OCR_RESULT);
                        Uri imageUri = result.getData().getParcelableExtra(CameraOcrActivity.IMAGE_RESULT);
                        loadingOcrDialog = helperResultLLM.showLoadingOcrBottomSheet(this,getLayoutInflater(),imageUri,modelPrimaryColor);
                        processReceiptWithLlm(ocrResult);
                    }else{
                        filePhoto= getFileFromUri(this,imageUri);

                        String filePath = new File(filePhoto.getPath()).getAbsolutePath();
                        String fileName = new File(filePath).getName();
                        binding.rlPreviewImage.setVisibility(View.VISIBLE);
                        binding.tvFileName.setText(fileName);
                    }
                }
            });

    // 🔹 Activity Result untuk Galeri
    private final ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (isFromScanImage) {
                        try {
                            Bitmap originalBitmap = Tools.getBitmapFromUri(this.getContentResolver(),imageUri);


                            Bitmap processedBitmap = helperResultLLM.preprocessReceipt(originalBitmap);

                            InputImage image = InputImage.fromBitmap(processedBitmap, 0);

                            TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

                            // 4. Proses OCR
                            recognizer.process(image).addOnSuccessListener(text -> {
                                String rawOcrText = text.getText();

                                // Tampilkan ke EditText
                                binding.etDescription.setText(rawOcrText);

                                // Panggil LLM kamu di sini
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



                    }else{
                        binding.rlPreviewImage.setVisibility(View.VISIBLE);
                        // Mendapatkan Nama File
                        String fileName = getFileName(this, imageUri);
                        filePhoto = getFileFromUri(this,imageUri);
                        // Mendapatkan Path (Jika memungkinkan)
                        String filePath = getRealPathFromURI(this, imageUri);
                        binding.tvFileName.setText(fileName);
                    }


                }
            });



    // 🔹 Fungsi Membuka Kamera
    private void openCamera() {


        if(isFromScanImage){
            Intent intent = new Intent(this, CameraOcrActivity.class);
            cameraLauncher.launch(intent);
        }else{
        // Buat file untuk menyimpan gambar
        File photoFile = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "photo_" + System.currentTimeMillis() + ".jpg");

        // Dapatkan Uri menggunakan FileProvider
        imageUri = FileProvider.getUriForFile(this, getPackageName() + ".provider", photoFile);

        // Buat intent kamera
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        cameraLauncher.launch(intent);
        }
    }

    // 🔹 Fungsi Membuka Galeri
    private void openGallery() {
        Intent intent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
            intent = new Intent(MediaStore.ACTION_PICK_IMAGES);
        } else {
            intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        }
        galleryLauncher.launch(intent);
    }


    private void processReceiptWithLlm(String ocrText) {
        if (litertConversation == null) {
            binding.etDescription.setText("Error: Engine LiteRT belum siap atau belum diinisialisasi.");
            return;
        }

        currentOcrText = ocrText;

        String cleanedOcr = currentOcrText
                .replaceAll("(?m)^[ \t]*\r?\n", "") // Hapus baris kosong
                .replaceAll("Rp\\s*", "")           // Hapus tulisan Rp
                .replaceAll("TOTAL", "Total");      // Standarisasi huruf


        try {
            StringBuilder llmResponseBuilder = new StringBuilder();
            Map<String, Object> extraContext = new HashMap<>();
            // Kirim pesan secara asinkron menggunakan callback bawaan LiteRT-LM
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
//                        parseLlmResult(fullTextResponse); // Sekarang teksnya sudah 100% utuh


                     helperResultLLM.showBottomSheetOcr(CreateFinanceActivity.this,getResources(),getLayoutInflater(),fullTextResponse,modelPrimaryColor);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
    private void showDialogChoosePicture() {
        if(litertEngine==null){
            Toast.makeText(this, "AI Belum Terunduh", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            litertConversation = helperResultLLM.getLitertConversation();
            if (litertConversation != null) {
                litertConversation.close();
            }

            // BUAT sesi baru
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
        return "You are a universal data extraction system for BOTH physical receipts and digital transactions (e-wallet/m-banking). Extract data into pure JSON.\n" +
                "STRICT RULES:\n" +
                "1. NO HALLUCINATION: Only use numbers present in the OCR. If missing, write '0'.\n" +
                "2. EXACT NAMES: Do not abbreviate. Keep merchant names, item names, or recipient names exactly as printed.\n" +
                "3. AMOUNT MATCHING: DO NOT confuse numbers inside product names (e.g., '77', '200ml', '500g') with the actual price. Prices are the final cost, usually located at the end of the line or near a quantity multiplier (e.g., '6,500 X 1'). For digital transactions, look for 'Nominal' or 'Transfer' amounts.\n" +
                "4. FINANCIALS: 'subtotal' = amount before tax/admin fee. 'other_fee' = tax/admin fee. 'total' = final paid amount.\n" +
                "5. CASHLESS = 0: If it is a digital wallet (GoPay, OVO), m-banking, QRIS, or Card, 'cash' MUST be '0'.\n" +
                "6. ITEM FORMAT: 'daftar_produk' MUST follow this format: [Item/Transaction Name] harga [Price]. For digital transactions, use the transfer destination or payment purpose as the item name.\n\n" +
                "=== EXAMPLE 1 (Physical Receipt with Numbers in Product Names) ===\n" +
                "OCR INPUT:\n" +
                "Ramen Ya\n" +
                "15/02/2026\n" +
                "Tori Miso Rmn 77 45.000\n" +
                "Ocha 200ml 10.000\n" +
                "Pajak 5.500\n" +
                "Total 60.500\n" +
                "Tunai 100.000\n" +
                "JSON OUTPUT:\n" +
                "{\n" +
                "  \"nama_toko\": \"Ramen Ya\",\n" +
                "  \"tanggal\": \"15/02/2026\",\n" +
                "  \"total\": \"60500\",\n" +
                "  \"subtotal\": \"55000\",\n" +
                "  \"other_fee\": \"5500\",\n" +
                "  \"cash\": \"100000\",\n" +
                "  \"daftar_produk\": \"Tori Miso Rmn 77 harga 45000, Ocha 200ml harga 10000\"\n" +
                "}\n\n" +
                "=== EXAMPLE 2 (Digital M-Banking/E-Wallet) ===\n" +
                "OCR INPUT:\n" +
                "GoPay\n" +
                "16/09/2026 10:45\n" +
                "Transfer Berhasil\n" +
                "Ke: Budi Santoso\n" +
                "Nominal Rp 150.000\n" +
                "Biaya Admin Rp 1.000\n" +
                "Total Keluar Rp 151.000\n" +
                "JSON OUTPUT:\n" +
                "{\n" +
                "  \"nama_toko\": \"GoPay\",\n" +
                "  \"tanggal\": \"16/09/2026\",\n" +
                "  \"total\": \"151000\",\n" +
                "  \"subtotal\": \"150000\",\n" +
                "  \"other_fee\": \"1000\",\n" +
                "  \"cash\": \"0\",\n" +
                "  \"daftar_produk\": \"Transfer ke Budi Santoso harga 150000\"\n" +
                "}\n\n" +
                "=== ACTUAL TASK ===\n" +
                "OCR INPUT:\n" +
                ocrText + "\n" +
                "JSON OUTPUT:\n";
    }
}
