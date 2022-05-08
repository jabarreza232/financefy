package id.co.evolution.financefy.fragment;

import static id.co.evolution.financefy.helper.Tools.convertToCurrency;
import static id.co.evolution.financefy.helper.Tools.numberFormat;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.Observer;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.MPPointF;
import com.whiteelephant.monthpicker.MonthPickerDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import id.co.evolution.financefy.MainActivity;
import id.co.evolution.financefy.R;
import id.co.evolution.financefy.activity.UpdateFinance;
import id.co.evolution.financefy.adapter.AdapterAnalysisFinance;
import id.co.evolution.financefy.adapter.AdapterFilter;
import id.co.evolution.financefy.adapter.AdapterFinance;
import id.co.evolution.financefy.asynctask.FilterMaxMonthAsynctask;
import id.co.evolution.financefy.asynctask.FilterMinYearAsynctask;
import id.co.evolution.financefy.callback.CallbackOnActivityResult;
import id.co.evolution.financefy.databinding.FragmentAllBinding;
import id.co.evolution.financefy.databinding.FragmentAnalysisBinding;
import id.co.evolution.financefy.helper.FinanceFilter;
import id.co.evolution.financefy.helper.LocalizedWeekHelper;
import id.co.evolution.financefy.helper.Tools;
import id.co.evolution.financefy.model.ModelFilter;
import id.co.evolution.financefy.model.ModelFinance;
import id.co.evolution.financefy.model.ModelNestedFinance;
import id.co.evolution.financefy.repository.FinanceRepository;
import id.co.evolution.financefy.viewmodel.ViewModelFinance;

@AndroidEntryPoint
public class FragmentAnalysis extends Fragment {
    Calendar today;
    Calendar prevNextMonth;
    List<ModelFinance> dataFinance = new ArrayList<>();
    FragmentAnalysisBinding binding;
    ViewModelFinance viewModelFinance;
    AdapterAnalysisFinance adapter;
    AdapterFinance.TYPE_LAYOUT_MANAGER type_layout_manager = AdapterFinance.TYPE_LAYOUT_MANAGER.GRID;
    Dialog dialog;
    LayoutInflater inflater;
    View dialogView;
    FinanceFilter financeFilter;
    String filterType,  filterPeriod = "Bulanan";
    String month;
    LocalizedWeekHelper localizedWeekHelper;
    int prevNextWeek = 0;
    boolean nextWeekEnabled;
    @Inject
    FinanceRepository financeRepository;
    CallbackOnActivityResult mCallbackOnActivityResult;
    int mPositionItem;
    long date_ship_millis;

