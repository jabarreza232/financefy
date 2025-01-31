package id.co.evolution.financefy.activity;

import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.databinding.DataBindingUtil;

import id.co.evolution.financefy.R;
import id.co.evolution.financefy.databinding.ActivityFullScreenPhotoBinding;
import id.co.evolution.financefy.helper.Tools;

public class FullScreenPhotoActivity extends AppCompatActivity {
    String photoPath;
    ActivityFullScreenPhotoBinding binding;
    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetector gestureDetector;

    private float translationX = 0f;
    private float translationY = 0f;
    private float scaleFactor = 1.0f;  // Skala gambar
    private static final float MIN_SCALE = 0.5f;
    private static final float MAX_SCALE = 3.0f;
    private float imageWidth, imageHeight;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_full_screen_photo);
        photoPath = getIntent().getStringExtra("path");
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);



        // Set the image (replace with your image resource)
        binding.fullScreenImage.setImageBitmap(Tools.convertFileToBitmap(photoPath));
        binding.btnClose.setOnClickListener(v->finish());
        // Set ScaleGestureDetector untuk menangani zoom
        scaleGestureDetector = new ScaleGestureDetector(this, new ScaleListener());
        gestureDetector = new GestureDetector(this, new GestureListener());
        // Mengambil dimensi gambar setelah dimuat
        binding.fullScreenImage.post(() -> {
            imageWidth = binding.fullScreenImage.getDrawable().getIntrinsicWidth();
            imageHeight = binding.fullScreenImage.getDrawable().getIntrinsicHeight();
        });
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Meneruskan event sentuhan ke ScaleGestureDetector untuk menangani zoom
        scaleGestureDetector.onTouchEvent(event);
        gestureDetector.onTouchEvent(event);

        return super.onTouchEvent(event);
    }
    // Listener untuk mendeteksi geser (drag) menggunakan GestureDetector
    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            // Menerapkan gerakan drag (geser) pada gambar
            if (scaleFactor > MIN_SCALE) {  // Pastikan gambar sudah di-zoom
                translationX -= distanceX;
                translationY -= distanceY;

                // Membatasi translasi agar gambar tetap dalam layar
                translationX = Math.max(0, Math.min(translationX, getMaxTranslationX()));
                translationY = Math.max(0, Math.min(translationY, getMaxTranslationY()));

                // Menerapkan transformasi posisi pada gambar
                binding.fullScreenImage.setTranslationX(translationX);
                binding.fullScreenImage.setTranslationY(translationY);
            }
            return true;
        }
    }
    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            // Mendapatkan faktor skala saat pinch terjadi
            scaleFactor *= detector.getScaleFactor();

            // Membatasi skala agar tidak terlalu kecil atau terlalu besar
            scaleFactor = Math.max(MIN_SCALE, Math.min(scaleFactor, MAX_SCALE));

            // Menerapkan transformasi skala pada ImageView
            binding.fullScreenImage.setScaleX(scaleFactor);
            binding.fullScreenImage.setScaleY(scaleFactor);
            updateTranslationLimits();

            return true;
        }

    }

    // Mengupdate batas translasi berdasarkan ukuran gambar dan layar
    private void updateTranslationLimits() {
        translationX = Math.max(0, Math.min(translationX, getMaxTranslationX()));
        translationY = Math.max(0, Math.min(translationY, getMaxTranslationY()));
    }

    // Menghitung batas translasi maksimum untuk X
    private float getMaxTranslationX() {
        float screenWidth = getResources().getDisplayMetrics().widthPixels;
        float maxTranslationX = imageWidth * scaleFactor - screenWidth;
        return Math.max(0, maxTranslationX);
    }

    // Menghitung batas translasi maksimum untuk Y
    private float getMaxTranslationY() {
        float screenHeight = getResources().getDisplayMetrics().heightPixels;
        float maxTranslationY = imageHeight * scaleFactor - screenHeight;
        return Math.max(0, maxTranslationY);
    }
}