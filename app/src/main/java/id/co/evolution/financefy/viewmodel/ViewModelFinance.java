package id.co.evolution.financefy.viewmodel;

import androidx.hilt.Assisted;
import androidx.hilt.lifecycle.ViewModelInject;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import java.util.List;

import id.co.evolution.financefy.model.ModelFinance;
import id.co.evolution.financefy.repository.FinanceRepository;

public class ViewModelFinance extends ViewModel {
    FinanceRepository financeRepository;


    public void init(FinanceRepository financeRepository) {
        this.financeRepository = financeRepository;
    }

    public LiveData<List<ModelFinance>> getAllFinance() {
        return financeRepository.getAllFinance();
    }


    public LiveData<List<ModelFinance>> getFinanceByMonth(String month) {
        return financeRepository.getFinanceByMonth(month);
    }

    public LiveData<List<ModelFinance>> getFinanceByWeek(List<String> date) {
        return financeRepository.getFinanceByWeek(date);
    }

    public LiveData<List<ModelFinance>> getFinanceByType(String type) {
        return financeRepository.getFinanceByType(type);
    }

    public LiveData<ModelFinance> getFinanceById(int id) {
        return financeRepository.getModelFinanceById(id);
    }

    public LiveData<List<ModelFinance>> getFinanceByTypeAndMonth(String type, String month) {
        return financeRepository.getFinanceByTypeAndMonth(type, month);
    }

    public LiveData<List<ModelFinance>> getFinanceByTypeAndWeek(String type, List<String> date) {
        return financeRepository.getFinanceByTypeAndWeek(type, date);
    }

    public void inputUpdateFinance(String type, ModelFinance modelFinance) {
        new FinanceRepository.InputUpdateFinance(modelFinance, type,financeRepository.financeDao).execute();
    }

    public void removeFinance(ModelFinance modelFinance) {
        new FinanceRepository.RemoveFinance(modelFinance,financeRepository.financeDao).execute();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
    }
}
