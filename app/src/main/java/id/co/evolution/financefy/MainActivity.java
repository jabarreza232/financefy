package id.co.evolution.financefy;


import static id.co.evolution.financefy.callback.CallbackOnActivityResult.REQUEST_CODE_FINANCE;
import static id.co.evolution.financefy.callback.CallbackOnActivityResult.REQUEST_CODE_SAVINGS;
import static id.co.evolution.financefy.helper.Tools.getObjectAnimator;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import id.co.evolution.financefy.activity.CreateFinanceActivity;
import id.co.evolution.financefy.activity.CreateSavingsProgressActivity;
import id.co.evolution.financefy.activity.CreateSavingsTargetActivity;
import id.co.evolution.financefy.activity.PinActivity;
import id.co.evolution.financefy.databinding.ActivityMainBinding;
import id.co.evolution.financefy.fragment.FragmentAccount;
import id.co.evolution.financefy.fragment.FragmentAll;
import id.co.evolution.financefy.fragment.FragmentAnalysis;
import id.co.evolution.financefy.fragment.FragmentAnalysisSavings;
import id.co.evolution.financefy.fragment.FragmentSettings;
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
import id.co.evolution.financefy.model.ModelUser;
import id.co.evolution.financefy.repository.FinanceRepository;
import id.co.evolution.financefy.repository.SavingsRepository;
import id.co.evolution.financefy.repository.UserRepository;
import id.co.evolution.financefy.viewmodel.ViewModelFinance;
import id.co.evolution.financefy.viewmodel.ViewModelSavings;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public ActivityMainBinding binding;
    public ViewModelFinance viewModelFinance;
    public ViewModelSavings viewModelSavings;
    public List<ModelFinance> dataFinance = new ArrayList<>();
    public ModelFinance modelFinance;
    AnimatorSet animatorSet = new AnimatorSet();

    @Inject
    FinanceRepository financeRepository;
    @Inject
    SavingsRepository savingsRepository;
    @Inject
    UserRepository userRepository;
    Locale locale;
    public ModelUser user;
    public ModelSavings modelSavings = new ModelSavings();

    @Inject
    TinyDb tinyDb;
    public boolean isFabOpen = false;
    public boolean isUserDailyFinance;
    public ModelPrimaryColor modelPrimaryColor = Tools.modelPrimaryColor;
    public HelperNotification helperNotification;
    public boolean isCheckedNotifSavings,isCheckedNotifFinance;
   public boolean isPinSetting;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        changeUINightMode();
        String type_currency = tinyDb.getString("currency");
         isPinSetting = tinyDb.getBoolean("isSettingPin");
        boolean isPinInput = getIntent().getBooleanExtra("isInputPin", false);

        if (isPinSetting && !isPinInput) {
            startActivity(new Intent(this, PinActivity.class));
        }

        if (tinyDb.getObject("model_primary_color", ModelPrimaryColor.class) != null) {
            modelPrimaryColor = tinyDb.getObject("model_primary_color", ModelPrimaryColor.class);
            Tools.setThemeActivity(getTheme(), modelPrimaryColor);
        }

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        viewModelFinance = new ViewModelProvider(this).get(ViewModelFinance.class);
        viewModelSavings = new ViewModelProvider(this).get(ViewModelSavings.class);
        viewModelFinance.init(financeRepository);
        viewModelSavings.init(savingsRepository);
        user = tinyDb.getObject("user", ModelUser.class);
        modelSavings = tinyDb.getObject("savings", ModelSavings.class);
        helperNotification=new HelperNotification(this);
        if(user!=null)
            locale =user.getType_currency().equalsIgnoreCase("IDR")? Tools.getLocaleIDN():Tools.getLocaleUS();

         isCheckedNotifSavings = tinyDb.getBoolean("isCheckedSavings");
         isCheckedNotifFinance = tinyDb.getBoolean("isCheckedFinance");


        if (user == null) {
            user = new ModelUser("Guest Account", "Pribadi", getString(R.string.jurnal_keuangan));
            user.setUuid(UUID.randomUUID().toString());

            if (type_currency == null || type_currency.isEmpty()) {
                tinyDb.putString("currency", "IDR");
                type_currency = "IDR";
                user.setType_currency(type_currency);
            }

            new UserRepository.InputUpdateUser(user, "create", userRepository.userDao)
                    .execute();

            tinyDb.putObject("user", user);
        }
        createNotificationChannel();
        if (user.getCategory().equalsIgnoreCase(getString(R.string.jurnal_keuangan))) {
            showHideFabSavings(false);

            viewModelFinance.getFinanceByUserId(user.getId(), user.getType_currency()).observe(this, modelFinances -> {
                dataFinance = modelFinances;
                setUpFragment();
            });
        } else {
            viewModelSavings.findAllSavingsByIdUser(user.getId(), user.getType_currency()).observe(this, dataSavings -> {
                if (dataSavings.size() == 0) {
                    modelSavings = new ModelSavings("Beli HP", 50_000_000, 10_000, user.getId(), "March 03, 2022");
                    tinyDb.putObject("savings", modelSavings);
                    viewModelSavings.inputUpdateSavings("create", modelSavings);
                } else {
                    for (ModelSavings savings : dataSavings)
                        modelSavings = savings;

                    tinyDb.putObject("savings", modelSavings);
                }
                setUpFragment();
            });
        }

        binding.layout.fabAdd.setOnClickListener(this);
        binding.layout.fabAddSavingsProgress.setOnClickListener(this);
        binding.layout.fabAddSavingsTarget.setOnClickListener(this);

        changeColorBottomNavigation(modelPrimaryColor);

    }

    private void createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Financefy Reminder Channel";
            String description = "Channel for Financefy Reminder";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel = new NotificationChannel(""+user.getId(), name, importance);
            notificationChannel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }

    private void changeUINightMode() {
        String nightMode = tinyDb.getString("night_mode");
        if (TextUtils.equals(nightMode, "mode_night_yes")) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    private void changeColorBottomNavigation(ModelPrimaryColor modelPrimaryColor) {
        int[][] states = new int[][]{
                new int[]{android.R.attr.state_checked}, // state_checked
                new int[]{}  //
        };

        int[] colors = new int[]{
                ContextCompat.getColor(this, modelPrimaryColor.getColorPrimary()),
                ContextCompat.getColor(this, R.color.colorGrey50)};
        ColorStateList dynamicColorList = new ColorStateList(states, colors);

        binding.layout.bnMain.setItemIconTintList(dynamicColorList);
        binding.layout.bnMain.setItemTextColor(dynamicColorList);
    }

    private void setUpFragment() {
        changeFragment(new FragmentAll());
        binding.layout.bnMain.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            item.setChecked(true);
            binding.layout.fabAdd.hide();
            showHideFabSavings(false);
            if (isFabOpen) startAnimationFabSavings();
            isUserDailyFinance = user.getCategory().equalsIgnoreCase(getString(R.string.jurnal_keuangan));

            switch (item.getTitle().toString().toLowerCase()) {
                case "records":
                    fragment = new FragmentAll();
                    changeFragment(fragment);
                    binding.layout.fabAdd.show();
                    getSupportActionBar().setTitle("Records");
                    break;
                case "analysis":
                    if (isUserDailyFinance)
                        fragment = new FragmentAnalysis();
                    else fragment = new FragmentAnalysisSavings();
                    getSupportActionBar().setTitle("Analysis");
                    changeFragment(fragment);
                    break;
                case "accounts":
                    getSupportActionBar().setTitle("Accounts");
                    fragment = new FragmentAccount();
                    changeFragment(fragment);
                    break;
                case "settings":
                    getSupportActionBar().setTitle("Settings");
                    fragment = new FragmentSettings();
                    changeFragment(fragment);
                    break;
                default:
                    break;
            }
            return false;
        });
    }

    private void showHideFabSavings(boolean isShow) {
        binding.layout.fabAddSavingsTarget.setVisibility(isShow ? View.VISIBLE : View.GONE);
        binding.layout.fabAddSavingsProgress.setVisibility(isShow ? View.VISIBLE : View.GONE);
    }

    private void changeFragment(Fragment fragment) {

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_frame, fragment, fragment.getClass().getSimpleName()).addToBackStack(null).commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE_FINANCE) {
                modelFinance = (ModelFinance) data.getSerializableExtra("finance");
                Log.e("TAG", "onActivityResult: " + new Gson().toJson((ModelFinance) data.getSerializableExtra("finance")));
                changeFragment(new FragmentAll());
            }
            if (requestCode == REQUEST_CODE_SAVINGS) {
                modelSavings = (ModelSavings) data.getSerializableExtra("savings");
                Log.e("TAG", "onActivityResult: " + new Gson().toJson((ModelSavings) data.getSerializableExtra("savings")));
                changeFragment(new FragmentAll());
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.fab_add) {


            if (user.getCategory().equalsIgnoreCase(getString(R.string.jurnal_keuangan))) {
                Log.e("TAG", "onClick: " + user.getId());
                Intent intent = new Intent(this, CreateFinanceActivity.class);
                intent.putExtra("user", user);
                startActivityForResult(intent, REQUEST_CODE_FINANCE);
            } else {
                startAnimationFabSavings();
            }
        }

        if (v.getId() == R.id.fab_add_savings_progress) {
            Intent intent = new Intent(this, CreateSavingsProgressActivity.class);
            intent.putExtra("savings", modelSavings);
            startActivityForResult(intent, REQUEST_CODE_SAVINGS);
        }

        if (v.getId() == R.id.fab_add_savings_target) {
            Intent intent = new Intent(this, CreateSavingsTargetActivity.class);
            intent.putExtra("savings", modelSavings);
            intent.putExtra("user", user);
            startActivityForResult(intent, REQUEST_CODE_SAVINGS);
        }
    }

    private void startAnimationFabSavings() {
        ObjectAnimator animatorFabOpen = getObjectAnimator(binding.layout.fabAdd, View.ROTATION, 135, 300);
        ObjectAnimator animatorFabClose = getObjectAnimator(binding.layout.fabAdd, View.ROTATION, 0, 300);
        ObjectAnimator animatorFabSavingsTarget = getObjectAnimator(binding.layout.fabAddSavingsTarget, View.ALPHA, 1, 300);
        ObjectAnimator animatorFabSavingsProgress = getObjectAnimator(binding.layout.fabAddSavingsProgress, View.ALPHA, 1, 300);
        ObjectAnimator animatorFabSavingsTargetClose = getObjectAnimator(binding.layout.fabAddSavingsTarget, View.ALPHA, 0, 300);
        ObjectAnimator animatorFabSavingsProgressClose = getObjectAnimator(binding.layout.fabAddSavingsProgress, View.ALPHA, 0, 300);
        if (!animatorSet.isStarted()) {
            if (!isFabOpen) {
                isFabOpen = true;
                animatorSet.playTogether(animatorFabOpen, animatorFabSavingsTarget, animatorFabSavingsProgress);
                isShowFabAddSavings(animatorFabSavingsProgress, binding.layout.fabAddSavingsProgress);
                isShowFabAddSavings(animatorFabSavingsTarget, binding.layout.fabAddSavingsTarget);
            } else {
                isFabOpen = false;
                animatorSet.playTogether(animatorFabSavingsProgressClose, animatorFabSavingsTargetClose, animatorFabClose);
            }
            animatorSet.start();

            isHideFabAddSavings(animatorFabSavingsProgressClose, binding.layout.fabAddSavingsProgress);
            isHideFabAddSavings(animatorFabSavingsTargetClose, binding.layout.fabAddSavingsTarget);

            animatorSet = new AnimatorSet();

        }

    }

    private void isHideFabAddSavings(ObjectAnimator objectAnimator, ExtendedFloatingActionButton fabSavings) {
        objectAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                fabSavings.setVisibility(View.GONE);
            }
        });

    }

    private void isShowFabAddSavings(ObjectAnimator objectAnimator, ExtendedFloatingActionButton fabSavings) {
        objectAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                fabSavings.show();
            }
        });

    }
}
