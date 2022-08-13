package id.co.evolution.financefy.viewmodel;

import androidx.hilt.Assisted;
import androidx.hilt.lifecycle.ViewModelInject;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import java.util.List;

import id.co.evolution.financefy.helper.Tools;
import id.co.evolution.financefy.model.ModelFinance;
import id.co.evolution.financefy.model.ModelUserWithFinance;
import id.co.evolution.financefy.repository.FinanceRepository;

public class ViewModelFinance extends ViewModel {
    FinanceRepository financeRepository;


    public void init(FinanceRepository financeRepository) {
        this.financeRepository = financeRepository;
    }

    public LiveData<List<ModelFinance>> getAllFinance() {
        return financeRepository.getAllFinance();
    }


    public LiveData<List<ModelFinance>> getFinanceByMonth(String month, int id_user) {
        return financeRepository.getFinanceByMonth(month, id_user);
    }

    public LiveData<List<ModelFinance>> getFinanceByWeek(List<String> date, int id_user) {
        return financeRepository.getFinanceByWeek(date, id_user);
    }

    public LiveData<List<ModelFinance>> getFinanceByType(String type) {
        return financeRepository.getFinanceByType(type);
    }

    public LiveData<ModelFinance> getFinanceById(int id) {
        return financeRepository.getModelFinanceById(id);
    }

    public LiveData<List<ModelUserWithFinance>> getFinanceByUserId(int id) {
        return financeRepository.getFinanceByUserId(id);
    }

    public LiveData<List<ModelFinance>> getFinanceByTypeAndMonth(String type, String month, int id_user) {
        if (type.equalsIgnoreCase(Tools.TYPE_FILTER.SEMUANYA.toString()))
            return financeRepository.getFinanceByMonth(month, id_user);
        else
            return financeRepository.getFinanceByTypeAndMonth(type, month, id_user);
    }

    public LiveData<List<ModelFinance>> getFinanceByTypeAndWeek(String type, List<String> date, int id_user) {
        if (type.equalsIgnoreCase(Tools.TYPE_FILTER.SEMUANYA.toString()))
            return financeRepository.getFinanceByWeek(date, id_user);
        else
        return financeRepository.getFinanceByTypeAndWeek(type, date, id_user);
    }

    public void inputUpdateFinance(String type, ModelFinance modelFinance) {
        new FinanceRepository.InputUpdateFinance(modelFinance, type, financeRepository.financeDao).execute();
    }

    public void removeFinance(ModelFinance modelFinance) {
        new FinanceRepository.RemoveFinance(modelFinance, financeRepository.financeDao).execute();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
    }
}
