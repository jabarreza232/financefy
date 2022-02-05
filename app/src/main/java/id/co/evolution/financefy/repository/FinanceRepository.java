package id.co.evolution.financefy.repository;

import android.content.Context;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import id.co.evolution.financefy.App;
import id.co.evolution.financefy.model.ModelFinance;

public class FinanceRepository {
    MutableLiveData<List<ModelFinance>> mutableLiveDataFinance = new MutableLiveData<>();
    MutableLiveData<List<ModelFinance>> mutableLiveDataFinanceMonth = new MutableLiveData<>();
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
}
