package id.co.evolution.financefy.activity;

import static id.co.evolution.financefy.helper.Tools.getFormattedDateSimple;
import static id.co.evolution.financefy.helper.Tools.getFormattedMonthSimple;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Build;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModel;
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

import id.co.evolution.financefy.App;
import id.co.evolution.financefy.R;
import id.co.evolution.financefy.databinding.ActivityCreateFinanceBinding;
import id.co.evolution.financefy.dialog.DialogCalculator;
import id.co.evolution.financefy.model.ModelFinance;
import id.co.evolution.financefy.repository.FinanceRepository;
import id.co.evolution.financefy.viewmodel.ViewModelFinance;

public class CreateFinance extends AppCompatActivity implements View.OnClickListener {

    String date = "";
    String category = "";
    String type = "";
    String[] kategori = null;
    private String jumlah = "";
    String month = "";
    ActivityCreateFinanceBinding binding;
    Calendar cur_calendar = Calendar.getInstance();
    ViewModelFinance viewModelFinance;
    DialogCalculator dialogCalculator;

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

        cur_calendar.get(Calendar.YEAR);
        cur_calendar.get(Calendar.MONTH);
        cur_calendar.get(Calendar.DAY_OF_MONTH);
        long date_ship_milis = cur_calendar.getTimeInMillis();

        viewModelFinance = new ViewModelProvider(this).get(ViewModelFinance.class);

        binding.txtHeader.setText("Input data");
        binding.txtDate.setText(getFormattedDateSimple(date_ship_milis));
        date = getFormattedDateSimple(date_ship_milis);
        month = getFormattedMonthSimple(date_ship_milis);
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


    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, monthOfYear);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                long date_ship_milis = calendar.getTimeInMillis();
                binding.txtDate.setText(getFormattedDateSimple(date_ship_milis));
                date = getFormattedDateSimple(date_ship_milis);

                Log.e("TAG", "onDateSet: " + date);
                month = getFormattedMonthSimple(date_ship_milis);
            }
        });

        datePickerDialog.setYearRange(cur_calendar.get(Calendar.YEAR), cur_calendar.get(Calendar.YEAR));
        datePickerDialog.setMaxDate(cur_calendar);
        datePickerDialog.setAccentColor(getResources().getColor(R.color.colorPrimary));
        datePickerDialog.show(getFragmentManager(), "PickerDialog");
    }

    private void showDialogCategory() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Pilih Kategori");
        if (type.equalsIgnoreCase("pengeluaran")) {
            kategori = getResources().getStringArray(R.array.category_pengeluaran);
            builder.setItems(kategori, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case 0:
                            dialog.dismiss();
                            category = kategori[0];
                            break;
                        case 1:
                            dialog.dismiss();
                            category = kategori[1];
                            break;
                        case 2:
                            dialog.dismiss();
                            category = kategori[2];
                            break;
                        case 3:
                            dialog.dismiss();
                            category = kategori[3];
                            break;
                        case 4:
                            dialog.dismiss();
                            category = kategori[4];
                            break;
                        case 5:
                            dialog.dismiss();
                            category = kategori[5];
                            break;

                    }
                    if (!category.isEmpty()) {
                        binding.txtKategori.setText(category);
                    }
                }
            });
        } else {
            kategori = getResources().getStringArray(R.array.category_pemasukan);
            builder.setItems(kategori, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case 0:
                            dialog.dismiss();
                            category = kategori[0];
                            break;
                        case 1:
                            dialog.dismiss();
                            category = kategori[1];
                            break;
                        case 2:
                            dialog.dismiss();
                            category = kategori[2];
                            break;
                    }
                    if (!category.isEmpty()) {
                        binding.txtKategori.setText(category);
                    }
                }
            });
        }

        android.app.AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showDialogTipe() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Pilih Tipe");
        final String[] tipe = {"Pengeluaran", "Pemasukan"};
        builder.setItems(tipe, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        type = tipe[0];
                        dialog.dismiss();
                        kategori = getResources().getStringArray(R.array.category_pengeluaran);
                        binding.txtKategori.setText(kategori[0]);
                        category = kategori[0];
                        break;
                    case 1:
                        type = tipe[1];
                        kategori = getResources().getStringArray(R.array.category_pemasukan);
                        binding.txtKategori.setText(kategori[0]);
                        category = kategori[0];
                        dialog.dismiss();
                        break;
                }
                binding.txtType.setText(type);
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

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
                showDialogTipe();
                break;

            case R.id.place_date:
                showDatePickerDialog();
                break;
            case R.id.img_back:
                finish();
                break;

            case R.id.btn_calculator:
                dialogCalculator = new DialogCalculator(this, getLayoutInflater(), result -> {

                });
                dialogCalculator.show();
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
                            .setTitleText("Submit")
                            .setContentText("Apakah anda yakin ingin submit data ?")
                            .setConfirmText("Ya")
                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                                    ModelFinance model = new ModelFinance();
                                    model.setDate(date);
                                    model.setJumlah(jumlah);
                                    model.setTipe(type);
                                    model.setKategori(category);
                                    model.setKeterangan(binding.etDescription.getText().toString().trim());
                                    model.setMonth(month);
                                    viewModelFinance.inputUpdateFinance(CreateFinance.this, "Create", model);
                                    finish();
                                }
                            })
                            .setCancelText("Tidak")
                            .show();
                }
                break;
        }
    }

}
