package id.co.evolution.financefy.repository;

import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.util.List;

import javax.inject.Inject;

import id.co.evolution.financefy.db.SavingsDao;
import id.co.evolution.financefy.db.UserDao;
import id.co.evolution.financefy.model.ModelSavings;
import id.co.evolution.financefy.model.ModelUser;
import id.co.evolution.financefy.model.ModelUserWithSavings;

public class SavingsRepository {
    public SavingsDao savingsDao;

    @Inject
    public SavingsRepository(SavingsDao savingsDao) {
        this.savingsDao = savingsDao;
    }

    public LiveData<List<ModelSavings>> getAllSavings() {
        return savingsDao.getAll();
    }

    public LiveData<ModelSavings> getSavingsById(int id) {
        return savingsDao.findById(id);
    }

    public LiveData<List<ModelSavings>> getSavingsByDate(String date) {
        return savingsDao.findByDate(date);
    }

    public LiveData<List<ModelSavings>> getSavingsByIdUser(int id_user) {
        return savingsDao.findByIdUser(id_user);
    }

    public static class InputUpdateSavings extends AsyncTask<Void, Void, Void> {
        ModelSavings modelSaving;
        String type;
        SavingsDao savingsDao;

        public InputUpdateSavings(ModelSavings modelSaving, String type, SavingsDao savingsDao) {
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
        List<ModelSavings> dataSavings;
        SavingsDao savingsDao;
        ModelSavings modelSavings;


        public RemoveSavings(List<ModelSavings> dataSavings, SavingsDao savingsDao) {
            this.dataSavings = dataSavings;
            this.savingsDao = savingsDao;
        }

        public RemoveSavings(ModelSavings modelSavings, SavingsDao savingsDao) {
            this.modelSavings = modelSavings;
            this.savingsDao = savingsDao;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if (dataSavings != null) {
                savingsDao.deleteSavings(dataSavings);
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