    public FragmentAnalysis() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_analysis, container, false);
        dataFinance = ((MainActivity) requireActivity()).dataFinance;
        financeFilter = new FinanceFilter();
        localizedWeekHelper = new LocalizedWeekHelper();
        Log.e("cek_list_week: ", localizedWeekHelper.getFirstDay(-7).substring(0, (localizedWeekHelper.getFirstDay(-7).length() - 3)));
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
        date_ship_millis = today.getTimeInMillis();
        filterType = "Pemasukan";
        viewModelFinance = new ViewModelProvider(this).get(ViewModelFinance.class);
        viewModelFinance.init(financeRepository);

        loadDataFinanceByMonth(date_ship_millis);

        binding.placeMonth.setOnClickListener(v -> showDialogMonthPicker());

        binding.btnNext.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.colorGrey50)));
        binding.btnNext.setEnabled(false);

        binding.btnPrev.setOnClickListener(v -> {
            if (filterPeriod.equalsIgnoreCase("bulanan")) {
                prevNextMonth.get(Calendar.YEAR);
                prevNextMonth.add(Calendar.MONTH, -1);
                long date_ship_milisecond = prevNextMonth.getTimeInMillis();

                loadDataFinanceByMonth(date_ship_milisecond);
            } else {
                prevNextWeek = prevNextWeek - 7;

                nextWeekEnabled = localizedWeekHelper.getMonthLastWeekDay(prevNextWeek) <= today.getTimeInMillis();
                if (nextWeekEnabled)
                    binding.btnNext.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.white)));
                else
                    binding.btnNext.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.colorGrey50)));

                binding.btnNext.setEnabled(nextWeekEnabled);
                binding.txtMonth.setText(Tools.convertDateFormatWeekText(localizedWeekHelper.getFirstDay(prevNextWeek - 7)) + " - " + Tools.convertDateFormatWeekText(localizedWeekHelper.getLastDay(prevNextWeek)));
                binding.txtMonth.setEnabled(false);
                viewModelFinance.getFinanceByTypeAndWeek(filterType, localizedWeekHelper.getListWeek(localizedWeekHelper.getFirstDay(prevNextWeek - 7), localizedWeekHelper.getLastDay(prevNextWeek))).observe(getViewLifecycleOwner(), modelFinances -> {
                    if (modelFinances != null) {
                        loadData(modelFinances);
                    }
                });
            }

        });

        binding.btnNext.setOnClickListener(v -> {
            if (filterPeriod.equalsIgnoreCase("bulanan")) {
                prevNextMonth.get(Calendar.YEAR);
                prevNextMonth.add(Calendar.MONTH, 1);
                long date_ship_milisecond = prevNextMonth.getTimeInMillis();

                loadDataFinanceByMonth(date_ship_milisecond);
            } else {
                prevNextWeek = prevNextWeek + 7;

                nextWeekEnabled = localizedWeekHelper.getMonthLastWeekDay(prevNextWeek) <= today.getTimeInMillis();
                binding.btnNext.setEnabled(nextWeekEnabled);
                if (nextWeekEnabled)
                    binding.btnNext.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.white)));
                else
                    binding.btnNext.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.colorGrey50)));

                binding.txtMonth.setText(Tools.convertDateFormatWeekText(localizedWeekHelper.getFirstDay(prevNextWeek - 7)) + " - " + Tools.convertDateFormatWeekText(localizedWeekHelper.getLastDay(prevNextWeek)));
                binding.txtMonth.setEnabled(false);
                viewModelFinance.getFinanceByTypeAndWeek(filterType, localizedWeekHelper.getListWeek(localizedWeekHelper.getFirstDay(prevNextWeek - 7), localizedWeekHelper.getLastDay(prevNextWeek))).observe(getViewLifecycleOwner(), modelFinances -> {
                    if (modelFinances != null) {
                        loadData(modelFinances);
                    }
                });
            }
        });

        initiateSayHaloWithTime();
    }

    private void initiateSayHaloWithTime() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
        String now = simpleDateFormat.format(new Date());
        Log.e("waktu", now + " - " + now.substring(0, 1) + " - " + now.substring(0, 2));
        if (Integer.parseInt(now.substring(0, 2)) >= 4 && Integer.parseInt(now.substring(0, 2)) < 10) {
            binding.txtName.setText("Selamat Pagi, Jackson!");
        } else if (Integer.parseInt(now.substring(0, 2)) >= 10 && Integer.parseInt(now.substring(0, 2)) < 15) {
            binding.txtName.setText("Selamat Siang, Jackson!");
        } else if (Integer.parseInt(now.substring(0, 2)) >= 15 && Integer.parseInt(now.substring(0, 2)) < 18) {
            binding.txtName.setText("Selamat Sore, Jackson!");
        } else {
            binding.txtName.setText("Selamat Malam, Jackson!");
        }
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


    private void showDialog(final List<ModelFinance> data, final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Pilih Opsi");
        final String[] tipe = {"Ubah", "Hapus"};
        builder.setItems(tipe, (dialog, which) -> {
            switch (which) {
                case 0:
                    mPositionItem = position;
                    mCallbackOnActivityResult.updateDataFinance(data, position);
                    dialog.dismiss();
                    break;
                case 1:
                    viewModelFinance.removeFinance(data.get(position));
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
                    item.setIcon(R.drawable.ic_baseline_grid_view_24);
                    type_layout_manager = AdapterFinance.TYPE_LAYOUT_MANAGER.VERTICAL;
                } else if (type_layout_manager == AdapterFinance.TYPE_LAYOUT_MANAGER.VERTICAL) {
                    item.setIcon(R.drawable.ic_baseline_format_list_bulleted_24);
                    type_layout_manager = AdapterFinance.TYPE_LAYOUT_MANAGER.GRID;
                }
                if (adapter != null) {
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
        TextView txtNominal = dialogView.findViewById(R.id.txt_nominal);
        RecyclerView rvListType = dialogView.findViewById(R.id.rv_type);
        RecyclerView rvListNominal = dialogView.findViewById(R.id.rv_nominal);
        RecyclerView rvListPeriod = dialogView.findViewById(R.id.rv_periode);
        if (financeFilter.filterType.size() == 3)
            financeFilter.filterType.remove(2);
        txtNominal.setVisibility(View.GONE);

        AdapterFilter adapterFilterType = new AdapterFilter(getContext(), financeFilter.filterType, (data, position) -> {
            filterType = data.get(position).getValue();
        });

        rvListType.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvListType.setAdapter(adapterFilterType);


        AdapterFilter adapterFilterPeriod = new AdapterFilter(getContext(), financeFilter.filterPeriod, (data, position) ->
                filterPeriod = data.get(position).getValue());
        rvListPeriod.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvListPeriod.setAdapter(adapterFilterPeriod);

        txtSubmit.setOnClickListener(v -> {
            if (filterType == null) {
                Toast.makeText(getContext(), "Mohon untuk pilih tipe terlebih dahulu!", Toast.LENGTH_SHORT).show();
                return;
            }
            dialog.dismiss();
            loadDataByType(filterType);
        });

        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();

        wlp.gravity = Gravity.CENTER;
        wlp.flags &= ~WindowManager.LayoutParams.FLAG_BLUR_BEHIND;
        window.setAttributes(wlp);
        dialog.getWindow().setLayout(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

        dialog.show();
    }

    @SuppressLint("NewApi")
    private void loadDataByType(String type) {
        prevNextWeek = 0;

        nextWeekEnabled = localizedWeekHelper.getMonthLastWeekDay(prevNextWeek) <= today.getTimeInMillis();

        if (filterPeriod.equalsIgnoreCase("bulanan")) {
            viewModelFinance.getFinanceByTypeAndMonth(type, month).observe(getViewLifecycleOwner(), new Observer<List<ModelFinance>>() {
                @Override
                public void onChanged(List<ModelFinance> modelFinances) {
                    if (modelFinances != null) loadData(modelFinances);
                }
            });
        } else {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (nextWeekEnabled)
                    binding.btnNext.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.white)));
                else
                    binding.btnNext.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.colorGrey50)));
            }
            binding.btnNext.setEnabled(nextWeekEnabled);
            viewModelFinance.getFinanceByTypeAndWeek(type, localizedWeekHelper.getListWeek(localizedWeekHelper.getFirstDay(prevNextWeek - 7), localizedWeekHelper.getLastDay(prevNextWeek))).observe(getViewLifecycleOwner(), modelFinances -> {
                if (modelFinances != null) loadData(modelFinances);
            });
        }

        if (filterPeriod.equalsIgnoreCase("bulanan")) {
            binding.txtMonth.setText(Tools.getFormattedMonthTextSimple(today.getTimeInMillis()));
            binding.placeMonth.setEnabled(true);
            binding.btnNext.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.colorGrey50)));
            binding.btnNext.setEnabled(false);
        } else {
            binding.txtMonth.setText(Tools.convertDateFormatWeekText(localizedWeekHelper.getFirstDay(prevNextWeek - 7)) + " - " + Tools.convertDateFormatWeekText(localizedWeekHelper.getLastDay(prevNextWeek)));
            binding.placeMonth.setEnabled(false);
        }

