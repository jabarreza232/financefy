package id.co.evolution.financefy.viewmodel;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;

import id.co.evolution.financefy.repository.FinanceRepository;
//implements ViewModelProvider.Factory
public class ViewModelFactory {
//    FinanceRepository financeRepository;
//    SavedStateHandle savedStateHandle;
//    ViewModel viewModel;
//
////    @Inject
//    public ViewModelFactory(FinanceRepository financeRepository, SavedStateHandle savedStateHandle) {
//        this.financeRepository = financeRepository;
//        this.savedStateHandle = savedStateHandle;
//    }
//
//    @NonNull
//    @Override
//    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
//
//        if (viewModel == null) {
//            if (modelClass == ViewModelFinance.class) {
////                viewModel = new ViewModelFinance(financeRepository, savedStateHandle);
//            } else {
//                throw new RuntimeException("unsupported view model class: " + modelClass);
//            }
//        }
//
//        return (T) viewModel;
//    }
}
