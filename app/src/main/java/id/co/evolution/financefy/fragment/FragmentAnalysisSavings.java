package id.co.evolution.financefy.fragment;

import static id.co.evolution.financefy.helper.Tools.calculatePercentage;
import static id.co.evolution.financefy.helper.Tools.changeTitleColor;
import static id.co.evolution.financefy.helper.Tools.convertToCurrency;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
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
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import id.co.evolution.financefy.MainActivity;
import id.co.evolution.financefy.R;
import id.co.evolution.financefy.adapter.AdapterAnalysisFinance;
import id.co.evolution.financefy.adapter.AdapterAnalysisSavings;
import id.co.evolution.financefy.callback.CallbackOnActivityResult;
import id.co.evolution.financefy.databinding.FragmentAnalysisSavingsBinding;
import id.co.evolution.financefy.dialog.DialogFilterFinance;
import id.co.evolution.financefy.dialog.DialogFilterSavings;
import id.co.evolution.financefy.dialog.DialogMonthPicker;
import id.co.evolution.financefy.dialog.DialogSavings;
import id.co.evolution.financefy.helper.FinanceFilter;
import id.co.evolution.financefy.helper.LocalizedWeekHelper;
import id.co.evolution.financefy.helper.MyMarkView;
import id.co.evolution.financefy.helper.SavingsFilter;
import id.co.evolution.financefy.helper.TinyDb;
import id.co.evolution.financefy.helper.Tools;
import id.co.evolution.financefy.model.ModelFinance;
import id.co.evolution.financefy.model.ModelNestedFinance;
import id.co.evolution.financefy.model.ModelNestedSavings;
import id.co.evolution.financefy.model.ModelSavings;
import id.co.evolution.financefy.model.ModelSavingsProgress;
import id.co.evolution.financefy.model.ModelUser;
import id.co.evolution.financefy.model.ModelUserWithFinance;
import id.co.evolution.financefy.repository.FinanceRepository;
import id.co.evolution.financefy.repository.SavingsProgressRepository;
import id.co.evolution.financefy.repository.SavingsRepository;
import id.co.evolution.financefy.repository.UserRepository;
import id.co.evolution.financefy.viewmodel.ViewModelFinance;
import id.co.evolution.financefy.viewmodel.ViewModelSavings;
import id.co.evolution.financefy.viewmodel.ViewModelSavingsProgress;
import id.co.evolution.financefy.viewmodel.ViewModelUser;

