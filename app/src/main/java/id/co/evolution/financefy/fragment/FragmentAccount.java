package id.co.evolution.financefy.fragment;

import static id.co.evolution.financefy.callback.CallbackOnActivityResult.REQUEST_CODE_UPDATE_SAVINGS_TARGET;
import static id.co.evolution.financefy.helper.Tools.convertToCurrency;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.activity.result.ActivityResult;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.gson.Gson;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import id.co.evolution.financefy.MainActivity;
import id.co.evolution.financefy.R;
import id.co.evolution.financefy.callback.CallbackOnActivityResult;
import id.co.evolution.financefy.databinding.FragmentAccountBinding;
import id.co.evolution.financefy.dialog.DialogCreateUser;
import id.co.evolution.financefy.dialog.DialogSavings;
import id.co.evolution.financefy.helper.FinanceFilter;
import id.co.evolution.financefy.helper.TinyDb;
import id.co.evolution.financefy.helper.Tools;
import id.co.evolution.financefy.model.ModelFinance;
import id.co.evolution.financefy.model.ModelSavings;
import id.co.evolution.financefy.model.ModelUser;
import id.co.evolution.financefy.repository.FinanceRepository;
import id.co.evolution.financefy.repository.SavingsRepository;
import id.co.evolution.financefy.repository.UserRepository;
import id.co.evolution.financefy.viewmodel.ViewModelFinance;
import id.co.evolution.financefy.viewmodel.ViewModelSavings;
import id.co.evolution.financefy.viewmodel.ViewModelUser;

@AndroidEntryPoint
public class FragmentAccount extends Fragment implements CallbackOnActivityResult.OnCallbackResult{
    Calendar today;
    Calendar prevNextYear;
    List<ModelUser> dataUser = new ArrayList<>();
    List<ModelSavings> dataSavings = new ArrayList<>();
    List<ModelFinance> dataFinance = new ArrayList<>();
    FragmentAccountBinding binding;
    DialogCreateUser dialogCreateUser;
    @Inject
    UserRepository userRepository;
    @Inject
    SavingsRepository savingsRepository;
    @Inject
    FinanceRepository financeRepository;
    @Inject
    FinanceFilter financeFilter;

    ViewModelUser viewModelUser;
    ViewModelSavings viewModelSavings;
    ViewModelFinance viewModelFinance;

    @Inject
    TinyDb tinyDb;

    boolean isAddSavings;
    int count_savings;

    DialogSavings dialogSavings;
    int id_savings_user = 0;
    long date_ship_millis;
    MainActivity mainActivity;
    CallbackOnActivityResult mCallbackOnActivityResult;

    //TODO NOTE SAVINGS : Menabung, FINANCE : JURNAL KEUANGAN

