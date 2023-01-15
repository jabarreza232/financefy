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

    public LiveData<ModelSavingsProgress> findSavingsById(int id) {
        return savingsRepository.getSavingsById(id);
    }

    public LiveData<Integer> findTotalProcessValueByIdSavings(int id) {
        return savingsRepository.getTotalProcessValueByIdSavings(id);
    }

    public LiveData<List<ModelSavingsProgress>> getSavingsByMonth(String month, int id_savings) {
        return savingsRepository.getSavingsByMonth(month, id_savings);
    }

    public LiveData<List<ModelSavingsProgress>> getSavingsByWeek(List<String> date, int id_savings) {
        return savingsRepository.getSavingsByWeek(date, id_savings);
    }

    public LiveData<List<ModelSavingsProgress>> findAllSavingsByDate(String date) {
        return savingsRepository.getSavingsByDate(date);
    }
    public LiveData<List<ModelSavingsProgress>> findAllSavingsByIdSavings(int id_savings) {
        return savingsRepository.getSavingsByIdSavings(id_savings);
    }
}
