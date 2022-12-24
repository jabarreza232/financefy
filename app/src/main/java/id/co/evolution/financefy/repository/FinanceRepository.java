package id.co.evolution.financefy.repository;

import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.util.List;

import javax.inject.Inject;

import id.co.evolution.financefy.db.FinanceDao;
import id.co.evolution.financefy.model.ModelFinance;
import id.co.evolution.financefy.model.ModelUser;
import id.co.evolution.financefy.model.ModelUserWithFinance;

public class FinanceRepository {
    public FinanceDao financeDao;

    @Inject
    public FinanceRepository(FinanceDao financeDao) {
        this.financeDao = financeDao;
    }

    public LiveData<List<ModelFinance>> getAllFinance() {
        return financeDao.getAll();
    }

    public LiveData<List<ModelFinance>> getFinanceByMonth(String month, int id_user) {
        return financeDao.loadAllbyMonth(month, id_user);
    }

    public LiveData<List<ModelFinance>> getFinanceByYear(String year, int id_user) {
        return financeDao.loadAllByYear(year, id_user);
    }

    public LiveData<List<ModelFinance>> getFinanceByWeek(List<String> date, int id_user) {
        return financeDao.loadAllbyWeek(date, id_user);
    }

    public LiveData<List<ModelFinance>> getFinanceByType(String type) {
        return financeDao.findByType(type);
    }

    public LiveData<ModelFinance> getModelFinanceById(int id) {
        return financeDao.findById(id);
    }

    public LiveData<List<ModelFinance>> getFinanceByTypeAndMonth(String type, String month, int id_user) {
        return financeDao.findByTypeAndMonth(type, month, id_user);
    }

    public LiveData<List<ModelFinance>> getFinanceByTypeAndWeek(String type, List<String> date, int id_user) {
        return financeDao.findByTypeAndWeek(type, date, id_user);
    }

    public LiveData<List<ModelFinance>> getFinanceByUserId(int id_user) {
        return financeDao.findFinanceByUserId(id_user);
    }

    public static class InputUpdateFinance extends AsyncTask<Void, Void, Void> {
        ModelFinance modelFinance;
        String type;
        String amount;
        int id_user,id_finance;
        FinanceDao financeDao;

        public InputUpdateFinance(ModelFinance modelFinance, String type, FinanceDao financeDao) {
            this.modelFinance = modelFinance;
            this.type = type;
            this.financeDao = financeDao;
        }

        public InputUpdateFinance(String amount,int id_finance,int id_user, String type, FinanceDao financeDao) {
            this.amount = amount;
            this.id_user = id_user;
            this.id_finance = id_finance;
            this.type = type;
            this.financeDao = financeDao;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if (type.equalsIgnoreCase("create"))
                financeDao.insertAll(modelFinance);
            else{
                if(id_user>0)
                financeDao.updateByIdUser(amount,id_finance,id_user);
                else
                    financeDao.update(modelFinance);

            }

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
        int id_user;

        public RemoveFinance(ModelFinance modelFinance, FinanceDao financeDao) {
            this.modelFinance = modelFinance;
            this.financeDao = financeDao;
        }
        public RemoveFinance( FinanceDao financeDao,int id_user) {
            this.financeDao = financeDao;
            this.id_user = id_user;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if(id_user>0){
                financeDao.deleteByIdUser(id_user);
            }else{
                financeDao.delete(modelFinance);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
        }
    }
}
