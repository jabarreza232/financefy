package id.co.evolution.financefy.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;

import id.co.evolution.financefy.BuildConfig;
import id.co.evolution.financefy.MainActivity;
import id.co.evolution.financefy.R;
import id.co.evolution.financefy.databinding.ActivityAboutBinding;
import id.co.evolution.financefy.databinding.FragmentSettingsBinding;

public class AboutActivity extends AppCompatActivity {
    ActivityAboutBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding= DataBindingUtil.setContentView(this,R.layout.activity_about);

        binding.txtAppVersion.setText(BuildConfig.VERSION_NAME);
        binding.txtSystemVersion.setText(Build.VERSION.RELEASE);
        binding.txtDeviceModel.setText(Build.MODEL);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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