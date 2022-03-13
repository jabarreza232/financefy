package id.co.evolution.financefy.repository;

import android.content.Context;
import android.os.AsyncTask;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import id.co.evolution.financefy.App;
import id.co.evolution.financefy.activity.CreateFinance;
import id.co.evolution.financefy.activity.UpdateFinance;
import id.co.evolution.financefy.model.ModelFinance;

public class FinanceRepository {
    ExecutorService executors;

    public FinanceRepository() {
        executors = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<ModelFinance>> getAllFinance(Context context) {
        return App.getDatabase(context).financeDao().getAll();
    }

    public LiveData<List<ModelFinance>> getFinanceByMonth(Context context, String month) {
        return App.getDatabase(context).financeDao().loadAllbyMonth(month);
    }

    public LiveData<List<ModelFinance>> getFinanceByType(Context context, String type) {
        return App.getDatabase(context).financeDao().findByType(type);
    }

    public LiveData<List<ModelFinance>> getFinanceByTypeAndMonth(Context context, String type, String month) {
        return App.getDatabase(context).financeDao().findByTypeAndMonth(type, month);
    }


    public static class InputUpdateFinance extends AsyncTask<Void, Void, Void> {
        ModelFinance modelFinance;
        Context context;
        String type;

        public InputUpdateFinance(ModelFinance modelFinance, String type, Context context) {
            this.modelFinance = modelFinance;
            this.context = context;
            this.type = type;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if (type.toLowerCase().equals("create"))
                App.getDatabase(context).financeDao().insertAll(modelFinance);
            else
                App.getDatabase(context).financeDao().update(modelFinance);

            return null;
        }
    }
}
