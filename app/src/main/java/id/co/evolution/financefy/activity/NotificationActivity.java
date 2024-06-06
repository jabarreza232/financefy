package id.co.evolution.financefy.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import id.co.evolution.financefy.MainActivity;
import id.co.evolution.financefy.R;
import id.co.evolution.financefy.databinding.ActivityNotificationBinding;
import id.co.evolution.financefy.helper.FinanceFilter;
import id.co.evolution.financefy.helper.HelperNotification;
import id.co.evolution.financefy.helper.ReminderBroadcast;
import id.co.evolution.financefy.helper.SavingsFilter;
import id.co.evolution.financefy.helper.TinyDb;
import id.co.evolution.financefy.helper.Tools;
import id.co.evolution.financefy.model.ModelFinance;
import id.co.evolution.financefy.model.ModelNotification;
import id.co.evolution.financefy.model.ModelPrimaryColor;
import id.co.evolution.financefy.model.ModelSavings;
import id.co.evolution.financefy.model.ModelSavingsProgress;
import id.co.evolution.financefy.model.ModelUser;
import id.co.evolution.financefy.repository.FinanceRepository;
import id.co.evolution.financefy.repository.SavingsProgressRepository;
import id.co.evolution.financefy.repository.SavingsRepository;
import id.co.evolution.financefy.viewmodel.ViewModelFinance;
import id.co.evolution.financefy.viewmodel.ViewModelSavings;
import id.co.evolution.financefy.viewmodel.ViewModelSavingsProgress;

@AndroidEntryPoint
public class NotificationActivity extends AppCompatActivity {
    ActivityNotificationBinding mBinding;
    ModelPrimaryColor modelPrimaryColor=Tools.modelPrimaryColor;
    public ViewModelFinance viewModelFinance;
    public ViewModelSavings viewModelSavings;
    public ViewModelSavingsProgress viewModelSavingsProgress;
    @Inject
    FinanceRepository financeRepository;
    @Inject
    SavingsRepository savingsRepository;
    @Inject
    SavingsProgressRepository savingsProgressRepository;
    public List<ModelFinance> dataFinance = new ArrayList<>();
    public List<ModelSavingsProgress> dataSaving = new ArrayList<>();
    public ArrayList<Object> dataNotification = new ArrayList<>();
    public ModelUser user;
    public ModelSavings modelSavings = new ModelSavings();

    @Inject
    TinyDb tinyDb;
    Locale locale;
    HelperNotification helperNotification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(tinyDb.getObject("model_primary_color", ModelPrimaryColor.class)!=null) {
            modelPrimaryColor= tinyDb.getObject("model_primary_color",ModelPrimaryColor.class);
            Tools.setThemeActivity(getTheme(),modelPrimaryColor);
        }
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_notification);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        viewModelFinance = new ViewModelProvider(this).get(ViewModelFinance.class);
        viewModelSavings = new ViewModelProvider(this).get(ViewModelSavings.class);
        viewModelSavingsProgress = new ViewModelProvider(this).get(ViewModelSavingsProgress.class);

        viewModelFinance.init(financeRepository);
        viewModelSavingsProgress.init(savingsProgressRepository);
        helperNotification=new HelperNotification(this);
        user = tinyDb.getObject("user", ModelUser.class);
        modelSavings = tinyDb.getObject("savings", ModelSavings.class);

        locale = user.getType_currency().equalsIgnoreCase("IDR") ? Tools.getLocaleIDN() : Tools.getLocaleUS();

        viewModelFinance.getFinanceByUserId(user.getId(), user.getType_currency()).observe(this, modelFinances -> {
            dataFinance = modelFinances;
            boolean isChecked = tinyDb.getBoolean("isCheckedFinance");
            mBinding.switchNotificationFinance.setChecked(isChecked);
        });
        if(modelSavings==null)
            modelSavings = new ModelSavings();

        viewModelSavingsProgress.findAllSavingsByIdSavings(modelSavings.getId(), modelSavings.getType_currency()).observe(this, dataSavings -> {
            dataSaving = dataSavings;
            boolean isChecked = tinyDb.getBoolean("isCheckedSavings");
            mBinding.switchNotificationSavings.setChecked(isChecked);
        });


        mBinding.switchNotificationFinance.setOnClickListener(view -> {
            boolean isChecked = mBinding.switchNotificationFinance.isChecked();
            tinyDb.putBoolean("isCheckedFinance", isChecked);
            String description = "Data pemasukan anda hari ini :" + Tools.convertToCurrency(new FinanceFilter().totalIncome(dataFinance), locale) + "\n" +
                    "Data pengeluaran anda hari ini :" + Tools.convertToCurrency(new FinanceFilter().totalExpense(dataFinance), locale);
            ModelNotification modelNotification =new ModelNotification("Pengingat Pemasukan & Pengeluaran: " + user.getName(), description);

            helperNotification.reminderSet(isChecked,modelNotification,getString(R.string.jurnal_keuangan),200);
        });


        mBinding.switchNotificationSavings.setOnClickListener(view -> {
            boolean isChecked = mBinding.switchNotificationSavings.isChecked();
            tinyDb.putBoolean("isCheckedSavings", isChecked);
            ModelNotification modelNotification =new ModelNotification("Pengingat Progress Menabung: " + user.getName(),"Progress menabung anda hari ini: " + Tools.convertToCurrency(new SavingsFilter().totalValueByType(dataSaving), locale));
            helperNotification.reminderSet(isChecked,modelNotification,getString(R.string.menabung),100);
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            Intent i = new Intent(this, MainActivity.class);
            i.putExtra("isInputPin", true);
            startActivity(i);
        }
        return super.onOptionsItemSelected(item);
    }
}