package id.co.evolution.financefy.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import id.co.evolution.financefy.MainActivity;
import id.co.evolution.financefy.R;
import id.co.evolution.financefy.adapter.AdapterPrimaryColor;
import id.co.evolution.financefy.databinding.ActivitySwitchThemeBinding;
import id.co.evolution.financefy.dummy.DummyPrimaryColor;
import id.co.evolution.financefy.helper.TinyDb;
import id.co.evolution.financefy.helper.Tools;
import id.co.evolution.financefy.model.ModelPrimaryColor;

@AndroidEntryPoint
public class SwitchThemeActivity extends AppCompatActivity {
    AdapterPrimaryColor adapterPrimaryColor;
    ActivitySwitchThemeBinding binding;
    ModelPrimaryColor modelPrimaryColor=Tools.modelPrimaryColor;
    @Inject
    public TinyDb tinyDb;
    String selectTheme;
    boolean isCustomActive;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         isCustomActive = tinyDb.getBoolean("is_custom_color_active");

        if (isCustomActive) {
            int customColor = tinyDb.getInt("custom_color_int");
            getWindow().setStatusBarColor(customColor);
        } else {
            if (tinyDb.getObject("model_primary_color", ModelPrimaryColor.class) != null) {
                modelPrimaryColor = tinyDb.getObject("model_primary_color", ModelPrimaryColor.class);
                Tools.setThemeActivity(getTheme(), modelPrimaryColor);
            }
        }
        binding = DataBindingUtil.setContentView(this, R.layout.activity_switch_theme);

        if (isCustomActive) {
            int customColor = tinyDb.getInt("custom_color_int");
            if (getSupportActionBar() != null) {
                getSupportActionBar().setBackgroundDrawable(new ColorDrawable(customColor));
            }
            binding.vPrimary.setBackgroundColor(customColor);
            binding.vFabMockup.setCardBackgroundColor(customColor);
            binding.indicatorCustomColor.setCardBackgroundColor(customColor);

        } else {
            binding.vPrimary.setBackgroundColor(ContextCompat.getColor(this, modelPrimaryColor.getColorPrimary()));
            binding.vFabMockup.setCardBackgroundColor(ContextCompat.getColor(this, modelPrimaryColor.getColorPrimary()));
            binding.indicatorCustomColor.setCardBackgroundColor(ContextCompat.getColor(this, modelPrimaryColor.getColorPrimary()));
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        adapterPrimaryColor = new AdapterPrimaryColor(DummyPrimaryColor.getDataPrimaryColor(), (data, position) -> {

            List<ModelPrimaryColor> dataTheme = (List<ModelPrimaryColor>) data;
            modelPrimaryColor = dataTheme.get(position);
            tinyDb.putObject("model_primary_color",modelPrimaryColor);
            tinyDb.putBoolean("is_custom_color_active", false);
            binding.vPrimary.setBackgroundColor(ContextCompat.getColor(this, modelPrimaryColor.getColorPrimary()));
            binding.vFabMockup.setCardBackgroundColor(ContextCompat.getColor(this, modelPrimaryColor.getColorPrimary()));

//            binding.vPrimaryDark.setBackgroundColor(ContextCompat.getColor(this, modelPrimaryColor.getColorPrimaryDark()));
        });


        binding.rvPrimaryColor.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.rvPrimaryColor.setAdapter(adapterPrimaryColor);
        binding.cardThemeDark.setOnClickListener(v -> {
            tinyDb.putString("night_mode", "mode_night_yes");
            setSelectTheme();

        });
        binding.cardThemeLight.setOnClickListener(v -> {
            tinyDb.putString("night_mode", "mode_night_no");
            setSelectTheme();
        });
        binding.cardCustomColor.setOnClickListener(v->{
            changeCustomColor();
        });
        setSelectTheme();
    }
    private void changeCustomColor(){
        int initialColors = tinyDb.getBoolean("is_custom_color_active") ?
                tinyDb.getInt("custom_color_int") :
                ContextCompat.getColor(this, R.color.colorPrimary);

        ColorPickerDialogBuilder
                .with(this)
                .setTitle("Pilih Warna Kustom")
                .initialColor(initialColors)
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(12)
                .setOnColorSelectedListener(selectedColor -> {
                    // (Opsional) Dijalankan saat kursor digeser secara real-time
                })
                .setPositiveButton("Simpan", (dialog, selectedColor, allColors) -> {
                    // 1. Simpan nilai Integer ke TinyDB
                    tinyDb.putInt("custom_color_int", selectedColor);

                    // 2. Simpan HEX (Ubah Integer ke format String HEX)
                    String hexColor = String.format("#%06X", (0xFFFFFF & selectedColor));
                    tinyDb.putString("custom_color_hex", hexColor);

                    // 3. Set Flag Aktif
                    tinyDb.putBoolean("is_custom_color_active", true);

                    // 4. Update Mockup UI
                    binding.vPrimary.setBackgroundColor(selectedColor);
                    binding.indicatorCustomColor.setCardBackgroundColor(selectedColor);
                    binding.vFabMockup.setCardBackgroundColor(selectedColor);
                })
                .setNegativeButton("Batal", (dialog, which) -> dialog.dismiss())
                // INILAH FITUR YANG ANDA CARI:
                .showColorEdit(true) // Memunculkan kolom input HEX
                .setColorEditTextColor(ContextCompat.getColor(this, android.R.color.black))
                .build()
                .show();
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent i = new Intent(this, MainActivity.class);
        i.putExtra("isInputPin",true);
        startActivity(i);
    }

