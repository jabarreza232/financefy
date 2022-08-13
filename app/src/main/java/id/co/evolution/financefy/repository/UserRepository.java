package id.co.evolution.financefy.repository;

import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.util.List;

import javax.inject.Inject;

import id.co.evolution.financefy.db.UserDao;
import id.co.evolution.financefy.model.ModelUser;
import id.co.evolution.financefy.model.ModelUserWithFinance;

public class UserRepository {
    public UserDao userDao;

    @Inject
    public UserRepository(UserDao userDao) {
        this.userDao = userDao;
    }

    public LiveData<List<ModelUser>> getAllUser() {
        return userDao.getAll();
    }

    public LiveData<ModelUser> getModelFinanceById(int id) {
        return userDao.findById(id);
    }

    public LiveData<ModelUserWithFinance> getFinanceByUserId(int id_user) {
        return userDao.findFinanceByUserId(id_user);
    }

    public static class InputUpdateUser extends AsyncTask<Void, Void, Void> {
        ModelUser modelUser;
        String type;
        UserDao userDao;

        public InputUpdateUser(ModelUser modelUser, String type, UserDao userDao) {
            this.modelUser = modelUser;
            this.type = type;
            this.userDao = userDao;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if (type.toLowerCase().equals("create"))
                userDao.insertAll(modelUser);
            else
                userDao.update(modelUser);

            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
        }
    }
}
