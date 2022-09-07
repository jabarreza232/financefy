package id.co.evolution.financefy.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import id.co.evolution.financefy.model.ModelFinance;
import id.co.evolution.financefy.model.ModelUser;
import id.co.evolution.financefy.model.ModelUserWithFinance;
import id.co.evolution.financefy.repository.FinanceRepository;
import id.co.evolution.financefy.repository.UserRepository;

public class ViewModelUser extends ViewModel {
   public UserRepository userRepository;

    public void init(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void inputUpdateUser(String type, ModelUser modelUser) {
        new UserRepository.InputUpdateUser(modelUser, type, userRepository.userDao).execute();
    }

    public LiveData<ModelUserWithFinance> getFinanceByUserId(int id) {
        return userRepository.getFinanceByUserId(id);
    }

    public LiveData<List<ModelUser>> getAllUser() {
        return userRepository.getAllUser();
    }

}
