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
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import id.co.evolution.financefy.R;
import id.co.evolution.financefy.databinding.ActivityCreateSavingsProgressBinding;
import id.co.evolution.financefy.dialog.DialogCalculator;
import id.co.evolution.financefy.dialog.DialogConfirm;
import id.co.evolution.financefy.dialog.DialogPreviewImage;
import id.co.evolution.financefy.helper.TinyDb;
import id.co.evolution.financefy.helper.Tools;
import id.co.evolution.financefy.model.ModelFinance;
import id.co.evolution.financefy.model.ModelPrimaryColor;
import id.co.evolution.financefy.model.ModelSavings;
import id.co.evolution.financefy.model.ModelSavingsProgress;
import id.co.evolution.financefy.repository.SavingsProgressRepository;
import id.co.evolution.financefy.repository.SavingsRepository;
import id.co.evolution.financefy.viewmodel.ViewModelSavings;
import id.co.evolution.financefy.viewmodel.ViewModelSavingsProgress;

@AndroidEntryPoint
public class CreateSavingsProgressActivity extends BaseFinanceActivity implements View.OnClickListener {
    String date = "";
    private String jumlah = "";
    String month = "";
    ActivityCreateSavingsProgressBinding binding;
    Calendar cur_calendar = Calendar.getInstance();
    ViewModelSavingsProgress viewModelSavingsProgress;
    ViewModelSavings viewModelSaving;
    DialogCalculator dialogCalculator;
    List<ModelSavingsProgress> listSavings;

    @Inject
    TinyDb tinyDb;


    @Inject
    SavingsProgressRepository savingsProgressRepository;
    @Inject
    SavingsRepository savingsRepository;
    ModelSavings modelSavings;
    Locale locale;
    File filePhoto;
    DialogPreviewImage dialogPreviewImage;
    Uri imageUri;
    boolean isFromScanImage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        modelPrimaryColor= tinyDb.getObject("model_primary_color", ModelPrimaryColor.class);

        Tools.setThemeNoActionBarActivity(getTheme(), modelPrimaryColor);

        binding = DataBindingUtil.setContentView(this,R.layout.activity_create_savings_progress);
        dialogPreviewImage = new DialogPreviewImage(this);

        Tools.setBackgroundColorView(binding.rlBackground,modelPrimaryColor);
        Tools.setImageTintView(binding.btnCalculator,modelPrimaryColor);
        Tools.setImageTintView(binding.btnScan,modelPrimaryColor);


        //TODO HIDE STATUS BAR


        cur_calendar.get(Calendar.YEAR);
        cur_calendar.get(Calendar.MONTH);
        cur_calendar.get(Calendar.DAY_OF_MONTH);
        long date_ship_milis = cur_calendar.getTimeInMillis();
        viewModelSavingsProgress = new ViewModelProvider(this).get(ViewModelSavingsProgress.class);
        viewModelSaving = new ViewModelProvider(this).get(ViewModelSavings.class);
        viewModelSavingsProgress.init(savingsProgressRepository);
        viewModelSaving.init(savingsRepository);

        binding.txtHeader.setText("Masukkan Data Progress Menabung");
        binding.txtDescription.setText("Silakan masukkan data tabungan Anda pada form yang tersedia.");
        binding.txtDate.setText(getFormattedDateSimple(date_ship_milis));
        date = getFormattedDateSimple(date_ship_milis);
        month = getFormattedMonthSimple(date_ship_milis);
        modelSavings =(ModelSavings) getIntent().getSerializableExtra("savings");

        locale =modelSavings.getType_currency().equalsIgnoreCase("IDR")? Tools.getLocaleIDN():Tools.getLocaleUS();

