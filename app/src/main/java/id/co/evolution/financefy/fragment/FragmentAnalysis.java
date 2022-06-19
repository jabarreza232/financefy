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
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.LargeValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.MPPointF;
import com.whiteelephant.monthpicker.MonthPickerDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.ConcurrentModificationException;
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
import id.co.evolution.financefy.helper.MyValueFormatter;
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
    TYPE_CHART typeChart = TYPE_CHART.PIE_CHART;
    Dialog dialog;
    LayoutInflater inflater;
    View dialogView;
    FinanceFilter financeFilter;
    String filterType, filterPeriod = "Bulanan";
    String month;
    LocalizedWeekHelper localizedWeekHelper;
    int prevNextWeek = 0;
    boolean nextWeekEnabled;
    @Inject
    FinanceRepository financeRepository;
    CallbackOnActivityResult mCallbackOnActivityResult;
    int mPositionItem;
    long date_ship_millis;

    public enum TYPE_CHART {
        PIE_CHART,
        BAR_CHART
    }

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
        filterType = "Pengeluaran";
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
        MenuItem item = menu.getItem(1);
        item.setIcon(ContextCompat.getDrawable(getContext(), R.drawable.ic_baseline_bar_chart_24));
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
                if (typeChart == TYPE_CHART.PIE_CHART) {
                    item.setIcon(R.drawable.ic_baseline_bar_chart_24);
                    typeChart = TYPE_CHART.BAR_CHART;
                } else if (typeChart == TYPE_CHART.BAR_CHART) {
                    item.setIcon(R.drawable.ic_baseline_pie_chart_24);
                    typeChart = TYPE_CHART.PIE_CHART;
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
        showBarChartData(data);
        data = financeFilter.listAnalysis(data, filterType);
        adapter = new AdapterAnalysisFinance(getActivity(), data, this::showDialog);
        binding.rvList.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.rvList.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        binding.placeEmpty.setVisibility(adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
//        binding.txtType.setVisibility(adapter.getItemCount() > 0 ? View.VISIBLE : View.GONE);
//        binding.pieChartAnalysis.setVisibility(adapter.getItemCount() > 0 ? View.VISIBLE : View.GONE);
//        showPieChartData(data);

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
                entries.add(new PieEntry((float) modelIncome.getJumlahValue(), Tools.calculatePercentage(modelIncome.getJumlahValue(), financeFilter.totalValueByType(data, filterType)) + "%"));
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
        colors.add(Color.rgb(0, 128, 0));
        colors.add(Color.rgb(139, 0, 0));
        colors.add(Color.rgb(218, 165, 32));
        colors.add(Color.rgb(0, 128, 128));
        colors.add(Color.rgb(255, 69, 0));
        colors.add(Color.rgb(46, 139, 87));
        dataSet.setColors(colors);
        PieData pieData = new PieData(dataSet);

        for (int i = 0; i < data.size(); i++) {
            legendEntries.add(new LegendEntry(data.get(i).getKategori(), Legend.LegendForm.SQUARE, 10f, 2f, null, colors.get(i % colors.size())));
        }

        Legend l = binding.pieChartAnalysis.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setDrawInside(false);
        l.setXEntrySpace(10f);
        l.setYEntrySpace(0f);
        l.setYOffset(0f);
        l.setTextSize(13);

        l.setCustom(legendEntries);
        binding.pieChartAnalysis.animateXY(2000, 2000);
        binding.pieChartAnalysis.getDescription().setEnabled(false);
        binding.pieChartAnalysis.setCenterText(filterType);
        binding.pieChartAnalysis.setCenterTextSize(17);
        binding.pieChartAnalysis.setCenterTextColor(ContextCompat.getColor(getContext(), R.color.blackTextColor));
        binding.pieChartAnalysis.setCenterTextTypeface(Typeface.DEFAULT_BOLD);
        binding.pieChartAnalysis.setPaddingRelative(10, 10, 10, 10);

        binding.pieChartAnalysis.offsetLeftAndRight(0);
        binding.pieChartAnalysis.setExtraOffsets(0, 0, 30, 0);
        binding.pieChartAnalysis.getCircleBox().offset(0, 0);

        binding.pieChartAnalysis.setDrawHoleEnabled(true);
        binding.pieChartAnalysis.setHoleColor(Color.WHITE);

        binding.pieChartAnalysis.setTransparentCircleColor(Color.WHITE);
        binding.pieChartAnalysis.setTransparentCircleAlpha(110);

        binding.pieChartAnalysis.setHoleRadius(58f);
        binding.pieChartAnalysis.setTransparentCircleRadius(61f);

        binding.pieChartAnalysis.setDrawCenterText(true);
        binding.pieChartAnalysis.setEntryLabelColor(Color.WHITE);
        binding.pieChartAnalysis.setRotationAngle(0);
        // enable rotation of the binding.pieChartAnalysis by touch
        binding.pieChartAnalysis.setRotationEnabled(true);
        binding.pieChartAnalysis.setHighlightPerTapEnabled(true);
        binding.pieChartAnalysis.setData(pieData);
        binding.pieChartAnalysis.invalidate();
    }

    private void showBarChartData(List<ModelFinance> data) {
        float groupSpace = 0.08f;
        float barSpace = 0.02f; // x3 DataSet
        float barWidth = 0.28f; // x3 DataSet
        // (0.2 + 0.03) * 4 + 0.08 = 1.00 -> interval per "group"

        List<ModelNestedFinance> listNestedFinance = financeFilter.filterNestedFinance(data);
//        float barWidth = (1 - groupSpace) / listNestedFinance.size() - barSpace; // x3 DataSet
        listNestedFinance = financeFilter.filterPeriod("terlama", listNestedFinance);


        List<String> listDate = new ArrayList<>();

        List<IBarDataSet> barDataSetsIncome = new ArrayList<>();
        List<IBarDataSet> barDataSetsExpense = new ArrayList<>();

        if (filterType.equalsIgnoreCase("Pemasukan")){
            ArrayList<BarEntry> entriesCompanyResults = new ArrayList<>();
            ArrayList<BarEntry> entriesBonus = new ArrayList<>();
            ArrayList<BarEntry> entriesSalary = new ArrayList<>();

            for (int i = 0; i < listNestedFinance.size(); i++) {
                ModelNestedFinance modelNestedFinance = listNestedFinance.get(i);
                List<ModelFinance> listData = financeFilter.listAnalysis(modelNestedFinance.getFinances(), filterType);

                listDate.add(modelNestedFinance.getDefaultDate());
                float fGaji=0;
                float fBonus=0;
                float fHasilUsaha=0;

                for (ModelFinance modelIncome : listData) {
                    Log.e("cek_date", modelNestedFinance.getDefaultDate() + " : " + modelIncome.getDefaultDate() + " : " + modelIncome.getKategori() + " : " + modelIncome.getJumlahValue());
                    if (modelIncome.getKategori().contains(financeFilter.mapIncome.get("hasil_usaha"))) {
                        fHasilUsaha = (float) modelIncome.getJumlahValue();
                    }
                    if (modelIncome.getKategori().contains(financeFilter.mapIncome.get("gaji"))) {
                        fGaji = (float) modelIncome.getJumlahValue();
                    }

                    if (modelIncome.getKategori().contains(financeFilter.mapIncome.get("bonus"))) {
                        fBonus = (float) modelIncome.getJumlahValue();
                    }
                }

                entriesCompanyResults.add(new BarEntry(i, fHasilUsaha, financeFilter.mapIncome.get("hasil_usaha")));
                entriesBonus.add(new BarEntry(i, fBonus, financeFilter.mapIncome.get("bonus")));
                entriesSalary.add(new BarEntry(i,fGaji, financeFilter.mapIncome.get("gaji")));
            }

            BarDataSet barDataSetCompanyResult = new BarDataSet(entriesCompanyResults, financeFilter.mapIncome.get("hasil_usaha"));
            BarDataSet barDataSetBonus = new BarDataSet(entriesBonus, financeFilter.mapIncome.get("bonus"));
            BarDataSet barDataSetSalary = new BarDataSet(entriesSalary, financeFilter.mapIncome.get("gaji"));


            barDataSetBonus.setColor(ContextCompat.getColor(getContext(), R.color.blueColor));
            barDataSetCompanyResult.setColor(ContextCompat.getColor(getContext(), R.color.red));
            barDataSetSalary.setColor(ContextCompat.getColor(getContext(), R.color.colorTextYellow));

            barDataSetsIncome.add(barDataSetCompanyResult);
            barDataSetsIncome.add(barDataSetBonus);
            barDataSetsIncome.add(barDataSetSalary);
        }else{
            ArrayList<BarEntry> entriesGeneralShopping = new ArrayList<>();
            ArrayList<BarEntry> entriesFood = new ArrayList<>();
            ArrayList<BarEntry> entriesPulse = new ArrayList<>();
            ArrayList<BarEntry> entriesTransportation = new ArrayList<>();
            ArrayList<BarEntry> entriesBill = new ArrayList<>();
            ArrayList<BarEntry> entriesInternetPackages = new ArrayList<>();
             barSpace = 0.02f; // x3 DataSet
             barWidth = 0.14f; // x3 DataSet
            for (int i = 0; i < listNestedFinance.size(); i++) {
                ModelNestedFinance modelNestedFinance = listNestedFinance.get(i);
                List<ModelFinance> listData = financeFilter.listAnalysis(modelNestedFinance.getFinances(), filterType);

                listDate.add(modelNestedFinance.getDefaultDate());
                float fGeneralShopping=0,fFood=0,fPulse=0,fTransportation=0,fBill=0,fInternetPackages=0;


                for (ModelFinance modelExpense : listData) {
                    Log.e("cek_date", modelNestedFinance.getDefaultDate() + " : " + modelExpense.getDefaultDate() + " : " + modelExpense.getKategori() + " : " + modelExpense.getJumlahValue());
                    if (modelExpense.getKategori().contains(financeFilter.mapExpense.get("belanja_umum"))) {
                        fGeneralShopping = (float) modelExpense.getJumlahValue();
                    }
                    if (modelExpense.getKategori().contains(financeFilter.mapExpense.get("makanan"))) {
                        fFood = (float) modelExpense.getJumlahValue();
                    }

                    if (modelExpense.getKategori().contains(financeFilter.mapExpense.get("pulsa_hp"))) {
                        fPulse = (float) modelExpense.getJumlahValue();
                    }
                    if (modelExpense.getKategori().contains(financeFilter.mapExpense.get("transportasi"))) {
                        fTransportation = (float) modelExpense.getJumlahValue();
                    }
                    if (modelExpense.getKategori().contains(financeFilter.mapExpense.get("paket_internet"))) {
                        fInternetPackages = (float) modelExpense.getJumlahValue();
                    }
                    if (modelExpense.getKategori().contains(financeFilter.mapExpense.get("tagihan"))) {
                        fBill = (float) modelExpense.getJumlahValue();
                    }
                }

                entriesGeneralShopping.add(new BarEntry(i, fGeneralShopping, financeFilter.mapExpense.get("belanja_umum")));
                entriesFood.add(new BarEntry(i, fFood, financeFilter.mapExpense.get("makanan")));
                entriesPulse.add(new BarEntry(i, fPulse, financeFilter.mapExpense.get("pulsa_hp")));
                entriesTransportation.add(new BarEntry(i, fTransportation, financeFilter.mapExpense.get("transportasi")));
                entriesBill.add(new BarEntry(i, fBill, financeFilter.mapExpense.get("tagihan")));
                entriesInternetPackages.add(new BarEntry(i,fInternetPackages, financeFilter.mapExpense.get("paket_internet")));
            }

            BarDataSet barDataSetGeneralShopping = new BarDataSet(entriesGeneralShopping, financeFilter.mapExpense.get("belanja_umum"));
            BarDataSet barDataSetFood = new BarDataSet(entriesFood, financeFilter.mapExpense.get("makanan"));
            BarDataSet barDataSetPulse = new BarDataSet(entriesPulse, financeFilter.mapExpense.get("pulsa_hp"));
            BarDataSet barDataSetTransportation = new BarDataSet(entriesTransportation, financeFilter.mapExpense.get("transportasi"));
            BarDataSet barDataSetBill = new BarDataSet(entriesBill, financeFilter.mapExpense.get("tagihan"));
            BarDataSet barDataSetInternetPackages = new BarDataSet(entriesInternetPackages, financeFilter.mapExpense.get("paket_internet"));


            barDataSetGeneralShopping.setColor(ContextCompat.getColor(getContext(), R.color.blueColor));
            barDataSetFood.setColor(ContextCompat.getColor(getContext(), R.color.red));
            barDataSetPulse.setColor(ContextCompat.getColor(getContext(), R.color.colorTextYellow));
            barDataSetTransportation.setColor(ContextCompat.getColor(getContext(), R.color.colorTextGreen));
            barDataSetBill.setColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
            barDataSetInternetPackages.setColor(ContextCompat.getColor(getContext(), R.color.colorTextOrange));


            barDataSetsExpense.add(barDataSetGeneralShopping);
            barDataSetsExpense.add(barDataSetFood);
            barDataSetsExpense.add(barDataSetPulse);
            barDataSetsExpense.add(barDataSetTransportation);
            barDataSetsExpense.add(barDataSetBill);
            barDataSetsExpense.add(barDataSetInternetPackages);
        }






        BarData barData;

        if(filterType.equalsIgnoreCase("Pemasukan"))
            barData= new BarData(barDataSetsIncome);
        else
            barData= new BarData(barDataSetsExpense);

        barData.setValueFormatter(new LargeValueFormatter());
        barData.setValueTextSize(11);
        binding.barChartAnalysis.setData(barData);
        // scaling can now only be done on x- and y-axis separately
        binding.barChartAnalysis.setPinchZoom(false);

        binding.barChartAnalysis.setDrawBarShadow(false);

        binding.barChartAnalysis.setDrawGridBackground(false);


        binding.barChartAnalysis.getDescription().setEnabled(false);
        Legend l = binding.barChartAnalysis.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        l.setForm(Legend.LegendForm.SQUARE);
        l.setFormSize(9f);
        l.setTextSize(11f);
        l.setXEntrySpace(4f);
        l.setWordWrapEnabled(true);
        l.setYOffset(6f);


        XAxis xAxis = binding.barChartAnalysis.getXAxis();
        xAxis.setGranularity(1f);
        xAxis.setCenterAxisLabels(true);

        xAxis.setDrawGridLines(true);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        xAxis.setLabelCount(listDate.size());
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                Log.e("cek_value", value + "");
                String date = "";
                if(value>-1&&value<listDate.size()){
                    date=Tools.convertDateFormatAnalysis(listDate.get((int)value));
                }

//                for (String hashDate : listDate) {
//                    if (Integer.parseInt(hashDate.split("-")[0]) == ((int) value)) {
//                        date = Tools.convertDateFormatAnalysis(hashDate);
//                    }
//                }
                return date;
            }
        });
//        binding.barChartAnalysis.setDrawValueAboveBar(false);
        binding.barChartAnalysis.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                Log.e("cek_entry", (String) e.getData());
            }

            @Override
            public void onNothingSelected() {

            }
        });
        // specify the width each bar should have
        binding.barChartAnalysis.getBarData().setBarWidth(barWidth);
        binding.barChartAnalysis.getXAxis().setAxisMinimum(0);
//        Tools.getDateFromDateFormat(listDate.get(0))
        // restrict the x-axis range
        binding.barChartAnalysis.getXAxis().setAxisMaximum(0 + binding.barChartAnalysis.getBarData().getGroupWidth(groupSpace, barSpace) * listDate.size());
        binding.barChartAnalysis.groupBars(0, groupSpace, barSpace);


        YAxis leftAxis = binding.barChartAnalysis.getAxisLeft();

        leftAxis.setDrawGridLines(false);
        leftAxis.setSpaceTop(35f);
        leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)
        binding.barChartAnalysis.getAxisRight().setEnabled(false);
        binding.barChartAnalysis.invalidate();
    }
}
