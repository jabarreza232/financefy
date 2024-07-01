package id.co.evolution.financefy.activity;

import static id.co.evolution.financefy.helper.Tools.getFormattedDateSimple;
import static id.co.evolution.financefy.helper.Tools.getFormattedMonthSimple;

import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.util.Calendar;

import javax.inject.Inject;

import id.co.evolution.financefy.R;
import id.co.evolution.financefy.databinding.ActivityCreateFinanceBinding;
import id.co.evolution.financefy.dialog.DialogCalculator;
import id.co.evolution.financefy.dialog.DialogFinance;
import id.co.evolution.financefy.helper.TinyDb;
import id.co.evolution.financefy.helper.Tools;
import id.co.evolution.financefy.model.ModelPrimaryColor;
import id.co.evolution.financefy.viewmodel.ViewModelFinance;

public class BaseFinanceActivity extends AppCompatActivity {
    public String date = "";
    public String category = "";
    public  String type = "";
    public String[] arrayCategory = null;
    public String jumlah = "";
    public String month = "";
    public ActivityCreateFinanceBinding binding;
    public Calendar cur_calendar = Calendar.getInstance();
    public ViewModelFinance viewModelFinance;
    public DialogCalculator dialogCalculator;
    @Inject
    TinyDb tinyDb;
    public ModelPrimaryColor modelPrimaryColor= Tools.modelPrimaryColor;


    public void showDatePickerDialog() {
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

        datePickerDialog.setYearRange(cur_calendar.get(Calendar.YEAR), cur_calendar.get(Calendar.YEAR));
        datePickerDialog.setMaxDate(cur_calendar);
        datePickerDialog.setAccentColor(getResources().getColor(modelPrimaryColor.getColorPrimary()));
        datePickerDialog.show(getSupportFragmentManager(), "PickerDialog");
    }

    public void showDialogCategory() {
        int arrayCategoryFromResource = type.equalsIgnoreCase(getString(R.string.pengeluaran)) ? R.array.category_pengeluaran : R.array.category_pemasukan;
        DialogFinance dialogFinance = new DialogFinance(this, (index, result) -> {
            category = result;
            if (!category.isEmpty()) {
                binding.txtKategori.setText(category);
            }
        });
        dialogFinance.showDialog(arrayCategoryFromResource,"Pilih Kategori");
    }

    public void showDialogType() {
        DialogFinance dialogFinance = new DialogFinance(this, (index,result) -> {
            type = result;
            int arrayCategoryFromResource = result.equalsIgnoreCase(getString(R.string.pengeluaran)) ? R.array.category_pengeluaran : R.array.category_pemasukan;
            arrayCategory = getResources().getStringArray(arrayCategoryFromResource);
            //TODO Default value di index pertama
            binding.txtKategori.setText(arrayCategory[0]);
            category = arrayCategory[0];
            binding.txtType.setText(type);
        });
        dialogFinance.showDialog(R.array.type_finance,"Pilih Tipe");
    }
}
