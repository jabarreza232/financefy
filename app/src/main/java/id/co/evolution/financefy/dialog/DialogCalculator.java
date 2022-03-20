package id.co.evolution.financefy.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import id.co.evolution.financefy.R;
import id.co.evolution.financefy.adapter.AdapterCalculator;
import id.co.evolution.financefy.dummy.DummyCalculator;

public class DialogCalculator {
    Dialog dialog;
    Context context;
    LayoutInflater inflater;
    View dialogView;
    DialogInterfaceCallback dialogInterfaceCallback;

    public DialogCalculator(Context context, LayoutInflater layoutInflater, DialogInterfaceCallback dialogInterfaceCallback) {
        this.context = context;
        this.inflater = layoutInflater;
        this.dialogInterfaceCallback = dialogInterfaceCallback;
        initCalculator();
    }

    public void initCalculator() {
        String result = "0";
        dialog = new Dialog(context);
        dialogView = inflater.inflate(R.layout.dialog_calculator, null);
        dialog.setContentView(dialogView);
        RecyclerView rvCalculator = dialogView.findViewById(R.id.rv_calculator);
        TextView btnSubmit = dialogView.findViewById(R.id.txt_submit);
        AdapterCalculator adapterCalculator = new AdapterCalculator(context, DummyCalculator.getDataCalculator(), (data, position) -> {

        });
        rvCalculator.setLayoutManager(new GridLayoutManager(context, 4));
        rvCalculator.setAdapter(adapterCalculator);
        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();

        wlp.gravity = Gravity.CENTER;
        wlp.flags &= ~WindowManager.LayoutParams.FLAG_BLUR_BEHIND;
        window.setAttributes(wlp);
        dialog.getWindow().setLayout(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

        btnSubmit.setOnClickListener(v -> {
            dismiss();
            dialogInterfaceCallback.onSubmit(result);
        });
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
