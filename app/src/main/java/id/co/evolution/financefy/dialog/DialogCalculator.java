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
import id.co.evolution.financefy.helper.TinyDb;
import id.co.evolution.financefy.helper.Tools;

public class DialogCalculator {
    Dialog dialog;
    Context context;
    LayoutInflater inflater;
    View dialogView;
    DialogInterfaceCallback dialogInterfaceCallback;
    String result = "0";
    DialogCalculatorBinding binding;
    Locale locale;
    int colorPrimary;
    public DialogCalculator(Context context, LayoutInflater layoutInflater, DialogInterfaceCallback dialogInterfaceCallback) {
        this.context = context;
        this.inflater = layoutInflater;
        this.dialogInterfaceCallback = dialogInterfaceCallback;
        initCalculator();
    }
    public DialogCalculator(Context context, int colorPrimary, LayoutInflater layoutInflater, DialogInterfaceCallback dialogInterfaceCallback) {
        this.context = context;
        this.inflater = layoutInflater;
        this.dialogInterfaceCallback = dialogInterfaceCallback;
        this.colorPrimary = colorPrimary;
        initCalculator();
    }
    public DialogCalculator(Context context, LayoutInflater layoutInflater, String result, DialogInterfaceCallback dialogInterfaceCallback) {
        this.context = context;
        this.inflater = layoutInflater;
        this.dialogInterfaceCallback = dialogInterfaceCallback;
        this.result = result;
        initCalculator();
    }
    public DialogCalculator(Context context, int colorPrimary, LayoutInflater layoutInflater, String result, DialogInterfaceCallback dialogInterfaceCallback) {
        this.context = context;
        this.inflater = layoutInflater;
        this.dialogInterfaceCallback = dialogInterfaceCallback;
        this.result = result;
        this.colorPrimary = colorPrimary;
        initCalculator();
    }

    public void initCalculator() {
        dialog = new Dialog(context);
        dialogView = inflater.inflate(R.layout.dialog_calculator, null);
        dialog.setContentView(dialogView);
        binding = DataBindingUtil.bind(dialogView);
        if(!result.equals("0"))
        binding.etAmount.setText(result);
        TinyDb tinyDb = new TinyDb(context);
        locale =tinyDb.getString("currency").equalsIgnoreCase("IDR")? Tools.getLocaleIDN():Tools.getLocaleUS();
        binding.btnSubmit.setBackgroundColor(colorPrimary);
        AdapterCalculator adapterCalculator = new AdapterCalculator(context, DummyCalculator.getDataCalculator(), (data, position) -> {
            List<String> dataList = (List<String>) data;
            String input = dataList.get(position);

            // 1. Jika result masih "0", dan tombol yang ditekan adalah angka (0-9), hapus "0" di awal
            if (result.equals("0") && input.matches("[0-9]")) {
                result = "";
            }

            // 2. Cek apakah karakter terakhir adalah operator (+, -, /, x)
            boolean hasTrailingOperator = result.length() > 0 && Tools.isSpecialCharacterInMyString(Tools.getLastChar(result));

            // 3. LOGIKA UTAMA (Tanpa Switch-Case Panjang)
            if (input.matches("[0-9]")) {
                // --- JIKA TOMBOL ANGKA (0-9) ---
                result += input;

            } else if (input.equals("C")) {
                // --- JIKA TOMBOL CLEAR ---
                clearCalculate();
                // Pastikan clearCalculate() mereset result menjadi "0"

            } else if (input.matches("[+\\-x/]")) {
                // --- JIKA TOMBOL OPERATOR (+, -, x, /) ---
                if (hasTrailingOperator) {
                    // Cegah penumpukan operator (misal "15++"). Ganti operator terakhir
                    result = Tools.removeLastChar(result);
                }
                result += input;

            } else if (input.equals("=")) {
                // --- JIKA TOMBOL SAMA DENGAN (=) ---
                if (hasTrailingOperator) {
                    // Hapus operator di akhir jika = ditekan prematur (misal "1500+" menjadi "1500")
                    result = Tools.removeLastChar(result);
                }

                try {
                    // Evaluasi string matematika (Ganti 'x' dengan '*' agar standar)
                    String evaluableString = result.replace("x", "*");

                    // Panggil fungsi penilai matematika
                    double mathResult = evaluateMathExpression(evaluableString);

                    // Cek apakah hasilnya bilangan bulat (misal 15000.0)
                    if (mathResult == (long) mathResult) {
                        result = String.valueOf((long) mathResult); // Tampilkan tanpa .0
                    } else {
                        result = String.valueOf(mathResult); // Tampilkan desimal (misal 2.5)
                    }
                } catch (Exception e) {
                    Log.e("Calculator", "Error memproses hitungan", e);
                }
            }

            // Format kembali ke bentuk Rupiah/Ribuan
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
        binding.btnDelete.setOnLongClickListener(v->{
            clearCalculate();
            return false;
        });
        binding.imgClose.setOnClickListener(v->{
            dialog.dismiss();
        });
        binding.etAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().equals(result)) {
                    binding.etAmount.removeTextChangedListener(this);
                    String cleanString = s.toString().replaceAll("[Rp,.$]", "");
                    if (!cleanString.isEmpty()) {
                        double parsed = Double.parseDouble(cleanString);
                        String formatted = Tools.convertToCurrency(parsed,locale);
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
    public static double evaluateMathExpression(final String str) {
        return new Object() {
            int pos = -1, ch;

            void nextChar() {
                ch = (++pos < str.length()) ? str.charAt(pos) : -1;
            }

            boolean eat(int charToEat) {
                while (ch == ' ') nextChar();
                if (ch == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }

            double parse() {
                nextChar();
                double x = parseExpression();
                if (pos < str.length()) throw new RuntimeException("Karakter aneh: " + (char)ch);
                return x;
            }

            double parseExpression() {
                double x = parseTerm();
                for (;;) {
                    if      (eat('+')) x += parseTerm(); // Tambah
                    else if (eat('-')) x -= parseTerm(); // Kurang
                    else return x;
                }
            }

            double parseTerm() {
                double x = parseFactor();
                for (;;) {
                    if      (eat('*')) x *= parseFactor(); // Kali
                    else if (eat('/')) x /= parseFactor(); // Bagi
                    else return x;
                }
            }

            double parseFactor() {
                if (eat('+')) return parseFactor(); // Unary plus
                if (eat('-')) return -parseFactor(); // Unary minus

                double x;
                int startPos = this.pos;
                if (eat('(')) {
                    x = parseExpression();
                    eat(')');
                } else if ((ch >= '0' && ch <= '9') || ch == '.') { // Angka
                    while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                    x = Double.parseDouble(str.substring(startPos, this.pos));
                } else {
                    throw new RuntimeException("Karakter aneh: " + (char)ch);
                }
                return x;
            }
        }.parse();
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