    private void setSelectTheme(){
        String currentNightMode = tinyDb.getString("night_mode");
        int themeColor;
        if(isCustomActive){
            themeColor  = tinyDb.getInt("custom_color_int");

        }else{
            themeColor  = ContextCompat.getColor(this, modelPrimaryColor.getColorPrimary());

        }

        int strokeThickness = 5;

        int colorInactive = ContextCompat.getColor(this, R.color.colorGrey);
        ColorStateList tintInactive = ColorStateList.valueOf(colorInactive);

        if (TextUtils.equals(currentNightMode, "mode_night_yes")) {
            // --- MODE GELAP AKTIF ---
            binding.cardThemeDark.setStrokeWidth(strokeThickness);
            binding.cardThemeDark.setStrokeColor(themeColor);

            // Ikon & Teks Gelap menyala
            Tools.setImageTintView(binding.imgGelap, modelPrimaryColor);
            binding.txtGelap.setTypeface(null, Typeface.BOLD);

            // --- Matikan Indikator di Kartu Terang ---
            binding.cardThemeLight.setStrokeWidth(0);
            binding.imgTerang.setImageTintList(tintInactive); // Pakai warna abu-abu
            binding.txtTerang.setTypeface(null, Typeface.NORMAL); // Kembalikan ke teks normal

        } else {
            // --- MODE TERANG AKTIF (Default) ---
            binding.cardThemeLight.setStrokeWidth(strokeThickness);
            binding.cardThemeLight.setStrokeColor(themeColor);

            // Ikon & Teks Terang menyala
            Tools.setImageTintView(binding.imgTerang, modelPrimaryColor);
            binding.txtTerang.setTypeface(null, Typeface.BOLD);

            // --- Matikan Indikator di Kartu Gelap ---
            binding.cardThemeDark.setStrokeWidth(0);
            binding.imgGelap.setImageTintList(tintInactive); // Pakai warna abu-abu
            binding.txtGelap.setTypeface(null, Typeface.NORMAL); // Kembalikan ke teks normal
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_save, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            Intent i = new Intent(this, MainActivity.class);
            i.putExtra("isInputPin",true);
            startActivity(i);
        } else {
            saveSettings();
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveSettings() {

        String currentNightMode = tinyDb.getString("night_mode");
        if (TextUtils.equals(currentNightMode, "mode_night_yes")) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {

            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        restartActivity();
    }

    private void restartActivity() {
        Intent intent = getIntent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        finish();
        startActivity(intent);
    }
}