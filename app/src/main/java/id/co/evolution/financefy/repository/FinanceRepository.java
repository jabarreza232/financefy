package id.co.evolution.financefy.repository;

import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.util.List;

import javax.inject.Inject;

import id.co.evolution.financefy.db.FinanceDao;
import id.co.evolution.financefy.model.ModelFinance;

public class FinanceRepository {
   public FinanceDao financeDao;

    @Inject
    public FinanceRepository(FinanceDao financeDao) {
        this.financeDao = financeDao;
    }

    public LiveData<List<ModelFinance>> getAllFinance() {
        return financeDao.getAll();
    }

    public LiveData<List<ModelFinance>> getFinanceByMonth(String month) {
        return financeDao.loadAllbyMonth(month);
    }

    public LiveData<List<ModelFinance>> getFinanceByWeek(List<String> date) {
        return financeDao.loadAllbyWeek(date);
    }

    public LiveData<List<ModelFinance>> getFinanceByType(String type) {
        return financeDao.findByType(type);
    }

    public LiveData<ModelFinance> getModelFinanceById(int id) {
        return financeDao.findById(id);
    }

    public LiveData<List<ModelFinance>> getFinanceByTypeAndMonth(String type, String month) {
        return financeDao.findByTypeAndMonth(type, month);
    }

    public LiveData<List<ModelFinance>> getFinanceByTypeAndWeek(String type, List<String> date) {
        return financeDao.findByTypeAndWeek(type, date);
    }

    public static class InputUpdateFinance extends AsyncTask<Void, Void, Void> {
        ModelFinance modelFinance;
        String type;
        FinanceDao financeDao;

        public InputUpdateFinance(ModelFinance modelFinance, String type, FinanceDao financeDao) {
            this.modelFinance = modelFinance;
            this.type = type;
            this.financeDao = financeDao;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if (type.toLowerCase().equals("create"))
                financeDao.insertAll(modelFinance);
            else
                financeDao.update(modelFinance);

            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
        }
    }

    public static class RemoveFinance extends AsyncTask<Void, Void, Void> {
        ModelFinance modelFinance;
        FinanceDao financeDao;

        public RemoveFinance(ModelFinance modelFinance, FinanceDao financeDao) {
            this.modelFinance = modelFinance;
            this.financeDao = financeDao;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            financeDao.delete(modelFinance);
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
        }
    }
}
