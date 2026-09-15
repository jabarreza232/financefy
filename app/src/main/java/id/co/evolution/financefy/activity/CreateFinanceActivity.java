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
import android.graphics.ImageDecoder;
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

import com.google.ai.edge.litertlm.Backend;
import com.google.ai.edge.litertlm.ConversationConfig;
import com.google.ai.edge.litertlm.Engine;
import com.google.ai.edge.litertlm.EngineConfig;
import com.google.ai.edge.litertlm.Conversation;
import com.google.ai.edge.litertlm.Message;
import com.google.ai.edge.litertlm.MessageCallback;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import org.json.JSONObject;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

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
    private String currentOcrText = ""; // Variabel global baru
        boolean isFromScanImage;
    @SuppressLint("ObsoleteSdkInt")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (tinyDb.getObject("model_primary_color", ModelPrimaryColor.class) != null) {
            modelPrimaryColor = tinyDb.getObject("model_primary_color", ModelPrimaryColor.class);
        }

        Tools.setThemeNoActionBarActivity(getTheme(), modelPrimaryColor);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_create_finance);
        dialogPreviewImage = new DialogPreviewImage(this);
        Tools.setBackgroundColorView(binding.rlBackground,modelPrimaryColor);
        Tools.setImageTintView(binding.btnCalculator,modelPrimaryColor);
        Tools.setImageTintView(binding.btnScan,modelPrimaryColor);
        //TODO HIDE STATUS BAR

        cur_calendar.get(Calendar.YEAR);
        cur_calendar.get(Calendar.MONTH);
        cur_calendar.get(Calendar.DAY_OF_MONTH);
        long date_ship_milis = cur_calendar.getTimeInMillis();
        viewModelFinance = new ViewModelProvider(this).get(ViewModelFinance.class);
        viewModelFinance.init(financeRepository);

        binding.txtHeader.setText("Masukkan Data Keuangan");
        binding.txtDescription.setText("Silakan masukkan data keuangan Anda pada form yang tersedia.");
        binding.txtDate.setText(getFormattedDateSimple(date_ship_milis));
        date = getFormattedDateSimple(date_ship_milis);
        month = getFormattedMonthSimple(date_ship_milis);
        modelUser = (ModelUser) getIntent().getSerializableExtra("user");
        locale =modelUser.getType_currency().equalsIgnoreCase("IDR")? Tools.getLocaleIDN():Tools.getLocaleUS();

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
            // Panggil fungsi inisialisasi Anda di sini
            initLlmInference();

            // 3. Setelah selesai dimuat (atau gagal), kembalikan kontrol ke UI (Main Thread)
            runOnUiThread(() -> {
                if (litertEngine != null) {
                    // Model berhasil dimuat
                    binding.etDescription.setText(""); // Kosongkan keterangan
                    binding.btnScan.setEnabled(true);  // Aktifkan kembali tombol scan
                    Toast.makeText(this, "AI Siap Digunakan!", Toast.LENGTH_SHORT).show();
                } else {
                    // Model gagal dimuat (misal belum diunduh)
                    binding.etDescription.setText("AI belum diunduh atau terjadi kesalahan.");
                    binding.btnScan.setEnabled(true); // Tetap aktifkan jika pengguna ingin input manual
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
        int id = v.getId();
        if (id == R.id.tvFileName) {
            dialogPreviewImage.show(filePhoto.getPath());
        } else if (id == R.id.btnCamera) {
            isFromScanImage = false;

            if (checkCameraPermission()) {
                openCamera();
            } else {
                requestCameraPermission.launch(android.Manifest.permission.CAMERA);
            }
        } else if (id == R.id.btn_scan) {
            isFromScanImage = true;
            showDialogChoosePicture();
        } else if (id == R.id.btnClose) {
            binding.rlPreviewImage.setVisibility(View.GONE);
            filePhoto = null;
        } else if (id == R.id.btnGallery) {
            isFromScanImage = false;
            String permission = getGalleryPermission();
            if (permission != null && ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{permission}, REQUEST_CODE);
            } else {
                openGallery();
            }
        } else if (id == R.id.place_category) {
            if (!type.isEmpty()) {
                showDialogCategory();
            } else {
                Toast.makeText(this, "Pilih tipe terlebih dahulu", Toast.LENGTH_SHORT).show();
            }
        } else if (id == R.id.place_type) {
            showDialogType();
        } else if (id == R.id.place_date) {
            showDatePickerDialog();
        } else if (id == R.id.img_back) {
            finish();
        } else if (id == R.id.btn_calculator) {
            dialogCalculator = new DialogCalculator(this, getLayoutInflater(), result -> {
                jumlah = Tools.convertToCurrency(result,locale);
                binding.etAmount.setText(jumlah);
            });
            dialogCalculator.show();
        } else if (id == R.id.place_submit) {
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
                       if(result.equalsIgnoreCase("yes")){
                           ModelFinance model = new ModelFinance();
                           model.setDate(date);
                           model.setJumlah(Tools.replaceCurrencyStringToDouble(jumlah));
                           model.setTipe(type);
                           model.setKategori(category);
                           model.setKeterangan(binding.etDescription.getText().toString().trim());
                           model.setMonth(month);
                           model.setId_finance_user(modelUser.getId());
                           model.setType_currency(modelUser.getType_currency());
                           if(filePhoto!=null)
                               model.setPhoto(filePhoto.getPath());

                           CreateFinanceActivity.this.onSubmit(model);
                           Intent intent = new Intent();
                           intent.putExtra("finance", model);
                           setResult(RESULT_OK, intent);
                           Toast.makeText(CreateFinanceActivity.this, "Catatan "+type+" berhasil di tambahkan !", Toast.LENGTH_SHORT).show();

                           finish();

                       }
                    }
                });
                dialogConfirm.showDialogConfirm("Submit","Apakah anda yakin ingin submit data ?");
            }
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
    @SuppressLint("SuspiciousIndentation")
    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    if(isFromScanImage){
//                        InputImage image;
//                        try {
//                            TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
//                            image = InputImage.fromFilePath(this, imageUri); // Ganti 'this' jika di Fragment
//                            Bitmap receiptBitmap = getBitmapFromUri(imageUri);
//                            recognizer.process(image).addOnSuccessListener(text -> {
//                                String rawOcrText = text.getText();
//
//                                // Tampilkan teks mentah ke UI (opsional, untuk debugging)
//                                binding.etDescription.setText("Sedang memproses AI...\n\nRaw OCR:\n" + rawOcrText);
//
//                                // Panggil fungsi LLM
//
//                                processReceiptWithLlm(rawOcrText);
//                            }).addOnFailureListener(e -> {
//                                Log.e("OCR_ERROR", "Gagal membaca gambar", e);
//                            });
////                            processReceiptWithLlm(receiptBitmap);
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
                        String ocrResult = result.getData().getStringExtra(CameraOcrActivity.EXTRA_OCR_RESULT);

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
                            Bitmap originalBitmap = getBitmapFromUri(imageUri);


                            Bitmap processedBitmap = preprocessReceipt(originalBitmap);

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
    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        Bitmap bitmap;

        // Untuk Android 9 (API 28) ke atas
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.Source source = ImageDecoder.createSource(this.getContentResolver(), uri);
            bitmap = ImageDecoder.decodeBitmap(source, (decoder, info, src) -> {
                // Memastikan bitmap menggunakan alokasi memori software agar bisa dikonversi OpenCV
                decoder.setAllocator(ImageDecoder.ALLOCATOR_SOFTWARE);
                decoder.setMutableRequired(true);
            });
        } else {
            // Untuk Android 8 ke bawah
            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
        }

        // Garansi format ARGB_8888 agar Utils.bitmapToMat() milik OpenCV tidak crash
        return bitmap.copy(Bitmap.Config.ARGB_8888, true);
    }
    private Bitmap preprocessReceipt(Bitmap originalBitmap) {
        // 1. Ubah Bitmap Android menjadi Mat (Matrix format OpenCV)
        Mat src = new Mat();
        Utils.bitmapToMat(originalBitmap, src);

        // 2. Ubah ke Grayscale (Hitam Putih)
        Mat gray = new Mat();
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);

        // 3. Beri sedikit efek Blur untuk menghilangkan noise/bintik pada foto kertas
        Imgproc.GaussianBlur(gray, gray, new Size(5, 5), 0);

        // 4. Adaptive Thresholding (Ini MAGIC-nya!)
        // Ini akan membaca kontras lokal, jadi bayangan HP yang jatuh ke struk akan hilang.
        // Teks hitam akan jadi sangat pekat, dan kertas putih/kuning akan jadi putih bersih.
        Mat thresholded = new Mat();
        Imgproc.adaptiveThreshold(
                gray,                   // Input
                thresholded,            // Output
                255,                    // Nilai maksimum (Putih)
                Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, // Metode adaptif
                Imgproc.THRESH_BINARY,  // Tipe threshold (hitam putih tegas)
                11,                     // Ukuran blok piksel (bisa diubah, biasanya ganjil 11-15)
                2                       // Konstanta pengurang (bisa diubah 2-5)
        );

        // 5. Kembalikan Mat menjadi Bitmap Android
        Bitmap processedBitmap = Bitmap.createBitmap(thresholded.cols(), thresholded.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(thresholded, processedBitmap);

        // 6. Bersihkan memori C++ (Sangat penting agar tidak memory leak!)
        src.release();
        gray.release();
        thresholded.release();

        return processedBitmap;
    }

    // 🔹 Fungsi Membuka Kamera
    private void openCamera() {

//        // Buat file untuk menyimpan gambar
//        File photoFile = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "photo_" + System.currentTimeMillis() + ".jpg");
//
//        // Dapatkan Uri menggunakan FileProvider
//        imageUri = FileProvider.getUriForFile(this, getPackageName() + ".provider", photoFile);
//
//        // Buat intent kamera
//        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
//        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//        cameraLauncher.launch(intent);
        Intent intent = new Intent(this, CameraOcrActivity.class);
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
    private void initLlmInference() {
        File modelFile = new File(getExternalFilesDir(null), "Qwen2_0.5B_Instruct.litertlm");

        if (modelFile.exists()) {
            new Thread(() -> {
                try {
                    // 1. Persiapkan parameter konfigurasi
                    String modelPath = modelFile.getAbsolutePath();

                    // Gunakan CPU sebagai default yang aman, atau Backend.GPU jika perangkat mendukung
                    // Asumsi class Backend memiliki enum/konstanta CPU
                    Backend backend = new Backend.CPU();

                    // Kosongkan vision dan audio karena Qwen2_0.5B_Instruct hanya model teks
                    Backend visionBackend = null;
                    Backend audioBackend = null;

                    // Batasi jumlah token karena output struk (JSON) tidak akan terlalu panjang
                    // Ini akan sangat menghemat pemakaian RAM
                    Integer maxNumTokens = 2054;
                    // Gunakan folder cache bawaan aplikasi Android untuk mempercepat inisialisasi berikutnya
                    String cacheDir = getCacheDir().getAbsolutePath();

                    // 2. Buat konfigurasi Engine
                    // (Sesuaikan urutan ini jika EngineConfig menggunakan pola Builder seperti EngineConfig.Builder())
                    EngineConfig config = new EngineConfig(
                            modelPath,
                            backend,
                            visionBackend,
                            audioBackend,
                            maxNumTokens,
                            cacheDir
                    );

                    // 3. Buat dan Inisialisasi Engine (Berjalan di background)
                    litertEngine = new Engine(config);
                    litertEngine.initialize();

                    // 4. Buat sesi percakapan
                    litertConversation = litertEngine.createConversation(new ConversationConfig());

                    runOnUiThread(() -> {
                        Log.d("LITERT", "Engine & Conversation berhasil diinisialisasi dengan maxTokens: " + maxNumTokens);
                    });

                } catch (Exception e) {
                    runOnUiThread(() -> {
                        Log.e("LITERT_ERROR", "Gagal memuat model LiteRT: ", e);
                    });
                }
            }).start();
        } else {
            Log.w("LITERT", "File model tidak ditemukan di path: " + modelFile.getAbsolutePath());
        }
    }
    private void parseLlmResult(String fullResult) {
        try {
            // 1. Ekstraksi teks aman (Hanya mengambil blok yang ada di dalam kurung kurawal)
            String jsonString = fullResult;
            int startIndex = jsonString.indexOf("{");
            int endIndex = jsonString.lastIndexOf("}");

            if (startIndex != -1 && endIndex != -1 && startIndex <= endIndex) {
                jsonString = jsonString.substring(startIndex, endIndex + 1);
            } else {
                throw new Exception("Kurung kurawal JSON tidak ditemukan pada output AI");
            }

            // 2. Parsing ke JSONObject
            JSONObject jsonObject = new JSONObject(jsonString);

            String namaToko = jsonObject.optString("nama_toko", "").trim();
            String tanggal = jsonObject.optString("tanggal", "").trim();
            String total = jsonObject.optString("total", "").trim();
            String subtotal = jsonObject.optString("subtotal", "").trim();
            String otherFee = jsonObject.optString("other_fee", "").trim();
            String cash = jsonObject.optString("cash", "").trim();
            String produk = jsonObject.optString("daftar_produk", "").trim();

            // Pastikan total hanya berisi angka murni (buang titik/koma jika AI bandel)
            total = total.replaceAll("[^\\d]", "");

            // 3. JARING PENGAMAN (FALLBACK) UNTUK TOTAL
            // Jika AI mengosongkan total atau mengembalikan 0, ambil alih pakai Regex
            if (total.isEmpty() || total.equals("0")) {
                Log.w("LLM_FIX", "AI gagal menebak Total, mengaktifkan pencarian Regex...");
                java.util.regex.Matcher m = java.util.regex.Pattern.compile("\\d{1,3}(?:[.,]\\d{3})+").matcher(currentOcrText);

                int maxTotal = 0;
                while (m.find()) {
                    try {
                        int foundNumber = Integer.parseInt(m.group().replace(".", "").replace(",", ""));
                        if (foundNumber > maxTotal) {
                            maxTotal = foundNumber;
                        }
                    } catch (NumberFormatException ignored) {}
                }

                if (maxTotal > 0) {
                    total = String.valueOf(maxTotal);
                    Log.i("LLM_FIX", "Total berhasil diselamatkan dengan Regex: " + total);
                }
            }

            // 4. Update UI untuk Total dan Tanggal
            if (!total.isEmpty() && !total.equals("0")) {
                binding.etAmount.setText(total);
            }
            if (!tanggal.isEmpty() && !tanggal.equals("-") && !tanggal.equalsIgnoreCase("null")) {
                binding.txtDate.setText(tanggal);
            }

            // 5. Susun catatan rapi untuk kolom Deskripsi
            StringBuilder catatanBuilder = new StringBuilder();

            if (!namaToko.isEmpty() && !namaToko.equals("-") && !namaToko.equalsIgnoreCase("null")) {
                catatanBuilder.append("Toko: ").append(namaToko).append("\n\n");
            }
            if (!produk.isEmpty() && !produk.equals("-") && !produk.equalsIgnoreCase("null")) {
                catatanBuilder.append("Produk:\n").append(produk).append("\n\n");
            }
            if (!subtotal.isEmpty() && !subtotal.equals("0")) {
                catatanBuilder.append("Subtotal: ").append(subtotal).append("\n");
            }
            if (!otherFee.isEmpty() && !otherFee.equals("0")) {
                catatanBuilder.append("Other Fee: ").append(otherFee).append("\n");
            }
            if (!cash.isEmpty() && !cash.equals("0")) {
                catatanBuilder.append("Cash: ").append(cash).append("\n");
            }

            String hasilAkhirCatatan = catatanBuilder.toString().trim();
            binding.etDescription.setText(hasilAkhirCatatan.isEmpty() ? "Hasil ekstraksi selesai" : hasilAkhirCatatan);

        } catch (Exception e) {
            Log.e("LLM_JSON_ERROR", "Gagal parsing JSON: " + fullResult, e);
            binding.etDescription.setText("Gagal memproses format JSON AI.\n\nTeks Asli:\n" + fullResult);
        }
    }
    private void processReceiptWithLlm(String ocrText) {
        if (litertConversation == null) {
            binding.etDescription.setText("Error: Engine LiteRT belum siap atau belum diinisialisasi.");
            return;
        }

        currentOcrText = ocrText;
        binding.etDescription.setText("AI sedang mengekstrak data struk...\nMohon tunggu.");
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

                    runOnUiThread(() -> {
                        parseLlmResult(fullTextResponse); // Sekarang teksnya sudah 100% utuh
                    });
                }

                @Override
                public void onError(@NonNull Throwable throwable) {
                    Log.e("LLM_ERROR", "Gagal memproses AI", throwable);
                    runOnUiThread(() -> {
                        binding.etDescription.setText("Gagal memproses AI: " + throwable.getMessage());
                    });
                }
            }, extraContext);

        } catch (Exception e) {
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
        try {
            // TUTUP sesi yang lama jika sudah ada
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

    private String getPrompt(String ocrText) {
        return "Anda adalah sistem ekstraksi data otomatis. Ekstrak data dari INPUT OCR ke dalam OUTPUT JSON murni tanpa tambahan teks apapun.\n" +
                "ATURAN MUTLAK (WAJIB DIPATUHI):\n" +
                "1. DILARANG MENGARANG ANGKA! Jika harga suatu produk gratis atau tidak terbaca, tulis 'harga 0'.\n" +
                "2. DILARANG MENYINGKAT NAMA PRODUK! Tulis nama produk persis seperti OCR (contoh: 'S/Mie Grg KS' JANGAN diubah jadi 'Mie Goreng').\n" +
                "3. PENCARIAN HARGA: Harga selalu berkaitan dengan pola jumlah (contoh: '6,500 X 1'). Pasangkan dengan hati-hati.\n" +
                "4. KEUANGAN: 'subtotal' adalah total harga sebelum pajak (DPP/Subtotal). 'other_fee' adalah pajak (PPN/PB1) atau biaya admin. 'total' adalah total bayar akhir.\n" +
                "5. PEMBAYARAN: Jika OCR mengandung kata QRIS, Card, Debit, BCA, Mandiri, dll (non-tunai), maka kolom 'cash' WAJIB diisi '0'.\n" +
                "6. Kolom 'daftar_produk' WAJIB menggunakan format: [Nama Produk Utuh] harga [Angka]. Pisahkan antar produk dengan koma.\n\n" +
                "=== CONTOH 1 (Kasus Standar & Tunai) ===\n" +
                "INPUT OCR:\n" +
                "Kopi Kenangan\n" +
                "15/02/2026\n" +
                "Kopi Susu 45.000\n" +
                "Pajak 5.000\n" +
                "Total 50.000\n" +
                "Tunai 100.000\n" +
                "OUTPUT JSON:\n" +
                "{\n" +
                "  \"nama_toko\": \"Kopi Kenangan\",\n" +
                "  \"tanggal\": \"15/02/2026\",\n" +
                "  \"total\": \"50000\",\n" +
                "  \"subtotal\": \"45000\",\n" +
                "  \"other_fee\": \"5000\",\n" +
                "  \"cash\": \"100000\",\n" +
                "  \"daftar_produk\": \"Kopi Susu harga 45000\"\n" +
                "}\n\n" +
                "=== CONTOH 2 (Kasus Minimarket & QRIS) ===\n" +
                "INPUT OCR:\n" +
                "FAMILY MART\n" +
                "08/09/2026 19:40\n" +
                "Ultra Plain 200Ml 6,500 X 1\n" +
                "SENDOK 0 X 1\n" +
                "S/Mie Grg KS Ckn81g 5,700 X 1\n" +
                "BCA QRIS\n" +
                "DPP : 11,091\n" +
                "PPN : 1,109\n" +
                "Total Rp 12,200\n" +
                "OUTPUT JSON:\n" +
                "{\n" +
                "  \"nama_toko\": \"FAMILY MART\",\n" +
                "  \"tanggal\": \"08/09/2026\",\n" +
                "  \"total\": \"12200\",\n" +
                "  \"subtotal\": \"11091\",\n" +
                "  \"other_fee\": \"1109\",\n" +
                "  \"cash\": \"0\",\n" +
                "  \"daftar_produk\": \"Ultra Plain 200Ml harga 6500, SENDOK harga 0, S/Mie Grg KS Ckn81g harga 5700\"\n" +
                "}\n\n" +
                "=== TUGAS ASLI ===\n" +
                "INPUT OCR:\n" +
                ocrText + "\n" +
                "OUTPUT JSON:\n";
    }
}
