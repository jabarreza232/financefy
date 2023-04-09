package id.co.evolution.financefy.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import id.co.evolution.financefy.MainActivity;
import id.co.evolution.financefy.R;
import id.co.evolution.financefy.activity.SwitchThemeActivity;
import id.co.evolution.financefy.databinding.FragmentSettingsBinding;
import id.co.evolution.financefy.helper.TinyDb;
import id.co.evolution.financefy.helper.Tools;
import id.co.evolution.financefy.model.ModelPrimaryColor;

@AndroidEntryPoint
public class FragmentSettings extends Fragment {
    MainActivity mainActivity;

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

//        changeColorThemeSettings(mainActivity.modelPrimaryColor);
        return binding.getRoot();
    }

    private void changeColorThemeSettings(ModelPrimaryColor modelPrimaryColor) {
        binding.txtGroups.setTextColor(ContextCompat.getColor(getContext(),modelPrimaryColor.getColorPrimary()));
        binding.txtAdvanced.setTextColor(ContextCompat.getColor(getContext(),modelPrimaryColor.getColorPrimary()));
        binding.txtSecurity.setTextColor(ContextCompat.getColor(getContext(),modelPrimaryColor.getColorPrimary()));

        setTextAndDrawableColor(binding.txtSwitchTheme,modelPrimaryColor);

        setTextAndDrawableColor(binding.txtMoney,modelPrimaryColor);

        setTextAndDrawableColor(binding.txtDeleteCache,modelPrimaryColor);

        setTextAndDrawableColor(binding.txtPinSetting,modelPrimaryColor);

        setTextAndDrawableColor(binding.txtNotification,modelPrimaryColor);

        setTextAndDrawableColor(binding.txtLanguage,modelPrimaryColor);

        setTextAndDrawableColor(binding.txtInfo,modelPrimaryColor);
    }

    private void setTextAndDrawableColor(TextView textView,ModelPrimaryColor modelPrimaryColor){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            textView.setCompoundDrawableTintList(ContextCompat.getColorStateList(getContext(), modelPrimaryColor.getColorPrimary()));
        }
        textView.setTextColor(ContextCompat.getColor(getContext(),modelPrimaryColor.getColorPrimary()));

    }
}