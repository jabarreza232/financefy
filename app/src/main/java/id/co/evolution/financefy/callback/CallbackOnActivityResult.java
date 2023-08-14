package id.co.evolution.financefy.callback;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.ActivityResultRegistry;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import java.util.List;

import id.co.evolution.financefy.MainActivity;
import id.co.evolution.financefy.activity.UpdateFinanceActivity;
import id.co.evolution.financefy.activity.UpdateSavingsActivity;
import id.co.evolution.financefy.activity.UpdateSavingsTargetActivity;
import id.co.evolution.financefy.model.ModelFinance;
import id.co.evolution.financefy.model.ModelSavings;
import id.co.evolution.financefy.model.ModelSavingsProgress;
import id.co.evolution.financefy.model.ModelUser;

public class CallbackOnActivityResult implements DefaultLifecycleObserver {
    private final ActivityResultRegistry mRegistry;
    private ActivityResultLauncher<Intent> mStartForResult;
    private final Context mContext;
    private final OnCallbackResult mOnCallbackResult;
   public static int REQUEST_CODE_FINANCE = 3, REQUEST_CODE_CREATE_PROGRESS_SAVINGS = 4, REQUEST_CODE_SAVINGS = 5, REQUEST_CODE_UPDATE_SAVINGS_TARGET=6;

    public CallbackOnActivityResult(Context context, @NonNull ActivityResultRegistry registry, OnCallbackResult onCallbackResult) {
        mRegistry = registry;
        mContext = context;
        mOnCallbackResult = onCallbackResult;
    }

    @Override
    public void onCreate(@NonNull LifecycleOwner owner) {
        mStartForResult = mRegistry.register("UpdateData", new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    Intent intent = result.getData();
                    // Handle the Intent
                    mOnCallbackResult.result(result,intent);
                });
    }

    public void updateDataFinance(List<ModelFinance> data, int position, ModelUser modelUser) {
        Intent i = new Intent(mContext, UpdateFinanceActivity.class);
        i.putExtra("id", data.get(position).getId());
        i.putExtra("position",position);
        i.putExtra("user",modelUser);
        mStartForResult.launch(i);
    }

    public void updateDataSavings(List<ModelSavingsProgress> data, int position, ModelSavings savings) {
        Intent i = new Intent(mContext, UpdateSavingsActivity.class);
        i.putExtra("id", data.get(position).getId());
        i.putExtra("position",position);
        i.putExtra("savings",savings);
        mStartForResult.launch(i);
    }

    public void updateDataSavingsTarget(List<ModelSavings> data, int position, ModelSavings savings) {
        Intent i = new Intent(mContext, UpdateSavingsTargetActivity.class);
        i.putExtra("id_user", data.get(position).getId_savings_user());
        i.putExtra("savings",savings);
        mStartForResult.launch(i);
    }

    public interface OnCallbackResult {
        void result(ActivityResult result, Intent intent);
    }
}