    public FragmentAccount() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCallbackOnActivityResult = new CallbackOnActivityResult(getContext(), requireActivity().getActivityResultRegistry(), this);
        getLifecycle().addObserver(mCallbackOnActivityResult);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_account, container, false);

        today = Calendar.getInstance();
        today.get(Calendar.YEAR);
        prevNextYear = Calendar.getInstance();
        date_ship_millis = today.getTimeInMillis();
        isAddSavings = false;
        return binding.getRoot();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivity = ((MainActivity) context);
    }

    @SuppressLint({"NewApi", "SetTextI18n"})
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModelUser = new ViewModelProvider(this).get(ViewModelUser.class);
        viewModelSavings = new ViewModelProvider(this).get(ViewModelSavings.class);
        viewModelFinance = new ViewModelProvider(this).get(ViewModelFinance.class);

        viewModelUser.init(userRepository);
        viewModelFinance.init(financeRepository);
        viewModelSavings.init(savingsRepository);

        viewModelSavings.findAllSavingsByIdUser(mainActivity.user.getId()).observe(getViewLifecycleOwner(),savings->{
            //TODO Hanya Sebagai pengecekan user apakah target menabung ada atau tidak
            // untuk kebutuhan visibility form category menabung.
            count_savings= savings.size();
        });
        //get the spinner from the xml.
        refreshDataUser(null, null);

        refreshDataUserByCategory();


        binding.spinChooseAccount.setOnItemClickListener((adapterView, view1, i, l) -> {

             setDataUser(dataUser.get(i));
             refreshDataUserByCategory();
        });


        binding.fabAddAccount.setOnClickListener(view12 -> {
            showDialogAddAccount(true);
        });

        binding.imgMore.setOnClickListener(v -> {
            showDialogChoose();
        });
    }

    private void refreshDataUserByCategory() {
        if (mainActivity.user.getCategory().equalsIgnoreCase(getString(R.string.menabung))) {
            refreshDataSavings();
        } else {
            refreshDataFinance();
        }
    }

    private void showDialogChoose() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Pilih Opsi");
        final String[] tipe = {"Ubah Akun", "Hapus Akun"};


        builder.setItems(tipe, (dialog, which) -> {
            switch (tipe[which]) {
                case "Ubah Akun":
//                    mPositionItem = position;
                    showDialogAddAccount(false);
                    dialog.dismiss();
                    break;
                case "Hapus Akun":
//                    viewModelFinance.removeFinance(data.get(position));
//                    dataFinance.remove(position);
//                    binding.rvList.getAdapter().notifyDataSetChanged();

                    new UserRepository.RemoveUser(mainActivity.user, userRepository.userDao).execute();

                    new SavingsRepository.RemoveSavings(savingsRepository.savingsDao,mainActivity.user.getId()).execute();

                    new FinanceRepository.RemoveFinance(financeRepository.financeDao,mainActivity.user.getId()).execute();

                    int selectedPositionUser= dataUser.indexOf(mainActivity.user);
                    mainActivity.user = dataUser.get((selectedPositionUser==0? 1 :selectedPositionUser - 1));

                    refreshDataUser(mainActivity.user, null);

                    refreshDataUserByCategory();
                    dialog.dismiss();
                    break;
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showDialogAddAccount(boolean isAddAccount) {
        String type = isAddAccount ? "create" : "update";



        dialogCreateUser = new DialogCreateUser(getContext(), getLayoutInflater(),  getFragmentManager(),count_savings, mainActivity.user,mainActivity.modelSavings, (modelUser, modelSavings) -> {

            new UserRepository.InputUpdateUser(modelUser, type, userRepository.userDao)
                    .execute();

            isAddSavings = isAddAccount;
            if (!isAddAccount) mainActivity.user = modelUser;
            modelSavings = modelUser.getCategory().equalsIgnoreCase(getString(R.string.menabung))?modelSavings:new ModelSavings();
            mainActivity.modelSavings = modelSavings;
            tinyDb.putObject("savings", modelSavings);
            refreshDataUser(modelUser,  modelSavings);

            refreshDataUserByCategory();
        });

        dialogCreateUser.showDialogCreateUser(isAddAccount);
    }

    private void refreshDataUser(ModelUser modelUser, ModelSavings modelSavings) {
        viewModelUser.getAllUser().observe(getViewLifecycleOwner(), modelUsers -> {
            for (ModelUser modelUser1 : modelUsers) {
                if (modelUser1.equals(modelUser)) {

                    id_savings_user = modelUser1.getId();
                    if(modelSavings!=null)  modelSavings.setId_savings_user(id_savings_user);
                    Log.e("TAG", "showDialogAddAccount: " + new Gson().toJson(modelUser1) + " : " + new Gson().toJson(modelSavings));

                }
            }


            List<String> dataName = new ArrayList<>();
            for (ModelUser user1 : modelUsers) dataName.add(user1.getName());
            dataUser = modelUsers;

            ArrayAdapter<String> adapter = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_dropdown_item, dataName.toArray());
            binding.spinChooseAccount.setAdapter(adapter);

            if (mainActivity.user != null) setDataUser(mainActivity.user);
        });

        if(modelSavings!=null && modelSavings.getTitle()!=null) {

            isAddSavings = count_savings==0;

            String type = isAddSavings ? "create" : "update";

            new SavingsRepository.InputUpdateSavings(modelSavings, type, savingsRepository.savingsDao).execute();
            isAddSavings = false;
            mainActivity.modelSavings= new ModelSavings();
        }
    }


    private void refreshDataFinance() {

        viewModelFinance.getFinanceByUserId(mainActivity.user.getId()).observe(getViewLifecycleOwner(), modelFinance -> {
            dataFinance = modelFinance;

            binding.layoutAccountFinanceJournal.txtTotalIncome.setText(convertToCurrency(financeFilter.totalIncome(dataFinance)));
            binding.layoutAccountFinanceJournal.txtTotalExpense.setText(convertToCurrency(financeFilter.totalExpense(dataFinance)));
        });

//
//        binding.layoutAccountFinanceJournal.txtYear.setText(Tools.getFormattedYearSimple(date_ship_milis));
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//
//            if (date_ship_milis != today.getTimeInMillis()) {
//                binding.layoutAccountFinanceJournal.btnNext.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.white)));
//
//                binding.layoutAccountFinanceJournal.btnNext.setEnabled(true);
//            }
//            if (date_ship_milis >= today.getTimeInMillis()) {
//                binding.layoutAccountFinanceJournal.btnNext.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.colorGrey50)));
//                binding.layoutAccountFinanceJournal.btnNext.setEnabled(false);
//            }
//        }
    }

    private void refreshDataSavings() {
        viewModelSavings.findAllSavingsByIdUser(mainActivity.user.getId()).observe(getViewLifecycleOwner(), modelSavings -> {
            dataSavings = modelSavings;

            if (dataSavings.size() > 0) {
                setDataSavings(mainActivity.modelSavings!=null? mainActivity.modelSavings:modelSavings.get(0));

                binding.layoutAccountSavings.placeChooseSavings.setOnClickListener(v -> {

                    dialogSavings = new DialogSavings(getContext(),getLayoutInflater(), (type,index, result) -> {
                        switch (type) {
                            case CLICKED:
                                setDataSavings(modelSavings.get(index));
                                break;
                            case REMOVED:
                                new SweetAlertDialog(getContext(), SweetAlertDialog.WARNING_TYPE)
                                        .setTitleText("Hapus")
                                        .setContentText("Apakah anda yakin ingin hapus tabungan '" + modelSavings.get(index).getTitle() + "'?")
                                        .setConfirmText("Ya")
                                        .setConfirmClickListener(sweetAlertDialog -> {
                                            ModelSavings modelSaving;
                                            if(index-1 < 0){
                                                modelSaving = modelSavings.get(index + 1);
                                            } else {
                                                modelSaving = modelSavings.get(index - 1);
                                            }
                                            setDataSavings(modelSaving);

                                            new SavingsRepository.RemoveSavings(modelSavings.get(index), savingsRepository.savingsDao).execute();
                                            modelSavings.remove(index);
                                            sweetAlertDialog.dismiss();
                                        })
                                        .setCancelText("Tidak")
                                        .show();
                                break;
                            case EDIT:
                                mCallbackOnActivityResult.updateDataSavingsTarget(this.dataSavings,index,this.dataSavings.get(index));
                                break;
                        }
                    });

                    dialogSavings.showDialogSavings(dataSavings);
                });
            }
        });

    }

    private void setDataSavings(ModelSavings modelSavings) {
        mainActivity.modelSavings = modelSavings;
        tinyDb.putObject("savings", modelSavings);
        binding.layoutAccountSavings.txtChooseSavings.setText(modelSavings.getTitle());
        binding.layoutAccountSavings.txtTarget.setText(Tools.convertToCurrency(modelSavings.getTargetValue()));
        binding.layoutAccountSavings.txtProgressValueSavings.setText(Tools.convertToCurrency(modelSavings.getProcessValue()));
        binding.layoutAccountSavings.txtPercentageSavings.setText(Tools.calculatePercentage(modelSavings.getProcessValue(), modelSavings.getTargetValue()) + "% ");
        binding.layoutAccountSavings.progressBarTargetSavings.setProgress((int) Tools.calculatePercentage(modelSavings.getProcessValue(), modelSavings.getTargetValue()));
        binding.layoutAccountSavings.progressBarTargetSavings.setMax(100);
    }


    private void setDataUser(ModelUser user) {
        binding.spinChooseAccount.setText("");
        binding.txtName.setText(user.getName());
        binding.txtDescription.setText(user.getType() + " - " + user.getCategory());
        tinyDb.putObject("user", user);
        mainActivity.user = user;
        boolean visibleAccountSavings = user.getCategory().equalsIgnoreCase(getString(R.string.menabung));
        binding.layoutAccountSavings.placeAccountSavings.setVisibility(visibleAccountSavings ? View.VISIBLE : View.GONE);
        binding.layoutAccountFinanceJournal.placeAccountFinanceJournal.setVisibility(!visibleAccountSavings ? View.VISIBLE : View.GONE);
    }

    @Override
    public void result(ActivityResult result, Intent intent) {
        if (result.getResultCode() == REQUEST_CODE_UPDATE_SAVINGS_TARGET) {


            if (intent != null) {
                ModelSavings modelSaving = (ModelSavings) intent.getSerializableExtra("savings");

                setDataSavings(modelSaving);
            }
        }
    }
}
