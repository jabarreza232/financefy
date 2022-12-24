package id.co.evolution.financefy.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import id.co.evolution.financefy.R;
import id.co.evolution.financefy.adapter.AdapterCalculator;
import id.co.evolution.financefy.databinding.DialogCalculatorBinding;
import id.co.evolution.financefy.dummy.DummyCalculator;
import id.co.evolution.financefy.helper.HelperCalculator;
import id.co.evolution.financefy.helper.Tools;

public class DialogCalculator {
    Dialog dialog;
    Context context;
    LayoutInflater inflater;
    View dialogView;
    DialogInterfaceCallback dialogInterfaceCallback;
    String result = "0";
    DialogCalculatorBinding binding;


    public DialogCalculator(Context context, LayoutInflater layoutInflater, DialogInterfaceCallback dialogInterfaceCallback) {
        this.context = context;
        this.inflater = layoutInflater;
        this.dialogInterfaceCallback = dialogInterfaceCallback;
        initCalculator();
    }
    public DialogCalculator(Context context, LayoutInflater layoutInflater,String result, DialogInterfaceCallback dialogInterfaceCallback) {
        this.context = context;
        this.inflater = layoutInflater;
        this.dialogInterfaceCallback = dialogInterfaceCallback;
        this.result = result;
        initCalculator();
    }

