package id.co.evolution.financefy;


import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import id.co.evolution.financefy.activity.CreateFinance;
import id.co.evolution.financefy.adapter.ViewPagerAdapter;
import id.co.evolution.financefy.databinding.ActivityMainBinding;
import id.co.evolution.financefy.fragment.FragmentAll;
import id.co.evolution.financefy.fragment.FragmentIncome;
import id.co.evolution.financefy.fragment.FragmentSpending;
import id.co.evolution.financefy.helper.TinyDb;
import id.co.evolution.financefy.model.ModelFinance;
import id.co.evolution.financefy.viewmodel.ViewModelFinance;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    TinyDb tinyDb;
    ActivityMainBinding binding;
    ViewModelFinance viewModelFinance;
    public List<ModelFinance> dataFinance = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        viewModelFinance = new ViewModelProvider(this).get(ViewModelFinance.class);

        viewModelFinance.getAllFinance(this).observe(this, modelFinances -> {
            dataFinance = modelFinances;
            ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
            adapter.addFragment(new FragmentAll(), "Semuanya");
            adapter.addFragment(new FragmentIncome(), "Pemasukan");
            adapter.addFragment(new FragmentSpending(), "Pengeluaran");
            Log.e("jumlah_size", modelFinances.size() + "");
            binding.layout.viewPager.setAdapter(adapter);
            binding.layout.tabLayout.setupWithViewPager(binding.layout.viewPager);
        });


        binding.fabAdd.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab_add:
                startActivity(new Intent(this, CreateFinance.class));
                break;
        }
    }
}
