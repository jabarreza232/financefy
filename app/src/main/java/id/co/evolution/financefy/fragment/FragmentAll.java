package id.co.evolution.financefy.fragment;

import static id.co.evolution.financefy.helper.Tools.convertToCurrency;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
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
import android.widget.Toast;

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
import id.co.evolution.financefy.helper.FinanceFilter;
import id.co.evolution.financefy.helper.Tools;
import id.co.evolution.financefy.model.ModelFilter;
import id.co.evolution.financefy.model.ModelFinance;
import id.co.evolution.financefy.model.ModelNestedFinance;
import id.co.evolution.financefy.viewmodel.ViewModelFinance;

public class FragmentAll extends Fragment {
    Calendar today;
    Calendar prevNextMonth;
    List<ModelFinance> dataFinance = new ArrayList<>();
    FragmentAllBinding binding;
    ViewModelFinance viewModelFinance;
    AdapterFinance adapter;
    AdapterFinance.TYPE_LAYOUT_MANAGER type_layout_manager = AdapterFinance.TYPE_LAYOUT_MANAGER.GRID;
    Dialog dialog;
    LayoutInflater inflater;
    View dialogView;
    FinanceFilter financeFilter;
    String filterType, filterNominal, filterPeriod;
    String month;

    public FragmentAll() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_all, container, false);
        dataFinance = ((MainActivity) requireActivity()).dataFinance;
        financeFilter = new FinanceFilter();
        setHasOptionsMenu(true);
        return binding.getRoot();
    }


    @SuppressLint("NewApi")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        today = Calendar.getInstance();
        today.get(Calendar.YEAR);
        today.get(Calendar.MONTH);
        prevNextMonth = Calendar.getInstance();
        long date_ship_milis = today.getTimeInMillis();

        viewModelFinance = new ViewModelProvider(this).get(ViewModelFinance.class);

        Log.e("TAG", "onViewCreated: " + Tools.getFormattedMonthSimple(date_ship_milis));
        loadDataFinanceByMonth(date_ship_milis);

        binding.placeMonth.setOnClickListener(v -> showDialogMonthPicker());

        binding.btnNext.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.colorGrey50)));
        binding.btnNext.setEnabled(false);

        binding.btnPrev.setOnClickListener(v -> {
            prevNextMonth.get(Calendar.YEAR);
            prevNextMonth.add(Calendar.MONTH, -1);
            long date_ship_milisecond = prevNextMonth.getTimeInMillis();

            loadDataFinanceByMonth(date_ship_milisecond);
        });

        binding.btnNext.setOnClickListener(v -> {
            prevNextMonth.get(Calendar.YEAR);
            prevNextMonth.add(Calendar.MONTH, 1);
            long date_ship_milisecond = prevNextMonth.getTimeInMillis();

            loadDataFinanceByMonth(date_ship_milisecond);
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
                        prevNextMonth.set(Calendar.YEAR, selectedYear);
                        prevNextMonth.set(Calendar.MONTH, selectedMonth);
                        long date_ship_milis = calendar.getTimeInMillis();
                        financeFilter.resetFilter();
                        loadDataFinanceByMonth(date_ship_milis);
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
    private void loadData(List<ModelFinance> data) {
        int total = (financeFilter.totalIncome(data) - financeFilter.totalExpense(data));
        binding.txtTotalIncome.setText(convertToCurrency(financeFilter.totalIncome(data)));
        binding.txtTotalExpense.setText(convertToCurrency(financeFilter.totalExpense(data)));
        binding.txtTotalAll.setText(convertToCurrency(total));
        binding.txtTotalAll.setTextColor(total < 0 ? ContextCompat.getColor(getContext(), R.color.red) : ContextCompat.getColor(getContext(), R.color.green));

        if (filterNominal != null)
            data = financeFilter.filterNominal(filterNominal, data);

        List<ModelNestedFinance> listNestedFinance = financeFilter.filterNestedFinance(data);

        if (filterPeriod != null)
            listNestedFinance = financeFilter.filterPeriod(filterPeriod, listNestedFinance);
        else listNestedFinance = financeFilter.filterPeriod("terbaru", listNestedFinance);

        adapter = new AdapterFinance(getActivity(), listNestedFinance, (data1, position) -> showDialog(data1, position));
        Log.e("jumlah", adapter.getItemCount() + "");
        adapter.setType(type_layout_manager);
        binding.rvList.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.rvList.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        binding.placeEmpty.setVisibility(adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);

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


        AdapterFilter adapterFilterType = new AdapterFilter(getContext(), financeFilter.filterType, (data, position) -> {
            filterType = data.get(position).getValue();
        });

        rvListType.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvListType.setAdapter(adapterFilterType);

        AdapterFilter adapterFilterNominal = new AdapterFilter(getContext(), financeFilter.filterNominal, new AdapterFilter.MethodCallback() {
            @Override
            public void onClick(List<ModelFilter> data, int position) {
                filterNominal = data.get(position).getValue();
            }
        });
        rvListNominal.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvListNominal.setAdapter(adapterFilterNominal);

        AdapterFilter adapterFilterPeriod = new AdapterFilter(getContext(), financeFilter.filterPeriod, (data, position) ->
                filterPeriod = data.get(position).getValue());
        rvListPeriod.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvListPeriod.setAdapter(adapterFilterPeriod);

//        dialog.setCancelable(true);
//        dialog.setIcon(R.mipmap.ic_launcher);
//        dialog.setTitle("Form Biodata");
//
        txtSubmit.setOnClickListener(v -> {
            if (filterType == null) {
                Toast.makeText(getContext(), "Mohon untuk pilih tipe terlebih dahulu!", Toast.LENGTH_SHORT).show();
                return;
            }
            dialog.dismiss();
            loadByType(filterType);
        });

        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();

        wlp.gravity = Gravity.CENTER;
        wlp.flags &= ~WindowManager.LayoutParams.FLAG_BLUR_BEHIND;
        window.setAttributes(wlp);
        dialog.getWindow().setLayout(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

        dialog.show();
    }

    private void loadByType(String type) {
        if (type.equalsIgnoreCase("semuanya")) {
            viewModelFinance.getFinanceByMonth(getContext(), month).observe(getViewLifecycleOwner(), new Observer<List<ModelFinance>>() {
                @Override
                public void onChanged(List<ModelFinance> modelFinances) {
                    if (modelFinances != null) loadData(modelFinances);
                }
            });
        } else {
            viewModelFinance.getFinanceByTypeAndMonth(getContext(), type, month).observe(getViewLifecycleOwner(), new Observer<List<ModelFinance>>() {
                @Override
                public void onChanged(List<ModelFinance> modelFinances) {
                    if (modelFinances != null) loadData(modelFinances);
                }
            });
        }
    }

    @SuppressLint("NewApi")
    private void loadDataFinanceByMonth(long date_ship_milis) {
        binding.txtMonth.setText(Tools.getFormattedMonthTextSimple(date_ship_milis));
        month = Tools.getFormattedMonthSimple(date_ship_milis);
        if (date_ship_milis != today.getTimeInMillis()) {
            binding.btnNext.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.blackTextColor)));
            binding.btnNext.setEnabled(true);
        }
        if (date_ship_milis >= today.getTimeInMillis()) {
            binding.btnNext.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.colorGrey50)));
            binding.btnNext.setEnabled(false);
        }
        viewModelFinance.getFinanceByMonth(getContext(), Tools.getFormattedMonthSimple(date_ship_milis)).observe(getViewLifecycleOwner(), modelFinances -> {
            if (modelFinances != null) {
                loadData(modelFinances);
            }
        });
    }
}
