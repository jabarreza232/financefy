package id.co.evolution.financefy.asynctask;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.util.Calendar;
import java.util.List;

import id.co.evolution.financefy.model.ModelFinance;

public class FilterMinYearAsynctask extends AsyncTask<Void, String, String> {
    @SuppressLint("StaticFieldLeak")
    Context context;
    List<ModelFinance> data;
    Calendar today;

    public FilterMinYearAsynctask(Calendar today, List<ModelFinance> data, Context context) {
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
        String year = null;

        int min;
        for (int i = 0; i < data.size(); i++) {
            Log.e("cek", "doInBackground: " + data.get(i).getMonth() + ":" + today.get(Calendar.YEAR));
            int yearValue = (Integer.parseInt(data.get(i).getMonth().split("-")[1]) );
            if (yearValue < today.get(Calendar.YEAR)) {
                min = (Integer.parseInt(data.get(0).getMonth().split("-")[1]) );
                for (int j = 1; j < data.size(); j++) {
                    int yearValueMin = (Integer.parseInt(data.get(j).getMonth().split("-")[1]));

                    if (yearValueMin < min) {
                        min = (Integer.parseInt(data.get(j).getMonth().split("-")[1]));   // new maximum
                    }
                }
                year = String.valueOf(min);
            }
        }
        if (year == null)
            year = String.valueOf(today.get(Calendar.YEAR));

//        Log.e("cek-lagi", "onPostExecute: "+year );

        return year;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        Log.e("cek-lagi", "onPostExecute: " + s);
    }
}
