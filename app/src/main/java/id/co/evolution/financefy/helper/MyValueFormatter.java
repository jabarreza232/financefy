package id.co.evolution.financefy.helper;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.text.DecimalFormat;

public class MyValueFormatter extends ValueFormatter {
    private DecimalFormat mFormat;

    public MyValueFormatter() {
        mFormat = new DecimalFormat("###,###,##0.00");
    }

    @Override
    public String getFormattedValue(float value) {
        if(value > 0) {
            return mFormat.format(value);
        } else {
            return "";
        }
    }

}
