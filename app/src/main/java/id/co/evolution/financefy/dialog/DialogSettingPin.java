package id.co.evolution.financefy.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.GridLayoutManager;

import com.google.gson.Gson;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;

import java.util.List;
import java.util.Locale;

import id.co.evolution.financefy.R;
import id.co.evolution.financefy.adapter.AdapterCalculator;
import id.co.evolution.financefy.databinding.DialogCalculatorBinding;
import id.co.evolution.financefy.databinding.DialogPinBinding;
import id.co.evolution.financefy.dummy.DummyCalculator;
import id.co.evolution.financefy.dummy.DummyNumberPin;
import id.co.evolution.financefy.helper.HelperCalculator;
import id.co.evolution.financefy.helper.TinyDb;
import id.co.evolution.financefy.helper.Tools;
import id.co.evolution.financefy.model.ModelSavings;

public class DialogSettingPin {
    Dialog dialog;
    Context context;
    LayoutInflater inflater;
    View dialogView;
    DialogInterfaceCallback dialogInterfaceCallback;
    String result = "";
    DialogPinBinding binding;
    TinyDb tinyDb;

    public DialogSettingPin(Context context, LayoutInflater layoutInflater, DialogInterfaceCallback dialogInterfaceCallback) {
        this.context = context;
        this.inflater = layoutInflater;
        this.dialogInterfaceCallback = dialogInterfaceCallback;
        tinyDb = new TinyDb(context);
        initSettingPin();
    }

    public DialogSettingPin(Context context, LayoutInflater layoutInflater, String result, DialogInterfaceCallback dialogInterfaceCallback) {
        this.context = context;
        this.inflater = layoutInflater;
        this.dialogInterfaceCallback = dialogInterfaceCallback;
        this.result = result;
        initSettingPin();
    }

    public void initSettingPin() {
        dialog = new Dialog(context);
        dialogView = inflater.inflate(R.layout.dialog_pin, null);
        dialog.setContentView(dialogView);
        binding = DataBindingUtil.bind(dialogView);
        binding.etAmount.setText(result);

        AdapterCalculator adapterCalculator = new AdapterCalculator(context, DummyNumberPin.getNumberPinConfirm(), (data, position) -> {
            List<String> dataList = (List<String>) data;


            switch (dataList.get(position)) {

                case "C":
                    clearCalculate();
                    break;
                case "OK":
                    if (result.length() < 6) {
                        Toast.makeText(context, "Mohon untuk di isi minimal 6 digit!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    new SweetAlertDialog(dialog.getContext(), SweetAlertDialog.WARNING_TYPE)
                            .setTitleText("Atur PIN")
                            .setContentText("Apakah anda yakin ingin mengatur PIN anda ?")
                            .setConfirmText("Ya")
                            .setConfirmClickListener(sweetAlertDialog -> {
                                sweetAlertDialog.dismiss();
                                dismiss();
                                Toast.makeText(context, "PIN telah di atur !", Toast.LENGTH_SHORT).show();
                            })
                            .setCancelText("Tidak")
                            .show();

                    dialogInterfaceCallback.onSubmit(Tools.replaceStringNumberFormat(result));
                    break;
                default:
                    result += dataList.get(position);
                    break;
            }
            resultText();
        });
        binding.rvCalculator.setLayoutManager(new GridLayoutManager(context, 3));
        binding.rvCalculator.setAdapter(adapterCalculator);

        binding.btnDelete.setOnClickListener(v -> {
            if (!result.isEmpty() && result.length() > 1) {
                result = Tools.removeLastChar(result);
                result = result.isEmpty() ? "" : result;
                binding.etAmount.setText(result);
            } else {
                clearCalculate();
            }
        });

        binding.etAmount.setEnabled(false);
        binding.etAmount.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);

        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();

        wlp.gravity = Gravity.CENTER;
        wlp.flags &= ~WindowManager.LayoutParams.FLAG_BLUR_BEHIND;
        window.setAttributes(wlp);
        dialog.getWindow().setLayout(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

    }

    private void resultText() {
        String text = result.replaceAll("[0123456789]", "*");
        binding.etAmount.setText(result);
    }

    private void clearCalculate() {
        result = "";
        binding.etAmount.setText("");
    }

    public void show() {
        dialog.show();
    }

    public void dismiss() {
        dialog.dismiss();
    }

    public interface DialogInterfaceCallback {
        void onSubmit(String result);
    }
}
