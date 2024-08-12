package id.co.evolution.financefy.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import id.co.evolution.financefy.model.ModelFinance;
import id.co.evolution.financefy.model.ModelSavings;
import id.co.evolution.financefy.model.ModelSavingsProgress;
import id.co.evolution.financefy.repository.SavingsProgressRepository;

public class ViewModelSavingsProgress extends ViewModel {
    public SavingsProgressRepository savingsRepository;

    public void init(SavingsProgressRepository savingsRepository) {
        this.savingsRepository = savingsRepository;
    }

    public void inputUpdateSavings(String type, ModelSavingsProgress modelSavings) {
        new SavingsProgressRepository.InputUpdateSavings(modelSavings, type, savingsRepository.savingsDao).execute();
    }
    public void removeSavings(ModelSavingsProgress modelSavings) {
        new SavingsProgressRepository.RemoveSavings(modelSavings, savingsRepository.savingsDao).execute();
    }

    public LiveData<List<ModelSavingsProgress>> getAllSavings() {
        return savingsRepository.getAllSavings();
    }

    public LiveData<ModelSavingsProgress> findSavingsById(int id,String type_currency) {
        return savingsRepository.getSavingsById(id,type_currency);
    }

    public LiveData<Integer> findTotalProcessValueByIdSavings(int id,String type_currency) {
        return savingsRepository.getTotalProcessValueByIdSavings(id,type_currency);
    }

    public LiveData<List<ModelSavingsProgress>> getSavingsByMonth(String month, int id_savings,String type_currency) {
        return savingsRepository.getSavingsByMonth(month, id_savings,type_currency);
    }

    public LiveData<List<ModelSavingsProgress>> getSavingsByWeek(List<String> date, int id_savings,String type_currency) {
        return savingsRepository.getSavingsByWeek(date, id_savings,type_currency);
    }

    public LiveData<List<ModelSavingsProgress>> findAllSavingsByDate(String date,int id_savings,String type_currency) {
        return savingsRepository.getSavingsByDate(date,id_savings,type_currency);
    }
    public LiveData<List<ModelSavingsProgress>> findAllSavingsByIdSavings(int id_savings,String type_currency) {
        return savingsRepository.getSavingsByIdSavings(id_savings,type_currency);
    }
}
