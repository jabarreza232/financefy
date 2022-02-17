package id.co.evolution.financefy.fragment;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;

import android.annotation.SuppressLint;
import android.app.Dialog;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.whiteelephant.monthpicker.MonthPickerDialog;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import id.co.evolution.financefy.App;
import id.co.evolution.financefy.MainActivity;
import id.co.evolution.financefy.R;
import id.co.evolution.financefy.activity.UpdateFinance;
import id.co.evolution.financefy.adapter.AdapterFilter;
import id.co.evolution.financefy.adapter.AdapterFinance;
import id.co.evolution.financefy.asynctask.FilterMaxMonthAsynctask;
import id.co.evolution.financefy.asynctask.FilterMinYearAsynctask;
import id.co.evolution.financefy.databinding.FragmentAllBinding;
import id.co.evolution.financefy.helper.Tools;
import id.co.evolution.financefy.model.ModelFinance;
import id.co.evolution.financefy.model.ModelNestedFinance;
import id.co.evolution.financefy.viewmodel.ViewModelFinance;

public class FragmentAll extends Fragment {
    Calendar today;
    List<ModelFinance> dataFinance = new ArrayList<>();
    FragmentAllBinding binding;
    ViewModelFinance viewModelFinance;
    AdapterFinance adapter;
    AdapterFinance.TYPE_LAYOUT_MANAGER type_layout_manager = AdapterFinance.TYPE_LAYOUT_MANAGER.GRID;
    Dialog dialog;
    LayoutInflater inflater;
    View dialogView;

    public FragmentAll() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_all, container, false);
        dataFinance = ((MainActivity) requireActivity()).dataFinance;
        setHasOptionsMenu(true);
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
                    .setMinYear(Integer.parseInt(new FilterMinYearAsynctask(today, dataFinance, getContext()).execute().get()))
                    .setMaxYear((today.get(Calendar.YEAR)))
                    .setMaxMonth(Integer.parseInt(new FilterMaxMonthAsynctask(today, dataFinance, getContext()).execute().get()));
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        builder.build().show();
    }


    @SuppressLint("NotifyDataSetChanged")
    private void loadDataByMonth(List<ModelFinance> data) {
        Collections.sort(data, (modelFinance, modelFinance2) -> Integer.parseInt(Tools.convertDateFormat(modelFinance.getDate()).split("-")[0]) - Integer.parseInt(Tools.convertDateFormat(modelFinance2.getDate()).split("-")[0]));

        adapter = new AdapterFinance(getActivity(), filterNestedFinance(data), (data1, position) -> showDialog(data1, position));
        Log.e("jumlah", adapter.getItemCount() + "");
        adapter.setType(type_layout_manager);
        binding.rvList.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.rvList.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        if (adapter.getItemCount() == 0) {
            binding.empty.setVisibility(View.VISIBLE);
            binding.txtEmpty.setVisibility(View.VISIBLE);
        } else {
            binding.empty.setVisibility(View.GONE);
            binding.txtEmpty.setVisibility(View.GONE);
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

    private List<ModelNestedFinance> filterNestedFinance(List<ModelFinance> data) {
        List<ModelNestedFinance> listData = new ArrayList<>();
        HashSet<String> hashset = new HashSet<>();

        for (ModelFinance modelFinance : data) {
            hashset.add(modelFinance.getDate());
        }
        for (String date : hashset) {
            Log.e("TAG", "filterNestedFinance: " + date);
            ModelNestedFinance modelNestedFinance = new ModelNestedFinance();
            modelNestedFinance.setDate(date);
            List<ModelFinance> dataFinance = new ArrayList<>();
            for (ModelFinance modelFinance : data) {
                if (date.contains(modelFinance.getDate()))
                    dataFinance.add(modelFinance);
            }
            modelNestedFinance.setFinances(dataFinance);
            listData.add(modelNestedFinance);
        }
        return listData;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_finance, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }


    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.filter:
                showDialogFilter();
                // Not implemented here
                break;
            case R.id.view_list:
                if (type_layout_manager == AdapterFinance.TYPE_LAYOUT_MANAGER.GRID) {
                    item.setIcon(R.drawable.ic_baseline_format_list_bulleted_24);
                    type_layout_manager = AdapterFinance.TYPE_LAYOUT_MANAGER.VERTICAL;
                } else if (type_layout_manager == AdapterFinance.TYPE_LAYOUT_MANAGER.VERTICAL) {
                    item.setIcon(R.drawable.ic_baseline_grid_view_24);
                    type_layout_manager = AdapterFinance.TYPE_LAYOUT_MANAGER.GRID;
                }
                if (adapter != null) {
                    adapter.setType(type_layout_manager);
                    adapter.notifyDataSetChanged();
                }
                return true;

            default:
                break;
        }
        return true;
    }


    private void showDialogFilter() {
        dialog = new Dialog(getContext());
        inflater = getLayoutInflater();
        dialogView = inflater.inflate(R.layout.dialog_choose_filter, null);
        dialog.setContentView(dialogView);
        TextView txtSubmit = dialogView.findViewById(R.id.txt_submit);
        RecyclerView rvListType = dialogView.findViewById(R.id.rv_type);
        RecyclerView rvListNominal = dialogView.findViewById(R.id.rv_nominal);
        RecyclerView rvListPeriod = dialogView.findViewById(R.id.rv_periode);


        txtSubmit.setOnClickListener(v -> {
            dialog.dismiss();
        });
        List<String> filterType = new ArrayList<>();
        List<String> filterNominal = new ArrayList<>();
        List<String> filterPeriod = new ArrayList<>();

        filterType.add("Pengeluaran");
        filterType.add("Pemasukan");
        filterType.add("Semuanya");

        filterNominal.add("Tertinggi-Terendah");
        filterNominal.add("Terendah-Tertinggi");

        filterPeriod.add("Terbaru-Terlama");
        filterPeriod.add("Terlama-Terbaru");

        AdapterFilter adapterFilterType = new AdapterFilter(getContext(), filterType, new AdapterFilter.MethodCallback() {
            @Override
            public void onClick(List<String> data, int position) {

            }
        });
        rvListType.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL,false));
        rvListType.setAdapter(adapterFilterType);

        AdapterFilter adapterFilterNominal = new AdapterFilter(getContext(), filterNominal, new AdapterFilter.MethodCallback() {
            @Override
            public void onClick(List<String> data, int position) {

            }
        });
        rvListNominal.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL,false));
        rvListNominal.setAdapter(adapterFilterNominal);

        AdapterFilter adapterFilterPeriod = new AdapterFilter(getContext(), filterPeriod, new AdapterFilter.MethodCallback() {
            @Override
            public void onClick(List<String> data, int position) {

            }
        });
        rvListPeriod.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL,false));
        rvListPeriod.setAdapter(adapterFilterPeriod);

//        dialog.setCancelable(true);
//        dialog.setIcon(R.mipmap.ic_launcher);
//        dialog.setTitle("Form Biodata");
//
        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();

        wlp.gravity = Gravity.CENTER;
        wlp.flags &= ~WindowManager.LayoutParams.FLAG_BLUR_BEHIND;
        window.setAttributes(wlp);
        dialog.getWindow().setLayout(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

        dialog.show();
    }
}
