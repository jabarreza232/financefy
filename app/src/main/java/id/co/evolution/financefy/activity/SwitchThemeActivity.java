package id.co.evolution.financefy.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.util.List;

import javax.inject.Inject;

import dagger.Binds;
import dagger.hilt.android.AndroidEntryPoint;
import id.co.evolution.financefy.MainActivity;
import id.co.evolution.financefy.R;
import id.co.evolution.financefy.adapter.AdapterPrimaryColor;
import id.co.evolution.financefy.callback.MethodCallback;
import id.co.evolution.financefy.databinding.ActivitySwitchThemeBinding;
import id.co.evolution.financefy.dummy.DummyPrimaryColor;
import id.co.evolution.financefy.dummy.DummyPrimaryColor.PRIMARY_COLOR;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(tinyDb.getObject("model_primary_color", ModelPrimaryColor.class)!=null) {
            modelPrimaryColor= tinyDb.getObject("model_primary_color",ModelPrimaryColor.class);
            Tools.setThemeActivity(getTheme(),modelPrimaryColor);
        }
        binding = DataBindingUtil.setContentView(this, R.layout.activity_switch_theme);


        binding.vPrimary.setBackgroundColor(ContextCompat.getColor(this, modelPrimaryColor.getColorPrimary()));
        binding.vPrimaryDark.setBackgroundColor(ContextCompat.getColor(this, modelPrimaryColor.getColorPrimaryDark()));

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        adapterPrimaryColor = new AdapterPrimaryColor(DummyPrimaryColor.getDataPrimaryColor(), (data, position) -> {
            List<ModelPrimaryColor> dataTheme = (List<ModelPrimaryColor>) data;
            modelPrimaryColor = dataTheme.get(position);
            tinyDb.putObject("model_primary_color",modelPrimaryColor);
            binding.vPrimary.setBackgroundColor(ContextCompat.getColor(this, modelPrimaryColor.getColorPrimary()));
            binding.vPrimaryDark.setBackgroundColor(ContextCompat.getColor(this, modelPrimaryColor.getColorPrimaryDark()));
        });


        binding.rvPrimaryColor.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.rvPrimaryColor.setAdapter(adapterPrimaryColor);

        setSelectTheme();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
    }

    private void setSelectTheme(){
        selectTheme=tinyDb.getString("night_mode");
        boolean isNightModeSelected=selectTheme.equalsIgnoreCase("mode_night_yes");

        binding.rbThemeDark.setChecked(isNightModeSelected);
        binding.rbThemeLight.setChecked(!isNightModeSelected);
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
            startActivity(i);
        } else {
            saveSettings();
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveSettings() {

        int selectedId = binding.rgTheme.getCheckedRadioButtonId();
        RadioButton selectedRadioButton = (RadioButton) findViewById(selectedId);

        if(TextUtils.equals(selectedRadioButton.getText().toString(),"Dark")){
            tinyDb.putString("night_mode", "mode_night_yes");
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }else{
            tinyDb.putString("night_mode", "mode_night_no");
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