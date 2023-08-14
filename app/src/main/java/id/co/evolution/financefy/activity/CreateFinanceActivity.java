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
import id.co.evolution.financefy.databinding.ActivityCreateFinanceBinding;
import id.co.evolution.financefy.dialog.DialogCalculator;
import id.co.evolution.financefy.dialog.DialogFinance;
import id.co.evolution.financefy.helper.TinyDb;
import id.co.evolution.financefy.helper.Tools;
import id.co.evolution.financefy.model.ModelFinance;
import id.co.evolution.financefy.model.ModelPrimaryColor;
import id.co.evolution.financefy.model.ModelUser;
import id.co.evolution.financefy.repository.FinanceRepository;
import id.co.evolution.financefy.viewmodel.ViewModelFinance;

@AndroidEntryPoint
public class CreateFinanceActivity extends BaseFinanceActivity implements View.OnClickListener {


    //    @Inject
//    ViewModelFactory viewModelFactory;
    public  List<ModelFinance> listFinance;
    @Inject
      FinanceRepository financeRepository;
    @Inject
    TinyDb tinyDb;
    public ModelPrimaryColor modelPrimaryColor=Tools.modelPrimaryColor;
    Locale locale;
    ModelUser modelUser;

    @SuppressLint("ObsoleteSdkInt")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_create_finance);

        modelPrimaryColor= tinyDb.getObject("model_primary_color", ModelPrimaryColor.class);

        Tools.setBackgroundColorView(binding.rlBackground,modelPrimaryColor);
        Tools.setBackgroundTintView(binding.btnCalculator,modelPrimaryColor);

        //TODO HIDE STATUS BAR
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        cur_calendar.get(Calendar.YEAR);
        cur_calendar.get(Calendar.MONTH);
        cur_calendar.get(Calendar.DAY_OF_MONTH);
        long date_ship_milis = cur_calendar.getTimeInMillis();
        viewModelFinance = new ViewModelProvider(this).get(ViewModelFinance.class);
        viewModelFinance.init(financeRepository);

        binding.txtHeader.setText("Input data");
        binding.txtDate.setText(getFormattedDateSimple(date_ship_milis));
        date = getFormattedDateSimple(date_ship_milis);
        month = getFormattedMonthSimple(date_ship_milis);
        modelUser = (ModelUser) getIntent().getSerializableExtra("user");
        locale =modelUser.getType_currency().equalsIgnoreCase("IDR")? Tools.getLocaleIDN():Tools.getLocaleUS();

        viewModelFinance.getFinanceByUserId(modelUser.getId(),modelUser.getType_currency()).observe(this, modelFinances -> {
            listFinance = modelFinances;
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

                        jumlah = Tools.convertToCurrency(parsed,locale);
                        binding.etAmount.setText(Tools.convertToCurrency(parsed,locale));
                        binding.etAmount.setSelection(Tools.convertToCurrency(parsed,locale).length());
                    }

                    binding.etAmount.addTextChangedListener(this);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        binding.placeDate.setOnClickListener(this);
        binding.placeCategory.setOnClickListener(this);
        binding.placeType.setOnClickListener(this);
        binding.imgBack.setOnClickListener(this);
        binding.placeSubmit.setOnClickListener(this);
        binding.btnCalculator.setOnClickListener(this);
    }





    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.place_category:
                if (!type.isEmpty()) {
                    showDialogCategory();
                } else {
                    Toast.makeText(this, "Pilih tipe terlebih dahulu", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.place_type:
                showDialogType();
                break;

            case R.id.place_date:
                showDatePickerDialog();
                break;
            case R.id.img_back:
                finish();
                break;

            case R.id.btn_calculator:
                dialogCalculator = new DialogCalculator(this, getLayoutInflater(), result -> {
                    jumlah = Tools.convertToCurrency(result,locale);
                    binding.etAmount.setText(jumlah);
                });
                dialogCalculator.show();
                break;
            case R.id.place_submit:
                if (type.isEmpty()) {
                    Toast.makeText(this, "Silahkan Pilih tipe terlebih dahulu", Toast.LENGTH_SHORT).show();
                }

                if (binding.etAmount.getText().toString().isEmpty()) {
                    binding.tilAmount.setError("Silahkan input jumlah mata uang anda terlebih dahulu");
                } else {
                    binding.tilAmount.setError(null);
                }

                if (!type.isEmpty() && !binding.etAmount.getText().toString().isEmpty()) {

                    new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                            .setTitleText("Submit")
                            .setContentText("Apakah anda yakin ingin submit data ?")
                            .setConfirmText("Ya")
                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                                    ModelFinance model = new ModelFinance();
                                    model.setDate(date);
                                    model.setJumlah(Tools.replaceCurrencyStringToDouble(jumlah));
                                    model.setTipe(type);
                                    model.setKategori(category);
                                    model.setKeterangan(binding.etDescription.getText().toString().trim());
                                    model.setMonth(month);
                                    model.setId_finance_user(modelUser.getId());
                                    model.setType_currency(modelUser.getType_currency());

                                    onSubmit(model);
                                    Intent intent = new Intent();
                                    intent.putExtra("finance", model);
                                    setResult(RESULT_OK, intent);
                                    finish();
                                }
                            })
                            .setCancelText("Tidak")
                            .show();
                }
                break;
        }
    }

    private void onSubmit(ModelFinance model) {
        boolean isUpdate = false;
        //TODO ketika submit terdapat data yang identik sama maka tidak dapat duplikasi.
        // melainkan hanya bisa melakukan penjumlahan value nya saja


        for (ModelFinance modelFinance : listFinance) {
            if (modelFinance.getKategori().contains(model.getKategori()) && modelFinance.getDate().contains(model.getDate())) {
                double jumlahValue = modelFinance.getJumlahValue() + model.getJumlahValue();
                model.setId(modelFinance.getId());
                model.setJumlah(jumlahValue);
                isUpdate = true;
            }
        }

        if (isUpdate)
            viewModelFinance.inputUpdateFinance("Update", model);
        else
            viewModelFinance.inputUpdateFinance("Create", model);

    }
}
