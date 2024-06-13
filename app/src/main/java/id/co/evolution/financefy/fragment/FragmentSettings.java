package id.co.evolution.financefy.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import id.co.evolution.financefy.MainActivity;
import id.co.evolution.financefy.R;
import id.co.evolution.financefy.activity.AboutActivity;
import id.co.evolution.financefy.activity.NotificationActivity;
import id.co.evolution.financefy.activity.SwitchThemeActivity;
import id.co.evolution.financefy.databinding.FragmentSettingsBinding;
import id.co.evolution.financefy.dialog.DialogConfirm;
import id.co.evolution.financefy.dialog.DialogSettingPin;
import id.co.evolution.financefy.helper.TinyDb;

@AndroidEntryPoint
public class FragmentSettings extends Fragment {
    private static final int PERMISSION_REQUEST_CODE = 100;

    MainActivity mainActivity;
    @Inject
    TinyDb tinyDb;

    public FragmentSettings() {
        // Required empty public constructor
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivity = ((MainActivity) context);
    }

    FragmentSettingsBinding binding;

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_settings, container, false);

        binding.cvSwitchTheme.setOnClickListener(v -> {
            Intent i = new Intent(getContext(), SwitchThemeActivity.class);
            startActivity(i);
            getActivity().finish();
        });


//        binding.txtDeleteCache.setText("Hapus Cache ("+formatSize(Tools.getCacheSize(getContext()))+")");
//        binding.txtDeleteCache.setOnClickListener(v->{
//            if (checkPermission()) {
//                Tools.clearCache(getContext());
//                binding.txtDeleteCache.setText("Hapus Cache ("+formatSize(Tools.getCacheSize(getContext()))+")");
//            } else {
//                requestPermission();
//            }
//        });

        binding.txtNotification.setOnClickListener(v->{
            Intent i = new Intent(getContext(), NotificationActivity.class);
            startActivity(i);
        });
        changeStatusPin();
        binding.txtPinSetting.setOnClickListener(v->{
            showDialogSettingPIN();
        });
        if(mainActivity.isPinSetting){
            binding.txtStatusPin.setText("Aktif");
            binding.txtStatusPin.setTextColor(ContextCompat.getColor(getContext(),R.color.green));
        }else{
            binding.txtStatusPin.setText("Tidak Aktif");
            binding.txtStatusPin.setTextColor(ContextCompat.getColor(getContext(),R.color.red));
        }
        binding.txtStatusPin.setOnClickListener(v->{
            if(mainActivity.isPinSetting){
                DialogConfirm dialogConfirm = new DialogConfirm(getContext(), inflater, result -> {

                    if (result.equalsIgnoreCase("yes")){
                        Toast.makeText(getContext(), "PIN telah berhasil di non aktifkan !", Toast.LENGTH_SHORT).show();
                        tinyDb.putString("pin", "");
                        tinyDb.putBoolean("isSettingPin", false);
                        mainActivity.isPinSetting = false;
                        binding.txtStatusPin.setText("Tidak Aktif");
                        binding.txtStatusPin.setTextColor(ContextCompat.getColor(getContext(),R.color.red));

                    }
                });
                dialogConfirm.showDialogConfirm("Menonaktifkan PIN","Apakah anda yakin ingin menonaktifkan PIN anda ? ");

            }else{
                showDialogSettingPIN();
            }

        });
        binding.txtMoney.setOnClickListener(v->{
            setCurrencySettings();
        });
        binding.txtInfo.setOnClickListener(v->{
            Intent i = new Intent(getContext(), AboutActivity.class);
            startActivity(i);
        });

//        changeColorThemeSettings(mainActivity.modelPrimaryColor);
        return binding.getRoot();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }
    private void showDialogSettingPIN(){
        DialogSettingPin dialogSettingPin = new DialogSettingPin(getContext(), getLayoutInflater(), new DialogSettingPin.DialogInterfaceCallback() {
            @Override
            public void onSubmit(String result) {
                tinyDb.putString("pin", result);
                tinyDb.putBoolean("isSettingPin", true);
                mainActivity.isPinSetting = true;
                changeStatusPin();
            }
        });

        dialogSettingPin.show();
    }
    private void changeStatusPin(){
        if(tinyDb.getBoolean("isSettingPin")){
            binding.txtStatusPin.setText("Aktif");
            binding.txtStatusPin.setTextColor(ContextCompat.getColor(getContext(),R.color.green));
        }else{
            binding.txtStatusPin.setText("Tidak Aktif");
            binding.txtStatusPin.setTextColor(ContextCompat.getColor(getContext(),R.color.red));
        }

    }

    private void setCurrencySettings() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Mata Uang");
        final String[] tipe = {"IDR (Rp)", "USD ($)"};

        builder.setItems(tipe, (dialog, which) -> {
            switch (which) {
                case 0:
                    tinyDb.putString("currency","IDR");
                    dialog.dismiss();
                    break;
                case 1:
                    tinyDb.putString("currency","USD");
                    dialog.dismiss();
                    break;
            }

            binding.txtSelectedMoney.setText(tipe[which]);
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

}