package id.co.evolution.financefy.callback;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.ActivityResultRegistry;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import com.google.gson.Gson;

import java.util.List;

import id.co.evolution.financefy.activity.UpdateFinance;
import id.co.evolution.financefy.model.ModelFinance;

public class CallbackOnActivityResult implements DefaultLifecycleObserver {
    private final ActivityResultRegistry mRegistry;
    private ActivityResultLauncher<Intent> mStartForResult;
    private final Context mContext;
    private final OnCallbackResult mOnCallbackResult;

    public CallbackOnActivityResult(Context context, @NonNull ActivityResultRegistry registry, OnCallbackResult onCallbackResult) {
        mRegistry = registry;
        mContext = context;
        mOnCallbackResult = onCallbackResult;
    }

    @Override
    public void onCreate(@NonNull LifecycleOwner owner) {
        mStartForResult = mRegistry.register("UpdateData", new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent intent = result.getData();
                        // Handle the Intent
                         mOnCallbackResult.result(intent);
                    }
                });
    }

    public void updateDataFinance(List<ModelFinance> data, int position) {
        Intent i = new Intent(mContext, UpdateFinance.class);
        i.putExtra("id", data.get(position).getId());
        i.putExtra("position",position);
        mStartForResult.launch(i);
    }

    public interface OnCallbackResult {
        void result(Intent intent);
    }
}
