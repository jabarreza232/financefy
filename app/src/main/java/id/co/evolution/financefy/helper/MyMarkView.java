package id.co.evolution.financefy.helper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.TextView;

import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;
import com.github.mikephil.charting.utils.Utils;

import java.util.Locale;

import id.co.evolution.financefy.R;

public class MyMarkView extends MarkerView {
    private final TextView tvContent;
    private final TextView tvTitle;
    Locale locale;
    TinyDb tinyDb;

    public MyMarkView(Context context, int layoutResource) {
        super(context, layoutResource);
        tinyDb = new TinyDb(context);
        tvContent = findViewById(R.id.tvContent);
        tvTitle = findViewById(R.id.tvTitle);
        locale =tinyDb.getString("currency").equalsIgnoreCase("IDR")? Tools.getLocaleIDN():Tools.getLocaleUS();
    }

    // runs every time the MarkerView is redrawn, can be used to update the
    // content (user-interface)
    @SuppressLint("SetTextI18n")
    @Override
    public void refreshContent(Entry e, Highlight highlight) {

        if (e instanceof CandleEntry) {

            CandleEntry ce = (CandleEntry) e;
            tvTitle.setText((String)ce.getData());
            tvContent.setText(Tools.convertToCurrency(ce.getHigh(),locale));
        } else {
            tvTitle.setText((String)e.getData());
//            tvContent.setText(Utils.formatNumber(e.getY(), 0, true));
            tvContent.setText(Tools.convertToCurrency(e.getY(),locale));
        }

        super.refreshContent(e, highlight);
    }

    @Override
    public MPPointF getOffset() {
        return new MPPointF(-(getWidth() / 2), -getHeight());
    }
}
