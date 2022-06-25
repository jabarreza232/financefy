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

import id.co.evolution.financefy.R;

public class MyMarkView extends MarkerView {
    private final TextView tvContent;
    private final TextView tvTitle;

    public MyMarkView(Context context, int layoutResource) {
        super(context, layoutResource);

        tvContent = findViewById(R.id.tvContent);
        tvTitle = findViewById(R.id.tvTitle);
    }

    // runs every time the MarkerView is redrawn, can be used to update the
    // content (user-interface)
    @SuppressLint("SetTextI18n")
    @Override
    public void refreshContent(Entry e, Highlight highlight) {

        if (e instanceof CandleEntry) {

            CandleEntry ce = (CandleEntry) e;
            tvTitle.setText((String)ce.getData());
            tvContent.setText(Tools.convertToCurrency(ce.getHigh()));
        } else {
            tvTitle.setText((String)e.getData());
//            tvContent.setText(Utils.formatNumber(e.getY(), 0, true));
            tvContent.setText(Tools.convertToCurrency(e.getY()));
        }

        super.refreshContent(e, highlight);
    }

    @Override
    public MPPointF getOffset() {
        return new MPPointF(-(getWidth() / 2), -getHeight());
    }
}
