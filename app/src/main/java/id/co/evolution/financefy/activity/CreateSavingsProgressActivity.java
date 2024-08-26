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
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import id.co.evolution.financefy.R;
import id.co.evolution.financefy.databinding.ActivityCreateSavingsProgressBinding;
import id.co.evolution.financefy.dialog.DialogCalculator;
import id.co.evolution.financefy.dialog.DialogConfirm;
import id.co.evolution.financefy.helper.TinyDb;
import id.co.evolution.financefy.helper.Tools;
import id.co.evolution.financefy.model.ModelFinance;
import id.co.evolution.financefy.model.ModelPrimaryColor;
import id.co.evolution.financefy.model.ModelSavings;
import id.co.evolution.financefy.model.ModelSavingsProgress;
import id.co.evolution.financefy.repository.SavingsProgressRepository;
import id.co.evolution.financefy.repository.SavingsRepository;
import id.co.evolution.financefy.viewmodel.ViewModelSavings;
import id.co.evolution.financefy.viewmodel.ViewModelSavingsProgress;

@AndroidEntryPoint
public class CreateSavingsProgressActivity extends AppCompatActivity implements View.OnClickListener {
    String date = "";
    private String jumlah = "";
    String month = "";
    ActivityCreateSavingsProgressBinding binding;
    Calendar cur_calendar = Calendar.getInstance();
    ViewModelSavingsProgress viewModelSavingsProgress;
    ViewModelSavings viewModelSaving;
    DialogCalculator dialogCalculator;
    List<ModelSavingsProgress> listSavings;

    @Inject
    TinyDb tinyDb;
    public ModelPrimaryColor modelPrimaryColor=Tools.modelPrimaryColor;


    @Inject
    SavingsProgressRepository savingsProgressRepository;
    @Inject
    SavingsRepository savingsRepository;
    ModelSavings modelSavings;
    Locale locale;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        modelPrimaryColor= tinyDb.getObject("model_primary_color", ModelPrimaryColor.class);

        Tools.setThemeNoActionBarActivity(getTheme(), modelPrimaryColor);

        binding = DataBindingUtil.setContentView(this,R.layout.activity_create_savings_progress);

        Tools.setBackgroundColorView(binding.rlBackground,modelPrimaryColor);
        Tools.setImageTintView(binding.btnCalculator,modelPrimaryColor);

        //TODO HIDE STATUS BAR


        cur_calendar.get(Calendar.YEAR);
        cur_calendar.get(Calendar.MONTH);
        cur_calendar.get(Calendar.DAY_OF_MONTH);
        long date_ship_milis = cur_calendar.getTimeInMillis();
        viewModelSavingsProgress = new ViewModelProvider(this).get(ViewModelSavingsProgress.class);
        viewModelSaving = new ViewModelProvider(this).get(ViewModelSavings.class);
        viewModelSavingsProgress.init(savingsProgressRepository);
        viewModelSaving.init(savingsRepository);

        binding.txtHeader.setText("Masukkan Data Progress Menabung");
        binding.txtDescription.setText("Silakan masukkan data tabungan Anda pada form yang tersedia.");
        binding.txtDate.setText(getFormattedDateSimple(date_ship_milis));
        date = getFormattedDateSimple(date_ship_milis);
        month = getFormattedMonthSimple(date_ship_milis);
        modelSavings =(ModelSavings) getIntent().getSerializableExtra("savings");

        locale =modelSavings.getType_currency().equalsIgnoreCase("IDR")? Tools.getLocaleIDN():Tools.getLocaleUS();

        viewModelSavingsProgress.findAllSavingsByIdSavings(modelSavings.getId(),modelSavings.getType_currency()).observe(this, modelSavings -> {
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

        datePickerDialog.setYearRange(cur_calendar.get(Calendar.YEAR), cur_calendar.get(Calendar.YEAR));
        datePickerDialog.setMaxDate(cur_calendar);
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
                            if(result.equalsIgnoreCase("yes")){
                                ModelSavingsProgress model = new ModelSavingsProgress();
                                model.setDate_progress_savings(date);
                                model.setMonth(month);
                                model.setProcessValue(Long.parseLong(Tools.convertCurrencyToValue(jumlah)));
                                model.setDescription(binding.etDescription.getText().toString().trim());
                                model.setTitle(binding.etTitle.getText().toString().trim());
                                model.setId_savings(modelSavings.getId());
                                model.setType_currency(modelSavings.getType_currency());
                                CreateSavingsProgressActivity.this.onSubmit(model);
                                Intent intent = new Intent();
                                intent.putExtra("savings", modelSavings);
                                setResult(RESULT_OK, intent);
                                Toast.makeText(CreateSavingsProgressActivity.this, "Catatan progress menabung berhasil di tambahkan !", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        }
                    });
                    dialogConfirm.showDialogConfirm("Submit","Apakah anda yakin ingin submit data ?");

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

        viewModelSavingsProgress.findAllSavingsByIdSavings(modelSavings.getId(),modelSavings.getType_currency()).observe(this, modelSavingsProgresses -> {
            long processValue=0;
            for (ModelSavingsProgress modelSavingsProgress:modelSavingsProgresses)
                processValue+= modelSavingsProgress.getProcessValue();

            modelSavings.setProcessValue(processValue);
            viewModelSaving.inputUpdateSavings("Update",modelSavings);
        });
    }
}