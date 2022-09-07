package id.co.evolution.financefy;


import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.View;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import id.co.evolution.financefy.activity.CreateFinanceActivity;
import id.co.evolution.financefy.databinding.ActivityMainBinding;
import id.co.evolution.financefy.fragment.FragmentAll;
import id.co.evolution.financefy.fragment.FragmentAnalysis;
import id.co.evolution.financefy.fragment.FragmentAccount;
import id.co.evolution.financefy.helper.TinyDb;
import id.co.evolution.financefy.model.ModelFinance;
import id.co.evolution.financefy.model.ModelUser;
import id.co.evolution.financefy.repository.FinanceRepository;
import id.co.evolution.financefy.viewmodel.ViewModelFinance;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    ActivityMainBinding binding;
    public ViewModelFinance viewModelFinance;
    public List<ModelFinance> dataFinance = new ArrayList<>();
    public ModelFinance modelFinance;
    int id_user = 0;

    @Inject
    FinanceRepository financeRepository;
    ModelUser user;
    @Inject
    TinyDb tinyDb;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        viewModelFinance = new ViewModelProvider(this).get(ViewModelFinance.class);
        viewModelFinance.init(financeRepository);
        user = tinyDb.getObject("user", ModelUser.class);

        viewModelFinance.getAllFinance().observe(this, modelFinances -> {
            dataFinance = modelFinances;
            Log.e("jumlah_size", modelFinances.size() + "");
//            ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
//            adapter.addFragment(new FragmentAll(), "Semuanya");
//            adapter.addFragment(new FragmentAnalysis(), "Pemasukan");
//            adapter.addFragment(new FragmentAccount(), "Pengeluaran");
//            adapter.addFragment(new FragmentAll(), "Pengeluaran");
//            binding.layout.viewPager.setAdapter(adapter);

//            binding.layout.tabLayout.setupWithViewPager(binding.layout.viewPager);

            changeFragment(new FragmentAll());
            binding.layout.bnMain.setOnItemSelectedListener(item -> {
                Fragment fragment = null;
                item.setChecked(true);
                binding.layout.fabAdd.hide();

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
            Log.e("TAG", "onClick: " + user.getId());
            Intent intent = new Intent(this, CreateFinanceActivity.class);
            intent.putExtra("id_user",user.getId());
            startActivityForResult(intent, 3);
        }
    }
}
