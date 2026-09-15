package id.co.evolution.financefy.activity;

import static id.co.evolution.financefy.callback.CallbackOnActivityResult.REQUEST_CODE_SAVINGS;
import static id.co.evolution.financefy.callback.CallbackOnActivityResult.REQUEST_CODE_UPDATE_SAVINGS_TARGET;
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

import com.google.gson.Gson;
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
import id.co.evolution.financefy.repository.SavingsRepository;
import id.co.evolution.financefy.viewmodel.ViewModelSavings;

@AndroidEntryPoint
public class UpdateSavingsTargetActivity extends AppCompatActivity implements View.OnClickListener{
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
    private int id_user;

    Locale locale;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        modelPrimaryColor= tinyDb.getObject("model_primary_color", ModelPrimaryColor.class);
        Tools.setThemeNoActionBarActivity(getTheme(), modelPrimaryColor);

        binding = DataBindingUtil.setContentView(this,R.layout.activity_create_savings_target);

        Tools.setBackgroundColorView(binding.rlBackground,modelPrimaryColor);
        Tools.setImageTintView(binding.btnCalculator,modelPrimaryColor);

        binding.txtHeader.setText("Ubah Data Target Menabung");
        binding.txtDescription.setText("Silakan ubah data target menabung Anda pada form yang tersedia.");

        //TODO HIDE STATUS BAR

        cur_calendar.get(Calendar.YEAR);
        cur_calendar.get(Calendar.MONTH);
        cur_calendar.get(Calendar.DAY_OF_MONTH);
        long date_ship_milis = cur_calendar.getTimeInMillis();
        viewModelSaving = new ViewModelProvider(this).get(ViewModelSavings.class);
        viewModelSaving.init(savingsRepository);

        binding.txtHeader.setText("Input data");
        binding.txtDate.setText(getFormattedDateSimple(date_ship_milis));
        date = getFormattedDateSimple(date_ship_milis);
        month = getFormattedMonthSimple(date_ship_milis);
        modelSavings =(ModelSavings) getIntent().getSerializableExtra("savings");
        locale =modelSavings.getType_currency().equalsIgnoreCase("IDR")? Tools.getLocaleIDN():Tools.getLocaleUS();

        Log.e("TAG", "onCreate: "+ new Gson().toJson(modelSavings));
        id_user = getIntent().getIntExtra("id_user", 0);
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

        loadData();
    }
    private void loadData() {
        cur_calendar.get(Calendar.YEAR);
        cur_calendar.get(Calendar.MONTH);
        cur_calendar.get(Calendar.DAY_OF_MONTH);


        jumlah = Tools.convertToCurrency(modelSavings.getTargetValue(),locale);
        date = modelSavings.getDate_target();

        binding.txtHeader.setText("Update data");
        binding.txtDate.setText(date);
        binding.etTitle.setText(modelSavings.getTitle());
        binding.etAmount.setText(jumlah);
    }
    private void showDatePickerDialog() {
        if(modelPrimaryColor==null) modelPrimaryColor= Tools.modelPrimaryColor;

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




    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.place_date) {
            showDatePickerDialog();
        } else if (id == R.id.img_back) {
            finish();
        } else if (id == R.id.btn_calculator) {
            dialogCalculator = new DialogCalculator(this, getLayoutInflater(), result -> {
                jumlah = result;
                binding.etAmount.setText(Tools.convertToCurrency(result,locale));
            });
            dialogCalculator.show();
        } else if (id == R.id.place_submit) {
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
                    if(result.equalsIgnoreCase("yes")){
                        ModelSavings model = new ModelSavings();
                        model.setId(modelSavings.getId());
                        model.setDate_target(date);
                        model.setId_savings_user(id_user);
                        model.setProcessValue(modelSavings.getProcessValue());
                        model.setTargetValue(Long.parseLong(Tools.convertCurrencyToValue(jumlah)));
                        model.setTitle(binding.etTitle.getText().toString().trim());
                        model.setType_currency(modelSavings.getType_currency());
                        viewModelSaving.inputUpdateSavings("Update", model);

                        Intent intent = new Intent();
                        intent.putExtra("savings", model);
                        setResult(REQUEST_CODE_UPDATE_SAVINGS_TARGET, intent);
                        finish();
                        Toast.makeText(UpdateSavingsTargetActivity.this, "Catatan target menabung berhasil di ubah !", Toast.LENGTH_SHORT).show();

                    }
                    }
                });
                dialogConfirm.showDialogConfirm("Submit","Apakah anda yakin ingin submit data ?");
            }
        }
    }
}