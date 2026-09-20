package id.co.evolution.financefy.fragment;

import static id.co.evolution.financefy.callback.CallbackOnActivityResult.REQUEST_CODE_SAVINGS;
import static id.co.evolution.financefy.callback.CallbackOnActivityResult.REQUEST_CODE_UPDATE_SAVINGS_TARGET;
import static id.co.evolution.financefy.helper.Tools.calculatePercentage;
import static id.co.evolution.financefy.helper.Tools.changeTitleColor;
import static id.co.evolution.financefy.helper.Tools.convertToCurrency;
import static id.co.evolution.financefy.helper.Tools.getObjectAnimator;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.PopupMenu;

import androidx.activity.result.ActivityResult;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import id.co.evolution.financefy.MainActivity;
import id.co.evolution.financefy.R;
import id.co.evolution.financefy.activity.CreateSavingsProgressActivity;
import id.co.evolution.financefy.adapter.AdapterAnalysisSavings;
import id.co.evolution.financefy.callback.CallbackOnActivityResult;
import id.co.evolution.financefy.databinding.FragmentAnalysisSavingsBinding;
import id.co.evolution.financefy.dialog.DialogConfirm;
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
import id.co.evolution.financefy.fragment.FragmentAll.TYPE_RECOMMENDATION_SAVINGS;
@AndroidEntryPoint
public class FragmentAnalysisSavings extends Fragment implements CallbackOnActivityResult.OnCallbackResult {
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
    String  filterPeriod,filterNominal;
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
    Locale locale;

    TYPE_RECOMMENDATION_SAVINGS typeRecommendationSavings;
    CallbackOnActivityResult mCallbackOnActivityResult;

    long date_ship_millis;
    TypedValue value = new TypedValue();
    MainActivity mainActivity;
    @Inject
    TinyDb tinyDb;
    int colorSurface;

