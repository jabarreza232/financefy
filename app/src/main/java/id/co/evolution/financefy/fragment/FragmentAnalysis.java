package id.co.evolution.financefy.fragment;

import static id.co.evolution.financefy.helper.Tools.convertToCurrency;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import id.co.evolution.financefy.MainActivity;
import id.co.evolution.financefy.R;
import id.co.evolution.financefy.adapter.AdapterAnalysisFinance;
import id.co.evolution.financefy.callback.CallbackOnActivityResult;
import id.co.evolution.financefy.databinding.FragmentAnalysisBinding;
import id.co.evolution.financefy.dialog.DialogFilterFinance;
import id.co.evolution.financefy.dialog.DialogMonthPicker;
import id.co.evolution.financefy.helper.FinanceFilter;
import id.co.evolution.financefy.helper.LocalizedWeekHelper;
import id.co.evolution.financefy.helper.MyMarkView;
import id.co.evolution.financefy.helper.TinyDb;
import id.co.evolution.financefy.helper.Tools;
import id.co.evolution.financefy.model.ModelFinance;
import id.co.evolution.financefy.model.ModelNestedFinance;
import id.co.evolution.financefy.model.ModelUser;
import id.co.evolution.financefy.model.ModelUserWithFinance;
import id.co.evolution.financefy.repository.FinanceRepository;
import id.co.evolution.financefy.repository.UserRepository;
import id.co.evolution.financefy.viewmodel.ViewModelFinance;
import id.co.evolution.financefy.viewmodel.ViewModelUser;

@AndroidEntryPoint
public class FragmentAnalysis extends Fragment {
    public Calendar today;
    Calendar prevNextMonth;
    List<ModelFinance> dataFinance = new ArrayList<>();
    List<ModelFinance> finances = new ArrayList<>();
    FragmentAnalysisBinding binding;
    ViewModelFinance viewModelFinance;
    ViewModelUser viewModelUser;
    AdapterAnalysisFinance adapter;
    TYPE_CHART typeChart = TYPE_CHART.BAR_CHART;
    @Inject
    FinanceFilter financeFilter;
    String filterType, filterPeriod;
    String month;
    TypedValue value = new TypedValue();
    @Inject
    LocalizedWeekHelper localizedWeekHelper;
    int prevNextWeek = 0;
    boolean nextWeekEnabled;
    @Inject
    FinanceRepository financeRepository;
    @Inject
    UserRepository userRepository;
    ModelUser user;

    public enum CATEGORY_INCOME {
        HASIL_USAHA,
        BONUS,
        GAJI
    }

    public enum CATEGORY_EXPENSE {
        BELANJA_UMUM,
        MAkANAN,
        PULSA_HP,
        TRANSPORTASI,
        TAGIHAN,
        PAKET_INTERNET
    }

    CallbackOnActivityResult mCallbackOnActivityResult;
    int mPositionItem;
    long date_ship_millis;

    MainActivity mainActivity;
    @Inject
    TinyDb tinyDb;
    public enum TYPE_CHART {
        PIE_CHART,
        BAR_CHART
    }

    public FragmentAnalysis() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivity = ((MainActivity) context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_analysis, container, false);
        dataFinance = ((MainActivity) requireActivity()).dataFinance;
        user = mainActivity.user;
        Tools.setBackgroundColorView(binding.llAppBar,mainActivity.modelPrimaryColor);
        getActivity().getTheme().resolveAttribute(android.R.attr.textColorPrimary, value, true);

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
        filterType = getString(R.string.pemasukan);
        filterPeriod = getString(R.string.bulanan);
        viewModelFinance = new ViewModelProvider(this).get(ViewModelFinance.class);
        viewModelUser = new ViewModelProvider(this).get(ViewModelUser.class);

        viewModelUser.init(userRepository);
        viewModelFinance.init(financeRepository);

        viewModelUser.getFinanceByUserId(user.getId()).observe(getViewLifecycleOwner(), new Observer<ModelUserWithFinance>() {
            @Override
            public void onChanged(ModelUserWithFinance modelUserWithFinances) {
                Log.e("TAG", "onChanged: " + new Gson().toJson(modelUserWithFinances));
                loadDataFinanceByMonth(date_ship_millis);
            }
        });


        binding.placeMonth.setOnClickListener(v -> showDialogMonthPicker());

