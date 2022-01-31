package id.co.evolution.financefy.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import id.co.evolution.financefy.App;
import id.co.evolution.financefy.R;
import id.co.evolution.financefy.model.ModelFinance;

public class UpdateFinance extends AppCompatActivity implements View.OnClickListener {
    @BindView(R.id.place_date)
    RelativeLayout placeDate;
    @BindView(R.id.place_category)
    RelativeLayout placeCategory;
    @BindView(R.id.place_tipe)
    RelativeLayout placeType;
    @BindView(R.id.txt_date)
    TextView txtDate;
    @BindView(R.id.txt_kategori)
    TextView txtKategori;
    @BindView(R.id.txt_tipe)
    TextView txtTipe;
    @BindView(R.id.til_amount)
    TextInputLayout tilAmount;
    @BindView(R.id.et_amount)
    TextInputEditText etAmount;
    @BindView(R.id.til_description)
    TextInputLayout tilDescription;
    @BindView(R.id.et_description)
    TextInputEditText etDescription;
    @BindView(R.id.img_back)
    ImageView imgBack;
    @BindView(R.id.place_submit)
    CardView placeSubmit;
    String date = "";
    String category = "";
    String type = "";
    String[] kategori = null;
    private String jumlah = "";
    String month = "";
    ModelFinance modelFinance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_finance);
        ButterKnife.bind(this);
        modelFinance = App.getDatabase(this).financeDao().findById(getIntent().getIntExtra("id", 0));
        placeDate.setOnClickListener(this);
        placeCategory.setOnClickListener(this);
        placeType.setOnClickListener(this);
        imgBack.setOnClickListener(this);
        placeSubmit.setOnClickListener(this);
        loadData();
        etAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().equals(jumlah)) {
                    etAmount.removeTextChangedListener(this);
                    String cleanString = s.toString().replaceAll("[Rp,.]", "");
                    if (!cleanString.isEmpty()) {
                        double parsed = Double.parseDouble(cleanString);
                        Locale localeID = new Locale("in", "ID");
                        String formatted = NumberFormat.getCurrencyInstance(localeID).format((parsed));
                        jumlah = formatted;
                        etAmount.setText(formatted);
                        etAmount.setSelection(formatted.length());
                    }

                    etAmount.addTextChangedListener(this);
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
        txtDate.setText(date);
        txtTipe.setText(type);
        txtKategori.setText(category);
        etAmount.setText(jumlah);
        etDescription.setText(modelFinance.getKeterangan());
    }

    public static String getFormattedDateSimple(Long dateTime) {
        SimpleDateFormat newFormat = new SimpleDateFormat("MMMM dd, yyyy");
        return newFormat.format(new Date(dateTime));
    }

    public static String getFormattedMonthSimple(Long dateTime) {
        SimpleDateFormat newFormat = new SimpleDateFormat("MM-yyyy");
        return newFormat.format(new Date(dateTime));
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
                txtDate.setText(getFormattedDateSimple(date_ship_milis));
                date = getFormattedDateSimple(date_ship_milis);
                month = getFormattedMonthSimple(date_ship_milis);
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
                        txtKategori.setText(category);
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
                        txtKategori.setText(category);
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
                        txtKategori.setText(kategori[0]);
                        category = kategori[0];
                        break;
                    case 1:
                        type = tipe[1];
                        kategori = getResources().getStringArray(R.array.category_pemasukan);
                        txtKategori.setText(kategori[0]);
                        category = kategori[0];
                        dialog.dismiss();
                        break;
                }
                txtTipe.setText(type);
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
            case R.id.place_tipe:
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
                } else if (etAmount.getText().toString().isEmpty()) {
                    tilAmount.setError("Silahkan input jumlah mata uang anda terlebih dahulu");
                } else if (etDescription.getText().toString().isEmpty()) {
                    tilDescription.setError("Silahkan isi deskripsi terlebih dahulu");
                } else if (!etAmount.getText().toString().isEmpty()) {
                    tilAmount.setError(null);
                } else {
                    tilDescription.setError(null);
                }
                Log.e("month:", month);
                if (!type.isEmpty() && !etAmount.getText().toString().isEmpty() && !etDescription.getText().toString().isEmpty()) {

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
                                    model.setKeterangan(etDescription.getText().toString().trim());
                                    model.setMonth(month);
                                    App.getDatabase(UpdateFinance.this).financeDao().update(model);
                                    App.getDatabase(UpdateFinance.this).financeDao().getAll();
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
