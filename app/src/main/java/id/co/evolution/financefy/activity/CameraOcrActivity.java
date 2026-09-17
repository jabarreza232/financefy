package id.co.evolution.financefy.activity;

import static id.co.evolution.financefy.helper.Tools.getFileFromUri;
import static id.co.evolution.financefy.helper.Tools.getFileName;
import static id.co.evolution.financefy.helper.Tools.getRealPathFromURI;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import id.co.evolution.financefy.R;
import id.co.evolution.financefy.databinding.ActivityCameraOcrBinding;
import id.co.evolution.financefy.helper.Tools;

@ExperimentalGetImage
public class CameraOcrActivity extends AppCompatActivity {


    private ImageCapture imageCapture;
    private ExecutorService cameraExecutor;
    private TextRecognizer textRecognizer;
    private ObjectAnimator scanAnimator;
    private static final int REQUEST_CODE_PERMISSIONS = 10;
    public static final String EXTRA_OCR_RESULT = "ocr_result_text";
    public static final String IMAGE_RESULT = "image_result";
    private Camera camera;
    private boolean isFlashOn = false;
    public ActivityCameraOcrBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding= DataBindingUtil.setContentView(this,R.layout.activity_camera_ocr);


        cameraExecutor = Executors.newSingleThreadExecutor();
        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

// Berikan aksi klik pada tombol Flash
        binding.btnFlash.setOnClickListener(v -> {
           toggleFlash();
        });
        // Cek Izin Kamera
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CODE_PERMISSIONS);
        }

        binding.btnScan.setOnClickListener(v -> takePhotoAndProcessOcr());
        binding.btnGallery.setOnClickListener(v->{
            openGallery();
        });

        binding.btnClose.setOnClickListener(v->{
            finish();
        });
        startScanAnimation();
    }
    private void startScanAnimation() {
        // Karena kita tidak tahu tinggi pasti layar saat onCreate, kita gunakan post()
        binding.scanFrame.post(() -> {
            // Ambil tinggi kotak pembatas
            float frameHeight = binding.scanFrame.getHeight();

            // Buat animasi bergerak dari Y=0 ke batas bawah kotak
            scanAnimator = ObjectAnimator.ofFloat(binding.scanLine, "translationY", 0f, frameHeight);
            scanAnimator.setDuration(2000); // Kecepatan scan (2 detik dari atas ke bawah)
            scanAnimator.setInterpolator(new AccelerateDecelerateInterpolator());

            // Buat bergerak bolak-balik (atas -> bawah -> atas) tiada henti
            scanAnimator.setRepeatMode(ValueAnimator.REVERSE);
            scanAnimator.setRepeatCount(ValueAnimator.INFINITE);

            scanAnimator.start();
        });
    }
    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                // Setup Preview
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(binding.viewFinder.getSurfaceProvider());

                // Setup ImageCapture (Fokus pada kualitas agar OCR maksimal)
                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                        .build();

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                cameraProvider.unbindAll();
                camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);

            } catch (ExecutionException | InterruptedException e) {
                Log.e("CameraX", "Gagal memulai kamera", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }
    private void openGallery() {
        Intent intent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
            intent = new Intent(MediaStore.ACTION_PICK_IMAGES);
        } else {
            intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        }
        galleryLauncher.launch(intent);
    }
    private final ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        processGalleryImage(imageUri);
                    } else {
                        // User menekan tombol 'Back' tanpa memilih gambar
                        resetLoadingState();
                    }

                }
            });
    private void processGalleryImage(Uri imageUri) {
        binding.btnScan.setEnabled(false);
        binding.btnGallery.setEnabled(false);
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.loadingOverlay.setVisibility(View.VISIBLE);

        try {
            InputImage image = InputImage.fromFilePath(this, imageUri);

            textRecognizer.process(image)
                    .addOnSuccessListener(visionText -> {
                        String sortedOcrText = extractStructuredText(visionText);

                        String cleanedText = sortedOcrText.replaceAll("[^a-zA-Z0-9.,/\\- RpX%]", " ")
                                .replaceAll(" +", " ");

                        Intent resultIntent = new Intent();
                        resultIntent.putExtra(EXTRA_OCR_RESULT, cleanedText);
                        resultIntent.putExtra(IMAGE_RESULT,imageUri);
                        setResult(RESULT_OK, resultIntent);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("ML_KIT", "Gagal membaca teks dari Galeri", e);
                        Toast.makeText(this, "Gagal mengenali teks pada gambar", Toast.LENGTH_SHORT).show();
                        resetLoadingState();
                    });

        } catch (IOException e) {
            Log.e("Gallery", "Gagal memuat gambar", e);
            Toast.makeText(this, "Gagal memuat gambar dari penyimpanan", Toast.LENGTH_SHORT).show();
            resetLoadingState();
        }
    }
    private void takePhotoAndProcessOcr() {
        if (imageCapture == null) return;

        if (scanAnimator != null) scanAnimator.cancel(); // Hentikan animasi garis
        binding.scanLine.setVisibility(View.GONE); // Sembunyikan garis
        // Kunci tombol agar tidak di-spam user
        binding.btnScan.setEnabled(false);
        binding.progressBar.setVisibility(View.VISIBLE);

        // Ambil gambar langsung ke memory (RAM), TIDAK DISIMPAN KE PENYIMPANAN
        imageCapture.takePicture(ContextCompat.getMainExecutor(this), new ImageCapture.OnImageCapturedCallback() {
            @Override
            public void onCaptureSuccess(@NonNull ImageProxy imageProxy) {
                processImageWithMlKit(imageProxy);
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                Log.e("CameraX", "Gagal memotret", exception);
                runOnUiThread(() -> {
                    Toast.makeText(CameraOcrActivity.this, "Gagal memotret gambar", Toast.LENGTH_SHORT).show();
                   resetLoadingState();
                });
            }
        });
    }
    private Bitmap preprocessReceipt(Bitmap originalBitmap) {
        // 1. Siapkan kanvas kosong (Matriks) untuk OpenCV
        Mat src = new Mat();
        Utils.bitmapToMat(originalBitmap, src);

        // 2. Grayscale (Ubah ke abu-abu)
        Mat gray = new Mat();
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_RGB2GRAY);

        // 3. Adaptive Thresholding (Fokus Utama)
        Mat thresholded = new Mat();
        /*
         * RAHASIA ANGKA DI SINI:
         * 255: Nilai warna putih maksimal.
         * ADAPTIVE_THRESH_GAUSSIAN_C: Algoritma yang pintar mencari bayangan lokal.
         * THRESH_BINARY: Latar jadi putih, teks jadi hitam.
         * 21 (Block Size): Ukuran area piksel tetangga yang dianalisis. Harus angka ganjil (15, 21, 25).
         * 10 (C): Konstanta pengurang. Semakin besar, background makin bersih dari bercak/noise.
         */
        Imgproc.adaptiveThreshold(gray, thresholded, 255,
                Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
                Imgproc.THRESH_BINARY,
                21, 10);

        // 4. Kembalikan ke format Bitmap Android
        Bitmap processedBitmap = Bitmap.createBitmap(thresholded.cols(), thresholded.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(thresholded, processedBitmap);

        // WAJIB: Hapus memori C++ secara manual agar tidak Force Close (OOM)
        src.release();
        gray.release();
        thresholded.release();

        return processedBitmap;
    }
    private void processImageWithMlKit(ImageProxy imageProxy) {
        try {
            // 1. Ambil Bitmap murni dari CameraX
            Bitmap rawBitmap = imageProxy.toBitmap();
            int rotationDegrees = imageProxy.getImageInfo().getRotationDegrees();

            // Tutup ImageProxy secepat mungkin agar kamera tidak "hang"
            imageProxy.close();

            // 2. Putar Bitmap agar tegak lurus (CameraX kadang memberikan gambar miring)
            Matrix matrix = new Matrix();
            matrix.postRotate(rotationDegrees);
            Bitmap rotatedBitmap = Bitmap.createBitmap(rawBitmap, 0, 0, rawBitmap.getWidth(), rawBitmap.getHeight(), matrix, true);
            Bitmap croppedBitmap = cropBitmapToFrame(rotatedBitmap);
            Bitmap finalCleanBitmap = preprocessReceipt(rotatedBitmap);

            InputImage image = InputImage.fromBitmap(finalCleanBitmap, 0);

            textRecognizer.process(image)
                    .addOnSuccessListener(visionText -> {
                        String sortedOcrText = extractStructuredText(visionText);

                        String cleanedText = sortedOcrText.replaceAll("[^a-zA-Z0-9.,/\\- RpX%]", " ")
                                .replaceAll(" +", " ");

                        Intent resultIntent = new Intent();
                        resultIntent.putExtra(EXTRA_OCR_RESULT, cleanedText);
                        resultIntent.putExtra(IMAGE_RESULT, Tools.bitmapToUri(this, rawBitmap) );
                        setResult(RESULT_OK, resultIntent);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("ML_KIT", "Gagal membaca teks", e);
                        resetLoadingState();
                    });

        } catch (Exception e) {
            Log.e("CameraX", "Gagal mengolah gambar", e);
            if (imageProxy != null) imageProxy.close();
            resetLoadingState();
        }
    }
    private Bitmap cropBitmapToFrame(Bitmap originalBitmap) {
        // 1. Ambil ukuran layar (PreviewView)
        float previewWidth = binding.viewFinder.getWidth();
        float previewHeight = binding.viewFinder.getHeight();

        // 2. Ambil ukuran gambar asli dari kamera
        float imageWidth = originalBitmap.getWidth();
        float imageHeight = originalBitmap.getHeight();

        // 3. Hitung rasio skala antara gambar asli dan layar HP
        float scaleX = imageWidth / previewWidth;
        float scaleY = imageHeight / previewHeight;

        // 4. Ambil posisi dan ukuran kotak scanFrame di layar
        float frameLeft = binding.scanFrame.getLeft();
        float frameTop = binding.scanFrame.getTop();
        float frameWidth = binding.scanFrame.getWidth();
        float frameHeight = binding.scanFrame.getHeight();

        // 5. Konversi koordinat UI kotak ke koordinat Bitmap murni
        int cropX = (int) (frameLeft * scaleX);
        int cropY = (int) (frameTop * scaleY);
        int cropWidth = (int) (frameWidth * scaleX);
        int cropHeight = (int) (frameHeight * scaleY);

        // 6. Validasi agar pemotongan tidak keluar dari batas gambar (mencegah Crash)
        cropX = Math.max(0, cropX);
        cropY = Math.max(0, cropY);
        if (cropX + cropWidth > imageWidth) cropWidth = (int) (imageWidth - cropX);
        if (cropY + cropHeight > imageHeight) cropHeight = (int) (imageHeight - cropY);

        // 7. Potong dan kembalikan gambarnya!
        return Bitmap.createBitmap(originalBitmap, cropX, cropY, cropWidth, cropHeight);
    }
    private String extractStructuredText(Text visionText) {
        // 1. Ambil semua elemen (kata) dan buang yang tidak punya koordinat
        List<Text.Element> allElements = new ArrayList<>();
        for (Text.TextBlock block : visionText.getTextBlocks()) {
            for (Text.Line line : block.getLines()) {
                for (Text.Element element : line.getElements()) {
                    if (element.getBoundingBox() != null) {
                        allElements.add(element);
                    }
                }
            }
        }

        if (allElements.isEmpty()) return "";

        // 2. Urutkan semua kata murni dari atas ke bawah (Sumbu Y)
        Collections.sort(allElements, (e1, e2) ->
                Integer.compare(e1.getBoundingBox().top, e2.getBoundingBox().top));

        // 3. Kelompokkan kata-kata yang sejajar (atau hampir sejajar) menjadi baris-baris
        List<List<Text.Element>> rows = new ArrayList<>();
        List<Text.Element> currentRow = new ArrayList<>();

        for (Text.Element element : allElements) {
            if (currentRow.isEmpty()) {
                currentRow.add(element);
            } else {
                Rect prevRect = currentRow.get(0).getBoundingBox(); // Kata pertama sebagai acuan baris
                Rect currRect = element.getBoundingBox();

                // Toleransi: setengah dari tinggi kata acuan.
                // Jika kemiringannya masih dalam batas ini, gabungkan dalam 1 baris.
                int yTolerance = prevRect.height() / 2;

                if (Math.abs(prevRect.centerY() - currRect.centerY()) <= yTolerance) {
                    currentRow.add(element); // Masih satu baris
                } else {
                    rows.add(currentRow); // Simpan baris yang sudah jadi
                    currentRow = new ArrayList<>(); // Buat baris baru
                    currentRow.add(element);
                }
            }
        }
        // Jangan lupa masukkan baris terakhir
        if (!currentRow.isEmpty()) {
            rows.add(currentRow);
        }

        // 4. Urutkan kata di dalam masing-masing baris dari kiri ke kanan (Sumbu X)
        StringBuilder structuredText = new StringBuilder();
        for (List<Text.Element> row : rows) {
            row.sort(Comparator.comparingInt(e -> {
                assert e.getBoundingBox() != null;
                return e.getBoundingBox().left;
            }));

            // Gabungkan kata dengan spasi
            for (int i = 0; i < row.size(); i++) {
                structuredText.append(row.get(i).getText());
                if (i < row.size() - 1) {
                    structuredText.append(" ");
                }
            }
            structuredText.append("\n"); // Pindah baris
        }

        return structuredText.toString();
    }
    private void resetLoadingState(){
        // 1. Matikan loading dan aktifkan tombol kembali
        binding.btnScan.setEnabled(true);
        binding.progressBar.setVisibility(View.GONE);
        binding.loadingOverlay.setVisibility(View.GONE);

        // 2. Tampilkan dan jalankan lagi animasi garis laser
        binding.scanLine.setVisibility(View.VISIBLE);
        if (scanAnimator != null) {
            scanAnimator.start();
        }
    }
    private void toggleFlash() {
        if (camera != null) {
            isFlashOn = !isFlashOn;

            // Nyalakan/matikan senter
            camera.getCameraControl().enableTorch(isFlashOn);

            // Ganti warna icon atau ubah iconnya (opsional)
            binding.btnFlash.setImageResource(isFlashOn ? R.drawable.outline_flashlight_on_24 : R.drawable.ic_outline_flashlight_off_24);
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
        textRecognizer.close();
        if (scanAnimator != null) scanAnimator.cancel(); // Jangan lupa matikan animator
    }
}