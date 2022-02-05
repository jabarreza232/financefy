package id.co.evolution.financefy.activity;

import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.databinding.DataBindingUtil;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
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
import id.co.evolution.financefy.helper.Tools;
import id.co.evolution.financefy.model.ModelFinance;

public class UpdateFinance extends AppCompatActivity implements View.OnClickListener {

    String date = "";
    String category = "";
    String type = "";
    String[] kategori = null;
    private String jumlah = "";
    String month = "";
    ModelFinance modelFinance;
    ActivityCreateFinanceBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_create_finance);
        modelFinance = App.getDatabase(this).financeDao().findById(getIntent().getIntExtra("id", 0));
        binding.placeDate.setOnClickListener(this);
        binding.placeCategory.setOnClickListener(this);
        binding.placeType.setOnClickListener(this);
        binding.imgBack.setOnClickListener(this);
        binding.placeSubmit.setOnClickListener(this);
        loadData();
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
    }

    private void loadData() {
        type = modelFinance.getTipe();
        category = modelFinance.getKategori();
        jumlah = modelFinance.getJumlah();
        date = modelFinance.getDate();
        month = modelFinance.getMonth();

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
        datePickerDialog.setAccentColor(getResources().getColor(R.color.colorPrimary));
        datePickerDialog.show(getFragmentManager(), "PickerDialog");
    }

    private void showDialogCategory() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
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

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showDialogTipe() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
                showDialogCategory();
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
            case R.id.place_submit:
                if (type.isEmpty()) {
                    Toast.makeText(this, "Silahkan Pilih tipe terlebih dahulu", Toast.LENGTH_SHORT).show();
                } else if (binding.etAmount.getText().toString().isEmpty()) {
                    binding.tilAmount.setError("Silahkan input jumlah mata uang anda terlebih dahulu");
                } else if (binding.etDescription.getText().toString().isEmpty()) {
                    binding.tilDescription.setError("Silahkan isi deskripsi terlebih dahulu");
                } else if (!binding.etAmount.getText().toString().isEmpty()) {
                    binding.tilAmount.setError(null);
                } else {
                    binding.tilDescription.setError(null);
                }
                Log.e("month:", month);
                if (!type.isEmpty() && !binding.etAmount.getText().toString().isEmpty() && !binding.etDescription.getText().toString().isEmpty()) {

                    new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                            .setTitleText("Update")
                            .setContentText("Apakah anda yakin ingin update data ?")
                            .setConfirmText("Ya")
                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                                    ModelFinance model = new ModelFinance();
                                    model.setId(modelFinance.getId());
                                    model.setDate(date);
                                    model.setJumlah(jumlah);
                                    model.setTipe(type);
                                    model.setKategori(category);
                                    model.setKeterangan(binding.etDescription.getText().toString().trim());
                                    model.setMonth(month);
                                    App.getDatabase(UpdateFinance.this).financeDao().update(model);
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