//        binding.txtType.setText(filterType);
    }

    @SuppressLint("NewApi")
    private void loadDataFinanceByMonth(long date_ship_milis) {
        binding.txtMonth.setText(Tools.getFormattedMonthTextSimple(date_ship_milis));
//        binding.txtType.setText(filterType);

        month = Tools.getFormattedMonthSimple(date_ship_milis);

        if (date_ship_milis != today.getTimeInMillis()) {
            binding.btnNext.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.white)));
            binding.btnNext.setEnabled(true);
        }
        if (date_ship_milis >= today.getTimeInMillis()) {
            binding.btnNext.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.colorGrey50)));
            binding.btnNext.setEnabled(false);
        }

        viewModelFinance.getFinanceByTypeAndMonth(filterType, month).observe(getViewLifecycleOwner(), new Observer<List<ModelFinance>>() {
            @Override
            public void onChanged(List<ModelFinance> modelFinances) {
                if (modelFinances != null) loadData(modelFinances);
            }
        });
    }


    @SuppressLint("NotifyDataSetChanged")
    private void loadData(List<ModelFinance> data) {


        loadDataHeader(data);

        data = financeFilter.listAnalysis(data, filterType);
        adapter = new AdapterAnalysisFinance(getActivity(), data, this::showDialog);
        binding.rvList.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.rvList.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        binding.placeEmpty.setVisibility(adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
//        binding.txtType.setVisibility(adapter.getItemCount() > 0 ? View.VISIBLE : View.GONE);
        binding.chartAnalysis.setVisibility(adapter.getItemCount() > 0 ? View.VISIBLE : View.GONE);
        showPieChartData(data);
    }

    private void loadDataHeader(List<ModelFinance> data) {
        long total = (financeFilter.totalIncome(data) - financeFilter.totalExpense(data));
        binding.txtTotalIncome.setText(convertToCurrency(financeFilter.totalIncome(data)));
        binding.txtTotalExpense.setText(convertToCurrency(financeFilter.totalExpense(data)));
        binding.txtTotalAll.setText(convertToCurrency(total));
        binding.txtTotalAll.setTextColor(total < 0 ? ContextCompat.getColor(getContext(), R.color.red) : ContextCompat.getColor(getContext(), R.color.green));
    }

    private void showPieChartData(List<ModelFinance> data) {
        List<PieEntry> entries = new ArrayList<PieEntry>();
        List<LegendEntry> legendEntries = new ArrayList<>();
        for (ModelFinance modelIncome : data) {
            if (modelIncome.getTipe().contains(filterType))
                entries.add(new PieEntry((float) modelIncome.getJumlahValue(),  Tools.calculatePercentage(modelIncome.getJumlahValue(), financeFilter.totalValueByType(data, filterType)) + "%"));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setDrawValues(false);
        dataSet.setColor(Color.rgb(120, 255, 255));
        dataSet.setValueLineColor(Color.rgb(123, 212, 232));
        dataSet.setValueTextColor(R.color.white);

        dataSet.setDrawIcons(false);

        dataSet.setSliceSpace(2f);
        dataSet.setIconsOffset(new MPPointF(10, 40));
        dataSet.setSelectionShift(3f);

        ArrayList<Integer> colors = new ArrayList<Integer>();
        colors.add(Color.rgb(0,128,0));
        colors.add(Color.rgb(139,0,0));
        colors.add(Color.rgb(218,165,32));
        colors.add(Color.rgb(0, 128, 128));
        colors.add(Color.rgb(255, 69, 0));
        colors.add(Color.rgb(46, 139, 87));
        dataSet.setColors(colors);
        PieData pieData = new PieData(dataSet);

        for (int i = 0; i < data.size(); i++) {
            legendEntries.add(new LegendEntry(data.get(i).getKategori(), Legend.LegendForm.SQUARE, 10f, 2f, null, colors.get(i % colors.size())));
        }

        Legend l = binding.chartAnalysis.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setDrawInside(false);
        l.setXEntrySpace(10f);
        l.setYEntrySpace(0f);
        l.setYOffset(0f);
        l.setTextSize(13);

        l.setCustom(legendEntries);
        binding.chartAnalysis.animateXY(2000, 2000);
        binding.chartAnalysis.getDescription().setEnabled(false);
        binding.chartAnalysis.setCenterText(filterType);
        binding.chartAnalysis.setCenterTextSize(17);
        binding.chartAnalysis.setCenterTextColor(ContextCompat.getColor(getContext(), R.color.blackTextColor));
        binding.chartAnalysis.setCenterTextTypeface(Typeface.DEFAULT_BOLD);
        binding.chartAnalysis.setPaddingRelative(10, 10, 10, 10);

        binding.chartAnalysis.offsetLeftAndRight(0);
        binding.chartAnalysis.setExtraOffsets(0,0,30,0);
        binding.chartAnalysis.getCircleBox().offset(0,0);

        binding.chartAnalysis.setDrawHoleEnabled(true);
        binding.chartAnalysis.setHoleColor(Color.WHITE);

        binding.chartAnalysis.setTransparentCircleColor(Color.WHITE);
        binding.chartAnalysis.setTransparentCircleAlpha(110);

        binding.chartAnalysis.setHoleRadius(58f);
        binding.chartAnalysis.setTransparentCircleRadius(61f);

        binding.chartAnalysis.setDrawCenterText(true);
        binding.chartAnalysis.setEntryLabelColor(Color.WHITE);
        binding.chartAnalysis.setRotationAngle(0);
        // enable rotation of the binding.chartAnalysis by touch
        binding.chartAnalysis.setRotationEnabled(true);
        binding.chartAnalysis.setHighlightPerTapEnabled(true);
        binding.chartAnalysis.setData(pieData);
        binding.chartAnalysis.invalidate();
    }

}
