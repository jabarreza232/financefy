package id.co.evolution.financefy.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import java.io.File;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import id.co.evolution.financefy.MainActivity;
import id.co.evolution.financefy.R;
import id.co.evolution.financefy.activity.NotificationActivity;
import id.co.evolution.financefy.activity.SwitchThemeActivity;
import id.co.evolution.financefy.databinding.FragmentSettingsBinding;
import id.co.evolution.financefy.dialog.DialogSettingPin;
import id.co.evolution.financefy.helper.TinyDb;

@AndroidEntryPoint
public class FragmentSettings extends Fragment {
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
        binding.txtDeleteCache.setOnClickListener(v->{
            deleteCache(getActivity());
        });

        binding.txtNotification.setOnClickListener(v->{
            Intent i = new Intent(getContext(), NotificationActivity.class);
            startActivity(i);
        });

        binding.txtPinSetting.setOnClickListener(v->{
            DialogSettingPin dialogSettingPin = new DialogSettingPin(getContext(), getLayoutInflater(), new DialogSettingPin.DialogInterfaceCallback() {
                @Override
                public void onSubmit(String result) {
                    tinyDb.putString("pin", result);
                    tinyDb.putBoolean("isSettingPin", true);
                }
            });

            dialogSettingPin.show();
        });
        binding.txtMoney.setOnClickListener(v->{
            setCurrencySettings();
        });
//        changeColorThemeSettings(mainActivity.modelPrimaryColor);
        return binding.getRoot();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        deleteCache(getActivity());
    }

    public static void deleteCache(Context context) {
        try {
            File dir = context.getCacheDir();
            deleteDir(dir);
        } catch (Exception e) { e.printStackTrace();}
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }

        return dir.delete();
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