        binding.btnNext.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(getActivity().getApplicationContext(), R.color.colorGrey50)));
        binding.btnNext.setEnabled(false);

        binding.btnPrev.setOnClickListener(v -> {
            if (filterPeriod.equalsIgnoreCase(getString(R.string.bulanan))) {
                prevNextMonth.get(Calendar.YEAR);
                prevNextMonth.add(Calendar.MONTH, -1);
                long date_ship_milisecond = prevNextMonth.getTimeInMillis();
                date_ship_millis = date_ship_milisecond;
                loadDataFinanceByMonth(date_ship_milisecond);
            } else {
                prevNextWeek = prevNextWeek - 7;

                nextWeekEnabled = localizedWeekHelper.getMonthLastWeekDay(prevNextWeek) <= today.getTimeInMillis();
                if (nextWeekEnabled)
                    binding.btnNext.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(getActivity().getApplicationContext(), R.color.white)));
                else
                    binding.btnNext.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(getActivity().getApplicationContext(), R.color.colorGrey50)));

                binding.btnNext.setEnabled(nextWeekEnabled);
                binding.txtMonth.setText(Tools.convertDateFormatWeekText(localizedWeekHelper.getFirstDay(prevNextWeek - 7)) + " - " + Tools.convertDateFormatWeekText(localizedWeekHelper.getLastDay(prevNextWeek)));
                binding.txtMonth.setEnabled(false);
                viewModelFinance.getFinanceByTypeAndWeek(filterType, getListDateWeek(), user.getId()).observe(getViewLifecycleOwner(), modelFinances -> {
                    if (modelFinances != null) {
                        loadData(modelFinances);
                    }
                });
            }

        });

        binding.btnNext.setOnClickListener(v -> {
            if (filterPeriod.equalsIgnoreCase(getString(R.string.bulanan))) {
                prevNextMonth.get(Calendar.YEAR);
                prevNextMonth.add(Calendar.MONTH, 1);
                long date_ship_milisecond = prevNextMonth.getTimeInMillis();
                date_ship_millis = date_ship_milisecond;
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
                viewModelFinance.getFinanceByTypeAndWeek(filterType, getListDateWeek(), user.getId()).observe(getViewLifecycleOwner(), modelFinances -> {
                    if (modelFinances != null) {
                        loadData(modelFinances);
                    }
                });
            }
        });

        Log.e("TAG", "cek_enum_category: " + new Gson().toJson(CATEGORY_INCOME.BONUS.name()));
        initiateSayHaloWithTime();
    }

    private void initiateSayHaloWithTime() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
        String now = simpleDateFormat.format(new Date());
        Log.e("waktu", now + " - " + now.substring(0, 1) + " - " + now.substring(0, 2));
        if (Integer.parseInt(now.substring(0, 2)) >= 4 && Integer.parseInt(now.substring(0, 2)) < 10) {
            binding.txtName.setText("Selamat Pagi, " + user.getName() + "!");
        } else if (Integer.parseInt(now.substring(0, 2)) >= 10 && Integer.parseInt(now.substring(0, 2)) < 15) {
            binding.txtName.setText("Selamat Siang, " + user.getName() + "!");
        } else if (Integer.parseInt(now.substring(0, 2)) >= 15 && Integer.parseInt(now.substring(0, 2)) < 18) {
            binding.txtName.setText("Selamat Sore, " + user.getName() + "!");
        } else {
            binding.txtName.setText("Selamat Malam, " + user.getName() + "!");
        }
        binding.txtTypeAccount.setText(user.getType()+" - "+user.getCategory());
    }

    private void showDialogMonthPicker() {
        DialogMonthPicker dialogMonthPicker = new DialogMonthPicker(getActivity(), today, dataFinance, new DialogMonthPicker.DialogMonthPickerCallback() {
            @Override
            public void onDateSet(int selectedMonth, int selectedYear) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.YEAR, selectedYear);
                calendar.set(Calendar.MONTH, selectedMonth);
                prevNextMonth.set(Calendar.YEAR, selectedYear);
                prevNextMonth.set(Calendar.MONTH, selectedMonth);
                long date_ship_milis = calendar.getTimeInMillis();
                date_ship_millis = date_ship_milis;
                financeFilter.resetFilter();
                loadDataFinanceByMonth(date_ship_milis);
            }
        });
        dialogMonthPicker.showDialogMonthPicker();
    }


    private void showDialog(final List<ModelFinance> data, final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Pilih Opsi");
        final String[] tipe = {"Ubah", "Hapus"};
        builder.setItems(tipe, (dialog, which) -> {
            switch (which) {
                case 0:
                    mPositionItem = position;
                    mCallbackOnActivityResult.updateDataFinance(data, position,user.getId());
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
                    if (finances.size() > 0) new BarChartAsyncTask(finances).execute();
                } else if (typeChart == TYPE_CHART.BAR_CHART) {
                    item.setIcon(R.drawable.ic_baseline_pie_chart_24);
                    typeChart = TYPE_CHART.PIE_CHART;
                    if (finances.size() > 0)
                        new PieChartAsyncTask(financeFilter.listAnalysis(finances, filterType)).execute();
                }
                binding.barChartAnalysis.setVisibility(finances.size() > 0 && typeChart == TYPE_CHART.BAR_CHART ? View.VISIBLE : View.GONE);
                binding.pieChartAnalysis.setVisibility(finances.size() > 0 && typeChart == TYPE_CHART.PIE_CHART ? View.VISIBLE : View.GONE);
                return true;

            default:
                break;
        }
        return true;
    }


    private void showDialogFilter() {
        DialogFilterFinance dialog = new DialogFilterFinance(getContext(), getLayoutInflater(), new DialogFilterFinance.DialogFilterFinanceCallback() {
            @Override
            public void resultFilterType(@NonNull String result) {
                filterType = result;
            }

            @Override
            public void resultFilterNominal(@NonNull String result) {

            }

            @Override
            public void resultFilterPeriod(@NonNull String result) {
                filterPeriod = result;
            }

            @Override
            public void onSubmit() {
                if (filterType == null) {
                    Toast.makeText(getContext(), "Mohon untuk pilih tipe terlebih dahulu!", Toast.LENGTH_SHORT).show();
                    return;
                }
                loadDataByType(filterType);
            }
        });

        dialog.showDialogFilterFinance(financeFilter.filterType, financeFilter.filterNominal, financeFilter.filterPeriod, true);
    }

    @SuppressLint("NewApi")
    private void loadDataByType(String type) {
        prevNextWeek = 0;

        nextWeekEnabled = localizedWeekHelper.getMonthLastWeekDay(prevNextWeek) <= today.getTimeInMillis();

        if (filterPeriod.equalsIgnoreCase(getString(R.string.bulanan))) {
            viewModelFinance.getFinanceByTypeAndMonth(type, month, user.getId()).observe(getViewLifecycleOwner(), new Observer<List<ModelFinance>>() {
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
            viewModelFinance.getFinanceByTypeAndWeek(type, getListDateWeek(), user.getId()).observe(getViewLifecycleOwner(), modelFinances -> {
                if (modelFinances != null) loadData(modelFinances);
            });
        }

        if (filterPeriod.equalsIgnoreCase(getString(R.string.bulanan))) {
            binding.txtMonth.setText(Tools.getFormattedMonthTextSimple(date_ship_millis));
            binding.placeMonth.setEnabled(true);

            //TODO jika range tanggal per minggu nya kurang dari tanggal hari maka bisa melakukan tombol next tanggal per minggunya
            if (date_ship_millis < today.getTimeInMillis())
                binding.btnNext.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.white)));
            else
                binding.btnNext.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.colorGrey50)));
            binding.btnNext.setEnabled(date_ship_millis < today.getTimeInMillis());
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

        viewModelFinance.getFinanceByTypeAndMonth(filterType, month, user.getId()).observe(getViewLifecycleOwner(), new Observer<List<ModelFinance>>() {
            @Override
            public void onChanged(List<ModelFinance> modelFinances) {
                if (modelFinances != null) loadData(modelFinances);
            }
        });
    }


    @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
    private void loadData(List<ModelFinance> data) {
        loadDataHeader(data);
        finances = data;
        data = financeFilter.listAnalysis(data, filterType);

        adapter = new AdapterAnalysisFinance(getActivity(), data, this::showDialog);
        binding.rvList.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.rvList.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        binding.placeEmpty.setVisibility(adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
        binding.txtEmpty.setText("Tidak ada data " + filterType.toLowerCase());
        if (typeChart == TYPE_CHART.BAR_CHART) {
            new BarChartAsyncTask(finances).execute();
            binding.barChartAnalysis.setVisibility(data.size() > 0 ? View.VISIBLE : View.GONE);
        } else {
            new PieChartAsyncTask(financeFilter.listAnalysis(finances, filterType)).execute();
            binding.pieChartAnalysis.setVisibility(data.size() > 0 ? View.VISIBLE : View.GONE);
        }
    }


    private void loadDataHeader(List<ModelFinance> data) {
        long total = (financeFilter.totalIncome(data) - financeFilter.totalExpense(data));
        binding.txtTotalIncome.setText(convertToCurrency(financeFilter.totalIncome(data)));
        binding.txtTotalExpense.setText(convertToCurrency(financeFilter.totalExpense(data)));
        binding.txtTotalAll.setText(convertToCurrency(total));
        binding.txtTotalAll.setTextColor(total < 0 ? ContextCompat.getColor(getContext(), R.color.red) : ContextCompat.getColor(getContext(), R.color.green));
    }


    private class PieChartAsyncTask extends AsyncTask<Void, PieDataSet, PieDataSet> {

        List<ModelFinance> data;
        List<LegendEntry> legendEntries = new ArrayList<>();
        ArrayList<Integer> colors = new ArrayList<Integer>();

        public PieChartAsyncTask(List<ModelFinance> data) {
            this.data = data;
        }

        @Override
        protected PieDataSet doInBackground(Void... voids) {
            List<PieEntry> entries = new ArrayList<PieEntry>();

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


            colors.add(Color.rgb(0, 128, 0));
            colors.add(Color.rgb(139, 0, 0));
            colors.add(Color.rgb(218, 165, 32));
            colors.add(Color.rgb(0, 128, 128));
            colors.add(Color.rgb(255, 69, 0));
            colors.add(Color.rgb(46, 139, 87));
            dataSet.setColors(colors);
            return dataSet;
        }

        @SuppressLint("ResourceType")
        @Override
        protected void onPostExecute(PieDataSet pieDataSet) {
            super.onPostExecute(pieDataSet);

            PieData pieData = new PieData(pieDataSet);

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
            l.setTextColor(ContextCompat.getColor(getContext(),value.resourceId));

            l.setCustom(legendEntries);
            binding.pieChartAnalysis.animateXY(2000, 2000);
            binding.pieChartAnalysis.getDescription().setEnabled(false);
            binding.pieChartAnalysis.setCenterText(filterType);
            binding.pieChartAnalysis.setCenterTextSize(17);
            binding.pieChartAnalysis.setNoDataTextColor(Color.BLACK);
            binding.pieChartAnalysis.setCenterTextColor(Color.BLACK);
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
    }

    private class BarChartAsyncTask extends AsyncTask<Void, List<IBarDataSet>, List<IBarDataSet>> {
        // (0.2 + 0.03) * 4 + 0.08 = 1.00 -> interval per "group"
        float groupSpace = 0.08f;
        float barSpace = 0.02f; // x3 DataSet
        float barWidth = 0.28f; // x3 DataSet

        List<ModelFinance> data;
        List<String> listDate;

        public BarChartAsyncTask(List<ModelFinance> data) {
            this.data = data;
        }

        @Override
        protected List<IBarDataSet> doInBackground(Void... voids) {
            List<ModelNestedFinance> listNestedFinance = financeFilter.filterNestedFinance(data);
//        float barWidth = (1 - groupSpace) / listNestedFinance.size() - barSpace; // x3 DataSet
            listNestedFinance = financeFilter.filterPeriod("terlama", listNestedFinance);
            for (ModelNestedFinance modelNestedFinance : listNestedFinance) {
                Log.e("cek_date_nested", modelNestedFinance.getDate());
            }

            listDate = new ArrayList<>();

            List<IBarDataSet> barDataSets = new ArrayList<>();


            if (filterType.equalsIgnoreCase(getString(R.string.pemasukan))) {
                ArrayList<BarEntry> entriesCompanyResults = new ArrayList<>();
                ArrayList<BarEntry> entriesBonus = new ArrayList<>();
                ArrayList<BarEntry> entriesSalary = new ArrayList<>();

                for (int i = 0; i < listNestedFinance.size(); i++) {
                    ModelNestedFinance modelNestedFinance = listNestedFinance.get(i);
                    List<ModelFinance> listData = financeFilter.listAnalysis(modelNestedFinance.getFinances(), filterType);

                    listDate.add(modelNestedFinance.getDefaultDate());
                    float fGaji = 0;
                    float fBonus = 0;
                    float fHasilUsaha = 0;

                    for (ModelFinance modelIncome : listData) {
                        Log.e("cek_date", modelNestedFinance.getDefaultDate() + " : " + modelIncome.getDefaultDate() + " : " + modelIncome.getKategori() + " : " + modelIncome.getJumlahValue());
                        if (modelIncome.getKategoriWithSeparator().contains(CATEGORY_INCOME.HASIL_USAHA.name().toLowerCase())) {
                            fHasilUsaha = (float) modelIncome.getJumlahValue();
                        }
                        if (modelIncome.getKategoriWithSeparator().contains(CATEGORY_INCOME.GAJI.name().toLowerCase())) {
                            fGaji = (float) modelIncome.getJumlahValue();
                        }

                        if (modelIncome.getKategoriWithSeparator().contains(CATEGORY_INCOME.BONUS.name().toLowerCase())) {
                            fBonus = (float) modelIncome.getJumlahValue();
                        }
                    }

                    entriesCompanyResults.add(new BarEntry(i, fHasilUsaha, CATEGORY_INCOME.HASIL_USAHA.name().toLowerCase()));
                    entriesBonus.add(new BarEntry(i, fBonus, CATEGORY_INCOME.BONUS.name().toLowerCase()));
                    entriesSalary.add(new BarEntry(i, fGaji, CATEGORY_INCOME.GAJI.name().toLowerCase()));
                }

                BarDataSet barDataSetCompanyResult = new BarDataSet(entriesCompanyResults, CATEGORY_INCOME.HASIL_USAHA.name().toLowerCase());
                BarDataSet barDataSetBonus = new BarDataSet(entriesBonus, CATEGORY_INCOME.BONUS.name().toLowerCase());
                BarDataSet barDataSetSalary = new BarDataSet(entriesSalary, CATEGORY_INCOME.GAJI.name().toLowerCase());


                barDataSetBonus.setColor(ContextCompat.getColor(getContext(), R.color.blueColor));
                barDataSetCompanyResult.setColor(ContextCompat.getColor(getContext(), R.color.red));
                barDataSetSalary.setColor(ContextCompat.getColor(getContext(), R.color.colorTextYellow));

                barDataSets.add(barDataSetCompanyResult);
                barDataSets.add(barDataSetBonus);
                barDataSets.add(barDataSetSalary);
            } else {
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
                    float fGeneralShopping = 0, fFood = 0, fPulse = 0, fTransportation = 0, fBill = 0, fInternetPackages = 0;


                    for (ModelFinance modelExpense : listData) {
                        Log.e("cek_date", modelNestedFinance.getDefaultDate() + " : " + modelExpense.getDefaultDate() + " : " + modelExpense.getKategori() + " : " + modelExpense.getJumlahValue());
                        if (modelExpense.getKategoriWithSeparator().contains(CATEGORY_EXPENSE.BELANJA_UMUM.name().toLowerCase())) {
                            fGeneralShopping = (float) modelExpense.getJumlahValue();
                        }
                        if (modelExpense.getKategoriWithSeparator().contains(CATEGORY_EXPENSE.MAkANAN.name().toLowerCase())) {
                            fFood = (float) modelExpense.getJumlahValue();
                        }

                        if (modelExpense.getKategoriWithSeparator().contains(CATEGORY_EXPENSE.PULSA_HP.name().toLowerCase())) {
                            fPulse = (float) modelExpense.getJumlahValue();
                        }
                        if (modelExpense.getKategoriWithSeparator().contains(CATEGORY_EXPENSE.TRANSPORTASI.name().toLowerCase())) {
                            fTransportation = (float) modelExpense.getJumlahValue();
                        }
                        if (modelExpense.getKategoriWithSeparator().contains(CATEGORY_EXPENSE.PAKET_INTERNET.name().toLowerCase())) {
                            fInternetPackages = (float) modelExpense.getJumlahValue();
                        }
                        if (modelExpense.getKategoriWithSeparator().contains(CATEGORY_EXPENSE.TAGIHAN.name().toLowerCase())) {
                            fBill = (float) modelExpense.getJumlahValue();
                        }
                    }

                    entriesGeneralShopping.add(new BarEntry(i, fGeneralShopping, CATEGORY_EXPENSE.BELANJA_UMUM.name().toLowerCase()));
                    entriesFood.add(new BarEntry(i, fFood, CATEGORY_EXPENSE.MAkANAN.name().toLowerCase()));
                    entriesPulse.add(new BarEntry(i, fPulse, CATEGORY_EXPENSE.PULSA_HP.name().toLowerCase()));
                    entriesTransportation.add(new BarEntry(i, fTransportation, CATEGORY_EXPENSE.TRANSPORTASI.name().toLowerCase()));
                    entriesBill.add(new BarEntry(i, fBill, CATEGORY_EXPENSE.TAGIHAN.name().toLowerCase()));
                    entriesInternetPackages.add(new BarEntry(i, fInternetPackages, CATEGORY_EXPENSE.PAKET_INTERNET.name().toLowerCase()));
                }

                BarDataSet barDataSetGeneralShopping = new BarDataSet(entriesGeneralShopping, CATEGORY_EXPENSE.BELANJA_UMUM.name().toLowerCase());
                BarDataSet barDataSetFood = new BarDataSet(entriesFood, CATEGORY_EXPENSE.MAkANAN.name().toLowerCase());
                BarDataSet barDataSetPulse = new BarDataSet(entriesPulse, CATEGORY_EXPENSE.PULSA_HP.name().toLowerCase());
                BarDataSet barDataSetTransportation = new BarDataSet(entriesTransportation, CATEGORY_EXPENSE.TRANSPORTASI.name().toLowerCase());
                BarDataSet barDataSetBill = new BarDataSet(entriesBill, CATEGORY_EXPENSE.TAGIHAN.name().toLowerCase());
                BarDataSet barDataSetInternetPackages = new BarDataSet(entriesInternetPackages, CATEGORY_EXPENSE.PAKET_INTERNET.name().toLowerCase());


                barDataSetGeneralShopping.setColor(ContextCompat.getColor(getContext(), R.color.blueColor));
                barDataSetFood.setColor(ContextCompat.getColor(getContext(), R.color.red));
                barDataSetPulse.setColor(ContextCompat.getColor(getContext(), R.color.colorTextYellow));
                barDataSetTransportation.setColor(ContextCompat.getColor(getContext(), R.color.colorTextGreen));
                barDataSetBill.setColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
                barDataSetInternetPackages.setColor(ContextCompat.getColor(getContext(), R.color.colorTextOrange));


                barDataSets.add(barDataSetGeneralShopping);
                barDataSets.add(barDataSetFood);
                barDataSets.add(barDataSetPulse);
                barDataSets.add(barDataSetTransportation);
                barDataSets.add(barDataSetBill);
                barDataSets.add(barDataSetInternetPackages);
            }

            return barDataSets;
        }

        @SuppressLint("ResourceType")
        @Override
        protected void onPostExecute(List<IBarDataSet> barDataSets) {

            BarData barData;

            barData = new BarData(barDataSets);
            barData.setDrawValues(false);
            barData.setValueFormatter(new LargeValueFormatter());
            barData.setValueTextSize(11);
            binding.barChartAnalysis.resetZoom();
            binding.barChartAnalysis.setData(barData);
            // scaling can now only be done on x- and y-axis separately
            binding.barChartAnalysis.setPinchZoom(true);

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
            l.setTextColor(ContextCompat.getColor(getContext(),value.resourceId));


            XAxis xAxis = binding.barChartAnalysis.getXAxis();
            xAxis.setGranularity(1f);
            xAxis.setCenterAxisLabels(true);

            xAxis.setDrawGridLines(true);
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

            xAxis.setLabelCount(listDate.size());
            xAxis.setTextColor(ContextCompat.getColor(getContext(),value.resourceId));
            xAxis.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    Log.e("cek_value", value + "");
                    String date = "";
                    if (value > -1 && value < listDate.size()) {
                        date = Tools.convertDateFormatAnalysis(listDate.size(), listDate.get((int) value));
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
            MyMarkView mv = new MyMarkView(getContext(), R.layout.custom_marker_view);
            mv.setChartView(binding.barChartAnalysis); // For bounds control

            binding.barChartAnalysis.setMarker(mv);

            YAxis leftAxis = binding.barChartAnalysis.getAxisLeft();

            leftAxis.setDrawGridLines(false);
            leftAxis.setSpaceTop(35f);
            leftAxis.setTextColor(ContextCompat.getColor(getContext(),value.resourceId));
            leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)
            binding.barChartAnalysis.getAxisRight().setEnabled(false);
            binding.barChartAnalysis.invalidate();
        }
    }


    private List<String> getListDateWeek() {
        return localizedWeekHelper.getListWeek(localizedWeekHelper.getFirstDay(prevNextWeek - 7), localizedWeekHelper.getLastDay(prevNextWeek));
    }
}


