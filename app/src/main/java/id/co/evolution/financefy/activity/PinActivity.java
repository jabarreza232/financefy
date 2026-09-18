package id.co.evolution.financefy.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.GridLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.List;
import java.util.concurrent.Executor;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import id.co.evolution.financefy.MainActivity;
import id.co.evolution.financefy.R;
import id.co.evolution.financefy.adapter.AdapterCalculator;
import id.co.evolution.financefy.adapter.AdapterPinNumber;
import id.co.evolution.financefy.databinding.ActivityPinBinding;
import id.co.evolution.financefy.dummy.DummyNumberPin;
import id.co.evolution.financefy.helper.TinyDb;
import id.co.evolution.financefy.helper.Tools;
import id.co.evolution.financefy.model.ModelPrimaryColor;

@AndroidEntryPoint
public class PinActivity extends AppCompatActivity {
    ActivityPinBinding binding;
    String result = "";
    @Inject
    TinyDb tinyDb;
    public ModelPrimaryColor modelPrimaryColor = Tools.modelPrimaryColor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Inisialisasi Tema
        boolean isCustomActive = tinyDb.getBoolean("is_custom_color_active");
        if (isCustomActive) {
            int customColor = tinyDb.getInt("custom_color_int");
            getWindow().setStatusBarColor(customColor);
        } else {
            if (tinyDb.getObject("model_primary_color", ModelPrimaryColor.class) != null) {
                modelPrimaryColor = tinyDb.getObject("model_primary_color", ModelPrimaryColor.class);
                Tools.setThemeNoActionBarActivity(getTheme(), modelPrimaryColor);
            }
        }

        binding = DataBindingUtil.setContentView(this, R.layout.activity_pin);

        // 2. Setup Keypad PIN Manual
        AdapterPinNumber adapterPin = new AdapterPinNumber(this, DummyNumberPin.getNumberPinConfirm(), (data, position) -> {
            List<String> dataList = (List<String>) data;

            switch (dataList.get(position)) {
                case "C":
                    clearCalculate();
                    break;
                case "OK":
                    String pinActually = tinyDb.getString("pin");

                    if (result.length() < 6) {
                        Toast.makeText(this, "Mohon untuk di isi minimal 6 digit!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (!result.equalsIgnoreCase(pinActually)) {
                        Toast.makeText(this, "Pin yang di input salah!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // PIN BENAR -> Masuk MainActivity
                    Intent intent = new Intent(this, MainActivity.class);
                    if (getIntent().getBooleanExtra("ACTION_TRIGGER_EXPORT", false)) {
                        intent.putExtra("ACTION_TRIGGER_EXPORT", true);
                    }
                    intent.putExtra("isInputPin", true);
                    startActivity(intent);
                    finish(); // PENTING: Hancurkan halaman PIN agar tidak bisa di-back
                    break;

                default:
                    if (result.length() < 6) { // Cegah input lebih dari 6 digit
                        result += dataList.get(position);
                    }
                    break;
            }
            resultText();
        });

        binding.rvCalculator.setLayoutManager(new GridLayoutManager(this, 3));
        binding.rvCalculator.setAdapter(adapterPin);
        binding.rvCalculator.setClickable(true);
        binding.rvCalculator.setFocusable(true);
        binding.rvCalculator.setFocusableInTouchMode(true);

        binding.btnDelete.setOnClickListener(v -> {
            if (!result.isEmpty() && result.length() > 1) {
                result = Tools.removeLastChar(result);
                result = result.isEmpty() ? "" : result;
                binding.etAmount.setText(result);
            } else {
                clearCalculate();
            }
        });

        binding.etAmount.setEnabled(false);
        binding.etAmount.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);

        // 3. TRIGGER FINGERPRINT OTOMATIS
        boolean isFingerprintActive = tinyDb.getBoolean("is_fingerprint_active");
        if (isFingerprintActive) {
            showBiometricPrompt();
        }
    }

    // Metode khusus untuk menangani Biometrik
    private void showBiometricPrompt() {
        BiometricManager biometricManager = BiometricManager.from(this);

        // Pastikan HP mendukung biometrik dan sudah ada sidik jari yang terdaftar
        if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS) {

            Executor executor = ContextCompat.getMainExecutor(this);
            BiometricPrompt biometricPrompt = new BiometricPrompt(PinActivity.this, executor, new BiometricPrompt.AuthenticationCallback() {
                @Override
                public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                    super.onAuthenticationSucceeded(result);

                    // SIDIK JARI BENAR -> Langsung masuk MainActivity tanpa tekan OK
                    Intent intent = new Intent(PinActivity.this, MainActivity.class);
                    intent.putExtra("isInputPin", true);
                    startActivity(intent);
                    finish(); // PENTING: Tutup halaman PIN
                }

                @Override
                public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                    super.onAuthenticationError(errorCode, errString);

                }

                @Override
                public void onAuthenticationFailed() {
                    super.onAuthenticationFailed();
                    // Sidik jari salah/tidak dikenali (Sistem Android akan mengurus pesannya otomatis)
                }
            });

            BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Login Sidik Jari")
                    .setSubtitle("Gunakan sidik jari untuk membuka Financefy")
                    .setNegativeButtonText("Gunakan PIN") // Tombol fallback ke keypad
                    .build();

            biometricPrompt.authenticate(promptInfo);
        }
    }

    private void resultText() {
        String text = result.replaceAll("[0123456789]", "*");
        binding.etAmount.setText(result);
    }

    private void clearCalculate() {
        result = "";
        binding.etAmount.setText("");
    }
}