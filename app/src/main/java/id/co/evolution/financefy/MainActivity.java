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
import android.app.ComponentCaller;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.OvershootInterpolator;

import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.ads.MobileAds;
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
    boolean isCustomActive;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        String type_currency = tinyDb.getString("currency");
         isPinSetting = tinyDb.getBoolean("isSettingPin");
        boolean isPinInput = getIntent().getBooleanExtra("isInputPin", false);


         isCustomActive = tinyDb.getBoolean("is_custom_color_active");
        if (isCustomActive) {
            int customColor = tinyDb.getInt("custom_color_int");
            getWindow().setStatusBarColor(customColor);
        } else {
            if (tinyDb.getObject("model_primary_color", ModelPrimaryColor.class) != null) {
                modelPrimaryColor = tinyDb.getObject("model_primary_color", ModelPrimaryColor.class);
                Tools.setThemeActivity(getTheme(), modelPrimaryColor);
            }
        }

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        if (getIntent().getBooleanExtra("ACTION_TRIGGER_EXPORT", false)) {
            isPinSetting = false;

            binding.layout.bnMain.post(() -> {
                binding.layout.bnMain.setSelectedItemId(R.id.settings);
                tinyDb.getBoolean("isSettingPin",true);
            });
        }
        if (isPinSetting && !isPinInput) {
            Intent intentToPin = new Intent(this, PinActivity.class);

            startActivity(intentToPin);
        }
        if (isCustomActive) {
            int customColor = tinyDb.getInt("custom_color_int");
            if (getSupportActionBar() != null) {
                getSupportActionBar().setBackgroundDrawable(new ColorDrawable(customColor));
            }
        }

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
//        createNotificationChannel();
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
        new Thread(
                () -> {
                    // Initialize the Google Mobile Ads SDK on a background thread.
                    MobileAds.initialize(this, initializationStatus -> {});
                })
                .start();

    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent); // Perbarui intent

        if ( intent.getBooleanExtra("GO_TO_SETTINGS", false)) {
            // Pastikan nama ID-nya sesuai dengan yang ada di XML menu Anda
            binding.layout.bnMain.setSelectedItemId(R.id.settings);
        }
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
    @ColorInt
    public int getColorFromAttr(Context context, @AttrRes int attrColor) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(attrColor, typedValue, true);
        if (typedValue.resourceId != 0) {
            return ContextCompat.getColor(context, typedValue.resourceId);
        } else {
            return typedValue.data;
        }
    }


    private void changeColorBottomNavigation(ModelPrimaryColor modelPrimaryColor) {
        // 1. Tentukan warna aktif yang BENAR
        int activeColor;
        boolean isCustomActive = tinyDb.getBoolean("is_custom_color_active");

        if (isCustomActive) {
            activeColor = tinyDb.getInt("custom_color_int");
        } else {
            activeColor = ContextCompat.getColor(this, modelPrimaryColor.getColorPrimary());
        }

        // Warna tidak aktif (abu-abu)
        int inactiveColor = ContextCompat.getColor(this, R.color.colorGrey50);

        // 2. Definisikan State (Kondisi Tab)
        int[][] states = new int[][]{
                new int[]{android.R.attr.state_checked},  // Saat tab ditekan/aktif
                new int[]{-android.R.attr.state_checked}  // Saat tab TIDAK aktif (Gunakan tanda minus)
        };

        // 3. Definisikan Warna Berdasarkan State
        int[] colors = new int[]{
                activeColor,    // Warna untuk state_checked
                inactiveColor   // Warna untuk -state_checked
        };

        ColorStateList dynamicColorList = new ColorStateList(states, colors);

        // 4. Terapkan ke Bottom Navigation
        binding.layout.bnMain.setItemIconTintList(dynamicColorList);
        binding.layout.bnMain.setItemTextColor(dynamicColorList);
    }

    private void setUpFragment() {
        getSupportActionBar().setTitle(getString(R.string.record));
        changeFragment(new FragmentAll());
        binding.layout.bnMain.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            item.setChecked(true);
            showHideFabSavings(false);
            if (isFabOpen) startAnimationFabSavings();
            isUserDailyFinance = user.getCategory().equalsIgnoreCase(getString(R.string.jurnal_keuangan));

            switch (item.getTitle().toString().toLowerCase()) {
                case "arsip":
                    fragment = new FragmentAll();
                    changeFragment(fragment);

                    getSupportActionBar().setTitle(getString(R.string.record));
                    break;
                case "analisa":
                    if (isUserDailyFinance)
                        fragment = new FragmentAnalysis();
                    else fragment = new FragmentAnalysisSavings();
                    getSupportActionBar().setTitle(getString(R.string.analysis));
                    changeFragment(fragment);
                    break;
                case "akun":
                    getSupportActionBar().setTitle(getString(R.string.account));
                    fragment = new FragmentAccount();
                    changeFragment(fragment);
                    break;
                case "pengaturan":
                    getSupportActionBar().setTitle(getString(R.string.settings));
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
            FragmentManager fm = getSupportFragmentManager();

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
        // Rotasi & Transparansi
        float rotation = isFabOpen ? 0f : 135f;
        float alpha = isFabOpen ? 0f : 1f;

        // --- MENGATUR JARAK MEKAR (Dalam Pixel) ---
        // Jika sedang terbuka, kembalikan ke titik 0 (tengah).
        // Jika tertutup, tembakkan ke atas sejauh -200, dan menyamping sejauh -160 / 160.
        float translationY = isFabOpen ? 0f : -160f;
        float translationXTarget = isFabOpen ? 0f : -120f;   // Bergerak ke Kiri
        float translationXProgress = isFabOpen ? 0f : 120f;  // Bergerak ke Kanan

        // Animasi FAB Utama (Muter)
        ObjectAnimator rotateMainFab = ObjectAnimator.ofFloat(binding.layout.fabAdd, View.ROTATION, rotation);

        // Animasi Target (Meluncur ke Kiri Atas)
        ObjectAnimator animTargetY = ObjectAnimator.ofFloat(binding.layout.fabAddSavingsTarget, View.TRANSLATION_Y, translationY);
        ObjectAnimator animTargetX = ObjectAnimator.ofFloat(binding.layout.fabAddSavingsTarget, View.TRANSLATION_X, translationXTarget);
        ObjectAnimator animTargetAlpha = ObjectAnimator.ofFloat(binding.layout.fabAddSavingsTarget, View.ALPHA, alpha);

        // Animasi Progress (Meluncur ke Kanan Atas)
        ObjectAnimator animProgressY = ObjectAnimator.ofFloat(binding.layout.fabAddSavingsProgress, View.TRANSLATION_Y, translationY);
        ObjectAnimator animProgressX = ObjectAnimator.ofFloat(binding.layout.fabAddSavingsProgress, View.TRANSLATION_X, translationXProgress);
        ObjectAnimator animProgressAlpha = ObjectAnimator.ofFloat(binding.layout.fabAddSavingsProgress, View.ALPHA, alpha);

        // Gabungkan semua animasi
        AnimatorSet set = new AnimatorSet();
        set.playTogether(
                rotateMainFab,
                animTargetY, animTargetX, animTargetAlpha,
                animProgressY, animProgressX, animProgressAlpha
        );
        set.setDuration(300);
        set.setInterpolator(new OvershootInterpolator()); // Efek pantulan pegas

        // Logika Show / Hide
        if (!isFabOpen) {
            // BUKA: Munculkan view lalu mulai animasi mekar
            binding.layout.fabAddSavingsTarget.setVisibility(View.VISIBLE);
            binding.layout.fabAddSavingsProgress.setVisibility(View.VISIBLE);
            set.start();
        } else {
            // TUTUP: Jalankan animasi kuncup, setelah selesai baru di-GONE
            set.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    binding.layout.fabAddSavingsTarget.setVisibility(View.GONE);
                    binding.layout.fabAddSavingsProgress.setVisibility(View.GONE);
                }
            });
            set.start();
        }

        isFabOpen = !isFabOpen;
    }

}
