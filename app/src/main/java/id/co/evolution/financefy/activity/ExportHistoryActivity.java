package id.co.evolution.financefy.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.content.ActivityNotFoundException;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.view.View;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import id.co.evolution.financefy.adapter.ExportHistoryAdapter;
import id.co.evolution.financefy.databinding.ActivityExportHistoryBinding;
import id.co.evolution.financefy.helper.TinyDb;
import id.co.evolution.financefy.helper.Tools;
import id.co.evolution.financefy.model.ModelPrimaryColor;
import id.co.evolution.financefy.model.ModelSavings;
import id.co.evolution.financefy.model.ModelUser;
@AndroidEntryPoint
public class ExportHistoryActivity extends AppCompatActivity {
    private ActivityExportHistoryBinding binding;
    private ExportHistoryAdapter adapter;
    @Inject
    TinyDb tinyDb;
    public ModelPrimaryColor modelPrimaryColor=Tools.modelPrimaryColor;

    private int colorPrimary;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityExportHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        boolean isCustomActive = tinyDb.getBoolean("is_custom_color_active");

        if (isCustomActive) {
            int customColor = tinyDb.getInt("custom_color_int");
            colorPrimary = customColor;

            getWindow().setStatusBarColor(customColor);
        } else {
            if (tinyDb.getObject("model_primary_color", ModelPrimaryColor.class) != null) {
                modelPrimaryColor = tinyDb.getObject("model_primary_color", ModelPrimaryColor.class);
                colorPrimary = ContextCompat.getColor(this, modelPrimaryColor.getColorPrimary());
                Tools.setThemeNoActionBarActivity(getTheme(), modelPrimaryColor);
            }
        }
        binding.btnBack.setOnClickListener(v -> finish());

        binding.rvExportHistory.setLayoutManager(new LinearLayoutManager(this));

        loadExportedFiles();

    }
    private void loadExportedFiles() {
        // Tentukan folder tempat Anda biasa mengekspor file
        File folder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            folder = getExternalFilesDir(null);
        } else {
            folder = Environment.getExternalStorageDirectory();
        }

        List<File> excelFiles = new ArrayList<>();

        // Pindai isi folder
        if (folder != null && folder.exists()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    // Ambil HANYA file Excel (.xlsx)
                    if (file.isFile() && file.getName().endsWith(".xlsx")) {
                        excelFiles.add(file);
                    }
                }
            }
        }

        // Urutkan file berdasarkan tanggal dibuat/dimodifikasi (Terbaru di atas)
        Collections.sort(excelFiles, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));

        // Tampilkan ke RecyclerView
        if (excelFiles.isEmpty()) {
            binding.rvExportHistory.setVisibility(View.GONE);
            binding.layoutEmptyState.setVisibility(View.VISIBLE);
        } else {
            binding.rvExportHistory.setVisibility(View.VISIBLE);
            binding.layoutEmptyState.setVisibility(View.GONE);

            adapter = new ExportHistoryAdapter(excelFiles, this::openExcelFile, this::shareExcelFile);
            binding.rvExportHistory.setAdapter(adapter);
        }
    }

    // Fungsi untuk membuka file Excel (ACTION_VIEW)
    private void openExcelFile(File file) {
        Uri fileUri = FileProvider.getUriForFile(this, getPackageName() + ".provider", file);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(fileUri, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Tidak ada aplikasi pembaca Excel (Coba install WPS Office).", Toast.LENGTH_SHORT).show();
        }
    }

    // Fungsi untuk membagikan file Excel (ACTION_SEND)
    private void shareExcelFile(File file) {
        Uri fileUri = FileProvider.getUriForFile(this, getPackageName() + ".provider", file);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setDataAndType(fileUri, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        intent.putExtra(Intent.EXTRA_STREAM, fileUri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(intent, "Bagikan Excel via..."));
    }
}