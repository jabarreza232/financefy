package id.co.evolution.financefy.fragment;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.whiteelephant.monthpicker.MonthPickerDialog;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import id.co.evolution.financefy.App;
import id.co.evolution.financefy.MainActivity;
import id.co.evolution.financefy.R;
import id.co.evolution.financefy.activity.UpdateFinance;
import id.co.evolution.financefy.adapter.AdapterFinance;
import id.co.evolution.financefy.asynctask.FilterMaxMonthAsynctask;
import id.co.evolution.financefy.databinding.FragmentAllBinding;
import id.co.evolution.financefy.helper.Tools;
import id.co.evolution.financefy.model.ModelFinance;
import id.co.evolution.financefy.viewmodel.ViewModelFinance;

public class All extends Fragment {
    Calendar today;
    List<ModelFinance> dataFinance = new ArrayList<>();
    FragmentAllBinding binding;
    ViewModelFinance viewModelFinance;

    public All() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_all, container, false);
        dataFinance = ((MainActivity) requireActivity()).dataFinance;
        return binding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        today = Calendar.getInstance();

        today.get(Calendar.YEAR);
        today.get(Calendar.MONTH);
        long date_ship_milis = today.getTimeInMillis();
        binding.txtMonth.setText(Tools.getFormattedMonthTextSimple(date_ship_milis));

        viewModelFinance = new ViewModelProvider(this).get(ViewModelFinance.class);

        Log.e("TAG", "onViewCreated: " + Tools.getFormattedMonthSimple(date_ship_milis));
        viewModelFinance.getFinanceByMonth(getContext(), Tools.getFormattedMonthSimple(date_ship_milis)).observe(getViewLifecycleOwner(), modelFinances -> {
            if (modelFinances != null) {
                loadDataByMonth(modelFinances);
            }
        });

        binding.placeMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialogMonthPicker();
            }
        });
    }


    private void refresh() {
        FragmentTransaction ft = getParentFragmentManager().beginTransaction();
        if (Build.VERSION.SDK_INT >= 26) {
            ft.setReorderingAllowed(false);
        }
        ft.detach(this).attach(this).commit();
    }

    private void showDialogMonthPicker() {
        MonthPickerDialog.Builder builder = new MonthPickerDialog.Builder(getActivity(),
                new MonthPickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(int selectedMonth, int selectedYear) { // on date set }
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(Calendar.YEAR, selectedYear);
                        calendar.set(Calendar.MONTH, selectedMonth);
                        long date_ship_milis = calendar.getTimeInMillis();
                        binding.txtMonth.setText(Tools.getFormattedMonthTextSimple(date_ship_milis));

                        viewModelFinance.getFinanceByMonth(getContext(), Tools.getFormattedMonthSimple(date_ship_milis)).observe(getViewLifecycleOwner(), modelFinances -> {
                            if (modelFinances != null) {
                                loadDataByMonth(modelFinances);
                            }
                        });
                    }
                }, today.get(Calendar.YEAR), today.get(Calendar.MONTH));

        try {
            builder.setMinYear(today.get(Calendar.YEAR))
                    .setActivatedYear(today.get(Calendar.YEAR))
                    .setMaxYear((today.get(Calendar.YEAR) + 20))
                    .setMaxMonth(Integer.parseInt(new FilterMaxMonthAsynctask(today, dataFinance, getContext()).execute().get()));
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        builder.build().show();
    }


    @SuppressLint("NotifyDataSetChanged")
    private void loadDataByMonth(List<ModelFinance> data) {
        Collections.sort(data, (modelFinance, modelFinance2) -> Integer.parseInt(Tools.convertDateFormat(modelFinance.getDate()).split("-")[0]) - Integer.parseInt(Tools.convertDateFormat(modelFinance2.getDate()).split("-")[0]));

        AdapterFinance adapter = new AdapterFinance(getActivity(), data, (data1, position) -> showDialog(data1, position));
        Log.e("jumlah", adapter.getItemCount() + "");
        adapter.setType(AdapterFinance.TYPE_LAYOUT_MANAGER.GRID);
        binding.rvList.setLayoutManager(new GridLayoutManager(getActivity(), 2));

        binding.rvList.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        if (adapter.getItemCount() == 0) {
            binding.empty.setVisibility(View.VISIBLE);
        } else {
            binding.empty.setVisibility(View.GONE);
        }
    }

    private void showDialog(final List<ModelFinance> data, final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Pilih Opsi");
        final String[] tipe = {"Ubah", "Hapus"};
        builder.setItems(tipe, (dialog, which) -> {
            switch (which) {
                case 0:
                    Intent i = new Intent(getActivity(), UpdateFinance.class);
                    i.putExtra("id", data.get(position).getId());
                    startActivity(i);
                    dialog.dismiss();
                    break;
                case 1:
                    App.getDatabase(getActivity()).financeDao().delete(data.get(position));
                    dataFinance.remove(position);
                    binding.rvList.getAdapter().notifyDataSetChanged();
                    dialog.dismiss();
                    break;
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

}