    public void initCalculator() {
        dialog = new Dialog(context);
        dialogView = inflater.inflate(R.layout.dialog_calculator, null);
        dialog.setContentView(dialogView);
        binding = DataBindingUtil.bind(dialogView);
        if(!result.equals("0"))
        binding.etAmount.setText(result);

        AdapterCalculator adapterCalculator = new AdapterCalculator(context, DummyCalculator.getDataCalculator(), (data, position) -> {
            List<String> dataList = (List<String>) data;

            if (result.equals("0") && !dataList.get(position).equals("0"))
                result = "";
            boolean checkSymbol = result.length() > 0 && Tools.isSpecialCharacterInMyString(Tools.getLastChar(result));

            switch (dataList.get(position)) {
                case "0":
                    if (dataList.get(position).equalsIgnoreCase("0")) {
                        if (result.equalsIgnoreCase(dataList.get(position))) {
                            result = "0";
                        } else {
                            result += "0";
                        }
                    }
                    break;
                case "1":
                    result += "1";
                    break;
                case "2":
                    result += "2";
                    break;
                case "3":
                    result += "3";
                    break;
                case "4":
                    result += "4";
                    break;
                case "5":
                    result += "5";
                    break;
                case "6":
                    result += "6";
                    break;
                case "7":
                    result += "7";
                    break;
                case "8":
                    result += "8";
                    break;
                case "9":
                    result += "9";
                    break;
                case "C":
                    clearCalculate();
                    break;
                case "+":
                    if (checkSymbol) {
                        if (Tools.getLastChar(result).equalsIgnoreCase("+")) return;
                        else {
                            result = Tools.removeLastChar(result);
                            result += "+";
                        }
                    } else
                        result += "+";
                    break;
                case "-":
                    if (checkSymbol) {
                        if (Tools.getLastChar(result).equalsIgnoreCase("-")) return;
                        else {
                            result = Tools.removeLastChar(result);
                            result += "-";
                        }
                    } else
                        result += "-";
                    break;
                case "/":
                    if (checkSymbol) {
                        if (Tools.getLastChar(result).equalsIgnoreCase("/")) return;
                        else {
                            result = Tools.removeLastChar(result);
                            result += "/";
                        }
                    } else
                        result += "/";
                    break;
                case "x":
                    if (checkSymbol) {
                        if (Tools.getLastChar(result).equalsIgnoreCase("x")) return;
                        else {
                            result = Tools.removeLastChar(result);
                            result += "x";
                        }
                    } else
                        result += "x";
                    break;

                case "=":
                    char[] typeCalculates = Tools.getSpecialCharacterInMyString(result).toCharArray();
                    Log.e("cek:", "" + Tools.getSpecialCharacterInMyString(result) + " : " + typeCalculates.length);
                    String symbolCalculate = "[x/\\-+]";
                    Log.e("cek_juga_valuenya:", "" + new Gson().toJson(result.split(symbolCalculate)));
                    long valueStart;
                    long valueEnd;
                    long valueResult = 0;
                    for (int i = 0; i < typeCalculates.length; i++) {
                        String value = Character.toString(typeCalculates[i]);

                        valueStart = 0;
                        valueEnd = 0;
                        for (int j = 0; j < result.split(symbolCalculate).length; j++) {

//                            Log.e("cek_juga_valuenya:", "" +value);

                            Log.e("cek_value_result:", valueResult + "");

                            if (j % 2 == 0) {
                                valueStart = Long.parseLong(result.split(symbolCalculate)[j].isEmpty() ? "0" : result.split(symbolCalculate)[j]);
                            } else {
                                valueEnd = Long.parseLong(result.split(symbolCalculate)[j].isEmpty() ? "0" : result.split(symbolCalculate)[j]);
                            }

//                            if (valueStart == 0) {
//                                if (valueResult > 0)
//                                    valueStart = valueResult;
//                                Log.e("cek_juga_value_start:", "" + valueStart);
//                            }
//
//                            if (valueEnd == 0) {
//                                if (valueResult > 0)
//                                    valueEnd = valueResult;
//                                Log.e("cek_juga_value_end:", "" + valueEnd);
//                            }


                            if (valueStart > 0 && valueEnd > 0) {
                                if (valueResult == 0)
                                    valueResult = Long.parseLong(HelperCalculator.calculate(valueStart, valueEnd, value));
                                else {
                                    if (j % 2 == 0)
                                        valueResult = Long.parseLong(HelperCalculator.calculate(valueResult, valueStart, value));
                                    else
                                        valueResult = Long.parseLong(HelperCalculator.calculate(valueResult, valueEnd, value));
                                }

                                Log.e("cek_value_start_end:", valueStart + " : " + valueEnd + " : " + valueResult);

                                valueStart = 0;
                                valueEnd = 0;
                            } else if (j > 0) {

                                if (j % 2 == 1)
                                    valueResult = Long.parseLong(HelperCalculator.calculate(valueResult, valueEnd, value));
                                else
                                    valueResult = Long.parseLong(HelperCalculator.calculate(valueResult, valueStart, value));
                            }


                            Log.e("cek_value_start_end:", valueStart + " : " + valueEnd + " : " + valueResult);

////                            try {
////                                result = HelperCalculator.calculate(valueStart, valueEnd, value);
////
////                            } catch (ArithmeticException e) {
////                                e.printStackTrace();
////                            }


                        }
                        result = String.valueOf(valueResult);
                    }

                    break;
                default:
                    break;
            }
            resultToNumberFormat();
        });
        binding.rvCalculator.setLayoutManager(new GridLayoutManager(context, 4));
        binding.rvCalculator.setAdapter(adapterCalculator);

        binding.btnDelete.setOnClickListener(v -> {
            if (!result.isEmpty() && result.length() > 1) {
                result = Tools.removeLastChar(result);
                result = result.isEmpty() ? "0" : result;
                binding.etAmount.setText(result);
            } else {
                clearCalculate();
            }
        });

        binding.etAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().equals(result)) {
                    binding.etAmount.removeTextChangedListener(this);
                    String cleanString = s.toString().replaceAll("[Rp,.]", "");
                    if (!cleanString.isEmpty()) {
                        double parsed = Double.parseDouble(cleanString);
                        Locale localeID = new Locale("in", "ID");
                        String formatted = NumberFormat.getCurrencyInstance(localeID).format((parsed));
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            formatted = formatted.replaceAll(",00", "");
                        }
                        formatted = Tools.convertCurrencyToValue(formatted);
                        result = formatted;
                        binding.etAmount.setText(formatted);
                        binding.etAmount.setSelection(formatted.length());
                    }

                    binding.etAmount.addTextChangedListener(this);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();

        wlp.gravity = Gravity.CENTER;
        wlp.flags &= ~WindowManager.LayoutParams.FLAG_BLUR_BEHIND;
        window.setAttributes(wlp);
        dialog.getWindow().setLayout(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

        binding.btnSubmit.setOnClickListener(v -> {
            boolean checkSymbol = result.length() > 0 && Tools.isSpecialCharacterInMyString(result);
            if (checkSymbol) {
                Toast.makeText(context, "Mohon untuk di isi dengan benar!", Toast.LENGTH_SHORT).show();
                return;
            }

            dismiss();
            dialogInterfaceCallback.onSubmit(Tools.replaceStringNumberFormat(result));
        });
    }

    private void resultToNumberFormat() {
        binding.etAmount.setText(result);
    }

    private void clearCalculate() {
        result = "0";
        binding.etAmount.setText("0");
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
