package id.co.evolution.financefy;


import static id.co.evolution.financefy.callback.CallbackOnActivityResult.REQUEST_CODE_FINANCE;
import static id.co.evolution.financefy.callback.CallbackOnActivityResult.REQUEST_CODE_SAVINGS;
import static id.co.evolution.financefy.helper.Tools.getObjectAnimator;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import id.co.evolution.financefy.activity.CreateFinanceActivity;
import id.co.evolution.financefy.activity.CreateSavingsProgressActivity;
import id.co.evolution.financefy.activity.CreateSavingsTargetActivity;
import id.co.evolution.financefy.databinding.ActivityMainBinding;
import id.co.evolution.financefy.fragment.FragmentAccount;
import id.co.evolution.financefy.fragment.FragmentAll;
import id.co.evolution.financefy.fragment.FragmentAnalysis;
import id.co.evolution.financefy.helper.TinyDb;
import id.co.evolution.financefy.model.ModelFinance;
import id.co.evolution.financefy.model.ModelSavings;
import id.co.evolution.financefy.model.ModelUser;
import id.co.evolution.financefy.repository.FinanceRepository;
import id.co.evolution.financefy.repository.SavingsRepository;
import id.co.evolution.financefy.viewmodel.ViewModelFinance;
import id.co.evolution.financefy.viewmodel.ViewModelSavings;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    ActivityMainBinding binding;
    public ViewModelFinance viewModelFinance;
    public ViewModelSavings viewModelSavings;
    public List<ModelFinance> dataFinance = new ArrayList<>();
    public ModelFinance modelFinance;
    AnimatorSet animatorSet = new AnimatorSet();

    @Inject
    FinanceRepository financeRepository;
    @Inject
    SavingsRepository savingsRepository;
    public ModelUser user;
    public ModelSavings modelSavings = new ModelSavings();

    @Inject
    TinyDb tinyDb;
    boolean isFabOpen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        viewModelFinance = new ViewModelProvider(this).get(ViewModelFinance.class);
        viewModelSavings = new ViewModelProvider(this).get(ViewModelSavings.class);
        viewModelFinance.init(financeRepository);
        viewModelSavings.init(savingsRepository);
        user = tinyDb.getObject("user", ModelUser.class);
        modelSavings = tinyDb.getObject("savings", ModelSavings.class);
        if (user == null) {
            user = new ModelUser("Reza", "Pribadi", "Menabung");
        }

        if (user.getCategory().equalsIgnoreCase(getString(R.string.jurnal_keuangan))) {
            showHideFabSavings(false);

            viewModelFinance.getAllFinance().observe(this, modelFinances -> {
                dataFinance = modelFinances;
                setUpFragment();
            });
        } else {
            viewModelSavings.findAllSavingsByIdUser(user.getId()).observe(this, dataSavings -> {
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

    }

    private void setUpFragment() {
        changeFragment(new FragmentAll());
        binding.layout.bnMain.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            item.setChecked(true);
            binding.layout.fabAdd.hide();
            showHideFabSavings(false);
            if (isFabOpen) startAnimationFabSavings();


            switch (item.getTitle().toString().toLowerCase()) {
                case "records":
                    fragment = new FragmentAll();
                    changeFragment(fragment);
                    binding.layout.fabAdd.show();
                    break;
                case "analysis":
                    fragment = new FragmentAnalysis();
                    changeFragment(fragment);
                    break;
                case "accounts":
                    fragment = new FragmentAccount();
                    changeFragment(fragment);
                    break;
                case "settings":
                    fragment = new FragmentAll();
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

    private ColorStateList getBottomNavigationColor() {
        int[][] states = new int[][]{
                new int[]{android.R.attr.state_checked}, // state_checked
                new int[]{}  //
        };

        int[] colors = new int[]{
                R.color.colorPrimaryDark,
                R.color.colorGrey50
        };
        return new ColorStateList(states, colors);
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
                intent.putExtra("id_user", user.getId());
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
            intent.putExtra("id_user", user.getId());
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
                isShowFabAddSavings(animatorFabSavingsProgress,binding.layout.fabAddSavingsProgress);
                isShowFabAddSavings(animatorFabSavingsTarget,binding.layout.fabAddSavingsTarget);
            } else {
                isFabOpen = false;
                animatorSet.playTogether(animatorFabSavingsProgressClose, animatorFabSavingsTargetClose, animatorFabClose);
            }
            animatorSet.start();

            isHideFabAddSavings(animatorFabSavingsProgressClose,binding.layout.fabAddSavingsProgress);
            isHideFabAddSavings(animatorFabSavingsTargetClose,binding.layout.fabAddSavingsTarget);

            animatorSet = new AnimatorSet();

        }

    }

    private void isHideFabAddSavings(ObjectAnimator objectAnimator, ExtendedFloatingActionButton fabSavings){
        objectAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                fabSavings.setVisibility(View.GONE);
            }
        });

    }
    private void isShowFabAddSavings(ObjectAnimator objectAnimator, ExtendedFloatingActionButton fabSavings){
        objectAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                fabSavings.show();
            }
        });

    }
}
