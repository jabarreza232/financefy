package id.co.evolution.financefy.viewmodel;

import androidx.lifecycle.LiveData;
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


    public LiveData<List<ModelFinance>> getFinanceByMonth(String month, int id_user, String type_currency) {
        return financeRepository.getFinanceByMonth(month, id_user,type_currency);
    } public LiveData<List<ModelFinance>> getFinanceByYear(String year, int id_user, String type_currency) {
        return financeRepository.getFinanceByYear(year, id_user,type_currency);
    }

    public LiveData<List<ModelFinance>> getFinanceByWeek(List<String> date, int id_user, String type_currency) {
        return financeRepository.getFinanceByWeek(date, id_user,type_currency);
    }
    public LiveData<List<ModelFinance>> getAllFinanceByDate(String date, int id_user, String type_currency) {
        return financeRepository.getAllFinanceByDate(date, id_user,type_currency);
    }

    public LiveData<List<ModelFinance>> getFinanceByType(String type, String type_currency) {
        return financeRepository.getFinanceByType(type, type_currency);
    }

    public LiveData<ModelFinance> getFinanceById(int id, String type_currency) {
        return financeRepository.getModelFinanceById(id, type_currency);
    }

    public LiveData<List<ModelFinance>> getFinanceByUserId(int id, String type_currency) {
        return financeRepository.getFinanceByUserId(id, type_currency);
    }

    public LiveData<List<ModelFinance>> getFinanceByTypeAndMonth(String type, String month, int id_user, String type_currency) {
        if (type.equalsIgnoreCase(Tools.TYPE_FILTER.SEMUANYA.toString()))
            return financeRepository.getFinanceByMonth(month, id_user, type_currency);
        else
            return financeRepository.getFinanceByTypeAndMonth(type, month, id_user, type_currency);
    }

    public LiveData<List<ModelFinance>> getFinanceByTypeAndWeek(String type, List<String> date, int id_user, String type_currency) {
        if (type.equalsIgnoreCase(Tools.TYPE_FILTER.SEMUANYA.toString()))
            return financeRepository.getFinanceByWeek(date, id_user, type_currency);
        else
        return financeRepository.getFinanceByTypeAndWeek(type, date, id_user, type_currency);
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