        viewModelSavingsProgress.findAllSavingsByIdSavings(modelSavings.getId(),modelSavings.getType_currency()).observe(this, modelSavings -> {
            listSavings = modelSavings;
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
                        String formatted = Tools.convertToCurrency(parsed,locale);

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


    public void showDatePickerDialog() {
        if(modelPrimaryColor==null) modelPrimaryColor= Tools.modelPrimaryColor;

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




    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tvFileName:
                dialogPreviewImage.show(filePhoto.getPath());
                break;
            case R.id.btnCamera:
                isFromScanImage = false;
                if (checkCameraPermission()) {
                    openCamera();
                } else {
                    requestCameraPermission.launch(android.Manifest.permission.CAMERA);
                }
                break;
            case R.id.btnClose:
                binding.rlPreviewImage.setVisibility(View.GONE);
                filePhoto = null;
                break;
            case R.id.btn_scan:
                isFromScanImage = true;
                showDialogChoosePicture();

                break;
            case R.id.btnGallery:
                isFromScanImage = false;

                String permission = getGalleryPermission();
                if (permission != null && ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{permission}, REQUEST_CODE);
                } else {
                    openGallery();
                }

                break;
            case R.id.place_date:
                showDatePickerDialog();
                break;
            case R.id.img_back:
                finish();
                break;

            case R.id.btn_calculator:
                dialogCalculator = new DialogCalculator(this, getLayoutInflater(), result -> {
                    jumlah = result;
                    binding.etAmount.setText(Tools.convertToCurrency(result,locale));
                });
                dialogCalculator.show();
                break;
            case R.id.place_submit:
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
                            if(result.equalsIgnoreCase("yes")){
                                ModelSavingsProgress model = new ModelSavingsProgress();
                                model.setDate_progress_savings(date);
                                model.setMonth(month);
                                model.setProcessValue(Long.parseLong(Tools.convertCurrencyToValue(jumlah)));
                                model.setDescription(binding.etDescription.getText().toString().trim());
                                model.setTitle(binding.etTitle.getText().toString().trim());
                                model.setId_savings(modelSavings.getId());
                                model.setType_currency(modelSavings.getType_currency());
                                if(filePhoto!=null)
                                    model.setPhoto(filePhoto.getPath());
                                CreateSavingsProgressActivity.this.onSubmit(model);
                                Intent intent = new Intent();
                                intent.putExtra("savings", modelSavings);
                                setResult(RESULT_OK, intent);
                                Toast.makeText(CreateSavingsProgressActivity.this, "Catatan progress menabung berhasil di tambahkan !", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        }
                    });
                    dialogConfirm.showDialogConfirm("Submit","Apakah anda yakin ingin submit data ?");

                }
                break;
        }
    }

    private void onSubmit(ModelSavingsProgress model) {
//        boolean isUpdate = false;
//        for (ModelSavingsProgress modelSavings : listSavings) {
//            if (modelSavings.getTitle().contains(model.getTitle()) && modelSavings.getDate_progress_savings().contains(model.getDate_progress_savings())) {
//                model.setId(modelSavings.getId());
//                model.setProcessValue(modelSavings.getProcessValue());
//                isUpdate = true;
//            }
//        }
//
//        if(isUpdate)
//        viewModelSavingsProgress.inputUpdateSavings("Update", model);
//        else
            viewModelSavingsProgress.inputUpdateSavings("Create", model);

        viewModelSavingsProgress.findAllSavingsByIdSavings(modelSavings.getId(),modelSavings.getType_currency()).observe(this, modelSavingsProgresses -> {
            long processValue=0;
            for (ModelSavingsProgress modelSavingsProgress:modelSavingsProgresses)
                processValue+= modelSavingsProgress.getProcessValue();

            modelSavings.setProcessValue(processValue);
            viewModelSaving.inputUpdateSavings("Update",modelSavings);
        });
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

    // 🔹 Activity Result untuk Izin Kamera
    private final ActivityResultLauncher<String> requestCameraPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    openCamera();
                } else {
                    Toast.makeText(this, "Izin Kamera Ditolak", Toast.LENGTH_SHORT).show();
                }
            });

    // 🔹 Activity Result untuk Izin Galeri
    private final ActivityResultLauncher<String> requestGalleryPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    openGallery();
                } else {
                    Toast.makeText(this, "Izin Akses Galeri Ditolak", Toast.LENGTH_SHORT).show();
                }
            });

    // 🔹 Activity Result untuk Kamera
    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    if(isFromScanImage){
                        InputImage image;
                        try {
                            // When using Latin script library
                            TextRecognizer recognizer =
                                    TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
                            image = InputImage.fromFilePath(this, imageUri);

                            recognizer.process(image).addOnSuccessListener(new OnSuccessListener<>() {
                                @Override
                                public void onSuccess(Text text) {
                                    binding.etDescription.setText(text.getText());
                                }
                            });

                        } catch (IOException e) {
                            e.printStackTrace();
                        }

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
                    if(isFromScanImage){
                        InputImage image;
                        try {
                            // When using Latin script library
                            TextRecognizer recognizer =
                                    TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
                            image = InputImage.fromFilePath(this, imageUri);

                            recognizer.process(image).addOnSuccessListener(new OnSuccessListener<>() {
                                @Override
                                public void onSuccess(Text text) {
                                    binding.etDescription.setText(text.getText());
                                }
                            });

                        } catch (IOException e) {
                            e.printStackTrace();
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
    private void showDialogChoosePicture() {
        if (checkCameraPermission()) {
            openCamera();
        } else {
            requestCameraPermission.launch(android.Manifest.permission.CAMERA);
        }
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle("Pilih Opsi");
//
//        final String[] tipe =  new String[]{"Ambil Gambar", "Pilih Galeri"} ;
//
//
//        builder.setItems(tipe, (dialog, which) -> {
//            switch (tipe[which]) {
//                case "Ambil Gambar":
//
//                    if (checkCameraPermission()) {
//                        openCamera();
//                    } else {
//                        requestCameraPermission.launch(android.Manifest.permission.CAMERA);
//                    }
//                    break;
//                case "Pilih Galeri":
//                    if (checkGalleryPermission()) {
//                        openGallery();
//                    } else {
//                        requestGalleryPermission.launch(getGalleryPermission());
//                    }
//                    break;
//            }
//        });
//        AlertDialog dialog = builder.create();
//        dialog.show();
    }
}