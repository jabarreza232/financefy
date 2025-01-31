package id.co.evolution.financefy.activity;

import static id.co.evolution.financefy.callback.CallbackOnActivityResult.REQUEST_CODE_FINANCE;
import static id.co.evolution.financefy.callback.CallbackOnActivityResult.REQUEST_CODE_SAVINGS;
import static id.co.evolution.financefy.callback.CallbackOnActivityResult.REQUEST_CODE_UPDATE_SAVINGS_TARGET;
import static id.co.evolution.financefy.helper.Tools.getFileFromUri;
import static id.co.evolution.financefy.helper.Tools.getFileName;
import static id.co.evolution.financefy.helper.Tools.getRealPathFromURI;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.io.File;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Locale;


import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import id.co.evolution.financefy.App;
import id.co.evolution.financefy.R;
import id.co.evolution.financefy.databinding.ActivityCreateFinanceBinding;
import id.co.evolution.financefy.db.FinanceDB;
import id.co.evolution.financefy.db.FinanceDao;
import id.co.evolution.financefy.dialog.DialogCalculator;
import id.co.evolution.financefy.dialog.DialogConfirm;
import id.co.evolution.financefy.dialog.DialogFinance;
import id.co.evolution.financefy.dialog.DialogPreviewImage;
import id.co.evolution.financefy.helper.TinyDb;
import id.co.evolution.financefy.helper.Tools;
import id.co.evolution.financefy.model.ModelFinance;
import id.co.evolution.financefy.model.ModelPrimaryColor;
import id.co.evolution.financefy.model.ModelSavings;
import id.co.evolution.financefy.model.ModelUser;
import id.co.evolution.financefy.repository.FinanceRepository;
import id.co.evolution.financefy.viewmodel.ViewModelFactory;
import id.co.evolution.financefy.viewmodel.ViewModelFinance;

@AndroidEntryPoint
public class UpdateFinanceActivity extends BaseFinanceActivity implements View.OnClickListener {


    ModelFinance modelFinance;
    ModelUser modelUser;
    int position,id_user;
    @Inject
    TinyDb tinyDb;
    public ModelPrimaryColor modelPrimaryColor=Tools.modelPrimaryColor;

