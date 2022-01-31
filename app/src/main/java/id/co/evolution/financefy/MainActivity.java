package id.co.evolution.financefy;


import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;

import java.io.File;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import id.co.evolution.financefy.activity.CreateFinance;
import id.co.evolution.financefy.activity.Login;
import id.co.evolution.financefy.adapter.ViewPagerAdapter;
import id.co.evolution.financefy.fragment.All;
import id.co.evolution.financefy.fragment.Pemasukan;
import id.co.evolution.financefy.fragment.Pengeluaran;
import id.co.evolution.financefy.helper.TinyDb;
import id.co.evolution.financefy.model.ModelUser;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    @BindView(R.id.tabLayout)
    TabLayout tabLayout;
    @BindView(R.id.viewPager)
    ViewPager viewPager;

    TinyDb tinyDb;
    @BindView(R.id.fab_add)
    FloatingActionButton fabAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new All(), "All");
        adapter.addFragment(new Pemasukan(), "Pemasukan");
        adapter.addFragment(new Pengeluaran(), "Pengeluaran");
        Log.e("jumlah", adapter.getCount() + "");
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);

        fabAdd.setOnClickListener(this);
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