    @Override
    public void result(ActivityResult result, Intent intent) {
        if (result.getResultCode() == REQUEST_CODE_SAVINGS) {
            if (intent != null) {
                ModelSavingsProgress modelSavingsProgress = (ModelSavingsProgress) intent.getSerializableExtra("savings_progress");

                for (int i = 0; i < savingsData.size(); i++) {
                    if (savingsData.get(i).getId() == modelSavingsProgress.getId())
                        savingsData.set(i, modelSavingsProgress);
                }


                Log.e("TAG", "result: " + new Gson().toJson(modelSavings));
                Log.e("TAG", "result: " + new Gson().toJson(modelSavingsProgress));
                loadDataSavings(savingsData);
            }
        } else if (result.getResultCode() == REQUEST_CODE_UPDATE_SAVINGS_TARGET) {


            if (intent != null) {
                this.modelSavings = (ModelSavings) intent.getSerializableExtra("savings");
                for (int i = 0; i < savingsTargetData.size(); i++) {
                    if (savingsTargetData.get(i).getId() == modelSavings.getId())
                        savingsTargetData.set(i, modelSavings);
                }

                setSavingsTarget();
                viewModelSavingsProgress.findAllSavingsByIdSavings(modelSavings.getId(),modelSavings.getType_currency()).observe(getViewLifecycleOwner(), dataSavingsProgress -> {
                    if (dataSavingsProgress != null) {
                        loadDataSavings(dataSavingsProgress);
                    }
                });
            }
        }
    }

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
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCallbackOnActivityResult = new CallbackOnActivityResult(getContext(), requireActivity().getActivityResultRegistry(), this);
        getLifecycle().addObserver(mCallbackOnActivityResult);

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_analysis_savings, container, false);
        Tools.setBackgroundColorView(binding.llAppBar,mainActivity.modelPrimaryColor);
        dataFinance = ((MainActivity) requireActivity()).dataFinance;
        colorSurface = ((MainActivity)getActivity()).getColorFromAttr(getContext(), R.attr.colorOnSurface);

        user = mainActivity.user;
        modelSavings = mainActivity.modelSavings;
        typeRecommendationSavings = TYPE_RECOMMENDATION_SAVINGS.DAY;
        getActivity().getTheme().resolveAttribute(android.R.attr.textColorPrimary, value, true);
        locale =mainActivity.user.getType_currency().equalsIgnoreCase("IDR")? Tools.getLocaleIDN():Tools.getLocaleUS();

        Log.e("cek_list_week: ", localizedWeekHelper.getFirstDay(-7).substring(0, (localizedWeekHelper.getFirstDay(-7).length() - 3)));
        setHasOptionsMenu(true);
        binding.layoutSavingsProgress.txtLabelRecom.setOnClickListener(v -> {
            showMenu(v);
        });
        setupHeaderToggle();
        FloatingActionButton fabAdd = ((MainActivity) getActivity()).binding.layout.fabAdd;
        binding.nsView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {

                if (scrollY > oldScrollY && fabAdd.isShown() && !((MainActivity) getActivity()).isFabOpen)
                    fabAdd.hide();
                else if(!fabAdd.isShown())
                    fabAdd.show();
            }
        });
        binding.layoutSavingsProgress.btnAddSaving.setOnClickListener(v->{
            Intent intent = new Intent(getContext(), CreateSavingsProgressActivity.class);
            intent.putExtra("savings", modelSavings);
            startActivityForResult(intent, REQUEST_CODE_SAVINGS);
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

            int itemId = menuItem.getItemId();
            if (itemId == R.id.year) {
                recommendationSavings = Tools.calculateRecommendationYear(recommendationSavingsDay);
                setTextRecommendationSavings("pertahun", recommendationSavings);
                typeRecommendationSavings = TYPE_RECOMMENDATION_SAVINGS.YEAR;
            } else if (itemId == R.id.month) {
                recommendationSavings = Tools.calculateRecommendationMonth(recommendationSavingsDay);
                setTextRecommendationSavings("perbulan", recommendationSavings);
                typeRecommendationSavings = TYPE_RECOMMENDATION_SAVINGS.MONTH;
            } else if (itemId == R.id.day) {
                recommendationSavings = recommendationSavingsDay;
                setTextRecommendationSavings("perhari", recommendationSavings);
                typeRecommendationSavings = TYPE_RECOMMENDATION_SAVINGS.DAY;
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
                    binding.btnNext.setImageTintList(ColorStateList.valueOf(colorSurface));
                else
                    binding.btnNext.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.colorGrey50)));

                binding.btnNext.setEnabled(nextWeekEnabled);
                binding.txtMonth.setText(Tools.convertDateFormatWeekText(localizedWeekHelper.getFirstDay(prevNextWeek - 7)) + " - " + Tools.convertDateFormatWeekText(localizedWeekHelper.getLastDay(prevNextWeek)));
                binding.txtMonth.setEnabled(false);
                viewModelSavingsProgress.getSavingsByWeek(getListDateWeek(), modelSavings.getId(),modelSavings.getType_currency()).observe(getViewLifecycleOwner(), modelSavingsProgresses -> {
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
                    binding.btnNext.setImageTintList(ColorStateList.valueOf(colorSurface));
                else
                    binding.btnNext.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.colorGrey50)));

                binding.txtMonth.setText(Tools.convertDateFormatWeekText(localizedWeekHelper.getFirstDay(prevNextWeek - 7)) + " - " + Tools.convertDateFormatWeekText(localizedWeekHelper.getLastDay(prevNextWeek)));
                binding.txtMonth.setEnabled(false);
                viewModelSavingsProgress.getSavingsByWeek(getListDateWeek(), modelSavings.getId(),modelSavings.getType_currency()).observe(getViewLifecycleOwner(), modelSavingsProgresses -> {
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


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.filter) {
            showDialogFilterSavings();
        } else if (itemId == R.id.view_list) {
            if (typeChart == TYPE_CHART.PIE_CHART) {
                item.setIcon(R.drawable.ic_baseline_bar_chart_24);
                typeChart = TYPE_CHART.BAR_CHART;
                if (savingsData.size() > 0) new FragmentAnalysisSavings.BarChartAsyncTask(savingsData).execute();
            } else if (typeChart == TYPE_CHART.BAR_CHART) {
                item.setIcon(R.drawable.ic_baseline_pie_chart_24);
                typeChart = TYPE_CHART.PIE_CHART;
                if (savingsData.size() > 0)
                    new FragmentAnalysisSavings.PieChartAsyncTask(savingsFilter.listAnalysis(savingsData)).execute();
            }
            binding.barChartAnalysis.setVisibility(savingsData.size() > 0 && typeChart == TYPE_CHART.BAR_CHART ? View.VISIBLE : View.GONE);
            binding.pieChartAnalysis.setVisibility(savingsData.size() > 0 && typeChart == TYPE_CHART.PIE_CHART ? View.VISIBLE : View.GONE);
            return true;
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
                filterNominal = result;
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
                    binding.btnNext.setImageTintList(ColorStateList.valueOf(colorSurface));
                else
                    binding.btnNext.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.colorGrey50)));
            }
            binding.btnNext.setEnabled(nextWeekEnabled);
            viewModelSavingsProgress.getSavingsByWeek(getListDateWeek(), modelSavings.getId(),modelSavings.getType_currency()).observe(getViewLifecycleOwner(), modelSavingsProgresses -> {
//                if (modelSavingsProgresses != null) loadDataSavings(modelSavingsProgresses);
            });
        }

        if (filterPeriod.equalsIgnoreCase(getString(R.string.bulanan))) {
            binding.txtMonth.setText(Tools.getFormattedMonthTextSimple(date_ship_millis));
            binding.placeMonth.setEnabled(true);

            //TODO jika range tanggal per minggu nya kurang dari tanggal hari maka bisa melakukan tombol next tanggal per minggunya
            if (date_ship_millis < today.getTimeInMillis())
                binding.btnNext.setImageTintList(ColorStateList.valueOf(colorSurface));
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
            binding.btnNext.setImageTintList(ColorStateList.valueOf(colorSurface));
            binding.btnNext.setEnabled(true);
        }
        if (date_ship_milis >= today.getTimeInMillis()) {
            binding.btnNext.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.colorGrey50)));
            binding.btnNext.setEnabled(false);
        }

        //BY SAVINGS
        viewModelSavings.findAllSavingsByIdUser(mainActivity.user.getId(),mainActivity.user.getType_currency()).observe(getViewLifecycleOwner(), dataSavings -> {
            if (dataSavings != null) {
                savingsTargetData = new ArrayList<>(dataSavings);
            }
        });

        if (modelSavings != null) {
            viewModelSavingsProgress.getSavingsByMonth(Tools.getFormattedMonthSimple(date_ship_milis), modelSavings.getId(),modelSavings.getType_currency()).observe(getViewLifecycleOwner(), modelSavings -> {
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
        if(filterNominal!=null) data = savingsFilter.filterNominal(filterNominal,data);
        adapter = new AdapterAnalysisSavings(getActivity(), data);
        adapter.setLocale(locale);
        adapter.setTotalValue(modelSavings.getTargetValue());
        binding.rvList.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.rvList.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        binding.placeEmpty.setVisibility(adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
        binding.txtEmpty.setText("Tidak ada data menabung");
        if (typeChart == TYPE_CHART.BAR_CHART) {

//          if(data.size()>1)  new FragmentAnalysisSavings.BarChartAsyncTask(data).execute();
          new FragmentAnalysisSavings.BarChartAsyncTask(data).execute();
          binding.barChartAnalysis.setVisibility(data.size() > 0 ? View.VISIBLE : View.GONE);
        } else {
            new FragmentAnalysisSavings.PieChartAsyncTask(savingsFilter.listAnalysis(data)).execute();
            binding.pieChartAnalysis.setVisibility(data.size() > 0 ? View.VISIBLE : View.GONE);
        }
    }
    private void loadTotalSavingsTarget() {
        viewModelSavingsProgress.findTotalProcessValueByIdSavings(mainActivity.modelSavings.getId(),mainActivity.user.getType_currency()).observe(getViewLifecycleOwner(), modelSavings1 -> {
            int progressValue = modelSavings1!=null?modelSavings1:0;

            if (progressValue>=0) {
                modelSavings.setProcessValue(progressValue);
                viewModelSavings.inputUpdateSavings("update",modelSavings);
                setSavingsTarget();
                loadDataHeader();
            }
        });
    }

    private void setTextRecommendationSavings(String type, long recommendationSavings) {
        binding.layoutSavingsProgress.txtRecommendationSaving.setText(Html.fromHtml( changeTitleColor(convertToCurrency(recommendationSavings,locale), "green")));
        binding.layoutSavingsProgress.txtLabelRecom.setText("Rekomendasi " + type);
    }
    private void loadDataHeader() {
        long restOfTheDay =Tools.getRestOfTheDay(Tools.getFormattedDateSimple(today.getTimeInMillis()), modelSavings.getDate_target());
        long recommendationSavingsDay= Tools.calculateRecommendationDay(modelSavings.getTargetValue(),restOfTheDay);
        double percentage = calculatePercentage((double) modelSavings.getProcessValue(), (double) modelSavings.getTargetValue());
        String txtPercentage = percentage >= 100 ? getString(R.string.achieved) : percentage+"%";

        binding.layoutSavingsProgress.txtTitle.setText(modelSavings.getTitle());
        binding.layoutSavingsProgress.txtCurrentAmount.setText(convertToCurrency(modelSavings.getProcessValue(), locale));
        binding.layoutSavingsProgress.txtTargetAmount.setText("Target: " + convertToCurrency(modelSavings.getTargetValue(), locale));
        binding.layoutSavingsProgress.progressSavings.setProgress((int) calculatePercentage(modelSavings.getProcessValue(), modelSavings.getTargetValue()));
        binding.layoutSavingsProgress.progressSavings.setMax(100);
        binding.layoutSavingsProgress.txtPercentage.setText(txtPercentage);
        if(restOfTheDay>0)
            binding.layoutSavingsProgress.txtDay.setText("Sisa " + restOfTheDay + " hari");
        else
            binding.layoutSavingsProgress.txtDay.setText("Selesai");

        if (typeRecommendationSavings == TYPE_RECOMMENDATION_SAVINGS.DAY) {
            setTextRecommendationSavings("perhari", recommendationSavingsDay);
        } else if (typeRecommendationSavings == TYPE_RECOMMENDATION_SAVINGS.MONTH) {
            setTextRecommendationSavings("perbulan", Tools.calculateRecommendationMonth(recommendationSavingsDay));
        } else {
            setTextRecommendationSavings("pertahun", Tools.calculateRecommendationYear(recommendationSavingsDay));
        }
        binding.layoutSavingsProgress.txtTitle.setOnClickListener(v -> {
            dialogSavings = new DialogSavings(getContext(), getLayoutInflater(), (type, index, result) -> {
                modelSavings = savingsTargetData.get(index);
                switch (type) {
                    case CLICKED:
                        setSavingsTarget();
                        viewModelSavingsProgress.getSavingsByMonth(month, savingsTargetData.get(index).getId(),modelSavings.getType_currency()).observe(getViewLifecycleOwner(), dataSavingsProgress -> {
                            if (dataSavingsProgress != null) {
                                loadDataSavings(dataSavingsProgress);
                            }
                        });
                        break;
                    case REMOVED:
                        DialogConfirm dialogConfirm = new DialogConfirm(getContext(), getLayoutInflater(), new DialogConfirm.DialogConfirm() {
                            @Override
                            public void onSubmit(@NonNull String result) {
                                if(result.equalsIgnoreCase("yes")){
                                    new SavingsRepository.RemoveSavings(modelSavings, savingsRepository.savingsDao).execute();
                                    if (index - 1 < 0) {
                                        modelSavings = savingsTargetData.get(index + 1);
                                        setSavingsTarget();
                                        viewModelSavingsProgress.findAllSavingsByIdSavings(modelSavings.getId(),modelSavings.getType_currency()).observe(getViewLifecycleOwner(), dataSavingsProgress -> {
                                            if (dataSavingsProgress != null) {
                                                loadDataSavings(dataSavingsProgress);
                                            }
                                        });
                                    } else {
                                        modelSavings = savingsTargetData.get(index - 1);
                                        setSavingsTarget();
                                        viewModelSavingsProgress.findAllSavingsByIdSavings(modelSavings.getId(),modelSavings.getType_currency()).observe(getViewLifecycleOwner(), dataSavingsProgress -> {
                                            if (dataSavingsProgress != null) {
                                                loadDataSavings(dataSavingsProgress);
                                            }
                                        });
                                    }

                                    savingsTargetData.remove(index);
                                }
                            }
                        });
                        dialogConfirm.showDialogConfirm("Hapus","Apakah anda yakin ingin hapus tabungan '" + modelSavings.getTitle() + "'?");
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
        ArrayList<Integer> colors = new ArrayList<>();

        public PieChartAsyncTask(List<ModelSavingsProgress> data) {
            this.data = data;
        }

        @SuppressLint("NewApi")
        @Override
        protected PieDataSet doInBackground(Void... voids) {
            List<PieEntry> entries = new ArrayList<>();

            for (ModelSavingsProgress modelSavingsProgress : data) {
                Random rand = new Random();
                float r = rand.nextFloat();
                float g = rand.nextFloat();
                float b = rand.nextFloat();

                // Masukkan judul sebagai label, dan persentase/nilai sebagai value
                entries.add(new PieEntry(
                        (float) modelSavingsProgress.getProcessValue(),
                        modelSavingsProgress.getTitle() // Nama tabungan akan jadi label
                ));
                colors.add(Color.rgb(r, g, b));
            }

            PieDataSet dataSet = new PieDataSet(entries, "");

            // 1. AKTIFKAN DRAW VALUES
            dataSet.setDrawValues(true);
            dataSet.setValueTextSize(12f);
            // Gunakan warna yang kontras, atau sesuaikan dengan tema
            dataSet.setValueTextColor(Color.DKGRAY);

            // 2. KONFIGURASI GARIS PENUNJUK (M-Banking Style)
            dataSet.setXValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
            dataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
            dataSet.setValueLinePart1OffsetPercentage(80.f);
            dataSet.setValueLinePart1Length(0.3f);
            dataSet.setValueLinePart2Length(0.4f);
            dataSet.setValueLineColor(Color.GRAY); // Warna garis penunjuk

            dataSet.setDrawIcons(false);
            dataSet.setSliceSpace(3f); // Jarak antar potongan diperlebar sedikit
            dataSet.setSelectionShift(5f);

            dataSet.setColors(colors);
            return dataSet;
        }

        @Override
        protected void onPostExecute(PieDataSet pieDataSet) {
            super.onPostExecute(pieDataSet);

            PieData pieData = new PieData(pieDataSet);

            // Pengaturan Legend (Opsional jika label sudah jelas di luar chart)
            for (int i = 0; i < data.size(); i++) {
                legendEntries.add(new LegendEntry(data.get(i).getTitle(), Legend.LegendForm.CIRCLE, 10f, 2f, null, colors.get(i % colors.size())));
            }

            Legend l = binding.pieChartAnalysis.getLegend();
            l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
            l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
            l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
            l.setDrawInside(false);
            l.setWordWrapEnabled(true);
            l.setTextColor(ContextCompat.getColor(getContext(), value.resourceId));
            l.setCustom(legendEntries);

            binding.pieChartAnalysis.animateXY(1500, 1500); // Durasi dipercepat sedikit agar lebih responsif
            binding.pieChartAnalysis.getDescription().setEnabled(false);

            binding.pieChartAnalysis.setCenterText("Total\nTabungan");
            binding.pieChartAnalysis.setCenterTextSize(16);
            binding.pieChartAnalysis.setCenterTextColor(ContextCompat.getColor(getContext(), R.color.blackTextColor));
            binding.pieChartAnalysis.setCenterTextTypeface(Typeface.DEFAULT_BOLD);
            binding.pieChartAnalysis.setExtraOffsets(20f, 0f, 20f, 0f); // Beri ruang agar teks luar tidak terpotong layar

            // 3. PENGATURAN LUBANG TENGAH (Mendukung Dark Mode)
            binding.pieChartAnalysis.setDrawHoleEnabled(true);
            // Ubah warna lubang menjadi transparan agar aman saat Night Mode
            binding.pieChartAnalysis.setHoleColor(Color.TRANSPARENT);
            binding.pieChartAnalysis.setTransparentCircleColor(Color.TRANSPARENT);

            binding.pieChartAnalysis.setHoleRadius(65f); // Lubang diperbesar ala M-Banking
            binding.pieChartAnalysis.setTransparentCircleRadius(68f);

            // 4. PENGATURAN LABEL ENTRY
            binding.pieChartAnalysis.setDrawEntryLabels(true);
            binding.pieChartAnalysis.setEntryLabelColor(Color.DKGRAY);
            binding.pieChartAnalysis.setEntryLabelTextSize(11f);

            binding.pieChartAnalysis.setRotationEnabled(true);
            binding.pieChartAnalysis.setHighlightPerTapEnabled(true);

            // Mematikan highlight saat chart pertama kali di-load
            binding.pieChartAnalysis.highlightValues(null);

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
            listDate = new ArrayList<>();

            List<IBarDataSet> barDataSets = new ArrayList<>();
            ArrayList<BarEntry> entriesSavings = new ArrayList<>();


            List<ModelSavingsProgress> listData = savingsFilter.listAnalysis(data);
            listData = savingsFilter.filterPeriodSavingProgress("Terlama",listData);

            int index=0;
            for (ModelSavingsProgress modelIncome : listData) {
                Spanned data = Html.fromHtml(Tools.convertDateFormat(modelIncome.getDate_progress_savings())+"<br>"+modelIncome.getTitle().toLowerCase());
                entriesSavings.add(new BarEntry(index, modelIncome.getProcessValue(), data.toString()));
                listDate.add(modelIncome.getTitle());
                index++;
            }

            BarDataSet barDataSetBonus = new BarDataSet(entriesSavings, "");
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
            if (data.size() == 1) {
                binding.barChartAnalysis.getXAxis().setAxisMinimum(-0.5f);
                binding.barChartAnalysis.getXAxis().setAxisMaximum(0.5f);
            } else {
                binding.barChartAnalysis.getXAxis().setAxisMinimum(barData.getXMin() - 0.5f);
                binding.barChartAnalysis.getXAxis().setAxisMaximum(barData.getXMax() + 0.5f);
            }

            if (data.size()==0) {
                binding.barChartAnalysis.clear();
                binding.barChartAnalysis.setNoDataText("No data available");
            } else {
                binding.barChartAnalysis.setData(barData);
                binding.barChartAnalysis.invalidate();  // Refresh chart
            }
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
            l.setTextColor(ContextCompat.getColor(getContext(),value.resourceId));

            binding.barChartAnalysis.getLegend().setEnabled(false);

            XAxis xAxis = binding.barChartAnalysis.getXAxis();
            xAxis.setGranularity(1f);
            xAxis.setCenterAxisLabels(false);

            xAxis.setAxisMinimum(0f);
            xAxis.setDrawGridLines(true);
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setLabelCount(listDate.size());
            xAxis.setTextColor(ContextCompat.getColor(getContext(),value.resourceId));

//            xAxis.setValueFormatter(new ValueFormatter() {
//                @Override
//                public String getFormattedValue(float value) {
//                    Log.e("cek_value", value + "");
//                    String date = "";
//                    if (value > -1 && value < listDate.size()) {
//                        date = listDate.get((int) value);
//                    }
//
////
//                    return "";
//                }
//            });
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
            leftAxis.setTextColor(ContextCompat.getColor(getContext(),value.resourceId));
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

    private boolean isHeaderExpanded = true;
    private int headerOriginalHeight = 0;
    private void setupHeaderToggle() {
        // 1. Post pada wadah luar untuk mendapatkan tinggi aslinya
        binding.flHeaderWrapper.post(() -> {
            headerOriginalHeight = binding.flHeaderWrapper.getHeight();

            // 2. KUNCI TINGGI WADAH DALAM
            // Ini adalah trik agar konten tidak tergencet/mengecil saat animasi
            ViewGroup.LayoutParams innerParams = binding.llHeaderContent.getLayoutParams();
            innerParams.height = headerOriginalHeight;
            binding.llHeaderContent.setLayoutParams(innerParams);
        });

        binding.btnToggle.setOnClickListener(v -> {
            if (headerOriginalHeight == 0) return; // Mencegah klik sebelum render selesai

            ValueAnimator slideAnimator;

            if (isHeaderExpanded) {
                // Animasi Menutup (Collapse) - Tinggi wadah luar menjadi 0
                slideAnimator = ValueAnimator.ofInt(headerOriginalHeight, 0);
                binding.btnToggle.animate().rotation(180f).setDuration(300).start();
            } else {
                // Animasi Membuka (Expand) - Tinggi wadah luar kembali normal
                slideAnimator = ValueAnimator.ofInt(0, headerOriginalHeight);
                binding.btnToggle.animate().rotation(0f).setDuration(300).start();
            }

            slideAnimator.addUpdateListener(animation -> {
                // 3. Terapkan perubahan tinggi HANYA pada WADAH LUAR
                int animatedValue = (int) animation.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = binding.flHeaderWrapper.getLayoutParams();
                layoutParams.height = animatedValue;
                binding.flHeaderWrapper.setLayoutParams(layoutParams);
            });

            slideAnimator.setDuration(300);
            slideAnimator.setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator());
            slideAnimator.start();

            // Balikkan status
            isHeaderExpanded = !isHeaderExpanded;
        });
    }
}