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

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.text.NumberFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import id.co.evolution.financefy.R;
import id.co.evolution.financefy.databinding.ActivityCreateSavingsBinding;
import id.co.evolution.financefy.dialog.DialogCalculator;
import id.co.evolution.financefy.helper.Tools;
import id.co.evolution.financefy.model.ModelSavings;
import id.co.evolution.financefy.model.ModelSavingsProgress;
import id.co.evolution.financefy.repository.SavingsProgressRepository;
import id.co.evolution.financefy.repository.SavingsRepository;
import id.co.evolution.financefy.viewmodel.ViewModelSavings;
import id.co.evolution.financefy.viewmodel.ViewModelSavingsProgress;

@AndroidEntryPoint
public class CreateSavingsActivity extends AppCompatActivity implements View.OnClickListener {
    String date = "";
    private String jumlah = "";
    String month = "";
    ActivityCreateSavingsBinding binding;
    Calendar cur_calendar = Calendar.getInstance();
    ViewModelSavingsProgress viewModelSavingsProgress;
    ViewModelSavings viewModelSaving;
    DialogCalculator dialogCalculator;
    List<ModelSavingsProgress> listSavings;

    @Inject
    SavingsProgressRepository savingsProgressRepository;
    @Inject
    SavingsRepository savingsRepository;
    ModelSavings modelSavings;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_create_savings);
        //TODO HIDE STATUS BAR
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        cur_calendar.get(Calendar.YEAR);
        cur_calendar.get(Calendar.MONTH);
        cur_calendar.get(Calendar.DAY_OF_MONTH);
        long date_ship_milis = cur_calendar.getTimeInMillis();
        viewModelSavingsProgress = new ViewModelProvider(this).get(ViewModelSavingsProgress.class);
        viewModelSaving = new ViewModelProvider(this).get(ViewModelSavings.class);
        viewModelSavingsProgress.init(savingsProgressRepository);
        viewModelSaving.init(savingsRepository);

        binding.txtHeader.setText("Input data");
        binding.txtDate.setText(getFormattedDateSimple(date_ship_milis));
        date = getFormattedDateSimple(date_ship_milis);
        month = getFormattedMonthSimple(date_ship_milis);
        modelSavings =(ModelSavings) getIntent().getSerializableExtra("savings");


        viewModelSavingsProgress.findAllSavingsByIdSavings(modelSavings.getId()).observe(this, modelSavings -> {
            listSavings = modelSavings;
        });

        binding.etAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().equals(jumlah)) {
                    binding.etAmount.removeTextChangedListener(this);
                    String cleanString = s.toString().replaceAll("[Rp,.]", "");
                    if (!cleanString.isEmpty()) {
                        double parsed = Double.parseDouble(cleanString);
                        Locale localeID = new Locale("in", "ID");
                        String formatted = NumberFormat.getCurrencyInstance(localeID).format((parsed));
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            formatted = formatted.replaceAll(",00", "");
                        }
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

        datePickerDialog.setYearRange(cur_calendar.get(Calendar.YEAR), cur_calendar.get(Calendar.YEAR));
        datePickerDialog.setMaxDate(cur_calendar);
        datePickerDialog.setAccentColor(getResources().getColor(R.color.colorPrimary));
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
                    binding.etAmount.setText(Tools.convertToCurrency(result));
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

                    new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                            .setTitleText("Submit")
                            .setContentText("Apakah anda yakin ingin submit data ?")
                            .setConfirmText("Ya")
                            .setConfirmClickListener(sweetAlertDialog -> {
                                ModelSavingsProgress model = new ModelSavingsProgress();
                                model.setDate_progress_savings(date);
                                model.setMonth(month);
                                model.setProcessValue(Long.parseLong(Tools.convertCurrencyToValue(jumlah)));
                                model.setDescription(binding.etDescription.getText().toString().trim());
                                model.setTitle(binding.etTitle.getText().toString().trim());
                                model.setId_savings(modelSavings.getId());
                                onSubmit(model);
                                Intent intent = new Intent();
                                intent.putExtra("savings", modelSavings);
                                setResult(RESULT_OK, intent);
                                finish();
                            })
                            .setCancelText("Tidak")
                            .show();
                }
                break;
        }
    }

    private void onSubmit(ModelSavingsProgress model) {
        boolean isUpdate = false;
        for (ModelSavingsProgress modelSavings : listSavings) {
            if (modelSavings.getTitle().contains(model.getTitle()) && modelSavings.getDate_progress_savings().contains(model.getDate_progress_savings())) {
                model.setId(modelSavings.getId());
                model.setProcessValue(modelSavings.getProcessValue());
                isUpdate = true;
            }
        }

        if(isUpdate)
        viewModelSavingsProgress.inputUpdateSavings("Update", model);
        else viewModelSavingsProgress.inputUpdateSavings("Create", model);

        long processValue =model.getProcessValue()+modelSavings.getProcessValue();
        modelSavings.setProcessValue(processValue);
        viewModelSaving.inputUpdateSavings("Update",modelSavings);
    }
}