package id.co.evolution.financefy.activity;

import androidx.appcompat.app.AppCompatActivity;
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

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import id.co.evolution.financefy.MainActivity;
import id.co.evolution.financefy.R;
import id.co.evolution.financefy.adapter.AdapterCalculator;
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
    public ModelPrimaryColor modelPrimaryColor=Tools.modelPrimaryColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(tinyDb.getObject("model_primary_color", ModelPrimaryColor.class)!=null){
            modelPrimaryColor= tinyDb.getObject("model_primary_color", ModelPrimaryColor.class);
            Tools.setThemeActivity(getTheme(),modelPrimaryColor);
            getSupportActionBar().hide();
        }

        binding = DataBindingUtil.setContentView(this,R.layout.activity_pin);

        AdapterCalculator adapterCalculator = new AdapterCalculator(this, DummyNumberPin.getNumberPinConfirm(), (data, position) -> {
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

                    Intent intent = new Intent(this, MainActivity.class);
                    intent.putExtra("isInputPin",true);

                    startActivity(intent);
                    break;

                default:
                    result += dataList.get(position);
                    break;
            }
            resultText();
        });
        binding.rvCalculator.setLayoutManager(new GridLayoutManager(this, 3));
        binding.rvCalculator.setAdapter(adapterCalculator);
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