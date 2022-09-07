package id.co.evolution.financefy.activity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

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
import id.co.evolution.financefy.dialog.DialogFinance;
import id.co.evolution.financefy.helper.Tools;
import id.co.evolution.financefy.model.ModelFinance;
import id.co.evolution.financefy.repository.FinanceRepository;
import id.co.evolution.financefy.viewmodel.ViewModelFactory;
import id.co.evolution.financefy.viewmodel.ViewModelFinance;

@AndroidEntryPoint
public class UpdateFinanceActivity extends AppCompatActivity implements View.OnClickListener {

    String date = "";
    String category = "";
    String type = "";
    String[] arrayCategory = null;
    private String jumlah = "";
    String month = "";
    ModelFinance modelFinance;
    ActivityCreateFinanceBinding binding;
    ViewModelFinance viewModelFinance;
    Calendar cur_calendar = Calendar.getInstance();
    DialogCalculator dialogCalculator;
    //    @Inject
//    ViewModelFactory viewModelFactory;
    int position,id_user;

    @Inject
    FinanceRepository financeRepository;

    @SuppressLint("ObsoleteSdkInt")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_create_finance);
        //TODO HIDE STATUS BAR
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        viewModelFinance = new ViewModelProvider(this).get(ViewModelFinance.class);
        viewModelFinance.init(financeRepository);
        position = getIntent().getIntExtra("position", 0);
        id_user = getIntent().getIntExtra("id_user", 0);
        viewModelFinance.getFinanceById(getIntent().getIntExtra("id", 0)).observe(this, modelFinance -> {
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
        jumlah = modelFinance.getJumlah();
        date = modelFinance.getDate();
        month = modelFinance.getMonth();

        binding.txtHeader.setText("Update data");
        binding.txtDate.setText(date);
        binding.txtType.setText(type);
        binding.txtKategori.setText(category);
        binding.etAmount.setText(jumlah);
        binding.etDescription.setText(modelFinance.getKeterangan());
    }


    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, monthOfYear);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                long date_ship_milis = calendar.getTimeInMillis();
                binding.txtDate.setText(Tools.getFormattedDateSimple(date_ship_milis));
                date = Tools.getFormattedDateSimple((date_ship_milis));
                month = Tools.getFormattedMonthSimple((date_ship_milis));
            }
        });

        datePickerDialog.setYearRange(cur_calendar.get(Calendar.YEAR), cur_calendar.get(Calendar.YEAR));
        datePickerDialog.setAccentColor(getResources().getColor(R.color.colorPrimary));
        datePickerDialog.setMaxDate(cur_calendar);
        datePickerDialog.show(getSupportFragmentManager(), "PickerDialog");
    }

    private void showDialogCategory() {
        int arrayCategoryFromResource = type.equalsIgnoreCase(getString(R.string.pengeluaran)) ? R.array.category_pengeluaran : R.array.category_pemasukan;
        DialogFinance dialogFinance = new DialogFinance(this, (index,result) -> {
            category = result;
            if (!category.isEmpty()) {
                binding.txtKategori.setText(category);
            }
        });
        dialogFinance.showDialogCategory(arrayCategoryFromResource);
    }

    private void showDialogType() {
        DialogFinance dialogFinance = new DialogFinance(this, (index,result) -> {
            type = result;
            int arrayCategoryFromResource = result.equalsIgnoreCase(getString(R.string.pengeluaran)) ? R.array.category_pengeluaran : R.array.category_pemasukan;
            arrayCategory = getResources().getStringArray(arrayCategoryFromResource);
            //TODO Default value di index pertama
            binding.txtKategori.setText(arrayCategory[0]);
            category = arrayCategory[0];
            binding.txtType.setText(type);
        });
        dialogFinance.showDialogType(R.array.type_finance);
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
                    jumlah = Tools.convertToCurrency(result);
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
                    new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                            .setTitleText("Update")
                            .setContentText("Apakah anda yakin ingin update data ?")
                            .setConfirmText("Ya")
                            .setConfirmClickListener(sweetAlertDialog -> {
                                ModelFinance model = new ModelFinance();
                                model.setId(modelFinance.getId());
                                model.setDate(date);
                                model.setJumlah(jumlah);
                                model.setTipe(type);
                                model.setKategori(category);
                                model.setKeterangan(binding.etDescription.getText().toString().trim());
                                model.setMonth(month);
                                viewModelFinance.inputUpdateFinance("Update", model);
                                Intent intent = new Intent();
                                intent.putExtra("finance", model);
                                intent.putExtra("position", position);
                                setResult(RESULT_OK, intent);
                                finish();
                            })
                            .setCancelText("Tidak")
                            .show();
                }
                break;
        }
    }

}
