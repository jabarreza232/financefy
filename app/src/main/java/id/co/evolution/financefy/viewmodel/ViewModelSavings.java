package id.co.evolution.financefy.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import id.co.evolution.financefy.model.ModelFinance;
import id.co.evolution.financefy.model.ModelSavings;
import id.co.evolution.financefy.repository.SavingsRepository;

public class ViewModelSavings extends ViewModel {
    public SavingsRepository savingsRepository;

    public void init(SavingsRepository savingsRepository) {
        this.savingsRepository = savingsRepository;
    }

    public void inputUpdateSavings(String type, ModelSavings modelSavings) {
        new SavingsRepository.InputUpdateSavings(modelSavings, type, savingsRepository.savingsDao).execute();
    }

    public LiveData<List<ModelSavings>> getAllSavings() {
        return savingsRepository.getAllSavings();
    }

    public LiveData<ModelSavings> findSavingsById(int id) {
        return savingsRepository.getSavingsById(id);
    }


    public LiveData<List<ModelSavings>> findAllSavingsByDate(String date) {
        return savingsRepository.getSavingsByDate(date);
    }
    public LiveData<List<ModelSavings>> findAllSavingsByIdUser(int id_user) {
        return savingsRepository.getSavingsByIdUser(id_user);
    }
}
