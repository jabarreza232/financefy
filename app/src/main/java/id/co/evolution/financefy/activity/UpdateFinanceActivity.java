package id.co.evolution.financefy.activity;

import static id.co.evolution.financefy.callback.CallbackOnActivityResult.REQUEST_CODE_FINANCE;
import static id.co.evolution.financefy.callback.CallbackOnActivityResult.REQUEST_CODE_SAVINGS;
import static id.co.evolution.financefy.callback.CallbackOnActivityResult.REQUEST_CODE_UPDATE_SAVINGS_TARGET;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Locale;


import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import id.co.evolution.financefy.App;
import id.co.evolution.financefy.R;
import id.co.evolution.financefy.databinding.ActivityCreateFinanceBinding;
import id.co.evolution.financefy.db.FinanceDB;
import id.co.evolution.financefy.db.FinanceDao;
import id.co.evolution.financefy.dialog.DialogCalculator;
import id.co.evolution.financefy.dialog.DialogConfirm;
import id.co.evolution.financefy.dialog.DialogFinance;
import id.co.evolution.financefy.helper.TinyDb;
import id.co.evolution.financefy.helper.Tools;
import id.co.evolution.financefy.model.ModelFinance;
import id.co.evolution.financefy.model.ModelPrimaryColor;
import id.co.evolution.financefy.model.ModelSavings;
import id.co.evolution.financefy.model.ModelUser;
import id.co.evolution.financefy.repository.FinanceRepository;
import id.co.evolution.financefy.viewmodel.ViewModelFactory;
import id.co.evolution.financefy.viewmodel.ViewModelFinance;

@AndroidEntryPoint
public class UpdateFinanceActivity extends BaseFinanceActivity implements View.OnClickListener {


    ModelFinance modelFinance;
    ModelUser modelUser;
    int position,id_user;
    @Inject
    TinyDb tinyDb;
    public ModelPrimaryColor modelPrimaryColor=Tools.modelPrimaryColor;

    @Inject
    FinanceRepository financeRepository;
    Locale locale;

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

        viewModelFinance = new ViewModelProvider(this).get(ViewModelFinance.class);
        viewModelFinance.init(financeRepository);
        position = getIntent().getIntExtra("position", 0);
        modelUser = (ModelUser) getIntent().getSerializableExtra("user");
        locale =modelUser.getType_currency().equalsIgnoreCase("IDR")? Tools.getLocaleIDN():Tools.getLocaleUS();

        viewModelFinance.getFinanceById(getIntent().getIntExtra("id",0), modelUser.getType_currency()).observe(this, modelFinance -> {
            this.modelFinance = modelFinance;
            loadData();
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

    private void loadData() {
        cur_calendar.get(Calendar.YEAR);
        cur_calendar.get(Calendar.MONTH);
        cur_calendar.get(Calendar.DAY_OF_MONTH);

        type = modelFinance.getTipe();
        category = modelFinance.getKategori();
        jumlah = modelFinance.getJumlahDesc(locale);
        date = modelFinance.getDate();
        month = modelFinance.getMonth();

        binding.txtHeader.setText("Update data");
        binding.txtDate.setText(date);
        binding.txtType.setText(type);
        binding.txtKategori.setText(category);
        binding.etAmount.setText(jumlah);
        binding.etDescription.setText(modelFinance.getKeterangan());
    }



    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.place_category:
                showDialogCategory();
                break;
            case R.id.place_type:
                showDialogType();
                break;
            case R.id.btn_calculator:
                dialogCalculator = new DialogCalculator(this, getLayoutInflater(), Tools.convertCurrencyToValue(jumlah), result -> {
                    jumlah = Tools.convertToCurrency(result,locale);
                    binding.etAmount.setText(jumlah);
                });
                dialogCalculator.show();
                break;
            case R.id.place_date:
                showDatePickerDialog();
                break;
            case R.id.img_back:
                finish();
                break;
            case R.id.place_submit:
                if (type.isEmpty()) {
                    Toast.makeText(this, "Silahkan Pilih tipe terlebih dahulu", Toast.LENGTH_SHORT).show();
                } else if (binding.etAmount.getText().toString().isEmpty()) {
                    binding.tilAmount.setError("Silahkan input jumlah mata uang anda terlebih dahulu");
                } else if (!binding.etAmount.getText().toString().isEmpty()) {
                    binding.tilAmount.setError(null);
                }

                if (!type.isEmpty() && !binding.etAmount.getText().toString().isEmpty()) {
                    DialogConfirm dialogConfirm = new DialogConfirm(this, getLayoutInflater(), new DialogConfirm.DialogConfirm() {
                        @Override
                        public void onSubmit(@NonNull String result) {
                            if(result.equalsIgnoreCase("yes")){
                                ModelFinance model = new ModelFinance();
                                model.setId(modelFinance.getId());
                                model.setDate(date);
                                model.setJumlah(Tools.replaceCurrencyStringToDouble(jumlah));
                                model.setTipe(type);
                                model.setType_currency(modelUser.getType_currency());
                                model.setKategori(category);
                                model.setKeterangan(binding.etDescription.getText().toString().trim());
                                model.setMonth(month);
                                model.setId_finance_user(modelUser.getId());
                                viewModelFinance.inputUpdateFinance("Update", model);
                                Intent intent = new Intent();
                                intent.putExtra("finance", model);
                                intent.putExtra("position", position);
                                setResult(REQUEST_CODE_FINANCE, intent);
                                finish();
                                Toast.makeText(UpdateFinanceActivity.this, "Catatan "+type+" berhasil di ubah !", Toast.LENGTH_SHORT).show();

                            }
                        }
                    });
                    dialogConfirm.showDialogConfirm("Update","Apakah anda yakin ingin update data ?");

                }
                break;
        }
    }

}
