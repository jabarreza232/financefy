package id.co.evolution.financefy.repository;

import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.lang.reflect.Method;
import java.util.List;

import javax.inject.Inject;

import id.co.evolution.financefy.db.FinanceDao;
import id.co.evolution.financefy.db.UserDao;
import id.co.evolution.financefy.model.ModelFinance;
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

    public static class InputUpdateUser extends AsyncTask<Void, Void, LiveData<List<ModelUser>>> {
        ModelUser modelUser;
        String type;
        UserDao userDao;
        MethodCallback methodCallback;
        public InputUpdateUser(ModelUser modelUser, String type, UserDao userDao) {
            this.modelUser = modelUser;
            this.type = type;
            this.userDao = userDao;
        }

        public InputUpdateUser(ModelUser modelUser, String type, UserDao userDao,MethodCallback methodCallback) {
            this.modelUser = modelUser;
            this.type = type;
            this.userDao = userDao;
            this.methodCallback = methodCallback;
        }

        @Override
        protected LiveData<List<ModelUser>> doInBackground(Void... voids) {
            if (type.equalsIgnoreCase("create"))
                userDao.insertAll(modelUser);
            else
                userDao.update(modelUser);

            return userDao.getAll();
        }

        @Override
        protected void onPostExecute(LiveData<List<ModelUser>> unused) {
            super.onPostExecute(unused);
            if (methodCallback!=null)
            methodCallback.onPostExecute();
        }
        public interface MethodCallback{
            void onPostExecute();
        }
    }

    public static class RemoveUser extends AsyncTask<Void, Void, Void> {
        ModelUser modelUser;
        UserDao userDao;

        public RemoveUser(ModelUser modelUser, UserDao userDao) {
            this.modelUser = modelUser;
            this.userDao = userDao;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            userDao.delete(modelUser);
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
        }
    }
}
