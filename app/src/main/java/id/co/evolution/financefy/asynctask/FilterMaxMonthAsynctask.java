package id.co.evolution.financefy.asynctask;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.whiteelephant.monthpicker.MonthPickerDialog;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.stream.IntStream;

import id.co.evolution.financefy.model.ModelFinance;

public class FilterMaxMonthAsynctask extends AsyncTask<Void, String, String> {
    @SuppressLint("StaticFieldLeak")
    Context context;
    List<ModelFinance> data;
    Calendar today;

    public FilterMaxMonthAsynctask(Calendar today, List<ModelFinance> data, Context context) {
        this.context = context;
        this.today = today;
        this.data = data;
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(Void... voids) {
        String month = null;

        int max;
        for (int i = 0; i < data.size(); i++) {
            Log.e("cek", "doInBackground: " + data.get(i).getMonth() + ":" + today.get(Calendar.MONTH));
            int monthValue = (Integer.parseInt(data.get(i).getMonth().split("-")[0]) - 1);
            if (monthValue > today.get(Calendar.MONTH)) {
                max = (Integer.parseInt(data.get(0).getMonth().split("-")[0]) - 1);
                for (int j = 1; j < data.size(); j++) {
                    int monthValueMax = (Integer.parseInt(data.get(j).getMonth().split("-")[0]));

                    if (monthValueMax > max) {
                        max = (Integer.parseInt(data.get(j).getMonth().split("-")[0]) - 1);   // new maximum
                    }
                }
                month = String.valueOf(max);
            }
        }
        if (month == null)
            month = String.valueOf(today.get(Calendar.MONTH));

//        Log.e("cek-lagi", "onPostExecute: "+month );

        return month;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        Log.e("cek-lagi", "onPostExecute: " + s);
    }
}
