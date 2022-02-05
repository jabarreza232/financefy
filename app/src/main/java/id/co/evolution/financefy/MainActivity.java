package id.co.evolution.financefy;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager.widget.ViewPager;

import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import id.co.evolution.financefy.activity.CreateFinance;
import id.co.evolution.financefy.adapter.ViewPagerAdapter;
import id.co.evolution.financefy.databinding.ActivityMainBinding;
import id.co.evolution.financefy.fragment.All;
import id.co.evolution.financefy.fragment.Pemasukan;
import id.co.evolution.financefy.fragment.Pengeluaran;
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
            adapter.addFragment(new All(), "Semuanya");
            adapter.addFragment(new Pemasukan(), "Pemasukan");
            adapter.addFragment(new Pengeluaran(), "Pengeluaran");
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
