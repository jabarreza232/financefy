package id.co.evolution.financefy.activity;

import static id.co.evolution.financefy.helper.Tools.getFormattedDateSimple;

import android.Manifest;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.NumberPicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import id.co.evolution.financefy.MainActivity;
import id.co.evolution.financefy.R;
import id.co.evolution.financefy.databinding.ActivityNotificationBinding;
import id.co.evolution.financefy.helper.FinanceFilter;
import id.co.evolution.financefy.helper.HelperNotification;
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
    String descriptionFinance = "", descriptionSavings = "";
    boolean isCheckedFinance;
    boolean isCheckedSavings;
    private static final int NOTIFICATION_PERMISSION_CODE = 101;

    int selectedHour = 8;
    int selectedMinute = 0;
    int selectedDay = 1;
    boolean isDailyMode = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean isCustomActive = tinyDb.getBoolean("is_custom_color_active");

        if (isCustomActive) {
            int customColor = tinyDb.getInt("custom_color_int");
            getWindow().setStatusBarColor(customColor);
        } else {
            if (tinyDb.getObject("model_primary_color", ModelPrimaryColor.class) != null) {
                modelPrimaryColor = tinyDb.getObject("model_primary_color", ModelPrimaryColor.class);
                Tools.setThemeActivity(getTheme(), modelPrimaryColor);
            }
        }
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_notification);

        if (isCustomActive) {
            int customColor = tinyDb.getInt("custom_color_int");
            if (getSupportActionBar() != null) {
                getSupportActionBar().setBackgroundDrawable(new ColorDrawable(customColor));
            }
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        viewModelFinance = new ViewModelProvider(this).get(ViewModelFinance.class);
        viewModelSavings = new ViewModelProvider(this).get(ViewModelSavings.class);
        viewModelSavingsProgress = new ViewModelProvider(this).get(ViewModelSavingsProgress.class);

        viewModelFinance.init(financeRepository);
        viewModelSavingsProgress.init(savingsProgressRepository);
        helperNotification = new HelperNotification(this);
        user = tinyDb.getObject("user", ModelUser.class);
        modelSavings = tinyDb.getObject("savings", ModelSavings.class);
        if (user != null) {
            locale = user.getType_currency().equalsIgnoreCase("IDR") ? Tools.getLocaleIDN() : Tools.getLocaleUS();
        } else {
            locale = Tools.getLocaleIDN();
        }

        selectedHour = tinyDb.getInt("notif_hour", 8);
        selectedMinute = tinyDb.getInt("notif_minute", 0);
        selectedDay = tinyDb.getInt("notif_day", 1);

        String notif = tinyDb.getString("time_notification");
        isCheckedFinance = tinyDb.getBoolean("isCheckedFinance");
        isCheckedSavings = tinyDb.getBoolean("isCheckedSavings");

        mBinding.cardTimeDatePicker.setOnClickListener(v -> {
            if (isDailyMode) {
                showTimePicker();
            } else {
                showDayPicker();
            }
        });

        mBinding.cardDaily.setOnClickListener(v -> {
            tinyDb.putString("time_notification", "daily");
            setFrequencySelection(true);
            getNotification("daily");
            mBinding.switchNotificationFinance.setEnabled(true);
            mBinding.switchNotificationSavings.setEnabled(true);
        });

        mBinding.cardMonthly.setOnClickListener(v -> {
            tinyDb.putString("time_notification", "monthly");
            setFrequencySelection(false);
            getNotification("monthly");
            mBinding.switchNotificationFinance.setEnabled(true);
            mBinding.switchNotificationSavings.setEnabled(true);
        });

        mBinding.switchNotificationFinance.setOnClickListener(view -> {
            boolean isChecked = mBinding.switchNotificationFinance.isChecked();
            tinyDb.putBoolean("isCheckedFinance", isChecked);
            isCheckedFinance = isChecked;

            ModelNotification modelNotification = new ModelNotification(
                    "Pengingat Pemasukan & Pengeluaran: " + (user != null ? user.getName() : ""),
                    descriptionFinance
            );
            helperNotification.reminderSet(isChecked, modelNotification, getString(R.string.jurnal_keuangan), 200);

            if (isChecked) {
                Toast.makeText(this, "Notifikasi Keuangan diaktifkan!", Toast.LENGTH_SHORT).show();
            }

            boolean isSavingsOn = tinyDb.getBoolean("isCheckedSavings");
            if (!isSavingsOn && !isChecked) {
                tinyDb.putString("time_notification", "");
                mBinding.switchNotificationFinance.setEnabled(false);
                mBinding.switchNotificationSavings.setEnabled(false);
            }
        });

        mBinding.switchNotificationSavings.setOnClickListener(view -> {
            boolean isChecked = mBinding.switchNotificationSavings.isChecked();
            tinyDb.putBoolean("isCheckedSavings", isChecked);
            isCheckedSavings = isChecked;

            ModelNotification modelNotification = new ModelNotification(
                    "Pengingat Progress Menabung: " + (user != null ? user.getName() : ""),
                    descriptionSavings
            );
            helperNotification.reminderSet(isChecked, modelNotification, getString(R.string.menabung), 100);

            if (isChecked) {
                Toast.makeText(this, "Notifikasi Tabungan diaktifkan!", Toast.LENGTH_SHORT).show();
            }

            boolean isFinanceOn = tinyDb.getBoolean("isCheckedFinance");
            if (!isFinanceOn && !isChecked) {
                tinyDb.putString("time_notification", "");
                mBinding.switchNotificationFinance.setEnabled(false);
                mBinding.switchNotificationSavings.setEnabled(false);
            }
        });

        if (!notif.isEmpty()) {
            boolean isDaily = notif.equalsIgnoreCase("daily");
            setFrequencySelection(isDaily);
            getNotification(notif);

            mBinding.switchNotificationFinance.setEnabled(true);
            mBinding.switchNotificationSavings.setEnabled(true);
            mBinding.switchNotificationFinance.setChecked(isCheckedFinance);
            mBinding.switchNotificationSavings.setChecked(isCheckedSavings);
        } else {
            setFrequencySelection(true);
            mBinding.switchNotificationFinance.setEnabled(false);
            mBinding.switchNotificationSavings.setEnabled(false);
        }
    }

    private void setFrequencySelection(boolean isDaily) {
        this.isDailyMode = isDaily;
        updateFrequencyUI(isDaily);

        if (isDaily) {
            mBinding.imgTimeDate.setImageResource(R.drawable.ic_access_time);
            mBinding.txtTimeDateTitle.setText("Jam Pengingat");
            mBinding.txtTimeDateValue.setText(String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute));
        } else {
            mBinding.imgTimeDate.setImageResource(R.drawable.baseline_calendar_today_24);
            mBinding.txtTimeDateTitle.setText("Tanggal Pengingat");
            mBinding.txtTimeDateValue.setText("Tgl " + selectedDay);
        }
    }

    private void showTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            selectedHour = hourOfDay;
            selectedMinute = minute;
            tinyDb.putInt("notif_hour", selectedHour);
            tinyDb.putInt("notif_minute", selectedMinute);

            mBinding.txtTimeDateValue.setText(String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute));
            updateActiveAlarms();
        }, selectedHour, selectedMinute, true);

        timePickerDialog.show();
    }

    private void showDayPicker() {
        NumberPicker numberPicker = new NumberPicker(this);
        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(31);
        numberPicker.setValue(selectedDay);

        new AlertDialog.Builder(this)
                .setTitle("Pilih Tanggal Pengingat")
                .setMessage("Notifikasi akan muncul setiap bulan pada tanggal ini.")
                .setView(numberPicker)
                .setPositiveButton("Simpan", (dialog, which) -> {
                    selectedDay = numberPicker.getValue();
                    tinyDb.putInt("notif_day", selectedDay);

                    mBinding.txtTimeDateValue.setText("Tgl " + selectedDay);
                    updateActiveAlarms();
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void updateActiveAlarms() {
        if (isCheckedFinance) {
            ModelNotification modelNotification = new ModelNotification(
                    "Pengingat Pemasukan & Pengeluaran: " + (user != null ? user.getName() : ""),
                    descriptionFinance
            );
            helperNotification.reminderSet(true, modelNotification, getString(R.string.jurnal_keuangan), 200);
        }
        if (isCheckedSavings) {
            ModelNotification modelNotification = new ModelNotification(
                    "Pengingat Progress Menabung: " + (user != null ? user.getName() : ""),
                    descriptionSavings
            );
            helperNotification.reminderSet(true, modelNotification, getString(R.string.menabung), 100);
        }
    }

    private void updateFrequencyUI(boolean isDaily) {
        int activeColor;
        boolean isCustomActive = tinyDb.getBoolean("is_custom_color_active");
        if (isCustomActive) {
            activeColor = tinyDb.getInt("custom_color_int");
        } else {
            activeColor = ContextCompat.getColor(this, modelPrimaryColor.getColorPrimary());
        }

        int inactiveContentColor = ContextCompat.getColor(this, R.color.colorDefaultText);
        int whiteColor = ContextCompat.getColor(this, R.color.white);
        int transparentColor = ContextCompat.getColor(this, android.R.color.transparent);
        int strokeWidth = Math.round(1 * getResources().getDisplayMetrics().density);

        if (isDaily) {
            // Card Daily ACTIVE
            mBinding.cardDaily.setCardBackgroundColor(activeColor);
            mBinding.cardDaily.setStrokeWidth(0);
            mBinding.imgDaily.setColorFilter(whiteColor);
            mBinding.txtDaily.setTextColor(whiteColor);

            // Card Monthly INACTIVE
            mBinding.cardMonthly.setCardBackgroundColor(transparentColor);
            mBinding.cardMonthly.setStrokeWidth(strokeWidth);
            mBinding.cardMonthly.setStrokeColor(inactiveContentColor);
            mBinding.imgMonthly.setColorFilter(inactiveContentColor);
            mBinding.txtMonthly.setTextColor(inactiveContentColor);
        } else {
            // Card Monthly ACTIVE
            mBinding.cardMonthly.setCardBackgroundColor(activeColor);
            mBinding.cardMonthly.setStrokeWidth(0);
            mBinding.imgMonthly.setColorFilter(whiteColor);
            mBinding.txtMonthly.setTextColor(whiteColor);

            // Card Daily INACTIVE
            mBinding.cardDaily.setCardBackgroundColor(transparentColor);
            mBinding.cardDaily.setStrokeWidth(strokeWidth);
            mBinding.cardDaily.setStrokeColor(inactiveContentColor);
            mBinding.imgDaily.setColorFilter(inactiveContentColor);
            mBinding.txtDaily.setTextColor(inactiveContentColor);
        }
    }

    private void getNotification(String time) {
        if (user == null) return;

        if (time.equalsIgnoreCase("daily")) {
            viewModelFinance.getAllFinanceByDate(getFormattedDateSimple(System.currentTimeMillis()), user.getId(), user.getType_currency()).observe(this, modelFinances -> {
                dataFinance = modelFinances;
                descriptionFinance = "Data pemasukan anda hari ini :" + Tools.convertToCurrency(new FinanceFilter().totalIncome(dataFinance), locale) + "\n" +
                        "Data pengeluaran anda hari ini :" + Tools.convertToCurrency(new FinanceFilter().totalExpense(dataFinance), locale);

                if (isCheckedFinance) {
                    ModelNotification modelNotification = new ModelNotification("Pengingat Pemasukan & Pengeluaran: " + user.getName(), descriptionFinance);
                    helperNotification.reminderSet(true, modelNotification, getString(R.string.jurnal_keuangan), 200);
                }
            });

            if (modelSavings == null) modelSavings = new ModelSavings();

            viewModelSavingsProgress.findAllSavingsByDate(getFormattedDateSimple(System.currentTimeMillis()), modelSavings.getId(), modelSavings.getType_currency()).observe(this, dataSavings -> {
                dataSaving = dataSavings;
                descriptionSavings = "Progress menabung anda hingga hari ini: " + Tools.convertToCurrency(new SavingsFilter().totalValueByType(dataSaving), locale);

                if (isCheckedSavings) {
                    ModelNotification modelNotification = new ModelNotification("Pengingat Progress Menabung: " + user.getName(), descriptionSavings);
                    helperNotification.reminderSet(true, modelNotification, getString(R.string.menabung), 100);
                }
            });
        } else {
            viewModelFinance.getFinanceByMonth(Tools.getFormattedMonthSimple(System.currentTimeMillis()), user.getId(), user.getType_currency()).observe(this, modelFinances -> {
                dataFinance = modelFinances;
                descriptionFinance = "Data pemasukan anda bulan ini :" + Tools.convertToCurrency(new FinanceFilter().totalIncome(dataFinance), locale) + "\n" +
                        "Data pengeluaran anda bulan ini :" + Tools.convertToCurrency(new FinanceFilter().totalExpense(dataFinance), locale);

                if (isCheckedFinance) {
                    ModelNotification modelNotification = new ModelNotification("Pengingat Pemasukan & Pengeluaran: " + user.getName(), descriptionFinance);
                    helperNotification.reminderSet(true, modelNotification, getString(R.string.jurnal_keuangan), 200);
                }
            });

            if (modelSavings == null) modelSavings = new ModelSavings();

            viewModelSavingsProgress.getSavingsByMonth(Tools.getFormattedMonthSimple(System.currentTimeMillis()), modelSavings.getId(), modelSavings.getType_currency()).observe(this, dataSavings -> {
                dataSaving = dataSavings;
                descriptionSavings = "Progress menabung anda bulan ini: " + Tools.convertToCurrency(new SavingsFilter().totalValueByType(dataSaving), locale);

                if (isCheckedSavings) {
                    ModelNotification modelNotification = new ModelNotification("Pengingat Progress Menabung: " + user.getName(), descriptionSavings);
                    helperNotification.reminderSet(true, modelNotification, getString(R.string.menabung), 100);
                }
            });
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
                // Izin diberikan
            }
        }
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
