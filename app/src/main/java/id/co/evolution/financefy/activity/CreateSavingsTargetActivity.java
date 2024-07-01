package id.co.evolution.financefy.activity;

import static id.co.evolution.financefy.helper.Tools.getFormattedDateSimple;
import static id.co.evolution.financefy.helper.Tools.getFormattedMonthSimple;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Locale;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import id.co.evolution.financefy.R;
import id.co.evolution.financefy.databinding.ActivityCreateSavingsTargetBinding;
import id.co.evolution.financefy.dialog.DialogCalculator;
import id.co.evolution.financefy.dialog.DialogConfirm;
import id.co.evolution.financefy.helper.TinyDb;
import id.co.evolution.financefy.helper.Tools;
import id.co.evolution.financefy.model.ModelPrimaryColor;
import id.co.evolution.financefy.model.ModelSavings;
import id.co.evolution.financefy.model.ModelUser;
import id.co.evolution.financefy.repository.SavingsRepository;
import id.co.evolution.financefy.viewmodel.ViewModelSavings;

@AndroidEntryPoint
public class CreateSavingsTargetActivity extends AppCompatActivity implements View.OnClickListener{
    String date = "";
    private String jumlah = "";
    String month = "";
    ActivityCreateSavingsTargetBinding binding;
    Calendar cur_calendar = Calendar.getInstance();
    ViewModelSavings viewModelSaving;
    DialogCalculator dialogCalculator;

    @Inject
    TinyDb tinyDb;
    public ModelPrimaryColor modelPrimaryColor=Tools.modelPrimaryColor;


    @Inject
    SavingsRepository savingsRepository;
    ModelSavings modelSavings;
    ModelUser modelUser;
    Locale locale;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        modelPrimaryColor= tinyDb.getObject("model_primary_color", ModelPrimaryColor.class);
        Tools.setThemeNoActionBarActivity(getTheme(), modelPrimaryColor);

        binding = DataBindingUtil.setContentView(this,R.layout.activity_create_savings_target);

        Tools.setBackgroundColorView(binding.rlBackground,modelPrimaryColor);
        Tools.setImageTintView(binding.btnCalculator,modelPrimaryColor);

        //TODO HIDE STATUS BAR
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        cur_calendar.get(Calendar.YEAR);
        cur_calendar.get(Calendar.MONTH);
        cur_calendar.get(Calendar.DAY_OF_MONTH);
        long date_ship_milis = cur_calendar.getTimeInMillis();
        viewModelSaving = new ViewModelProvider(this).get(ViewModelSavings.class);
        viewModelSaving.init(savingsRepository);

        binding.txtHeader.setText("Input Data Target Tabungan");
        binding.txtDescription.setText("Silakan input data target tabungan Anda pada form yang tersedia.");

        binding.txtDate.setText(getFormattedDateSimple(date_ship_milis));
        date = getFormattedDateSimple(date_ship_milis);
        month = getFormattedMonthSimple(date_ship_milis);
        modelSavings =(ModelSavings) getIntent().getSerializableExtra("savings");
        modelUser = (ModelUser) getIntent().getSerializableExtra("user");
        locale =modelUser.getType_currency().equalsIgnoreCase("IDR")? Tools.getLocaleIDN():Tools.getLocaleUS();

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
                        String formatted = Tools.convertToCurrency(parsed,locale);

                        jumlah = formatted;
                        binding.etAmount.setText(formatted);
                        binding.etAmount.setSelection(formatted.length());
                    }

                    binding.etAmount.addTextChangedListener(this);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        binding.placeDate.setOnClickListener(this);

        binding.imgBack.setOnClickListener(this);
        binding.placeSubmit.setOnClickListener(this);
        binding.btnCalculator.setOnClickListener(this);
    }

    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = DatePickerDialog.newInstance((view, year, monthOfYear, dayOfMonth) -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, monthOfYear);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            long date_ship_milis = calendar.getTimeInMillis();
            binding.txtDate.setText(getFormattedDateSimple(date_ship_milis));
            date = getFormattedDateSimple(date_ship_milis);

            Log.e("TAG", "onDateSet: " + date);
            month = getFormattedMonthSimple(date_ship_milis);
        });

        datePickerDialog.setMinDate(cur_calendar);
        datePickerDialog.setAccentColor(getResources().getColor(modelPrimaryColor.getColorPrimary()));
        datePickerDialog.show(getSupportFragmentManager(), "PickerDialog");
    }




    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.place_date:
                showDatePickerDialog();
                break;
            case R.id.img_back:
                finish();
                break;

            case R.id.btn_calculator:
                dialogCalculator = new DialogCalculator(this, getLayoutInflater(), result -> {
                    jumlah = result;
                    binding.etAmount.setText(Tools.convertToCurrency(result,locale));
                });
                dialogCalculator.show();
                break;
            case R.id.place_submit:
                if (binding.etTitle.getText().toString().isEmpty()) {
                    binding.tilTitle.setError("Silahkan input judul terlebih dahulu");
                } else {
                    binding.tilTitle.setError(null);
                }

                if (binding.etAmount.getText().toString().isEmpty()) {
                    binding.tilAmount.setError("Silahkan input jumlah mata uang anda terlebih dahulu");
                } else {
                    binding.tilAmount.setError(null);
                }

                if (!binding.etAmount.getText().toString().isEmpty()) {
                    DialogConfirm dialogConfirm = new DialogConfirm(this, getLayoutInflater(), new DialogConfirm.DialogConfirm() {
                        @Override
                        public void onSubmit(@NonNull String result) {
                            ModelSavings model = new ModelSavings();
                            model.setDate_target(date);
                            model.setId_savings_user(modelUser.getId());
                            model.setType_currency(modelUser.getType_currency());
                            model.setTargetValue(Long.parseLong(Tools.convertCurrencyToValue(jumlah)));
                            model.setTitle(binding.etTitle.getText().toString().trim());
                            viewModelSaving.inputUpdateSavings("Create", model);

                            Intent intent = new Intent();
                            intent.putExtra("savings", modelSavings);
                            setResult(RESULT_OK, intent);
                            finish();
                            Toast.makeText(CreateSavingsTargetActivity.this, "Catatan target menabung berhasil di tambahkan !", Toast.LENGTH_SHORT).show();

                        }
                    });
                    dialogConfirm.showDialogConfirm("Submit","Apakah anda yakin ingin submit data ?");

                }
                break;
        }
    }
}