@AndroidEntryPoint
public class FragmentAnalysisSavings extends Fragment {
    public Calendar today;
    Calendar prevNextMonth;
    List<ModelFinance> dataFinance = new ArrayList<>();
    List<ModelFinance> finances = new ArrayList<>();
    FragmentAnalysisSavingsBinding binding;
    ViewModelFinance viewModelFinance;
    ViewModelUser viewModelUser;
    AdapterAnalysisSavings adapter;
    TYPE_CHART typeChart = TYPE_CHART.BAR_CHART;
    @Inject
    FinanceFilter financeFilter;
    String filterType, filterPeriod;
    String month;
    @Inject
    LocalizedWeekHelper localizedWeekHelper;
    int prevNextWeek = 0;
    boolean nextWeekEnabled;
    @Inject
    FinanceRepository financeRepository;
    @Inject
    UserRepository userRepository;
    ModelUser user;
    DialogSavings dialogSavings;
    ModelSavings modelSavings;
    List<ModelSavings> savingsTargetData = new ArrayList<>();
    List<ModelSavingsProgress> savingsData;
    @Inject
    SavingsRepository savingsRepository;
    @Inject
    SavingsProgressRepository savingsProgressRepository;
    ViewModelSavings viewModelSavings;
    ViewModelSavingsProgress viewModelSavingsProgress;
    @Inject
    SavingsFilter savingsFilter;

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

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivity = ((MainActivity) context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_analysis_savings, container, false);
        dataFinance = ((MainActivity) requireActivity()).dataFinance;
        user = mainActivity.user;
        modelSavings = mainActivity.modelSavings;
        Log.e("cek_list_week: ", localizedWeekHelper.getFirstDay(-7).substring(0, (localizedWeekHelper.getFirstDay(-7).length() - 3)));
        setHasOptionsMenu(true);
        binding.layoutSavingsProgress.imgChooseRecommendation.setOnClickListener(v -> {

            showMenu(v);
        });
        return binding.getRoot();
    }
    private void showMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(getContext(), view);
        popupMenu.getMenuInflater().inflate(R.menu.menu_choose_recommendation, popupMenu.getMenu());


        long restOfTheDay = Tools.getRestOfTheDay(Tools.getFormattedDateSimple(today.getTimeInMillis()), modelSavings.getDate_target());

        popupMenu.getMenu().findItem(R.id.year).setVisible(restOfTheDay>=365);

        popupMenu.getMenu().findItem(R.id.month).setVisible(restOfTheDay>=30);

        popupMenu.setOnMenuItemClickListener(menuItem -> {
            long recommendationSavings = 0;
            long recommendationSavingsDay = Tools.calculateRecommendationDay(modelSavings.getTargetValue(), restOfTheDay);

            switch (menuItem.getItemId()) {
                case R.id.year:
                    recommendationSavings = Tools.calculateRecommendationYear(recommendationSavingsDay);
                    binding.layoutSavingsProgress.txtRecommendationSaving.setText(Html.fromHtml(changeTitleColor("Rekomendasi pertahun: ", "#FFFFFF") + changeTitleColor(convertToCurrency(recommendationSavings), "green")));
                    break;
                case R.id.month:
                    recommendationSavings = Tools.calculateRecommendationMonth(recommendationSavingsDay);
                    binding.layoutSavingsProgress.txtRecommendationSaving.setText(Html.fromHtml(changeTitleColor("Rekomendasi perbulan: ", "#FFFFFF") + changeTitleColor(convertToCurrency(recommendationSavings), "green")));
                    break;
                case R.id.day:
                    recommendationSavings = recommendationSavingsDay;
                    binding.layoutSavingsProgress.txtRecommendationSaving.setText(Html.fromHtml(changeTitleColor("Rekomendasi perhari: ", "#FFFFFF") + changeTitleColor(convertToCurrency(recommendationSavings), "green")));
                    break;
            }
            return true;
        });
        popupMenu.show();
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
        viewModelSavings = new ViewModelProvider(this).get(ViewModelSavings.class);
        viewModelSavingsProgress = new ViewModelProvider(this).get(ViewModelSavingsProgress.class);
        viewModelSavings.init(savingsRepository);
        viewModelSavingsProgress.init(savingsProgressRepository);
        viewModelUser.init(userRepository);
        viewModelFinance.init(financeRepository);

        viewModelUser.getFinanceByUserId(user.getId()).observe(getViewLifecycleOwner(), new Observer<ModelUserWithFinance>() {
            @Override
            public void onChanged(ModelUserWithFinance modelUserWithFinances) {
                Log.e("TAG", "onChanged: " + new Gson().toJson(modelUserWithFinances));
                loadDataSavingsByMonth(date_ship_millis);
            }
        });


        binding.placeMonth.setOnClickListener(v -> showDialogMonthPicker());

        binding.btnNext.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.colorGrey50)));
        binding.btnNext.setEnabled(false);

        binding.btnPrev.setOnClickListener(v -> {
            if (filterPeriod.equalsIgnoreCase(getString(R.string.bulanan))) {
                prevNextMonth.get(Calendar.YEAR);
                prevNextMonth.add(Calendar.MONTH, -1);
                long date_ship_milisecond = prevNextMonth.getTimeInMillis();
                date_ship_millis = date_ship_milisecond;
                loadDataSavingsByMonth(date_ship_milisecond);
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
                viewModelSavingsProgress.getSavingsByWeek(getListDateWeek(), modelSavings.getId()).observe(getViewLifecycleOwner(), modelSavingsProgresses -> {
                    if (modelSavingsProgresses != null) loadDataSavings(modelSavingsProgresses);
                });
            }

        });

        binding.btnNext.setOnClickListener(v -> {
            if (filterPeriod.equalsIgnoreCase(getString(R.string.bulanan))) {
                prevNextMonth.get(Calendar.YEAR);
                prevNextMonth.add(Calendar.MONTH, 1);
                long date_ship_milisecond = prevNextMonth.getTimeInMillis();
                date_ship_millis = date_ship_milisecond;
                loadDataSavingsByMonth(date_ship_milisecond);
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
                viewModelSavingsProgress.getSavingsByWeek(getListDateWeek(), modelSavings.getId()).observe(getViewLifecycleOwner(), modelSavingsProgresses -> {
                    if (modelSavingsProgresses != null) loadDataSavings(modelSavingsProgresses);
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
                loadDataSavingsByMonth(date_ship_milis);
            }
        });
        dialogMonthPicker.showDialogMonthPicker();
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
                showDialogFilterSavings();
                // Not implemented here
                break;
            case R.id.view_list:
                if (typeChart == TYPE_CHART.PIE_CHART) {
                    item.setIcon(R.drawable.ic_baseline_bar_chart_24);
                    typeChart = TYPE_CHART.BAR_CHART;
                    if (savingsData.size() > 1) new FragmentAnalysisSavings.BarChartAsyncTask(savingsData).execute();
                } else if (typeChart == TYPE_CHART.BAR_CHART) {
                    item.setIcon(R.drawable.ic_baseline_pie_chart_24);
                    typeChart = TYPE_CHART.PIE_CHART;
                    if (savingsData.size() > 0)
                        new FragmentAnalysisSavings.PieChartAsyncTask(savingsFilter.listAnalysis(savingsData)).execute();
                }
                binding.barChartAnalysis.setVisibility(savingsData.size() > 0 && typeChart == TYPE_CHART.BAR_CHART ? View.VISIBLE : View.GONE);
                binding.pieChartAnalysis.setVisibility(savingsData.size() > 0 && typeChart == TYPE_CHART.PIE_CHART ? View.VISIBLE : View.GONE);
                return true;

            default:
                break;
        }
        return true;
    }


    private void showDialogFilterSavings() {
        DialogFilterSavings dialog = new DialogFilterSavings(getContext(), getLayoutInflater(), new DialogFilterFinance.DialogFilterFinanceCallback() {
            @Override
            public void resultFilterType(@NonNull String result) {

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
                loadDataByType();
            }
        });

        dialog.showDialogFilterSavings(financeFilter.filterNominal, financeFilter.filterPeriod, false);
    }


    @SuppressLint("NewApi")
    private void loadDataByType() {
        prevNextWeek = 0;

        nextWeekEnabled = localizedWeekHelper.getMonthLastWeekDay(prevNextWeek) <= today.getTimeInMillis();

        if (filterPeriod.equalsIgnoreCase(getString(R.string.bulanan))) {
           loadDataSavingsByMonth(Tools.getFormattedMonthToTime(month));
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (nextWeekEnabled)
                    binding.btnNext.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.white)));
                else
                    binding.btnNext.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.colorGrey50)));
            }
            binding.btnNext.setEnabled(nextWeekEnabled);
            viewModelSavingsProgress.getSavingsByWeek(getListDateWeek(), modelSavings.getId()).observe(getViewLifecycleOwner(), modelSavingsProgresses -> {
//                if (modelSavingsProgresses != null) loadDataSavings(modelSavingsProgresses);
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
    private void loadDataSavingsByMonth(long date_ship_milis) {
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

        //BY SAVINGS
        viewModelSavings.findAllSavingsByIdUser(mainActivity.user.getId()).observe(getViewLifecycleOwner(), dataSavings -> {
            if (dataSavings != null) {
                savingsTargetData = new ArrayList<>(dataSavings);
            }
        });

        if (modelSavings != null) {
            viewModelSavingsProgress.getSavingsByMonth(Tools.getFormattedMonthSimple(date_ship_milis), modelSavings.getId()).observe(getViewLifecycleOwner(), modelSavings -> {
                if (modelSavings != null) loadDataSavings(modelSavings);
            });
        }
    }


    @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
    private void loadDataSavings(List<ModelSavingsProgress> data) {
        savingsData = new ArrayList<>(data);

        loadDataHeader();
        loadTotalSavingsTarget();

        data = savingsFilter.listAnalysis(data);

        adapter = new AdapterAnalysisSavings(getActivity(), data);
        adapter.setTotalValue(modelSavings.getTargetValue());
        binding.rvList.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.rvList.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        binding.placeEmpty.setVisibility(adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
        binding.txtEmpty.setText("Tidak ada data menabung");
        if (typeChart == TYPE_CHART.BAR_CHART) {

          if(data.size()>1)  new FragmentAnalysisSavings.BarChartAsyncTask(data).execute();
            binding.barChartAnalysis.setVisibility(data.size() > 0 ? View.VISIBLE : View.GONE);
        } else {
            new FragmentAnalysisSavings.PieChartAsyncTask(savingsFilter.listAnalysis(data)).execute();
            binding.pieChartAnalysis.setVisibility(data.size() > 0 ? View.VISIBLE : View.GONE);
        }
    }
    private void loadTotalSavingsTarget() {
        viewModelSavings.findSavingsById(mainActivity.modelSavings.getId()).observe(getViewLifecycleOwner(), modelSavings1 -> {
            if (modelSavings1 != null) {
                mainActivity.modelSavings.setProcessValue(modelSavings1.getProcessValue());
                setSavingsTarget();
                loadDataHeader();
            }
        });
    }

    private void loadDataHeader() {
        long restOfTheDay =Tools.getRestOfTheDay(Tools.getFormattedDateSimple(today.getTimeInMillis()), modelSavings.getDate_target());
        long recommendationSavingsDay= Tools.calculateRecommendationDay(modelSavings.getTargetValue(),restOfTheDay);
        double percentage = calculatePercentage((double) modelSavings.getProcessValue(), (double) modelSavings.getTargetValue());
        String txtPercentage = percentage >= 100 ? getString(R.string.achieved) : percentage+"%";

        binding.layoutSavingsProgress.txtTitle.setText(modelSavings.getTitle());
        binding.layoutSavingsProgress.txtProgress.setText(convertToCurrency(modelSavings.getProcessValue()) + " s/d " + convertToCurrency(modelSavings.getTargetValue()));
        binding.layoutSavingsProgress.progressSavings.setProgress((int) calculatePercentage(modelSavings.getProcessValue(), modelSavings.getTargetValue()));
        binding.layoutSavingsProgress.progressSavings.setMax(100);
        binding.layoutSavingsProgress.txtPercentage.setText(txtPercentage);
        if(restOfTheDay>0)
            binding.layoutSavingsProgress.txtDay.setText("Sisa " + restOfTheDay + " hari");
        else
            binding.layoutSavingsProgress.txtDay.setText("Selesai");

        binding.layoutSavingsProgress.txtRecommendationSaving.setText(Html.fromHtml(changeTitleColor("Rekomendasi perhari: ", "#FFFFFF") + changeTitleColor(convertToCurrency(recommendationSavingsDay),"green")));

        binding.layoutSavingsProgress.txtTitle.setOnClickListener(v -> {

            dialogSavings = new DialogSavings(getContext(), getLayoutInflater(), (type, index, result) -> {
                modelSavings = savingsTargetData.get(index);
                switch (type) {
                    case CLICKED:

                        setSavingsTarget();
                        viewModelSavingsProgress.getSavingsByMonth(month, savingsTargetData.get(index).getId()).observe(getViewLifecycleOwner(), dataSavingsProgress -> {
                            if (dataSavingsProgress != null) {
                                loadDataSavings(dataSavingsProgress);
                            }
                        });
                        break;
                    case REMOVED:
                        new SweetAlertDialog(getContext(), SweetAlertDialog.WARNING_TYPE)
                                .setTitleText("Hapus")
                                .setContentText("Apakah anda yakin ingin hapus tabungan '" + modelSavings.getTitle() + "'?")
                                .setConfirmText("Ya")
                                .setConfirmClickListener(sweetAlertDialog -> {
                                    new SavingsRepository.RemoveSavings(modelSavings, savingsRepository.savingsDao).execute();
                                    if (index - 1 < 0) {
                                        modelSavings = savingsTargetData.get(index + 1);
                                        setSavingsTarget();
                                        viewModelSavingsProgress.findAllSavingsByIdSavings(modelSavings.getId()).observe(getViewLifecycleOwner(), dataSavingsProgress -> {
                                            if (dataSavingsProgress != null) {
                                                loadDataSavings(dataSavingsProgress);
                                            }
                                        });
                                    } else {
                                        modelSavings = savingsTargetData.get(index - 1);
                                        setSavingsTarget();
                                        viewModelSavingsProgress.findAllSavingsByIdSavings(modelSavings.getId()).observe(getViewLifecycleOwner(), dataSavingsProgress -> {
                                            if (dataSavingsProgress != null) {
                                                loadDataSavings(dataSavingsProgress);
                                            }
                                        });
                                    }

                                    savingsTargetData.remove(index);
                                    sweetAlertDialog.dismiss();
                                })
                                .setCancelText("Tidak")
                                .show();
                        break;
                    case EDIT:
                        mCallbackOnActivityResult.updateDataSavingsTarget(savingsTargetData, index, modelSavings);
                        break;

                }

            });
            dialogSavings.showDialogSavings(savingsTargetData);
        });
    }

    private class PieChartAsyncTask extends AsyncTask<Void, PieDataSet, PieDataSet> {

        List<ModelSavingsProgress> data;
        List<LegendEntry> legendEntries = new ArrayList<>();
        ArrayList<Integer> colors = new ArrayList<Integer>();

        public PieChartAsyncTask(List<ModelSavingsProgress> data) {
            this.data = data;
        }

        @SuppressLint("NewApi")
        @Override
        protected PieDataSet doInBackground(Void... voids) {
            List<PieEntry> entries = new ArrayList<PieEntry>();

            for (ModelSavingsProgress modelSavingsProgress : data) {
                Random rand = new Random();
                float r = rand.nextFloat();
                float g = rand.nextFloat();
                float b = rand.nextFloat();
                entries.add(new PieEntry((float) modelSavingsProgress.getProcessValue(), Tools.calculatePercentage(modelSavingsProgress.getProcessValue(), modelSavings.getTargetValue()) + "%"));
                colors.add(Color.rgb(r, g, b));
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




            dataSet.setColors(colors);
            return dataSet;
        }

        @Override
        protected void onPostExecute(PieDataSet pieDataSet) {
            super.onPostExecute(pieDataSet);

            PieData pieData = new PieData(pieDataSet);

            for (int i = 0; i < data.size(); i++) {
                legendEntries.add(new LegendEntry(data.get(i).getTitle(), Legend.LegendForm.SQUARE, 10f, 2f, null, colors.get(i % colors.size())));
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
            binding.pieChartAnalysis.setCenterText("Menabung");
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
    }

    private class BarChartAsyncTask extends AsyncTask<Void, List<IBarDataSet>, List<IBarDataSet>> {


        List<ModelSavingsProgress> data;
        List<String> listDate;

        public BarChartAsyncTask(List<ModelSavingsProgress> data) {
            this.data = data;
        }

        @Override
        protected List<IBarDataSet> doInBackground(Void... voids) {
            List<ModelNestedSavings> listNestedFinance = savingsFilter.filterNestedSavings(data);
            listDate = new ArrayList<>();

            List<IBarDataSet> barDataSets = new ArrayList<>();
            ArrayList<BarEntry> entriesBonus = new ArrayList<>();


            List<ModelSavingsProgress> listData = savingsFilter.listAnalysis(data);
            listData = savingsFilter.filterPeriodSavingProgress("Terlama",listData);

            int index=0;
            for (ModelSavingsProgress modelIncome : listData) {
                Spanned data = Html.fromHtml(modelIncome.getDate_progress_savings().toLowerCase());
                entriesBonus.add(new BarEntry(index, modelIncome.getProcessValue(), data.toString()));
                listDate.add(modelIncome.getTitle());
                index++;
            }

            BarDataSet barDataSetBonus = new BarDataSet(entriesBonus, "");
            barDataSetBonus.setColor(ContextCompat.getColor(getContext(), R.color.blueColor));
            barDataSets.add(barDataSetBonus);

            return barDataSets;
        }

        @Override
        protected void onPostExecute(List<IBarDataSet> barDataSets) {

            BarData barData;

            barData = new BarData(barDataSets);
            barData.setDrawValues(false);
            barData.setValueFormatter(new LargeValueFormatter());
            barData.setValueTextSize(11);
            barData.setBarWidth(0.4f);

            binding.barChartAnalysis.setData(barData);
            // scaling can now only be done on x- and y-axis separately
            binding.barChartAnalysis.setPinchZoom(true);

            binding.barChartAnalysis.setDrawBarShadow(false);
            binding.barChartAnalysis.setClipValuesToContent(false);

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

            binding.barChartAnalysis.getLegend().setEnabled(false);

            XAxis xAxis = binding.barChartAnalysis.getXAxis();
            xAxis.setGranularity(1f);
            xAxis.setCenterAxisLabels(false);

            xAxis.setAxisMinimum(0f);
            xAxis.setDrawGridLines(true);
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

            xAxis.setLabelCount(listDate.size());
            xAxis.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    Log.e("cek_value", value + "");
                    String date = "";
                    if (value > -1 && value < listDate.size()) {
                        date = listDate.get((int) value);
                    }

//
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


//        Tools.getDateFromDateFormat(listDate.get(0))
            // restrict the x-axis range
            MyMarkView mv = new MyMarkView(getContext(), R.layout.custom_marker_view);
            mv.setChartView(binding.barChartAnalysis); // For bounds control

            binding.barChartAnalysis.setMarker(mv);
            binding.barChartAnalysis.setExtraOffsets(10f,0f,0f,0f);

            YAxis leftAxis = binding.barChartAnalysis.getAxisLeft();

            leftAxis.setDrawGridLines(false);
            leftAxis.setSpaceTop(35f);
            leftAxis.setXOffset(5f);
            leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)
            binding.barChartAnalysis.getAxisRight().setEnabled(false);
            binding.barChartAnalysis.invalidate();

        }
    }


    private void setSavingsTarget() {
        tinyDb.putObject("savings", modelSavings);
        mainActivity.modelSavings = modelSavings;
    }

    private List<String> getListDateWeek() {
        return localizedWeekHelper.getListWeek(localizedWeekHelper.getFirstDay(prevNextWeek - 7), localizedWeekHelper.getLastDay(prevNextWeek));
    }
}