package id.co.evolution.financefy.viewmodel;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import id.co.evolution.financefy.App;
import id.co.evolution.financefy.model.ModelFinance;
import id.co.evolution.financefy.repository.FinanceRepository;

public class ViewModelFinance extends ViewModel {
    FinanceRepository financeRepository;

    public ViewModelFinance() {
        financeRepository = new FinanceRepository();
    }

    public LiveData<List<ModelFinance>> getAllFinance(Context context) {
        return financeRepository.getAllFinance(context);
    }

    public LiveData<List<ModelFinance>> getFinanceByMonth(Context context, String month) {
        return financeRepository.getFinanceByMonth(context, month);
    }

    public LiveData<List<ModelFinance>> getFinanceByType(Context context, String type) {
        return financeRepository.getFinanceByType(context, type);
    }

    public LiveData<List<ModelFinance>> getFinanceByTypeAndMonth(Context context, String type,String month) {
        return financeRepository.getFinanceByTypeAndMonth(context, type, month);
    }

    public void inputUpdateFinance(Context context, String type, ModelFinance modelFinance){
       new FinanceRepository.InputUpdateFinance(modelFinance, type, context).execute();
    }
}
