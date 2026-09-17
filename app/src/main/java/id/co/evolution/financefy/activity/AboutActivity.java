package id.co.evolution.financefy.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import id.co.evolution.financefy.BuildConfig;
import id.co.evolution.financefy.MainActivity;
import id.co.evolution.financefy.R;
import id.co.evolution.financefy.databinding.ActivityAboutBinding;
import id.co.evolution.financefy.databinding.FragmentSettingsBinding;
import id.co.evolution.financefy.helper.TinyDb;
import id.co.evolution.financefy.helper.Tools;
import id.co.evolution.financefy.model.ModelPrimaryColor;

@AndroidEntryPoint
public class AboutActivity extends AppCompatActivity {
    ActivityAboutBinding binding;
    ModelPrimaryColor modelPrimaryColor=Tools.modelPrimaryColor;
    @Inject
    TinyDb tinyDb;


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
                Tools.setThemeActivity(getTheme(), modelPrimaryColor);
            }
        }
        binding = DataBindingUtil.setContentView(this, R.layout.activity_about);

        if (isCustomActive) {
            int customColor = tinyDb.getInt("custom_color_int");
            if (getSupportActionBar() != null) {
                getSupportActionBar().setBackgroundDrawable(new ColorDrawable(customColor));
            }
        }

        binding.txtAppVersion.setText(BuildConfig.VERSION_NAME);
        binding.txtSystemVersion.setText(Build.VERSION.RELEASE);
        binding.txtDeviceModel.setText(Build.MODEL);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            Intent i = new Intent(this, MainActivity.class);
            i.putExtra("isInputPin", true);
            startActivity(i);
        }
        return super.onOptionsItemSelected(item);
    }
}