    @Inject
    FinanceRepository financeRepository;
    Locale locale;
    File filePhoto;
    DialogPreviewImage dialogPreviewImage;
    Uri imageUri;
    @SuppressLint("ObsoleteSdkInt")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        modelPrimaryColor= tinyDb.getObject("model_primary_color", ModelPrimaryColor.class);
        Tools.setThemeNoActionBarActivity(getTheme(), modelPrimaryColor);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_create_finance);
        binding.txtHeader.setText("Ubah Data Keuangan");
        binding.txtDescription.setText("Silakan ubah data keuangan Anda pada form yang tersedia.");

        Tools.setBackgroundColorView(binding.rlBackground,modelPrimaryColor);
        Tools.setImageTintView(binding.btnCalculator,modelPrimaryColor);

        //TODO HIDE STATUS BAR

        dialogPreviewImage = new DialogPreviewImage(this);
        viewModelFinance = new ViewModelProvider(this).get(ViewModelFinance.class);
        viewModelFinance.init(financeRepository);
        position = getIntent().getIntExtra("position", 0);
        modelUser = (ModelUser) getIntent().getSerializableExtra("user");
        locale =modelUser.getType_currency().equalsIgnoreCase("IDR")? Tools.getLocaleIDN():Tools.getLocaleUS();

        viewModelFinance.getFinanceById(getIntent().getIntExtra("id",0), modelUser.getType_currency()).observe(this, modelFinance -> {
            this.modelFinance = modelFinance;
            loadData();
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
    }

    private void loadData() {
        cur_calendar.get(Calendar.YEAR);
        cur_calendar.get(Calendar.MONTH);
        cur_calendar.get(Calendar.DAY_OF_MONTH);

        type = modelFinance.getTipe();
        category = modelFinance.getKategori();
        jumlah = modelFinance.getJumlahDesc(locale);
        date = modelFinance.getDate();
        month = modelFinance.getMonth();

        binding.txtHeader.setText("Update data");
        binding.txtDate.setText(date);
        binding.txtType.setText(type);
        binding.txtKategori.setText(category);
        binding.etAmount.setText(jumlah);
        binding.etDescription.setText(modelFinance.getKeterangan());
        binding.txtSubmit.setText("Ubah");

        if(modelFinance.getPhoto()!=null){
            filePhoto = new File(modelFinance.getPhoto());
            String filePath = new File(filePhoto.getPath()).getAbsolutePath();
            String fileName = new File(filePath).getName();
            binding.rlPreviewImage.setVisibility(View.VISIBLE);
            binding.tvFileName.setText(fileName);
        }
    }



    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tvFileName:
                dialogPreviewImage.show(filePhoto.getPath());
                break;
            case R.id.btnCamera:
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
            case R.id.btnGallery:
                if (checkGalleryPermission()) {
                    openGallery();
                } else {
                    requestGalleryPermission.launch(getGalleryPermission());
                }
                break;
            case R.id.place_category:
                showDialogCategory();
                break;
            case R.id.place_type:
                showDialogType();
                break;
            case R.id.btn_calculator:
                dialogCalculator = new DialogCalculator(this, getLayoutInflater(), Tools.convertCurrencyToValue(jumlah), result -> {
                    jumlah = Tools.convertToCurrency(result,locale);
                    binding.etAmount.setText(jumlah);
                });
                dialogCalculator.show();
                break;
            case R.id.place_date:
                showDatePickerDialog();
                break;
            case R.id.img_back:
                finish();
                break;
            case R.id.place_submit:
                if (type.isEmpty()) {
                    Toast.makeText(this, "Silahkan Pilih tipe terlebih dahulu", Toast.LENGTH_SHORT).show();
                } else if (binding.etAmount.getText().toString().isEmpty()) {
                    binding.tilAmount.setError("Silahkan input jumlah mata uang anda terlebih dahulu");
                } else if (!binding.etAmount.getText().toString().isEmpty()) {
                    binding.tilAmount.setError(null);
                }

                if (!type.isEmpty() && !binding.etAmount.getText().toString().isEmpty()) {
                    DialogConfirm dialogConfirm = new DialogConfirm(this, getLayoutInflater(), new DialogConfirm.DialogConfirm() {
                        @Override
                        public void onSubmit(@NonNull String result) {
                            if(result.equalsIgnoreCase("yes")){
                                ModelFinance model = new ModelFinance();
                                model.setId(modelFinance.getId());
                                model.setDate(date);
                                model.setJumlah(Tools.replaceCurrencyStringToDouble(jumlah));
                                model.setTipe(type);
                                model.setType_currency(modelUser.getType_currency());
                                model.setKategori(category);
                                model.setKeterangan(binding.etDescription.getText().toString().trim());
                                model.setMonth(month);
                                model.setId_finance_user(modelUser.getId());
                                if(filePhoto!=null)
                                    model.setPhoto(filePhoto.getPath());

                                viewModelFinance.inputUpdateFinance("Update", model);
                                Intent intent = new Intent();
                                intent.putExtra("finance", model);
                                intent.putExtra("position", position);
                                setResult(REQUEST_CODE_FINANCE, intent);
                                finish();
                                Toast.makeText(UpdateFinanceActivity.this, "Catatan "+type+" berhasil di ubah !", Toast.LENGTH_SHORT).show();

                            }
                        }
                    });
                    dialogConfirm.showDialogConfirm("Update","Apakah anda yakin ingin update data ?");

                }
                break;
        }
    }

    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    // 🔹 Cek Izin Galeri
    private boolean checkGalleryPermission() {
        return ContextCompat.checkSelfPermission(this, getGalleryPermission()) == PackageManager.PERMISSION_GRANTED;
    }

    // 🔹 Mendapatkan permission sesuai Android Version
    private String getGalleryPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
            return android.Manifest.permission.READ_MEDIA_IMAGES;
        } else {
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
                    filePhoto= getFileFromUri(this,imageUri);

                    String filePath = new File(filePhoto.getPath()).getAbsolutePath();
                    String fileName = new File(filePath).getName();
                    binding.rlPreviewImage.setVisibility(View.VISIBLE);
                    binding.tvFileName.setText(fileName);
                }
            });

    // 🔹 Activity Result untuk Galeri
    private final ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    binding.rlPreviewImage.setVisibility(View.VISIBLE);
                    // Mendapatkan Nama File
                    String fileName = getFileName(this, imageUri);
                    filePhoto = getFileFromUri(this,imageUri);
                    // Mendapatkan Path (Jika memungkinkan)
                    String filePath = getRealPathFromURI(this, imageUri);
                    binding.tvFileName.setText(fileName);
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

}
