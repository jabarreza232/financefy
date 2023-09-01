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

    public LiveData<ModelSavings> getSavingsById(int id, String type_currency) {
        return savingsDao.findById(id, type_currency);
    }

    public LiveData<List<ModelSavings>> getSavingsByDate(String date, String type_currency) {
        return savingsDao.findByDate(date, type_currency);
    }

    public LiveData<List<ModelSavings>> getSavingsByIdUser(int id_user, String type_currency) {
        return savingsDao.findByIdUser(id_user, type_currency);
    }

    public static class InputUpdateSavings extends AsyncTask<Void, Void, Void> {
        ModelSavings modelSaving;
        String type;
        SavingsDao savingsDao;
        MethodCallback methodCallback;

        public InputUpdateSavings(ModelSavings modelSaving, String type, SavingsDao savingsDao) {
            this.modelSaving = modelSaving;
            this.type = type;
            this.savingsDao = savingsDao;
        }

        public InputUpdateSavings(ModelSavings modelSaving, String type, SavingsDao savingsDao, MethodCallback methodCallback) {
            this.modelSaving = modelSaving;
            this.type = type;
            this.savingsDao = savingsDao;
            this.methodCallback = methodCallback;
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

            if(methodCallback!=null)
            methodCallback.onPostExecute();
        }

        public interface MethodCallback {
            void onPostExecute();
        }
    }

    public static class RemoveSavings extends AsyncTask<Void, Void, Void> {
        SavingsDao savingsDao;
        ModelSavings modelSavings;
        int id_user;

        public RemoveSavings(List<ModelSavings> dataSavings, SavingsDao savingsDao) {
            this.savingsDao = savingsDao;
        }

        public RemoveSavings(SavingsDao savingsDao, int id_user) {
            this.savingsDao = savingsDao;
            this.id_user = id_user;
        }

        public RemoveSavings(ModelSavings modelSavings, SavingsDao savingsDao) {
            this.modelSavings = modelSavings;
            this.savingsDao = savingsDao;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if (id_user > 0) {
                savingsDao.deleteSavingsByIdUser(id_user);
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
