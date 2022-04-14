package id.co.evolution.financefy;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.ColorStateListDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.navigation.NavigationBarView;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import id.co.evolution.financefy.activity.CreateFinance;
import id.co.evolution.financefy.adapter.ViewPagerAdapter;
import id.co.evolution.financefy.databinding.ActivityMainBinding;
import id.co.evolution.financefy.db.FinanceDB;
import id.co.evolution.financefy.db.FinanceDao;
import id.co.evolution.financefy.fragment.FragmentAll;
import id.co.evolution.financefy.fragment.FragmentIncome;
import id.co.evolution.financefy.fragment.FragmentSpending;
import id.co.evolution.financefy.helper.TinyDb;
import id.co.evolution.financefy.model.ModelFinance;
import id.co.evolution.financefy.repository.FinanceRepository;
import id.co.evolution.financefy.viewmodel.ViewModelFactory;
import id.co.evolution.financefy.viewmodel.ViewModelFinance;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    TinyDb tinyDb;
    ActivityMainBinding binding;
    public ViewModelFinance viewModelFinance;
    public List<ModelFinance> dataFinance = new ArrayList<>();
    public ModelFinance modelFinance;

    @Inject
    FinanceRepository financeRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        viewModelFinance = new ViewModelProvider(this).get(ViewModelFinance.class);
        viewModelFinance.init(financeRepository);

        viewModelFinance.getAllFinance().observe(this, modelFinances -> {
            dataFinance = modelFinances;
            Log.e("jumlah_size", modelFinances.size() + "");
//            ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
//            adapter.addFragment(new FragmentAll(), "Semuanya");
//            adapter.addFragment(new FragmentIncome(), "Pemasukan");
//            adapter.addFragment(new FragmentSpending(), "Pengeluaran");
//            adapter.addFragment(new FragmentAll(), "Pengeluaran");
//            binding.layout.viewPager.setAdapter(adapter);

//            binding.layout.tabLayout.setupWithViewPager(binding.layout.viewPager);

            changeFragment(new FragmentAll());
            binding.layout.bnMain.setOnItemSelectedListener(item -> {
                Fragment fragment = null;
                item.setChecked(true);

                switch (item.getTitle().toString().toLowerCase()) {
                    case "records":
                        fragment = new FragmentAll();
                        changeFragment(fragment);
                        break;
                    case "analysis":
                        fragment = new FragmentIncome();
                        changeFragment(fragment);
                        break;
                    case "accounts":
                        fragment = new FragmentSpending();
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
        });

        binding.layout.fabAdd.setOnClickListener(this);
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
            if (requestCode == 3) {
                modelFinance = (ModelFinance) data.getSerializableExtra("finance");
                Log.e("TAG", "onActivityResult: " + new Gson().toJson((ModelFinance) data.getSerializableExtra("finance")));
                changeFragment(new FragmentAll());
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.fab_add) {
            startActivityForResult(new Intent(this, CreateFinance.class), 3);
        }
    }
}
