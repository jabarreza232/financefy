package id.co.evolution.financefy.repository;

import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.util.List;

import javax.inject.Inject;

import id.co.evolution.financefy.db.SavingsProgressDao;
import id.co.evolution.financefy.model.ModelSavingsProgress;

public class SavingsProgressRepository {

    public SavingsProgressDao savingsDao;

    @Inject
    public SavingsProgressRepository(SavingsProgressDao savingsDao) {
        this.savingsDao = savingsDao;
    }

    public LiveData<List<ModelSavingsProgress>> getAllSavings() {
        return savingsDao.getAll();
    }

    public LiveData<ModelSavingsProgress> getSavingsById(int id) {
        return savingsDao.findById(id);
    }
    public LiveData<Integer> getTotalProcessValueByIdSavings(int id) {
        return savingsDao.findTotalProcessValueByIdSavings(id);
    }

    public LiveData<List<ModelSavingsProgress>> getSavingsByDate(String date) {
        return savingsDao.findByDate(date);
    }

    public LiveData<List<ModelSavingsProgress>> getSavingsByWeek(List<String> date, int id_savings) {
        return savingsDao.loadAllByWeek(date, id_savings);
    }

    public LiveData<List<ModelSavingsProgress>> getSavingsByMonth(String month, int id_savings) {
        return savingsDao.loadAllByMonth(month, id_savings);
    }
    public LiveData<List<ModelSavingsProgress>> getSavingsByIdSavings(int id_savings) {
        return savingsDao.findByIdSavings(id_savings);
    }

    public static class InputUpdateSavings extends AsyncTask<Void, Void, Void> {
        ModelSavingsProgress modelSaving;
        String type;
        SavingsProgressDao savingsDao;

        public InputUpdateSavings(ModelSavingsProgress modelSaving, String type, SavingsProgressDao savingsDao) {
            this.modelSaving = modelSaving;
            this.type = type;
            this.savingsDao = savingsDao;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if (type.equalsIgnoreCase("create"))
                savingsDao.insertAll(modelSaving);
            else
                savingsDao.update(modelSaving);

            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
        }
    }

    public static class RemoveSavings extends AsyncTask<Void, Void, Void> {
        SavingsProgressDao savingsDao;
        ModelSavingsProgress modelSavings;
        int id_savings;

        public RemoveSavings(List<ModelSavingsProgress> dataSavings, SavingsProgressDao savingsDao) {
            this.savingsDao = savingsDao;
        }
        public RemoveSavings(SavingsProgressDao savingsDao, int id_savings) {
            this.savingsDao = savingsDao;
            this.id_savings= id_savings;
        }

        public RemoveSavings(ModelSavingsProgress modelSavings, SavingsProgressDao savingsDao) {
            this.modelSavings = modelSavings;
            this.savingsDao = savingsDao;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if (id_savings>0) {
                savingsDao.deleteSavingsByIdUser(id_savings);
            } else {
                savingsDao.delete(modelSavings);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
        }
    }
}
