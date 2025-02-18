package id.co.evolution.financefy.activity;

import static id.co.evolution.financefy.helper.Tools.getFormattedDateSimple;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
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
    ModelPrimaryColor modelPrimaryColor = Tools.modelPrimaryColor;
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

    public ModelUser user;
    public ModelSavings modelSavings = new ModelSavings();

    @Inject
    TinyDb tinyDb;
    Locale locale;
    HelperNotification helperNotification;
    Button activeButton;
    boolean isDailyNotification;
    String descriptionFinance, descriptionSavings;
    boolean isCheckedFinance;
    boolean isCheckedSavings;
    private static final int NOTIFICATION_PERMISSION_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (tinyDb.getObject("model_primary_color", ModelPrimaryColor.class) != null) {
            modelPrimaryColor = tinyDb.getObject("model_primary_color", ModelPrimaryColor.class);
            Tools.setThemeActivity(getTheme(), modelPrimaryColor);
        }
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_notification);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        viewModelFinance = new ViewModelProvider(this).get(ViewModelFinance.class);
        viewModelSavings = new ViewModelProvider(this).get(ViewModelSavings.class);
        viewModelSavingsProgress = new ViewModelProvider(this).get(ViewModelSavingsProgress.class);

        viewModelFinance.init(financeRepository);
        viewModelSavingsProgress.init(savingsProgressRepository);
        helperNotification = new HelperNotification(this);
        user = tinyDb.getObject("user", ModelUser.class);
        modelSavings = tinyDb.getObject("savings", ModelSavings.class);
        String notif=tinyDb.getString("time_notification");
        locale = user.getType_currency().equalsIgnoreCase("IDR") ? Tools.getLocaleIDN() : Tools.getLocaleUS();
        isCheckedFinance= tinyDb.getBoolean("isCheckedFinance");
        isCheckedSavings= tinyDb.getBoolean("isCheckedSavings");
        mBinding.switchNotificationFinance.setOnClickListener(view -> {
            boolean  isCheckedSavings = tinyDb.getBoolean("isCheckedSavings");
            boolean isChecked = mBinding.switchNotificationFinance.isChecked();
            tinyDb.putBoolean("isCheckedFinance", isChecked);
            ModelNotification modelNotification = new ModelNotification("Pengingat Pemasukan & Pengeluaran: " + user.getName(), descriptionFinance);
            helperNotification.reminderSet(isChecked, modelNotification, getString(R.string.jurnal_keuangan), 200);
            if(isChecked) Toast.makeText(this, "Notifikasi berhasil di setting!", Toast.LENGTH_SHORT).show();
            if(!isCheckedSavings&&!isChecked){
                tinyDb.putString("time_notification", "");
                mBinding.buttonMonthly.setSelected(false);
                mBinding.buttonDaily.setSelected(false);
                mBinding.switchNotificationFinance.setEnabled(false);
                mBinding.switchNotificationSavings.setEnabled(false);
            }
        });


        mBinding.switchNotificationSavings.setOnClickListener(view -> {
            boolean isCheckedFinance = tinyDb.getBoolean("isCheckedFinance");
            boolean isChecked = mBinding.switchNotificationSavings.isChecked();
            tinyDb.putBoolean("isCheckedSavings", isChecked);
            ModelNotification modelNotification = new ModelNotification("Pengingat Progress Menabung: " + user.getName(), descriptionSavings);
            helperNotification.reminderSet(isChecked, modelNotification, getString(R.string.menabung), 100);
            if(isChecked)  Toast.makeText(this, "Notifikasi berhasil di setting!", Toast.LENGTH_SHORT).show();
            if(!isCheckedFinance&&!isChecked){
                tinyDb.putString("time_notification", "");
                mBinding.buttonMonthly.setSelected(false);
                mBinding.buttonDaily.setSelected(false);
                mBinding.switchNotificationFinance.setEnabled(false);
                mBinding.switchNotificationSavings.setEnabled(false);
            }
        });

        mBinding.buttonDaily.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setActiveButton((Button) v);
                tinyDb.putString("time_notification", "daily");
                getNotification("daily");
                mBinding.switchNotificationFinance.setEnabled(true);
                mBinding.switchNotificationSavings.setEnabled(true);
            }
        });

        mBinding.buttonMonthly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setActiveButton((Button) v);
                tinyDb.putString("time_notification", "monthly");
                getNotification("monthly");
                mBinding.switchNotificationFinance.setEnabled(true);
                mBinding.switchNotificationSavings.setEnabled(true);
            }
        });
        if(!notif.isEmpty()){
            isDailyNotification = tinyDb.getString("time_notification").equalsIgnoreCase("daily");

            if (isDailyNotification) {
                mBinding.buttonDaily.setSelected(true);
                mBinding.buttonMonthly.setSelected(false);
                descriptionFinance = "Data pemasukan anda hari ini :" + Tools.convertToCurrency(new FinanceFilter().totalIncome(dataFinance), locale) + "\n" +
                        "Data pengeluaran anda hari ini :" + Tools.convertToCurrency(new FinanceFilter().totalExpense(dataFinance), locale);

                descriptionSavings = "Progress menabung anda hingga hari ini: " + Tools.convertToCurrency(new SavingsFilter().totalValueByType(dataSaving), locale);
            } else {
                mBinding.buttonMonthly.setSelected(true);
                mBinding.buttonDaily.setSelected(false);
                descriptionFinance = "Data pemasukan anda bulan ini :" + Tools.convertToCurrency(new FinanceFilter().totalIncome(dataFinance), locale) + "\n" +
                        "Data pengeluaran anda bulan ini :" + Tools.convertToCurrency(new FinanceFilter().totalExpense(dataFinance), locale);

                descriptionSavings = "Progress menabung anda bulan ini: " + Tools.convertToCurrency(new SavingsFilter().totalValueByType(dataSaving), locale);
            }

            mBinding.switchNotificationFinance.setChecked(isCheckedFinance);


            mBinding.switchNotificationSavings.setChecked(isCheckedSavings);
        }else{
            mBinding.switchNotificationFinance.setEnabled(false);
            mBinding.switchNotificationSavings.setEnabled(false);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Izin diberikan, lanjutkan dengan menampilkan notifikasi

            } else {
                // Izin ditolak, beri tahu pengguna
            }
        }
    }

    private void getNotification(String time) {
        if (time.equalsIgnoreCase("daily")) {
            activeButton = mBinding.buttonMonthly;
            setActiveButton(mBinding.buttonDaily);
            viewModelFinance.getAllFinanceByDate(getFormattedDateSimple(System.currentTimeMillis()), user.getId(), user.getType_currency()).observe(this, modelFinances -> {
                dataFinance = modelFinances;
            });
            if (modelSavings == null)
                modelSavings = new ModelSavings();

            viewModelSavingsProgress.findAllSavingsByDate(getFormattedDateSimple(System.currentTimeMillis()), modelSavings.getId(), modelSavings.getType_currency()).observe(this, dataSavings -> {
                dataSaving = dataSavings;
            });
        } else {
            activeButton = mBinding.buttonDaily;

            setActiveButton(mBinding.buttonMonthly);

            viewModelFinance.getFinanceByMonth(Tools.getFormattedMonthSimple(System.currentTimeMillis()), user.getId(), user.getType_currency()).observe(this, modelFinances -> {
                dataFinance = modelFinances;
            });
            if (modelSavings == null)
                modelSavings = new ModelSavings();

            viewModelSavingsProgress.getSavingsByMonth(Tools.getFormattedMonthSimple(System.currentTimeMillis()), modelSavings.getId(), modelSavings.getType_currency()).observe(this, dataSavings -> {
                dataSaving = dataSavings;

            });
        }
    }

    private void setActiveButton(Button selectedButton) {
        // Reset the previous active button
        if (activeButton != null) {
            activeButton.setSelected(false);
        }
        // Set the new active button
        selectedButton.setSelected(true);
        activeButton = selectedButton;
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