package id.co.evolution.financefy.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import id.co.evolution.financefy.R;
import id.co.evolution.financefy.adapter.AdapterFinance;
import id.co.evolution.financefy.databinding.FragmentAccountBinding;
import id.co.evolution.financefy.dialog.DialogCreateUser;
import id.co.evolution.financefy.dialog.DialogFinance;
import id.co.evolution.financefy.helper.TinyDb;
import id.co.evolution.financefy.helper.Tools;
import id.co.evolution.financefy.model.ModelSavings;
import id.co.evolution.financefy.model.ModelUser;
import id.co.evolution.financefy.repository.SavingsRepository;
import id.co.evolution.financefy.repository.UserRepository;
import id.co.evolution.financefy.viewmodel.ViewModelSavings;
import id.co.evolution.financefy.viewmodel.ViewModelUser;

@AndroidEntryPoint
public class FragmentAccount extends Fragment {
    Calendar today;
    List<ModelUser> dataUser = new ArrayList<>();
    List<ModelSavings> dataSavings = new ArrayList<>();
    FragmentAccountBinding binding;
    AdapterFinance.TYPE_LAYOUT_MANAGER type_layout_manager = AdapterFinance.TYPE_LAYOUT_MANAGER.GRID;
    DialogCreateUser dialogCreateUser;
    @Inject
    UserRepository userRepository;
    @Inject
    SavingsRepository savingsRepository;
    ViewModelUser viewModelUser;
    ViewModelSavings viewModelSavings;
    @Inject
    TinyDb tinyDb;
    ModelUser user;

    boolean isAddSavings;

    DialogFinance dialogFinance;
    int id_savings_user = 0;

    public FragmentAccount() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_account, container, false);

        today = Calendar.getInstance();
        isAddSavings = false;
        return binding.getRoot();
    }

    @SuppressLint({"NewApi", "SetTextI18n"})
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModelUser = new ViewModelProvider(this).get(ViewModelUser.class);
        viewModelSavings = new ViewModelProvider(this).get(ViewModelSavings.class);

        viewModelUser.init(userRepository);
        viewModelSavings.init(savingsRepository);
        //get the spinner from the xml.
        user = tinyDb.getObject("user", ModelUser.class);
        refreshDataUser(null, null);
        refreshDataSavings();


        binding.spinChooseAccount.setOnItemClickListener((adapterView, view1, i, l) -> {

            setDataUser(dataUser.get(i));

            refreshDataSavings();
        });


        binding.fabAddAccount.setOnClickListener(view12 -> {
            showDialogAddAccount(true);
        });

        binding.imgMore.setOnClickListener(v -> {
            showDialogChoose();
        });
    }

    private void showDialogChoose() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Pilih Opsi");
        final String[] tipe = {"Ubah Akun", "Hapus Akun"};


        builder.setItems(tipe, (dialog, which) -> {
            switch (which) {
                case 0:
//                    mPositionItem = position;
                    showDialogAddAccount(false);
                    dialog.dismiss();
                    break;
                case 1:
//                    viewModelFinance.removeFinance(data.get(position));
//                    dataFinance.remove(position);
//                    binding.rvList.getAdapter().notifyDataSetChanged();

                    new UserRepository.RemoveUser(user, userRepository.userDao).execute();
                    new SavingsRepository.RemoveSavings(dataSavings, savingsRepository.savingsDao).execute();

                    user = dataUser.get((dataUser.indexOf(user) - 1));

                    refreshDataUser(user, null);
                    refreshDataSavings();
                    dialog.dismiss();
                    break;
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showDialogAddAccount(boolean isAddAccount) {


        dialogCreateUser = new DialogCreateUser(getContext(), getLayoutInflater(), userRepository, savingsRepository, getFragmentManager(), user, (modelUser, modelSavings) -> {
            String type = isAddAccount ? "create" : "update";

            new UserRepository.InputUpdateUser(modelUser, type, userRepository.userDao)
                    .execute();

            isAddSavings = isAddAccount;
            if (!isAddAccount) user = modelUser;

            refreshDataUser(modelUser, modelSavings);
        });

        dialogCreateUser.showDialogCreateUser(isAddAccount);
    }

    private void refreshDataUser(ModelUser modelUser, ModelSavings modelSavings) {
        viewModelUser.getAllUser().observe(getViewLifecycleOwner(), modelUsers -> {
            if (isAddSavings) {
                for (ModelUser modelUser1 : modelUsers) {
                    if (modelUser1.equals(modelUser)) {

                        id_savings_user = modelUser1.getId();
                        modelSavings.setId_savings_user(id_savings_user);
                        Log.e("TAG", "showDialogAddAccount: " + new Gson().toJson(modelUser1) + " : " + new Gson().toJson(modelSavings));
                        new SavingsRepository.InputUpdateSavings(modelSavings, "create", savingsRepository.savingsDao).execute();
                        isAddSavings = false;
                    }
                }
            }


            List<String> dataName = new ArrayList<>();
            for (ModelUser user1 : modelUsers) dataName.add(user1.getName());
            dataUser = modelUsers;

            ArrayAdapter<String> adapter = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_dropdown_item, dataName.toArray());
            binding.spinChooseAccount.setAdapter(adapter);

            if (user != null)
                setDataUser(user);
        });
    }

    private void refreshDataSavings() {
        viewModelSavings.findAllSavingsByIdUser(user.getId()).observe(getViewLifecycleOwner(), modelSavings -> {
            dataSavings = modelSavings;
            List<String> dataSavings = new ArrayList<>();
            for (ModelSavings savings : modelSavings)
                dataSavings.add(savings.getTitle());
            if (dataSavings.size() > 0) {

                setDataSavings(modelSavings.get(0));
                binding.layoutAccountSavings.placeChooseSavings.setOnClickListener(v -> {
                    dialogFinance = new DialogFinance(getContext(), (index, result) -> {
                        setDataSavings(modelSavings.get(index));
                    });

                    dialogFinance.showDialogSavings(dataSavings);
                });
            }
        });
    }

    private void setDataSavings(ModelSavings modelSavings) {
        binding.layoutAccountSavings.txtChooseSavings.setText(modelSavings.getTitle());
        binding.layoutAccountSavings.txtTarget.setText(Tools.convertToCurrency(modelSavings.getTargetValue()));
        binding.layoutAccountSavings.txtProgressValueSavings.setText(Tools.convertToCurrency(modelSavings.getProcessValue()));
        binding.layoutAccountSavings.txtPercentageSavings.setText(Tools.calculatePercentage(modelSavings.getProcessValue(), modelSavings.getTargetValue()) + "% ");
        binding.layoutAccountSavings.progressBarTargetSavings.setProgress((int) Tools.calculatePercentage(modelSavings.getProcessValue(), modelSavings.getTargetValue()));
        binding.layoutAccountSavings.progressBarTargetSavings.setMax(100);
    }


    private void setDataUser(ModelUser user) {
        binding.txtName.setText(user.getName());
        binding.txtDescription.setText(user.getType() + " - " + user.getCategory());
        tinyDb.putObject("user", user);
        this.user = user;
        boolean visibleAccountSavings = user.getCategory().equalsIgnoreCase(getString(R.string.menabung));
        binding.layoutAccountSavings.placeAccountSavings.setVisibility(visibleAccountSavings ? View.VISIBLE : View.GONE);
        binding.layoutAccountFinanceJournal.placeAccountFinanceJournal.setVisibility(!visibleAccountSavings ? View.VISIBLE : View.GONE);
    }
}
