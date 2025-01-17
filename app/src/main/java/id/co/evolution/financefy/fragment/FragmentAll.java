package id.co.evolution.financefy.fragment;

import static id.co.evolution.financefy.callback.CallbackOnActivityResult.REQUEST_CODE_FINANCE;
import static id.co.evolution.financefy.callback.CallbackOnActivityResult.REQUEST_CODE_SAVINGS;
import static id.co.evolution.financefy.callback.CallbackOnActivityResult.REQUEST_CODE_UPDATE_SAVINGS_TARGET;
import static id.co.evolution.financefy.helper.Tools.calculatePercentage;
import static id.co.evolution.financefy.helper.Tools.changeTitleColor;
import static id.co.evolution.financefy.helper.Tools.convertToCurrency;
import static id.co.evolution.financefy.helper.Tools.getFormattedDateSimple;
import static id.co.evolution.financefy.helper.Tools.getObjectAnimator;
import static id.co.evolution.financefy.helper.Tools.modelPrimaryColor;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
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
import android.view.animation.RotateAnimation;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import id.co.evolution.financefy.MainActivity;
import id.co.evolution.financefy.R;
import id.co.evolution.financefy.adapter.AdapterFinance;
import id.co.evolution.financefy.adapter.AdapterSavings;
import id.co.evolution.financefy.callback.CallbackOnActivityResult;
import id.co.evolution.financefy.databinding.FragmentAllBinding;
import id.co.evolution.financefy.dialog.DialogConfirm;
import id.co.evolution.financefy.dialog.DialogFilterFinance;
import id.co.evolution.financefy.dialog.DialogFilterSavings;
import id.co.evolution.financefy.dialog.DialogMonthPicker;
import id.co.evolution.financefy.dialog.DialogSavings;
import id.co.evolution.financefy.helper.FinanceFilter;
import id.co.evolution.financefy.helper.LocalizedWeekHelper;
import id.co.evolution.financefy.helper.SavingsFilter;
import id.co.evolution.financefy.helper.TinyDb;
import id.co.evolution.financefy.helper.Tools;
import id.co.evolution.financefy.model.ModelFinance;
import id.co.evolution.financefy.model.ModelNestedFinance;
import id.co.evolution.financefy.model.ModelNestedSavings;
import id.co.evolution.financefy.model.ModelNotification;
import id.co.evolution.financefy.model.ModelSavings;
import id.co.evolution.financefy.model.ModelSavingsProgress;
import id.co.evolution.financefy.model.ModelUser;
import id.co.evolution.financefy.repository.FinanceRepository;
import id.co.evolution.financefy.repository.SavingsProgressRepository;
import id.co.evolution.financefy.repository.SavingsRepository;
import id.co.evolution.financefy.repository.UserRepository;
import id.co.evolution.financefy.viewmodel.ViewModelFinance;
import id.co.evolution.financefy.viewmodel.ViewModelSavings;
import id.co.evolution.financefy.viewmodel.ViewModelSavingsProgress;
import id.co.evolution.financefy.viewmodel.ViewModelUser;

@AndroidEntryPoint
public class FragmentAll extends Fragment implements CallbackOnActivityResult.OnCallbackResult {
    Calendar today;
    Calendar prevNextMonth;
    List<ModelFinance> dataFinance = new ArrayList<>();
    List<ModelFinance> financeData;
    List<ModelSavings> savingsTargetData = new ArrayList<>();
    List<ModelSavingsProgress> savingsData;
    FragmentAllBinding binding;
    ViewModelFinance viewModelFinance;
    ViewModelSavings viewModelSavings;
    ViewModelSavingsProgress viewModelSavingsProgress;

    ViewModelUser viewModelUser;
    AdapterFinance adapter;
    AdapterSavings adapterSavings;
    TYPE_LAYOUT_MANAGER type_layout_manager = TYPE_LAYOUT_MANAGER.GRID;
    @Inject
    FinanceFilter financeFilter;
    @Inject
    SavingsFilter savingsFilter;

    String filterType, filterNominal, filterPeriod;
    String month;
    @Inject
    LocalizedWeekHelper localizedWeekHelper;
    int prevNextWeek = 0;
    boolean nextWeekEnabled;
    @Inject
    FinanceRepository financeRepository;
    @Inject
    SavingsRepository savingsRepository;
    @Inject
    SavingsProgressRepository savingsProgressRepository;

    Tools.TYPE type;
    TYPE_RECOMMENDATION_SAVINGS typeRecommendationSavings;
    Locale locale;

    public enum TYPE_RECOMMENDATION_SAVINGS {
        YEAR,
        MONTH,
        DAY
    }

    public enum TYPE_LAYOUT_MANAGER {
        GRID,
        HORIZONTAL,
        VERTICAL
    }

    DialogSavings dialogSavings;
    @Inject
    UserRepository userRepository;
    CallbackOnActivityResult mCallbackOnActivityResult;
    int mPositionItem;
    long date_ship_millis;
    ModelUser user;
    ModelSavings modelSavings;
    @Inject
    TinyDb tinyDb;

    MainActivity mainActivity;

    public FragmentAll() {
        // Required empty public constructor
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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_all, container, false);
        dataFinance = ((MainActivity) requireActivity()).dataFinance;
        typeRecommendationSavings = TYPE_RECOMMENDATION_SAVINGS.DAY;
        filterType = getString(R.string.semuanya);
        filterPeriod = getString(R.string.bulanan);

        Tools.setBackgroundColorView(binding.llAppBar,mainActivity.modelPrimaryColor);
        Log.e("cek_list_week: ", localizedWeekHelper.getFirstDay(-7).substring(0, (localizedWeekHelper.getFirstDay(-7).length() - 3)));
        setHasOptionsMenu(true);
        return binding.getRoot();
    }


    @SuppressLint({"NewApi", "ResourceType"})
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        today = Calendar.getInstance();
        today.get(Calendar.YEAR);
        today.get(Calendar.MONTH);
        prevNextMonth = Calendar.getInstance();
        date_ship_millis = today.getTimeInMillis();
        viewModelFinance = new ViewModelProvider(this).get(ViewModelFinance.class);
        viewModelUser = new ViewModelProvider(this).get(ViewModelUser.class);
        viewModelSavings = new ViewModelProvider(this).get(ViewModelSavings.class);
        viewModelSavingsProgress = new ViewModelProvider(this).get(ViewModelSavingsProgress.class);
        viewModelSavings.init(savingsRepository);
        viewModelSavingsProgress.init(savingsProgressRepository);
        viewModelUser.init(userRepository);
        viewModelFinance.init(financeRepository);

        user = mainActivity.user;
        modelSavings = mainActivity.modelSavings;

        if(user!=null)
        locale =user.getType_currency().equalsIgnoreCase("IDR")? Tools.getLocaleIDN():Tools.getLocaleUS();

        if (user != null) {
            loadDataByMonth(date_ship_millis);
            initiateSayHaloWithTime();
        }

        binding.placeMonth.setOnClickListener(v -> showDialogMonthPicker());

        binding.btnNext.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.colorGrey50)));
        binding.btnNext.setEnabled(false);
binding.btnToggle.setOnClickListener(v->{
    toggleView();
});
        binding.btnPrev.setOnClickListener(v -> {
            if (filterPeriod.equalsIgnoreCase(getString(R.string.bulanan))) {
                prevNextMonth.get(Calendar.YEAR);
                prevNextMonth.add(Calendar.MONTH, -1);
                long date_ship_milisecond = prevNextMonth.getTimeInMillis();
                this.date_ship_millis = date_ship_milisecond;
                loadDataByMonth(date_ship_milisecond);
            } else {

                prevNextWeek -= 7;
                /*TODO
                    jika sudah sampai minggu saat ini di bulan saat ini  maka tombol next tidak berfungsi
                 */
                nextWeekEnabled = localizedWeekHelper.getMonthLastWeekDay(prevNextWeek) <= today.getTimeInMillis();
                @ColorInt int colorSurface = ((MainActivity)getActivity()).getColorFromAttr(getContext(), R.attr.colorOnSurface);

                if (nextWeekEnabled)
                    binding.btnNext.setImageTintList(ColorStateList.valueOf(colorSurface));
                else
                    binding.btnNext.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.colorGrey50)));

                binding.btnNext.setEnabled(nextWeekEnabled);
                binding.txtMonth.setText(getTextWeek());
                binding.txtMonth.setEnabled(false);

                loadDataByWeek();
            }
        });

        binding.btnNext.setOnClickListener(v -> {
            if (filterPeriod.equalsIgnoreCase(getString(R.string.bulanan))) {
                prevNextMonth.get(Calendar.YEAR);
                prevNextMonth.add(Calendar.MONTH, 1);
                long date_ship_milisecond = prevNextMonth.getTimeInMillis();
                this.date_ship_millis = date_ship_milisecond;
                loadDataByMonth(date_ship_milisecond);
            } else {
                prevNextWeek += 7;

                nextWeekEnabled = localizedWeekHelper.getMonthLastWeekDay(prevNextWeek) <= today.getTimeInMillis();
                binding.btnNext.setEnabled(nextWeekEnabled);
                @ColorInt int colorSurface = ((MainActivity)getActivity()).getColorFromAttr(getContext(), R.attr.colorOnSurface);

                if (nextWeekEnabled)
                    binding.btnNext.setImageTintList(ColorStateList.valueOf(colorSurface));
                else
                    binding.btnNext.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.colorGrey50)));

                binding.txtMonth.setText(getTextWeek());
                binding.txtMonth.setEnabled(false);

                loadDataByWeek();
            }
        });
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

        binding.rvList.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy < 0 && !fabAdd.isShown())
                    fabAdd.show();
                else if (dy > 0 && fabAdd.isShown() && !((MainActivity) getActivity()).isFabOpen)
                    fabAdd.hide();
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }
        });

        binding.layoutSavingsProgress.imgChooseRecommendation.setOnClickListener(v -> {
            showMenu(v);
        });
        String timeNotification=        tinyDb.getString("time_notification");
        if(!timeNotification.isEmpty()){
            setUpNotification();
        }
    }

    private void showMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(getContext(), view);
        popupMenu.getMenuInflater().inflate(R.menu.menu_choose_recommendation, popupMenu.getMenu());


        long restOfTheDay = Tools.getRestOfTheDay(Tools.getFormattedDateSimple(today.getTimeInMillis()), modelSavings.getDate_target());

        popupMenu.getMenu().findItem(R.id.year).setVisible(restOfTheDay >= 365);

        popupMenu.getMenu().findItem(R.id.month).setVisible(restOfTheDay >= 30);

        popupMenu.setOnMenuItemClickListener(menuItem -> {
            long recommendationSavings = 0;
            long recommendationSavingsDay = Tools.calculateRecommendationDay(modelSavings.getTargetValue(), restOfTheDay);

            switch (menuItem.getItemId()) {
                case R.id.year:
                    recommendationSavings = Tools.calculateRecommendationYear(recommendationSavingsDay);
                    setTextRecommendationSavings("pertahun", recommendationSavings);
                    typeRecommendationSavings = TYPE_RECOMMENDATION_SAVINGS.YEAR;
                    break;
                case R.id.month:
                    recommendationSavings = Tools.calculateRecommendationMonth(recommendationSavingsDay);
                    setTextRecommendationSavings("perbulan", recommendationSavings);
                    typeRecommendationSavings = TYPE_RECOMMENDATION_SAVINGS.MONTH;
                    break;
                case R.id.day:
                    recommendationSavings = recommendationSavingsDay;
                    setTextRecommendationSavings("perhari", recommendationSavings);
                    typeRecommendationSavings = TYPE_RECOMMENDATION_SAVINGS.DAY;
                    break;
            }
            return true;
        });
        popupMenu.show();
    }

    private void setTextRecommendationSavings(String type, long recommendationSavings) {
        binding.layoutSavingsProgress.txtRecommendationSaving.setText(Html.fromHtml(changeTitleColor("Rekomendasi " + type + ": ", "#FFFFFF") + changeTitleColor(convertToCurrency(recommendationSavings,locale), "green")));
    }

    @SuppressLint("SetTextI18n")
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
        binding.txtTypeAccount.setText(user.getType() + " - " + user.getCategory());
    }

    private void showDialogMonthPicker() {
        DialogMonthPicker dialogMonthPicker = new DialogMonthPicker(getActivity(), today, dataFinance, (selectedMonth, selectedYear) -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.YEAR, selectedYear);
            calendar.set(Calendar.MONTH, selectedMonth);
            prevNextMonth.set(Calendar.YEAR, selectedYear);
            prevNextMonth.set(Calendar.MONTH, selectedMonth);
            long date_ship_milis = calendar.getTimeInMillis();
            FragmentAll.this.date_ship_millis = date_ship_milis;
            financeFilter.resetFilter();
            loadDataByMonth(date_ship_milis);
        });
        dialogMonthPicker.showDialogMonthPicker();
    }


    private void showDialogFinance(final List<ModelFinance> data, final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Pilih Opsi");
        final String[] tipe = {"Ubah", "Hapus"};
        builder.setItems(tipe, (dialog, which) -> {
            switch (which) {
                case 0:
                    mPositionItem = position;
                    mCallbackOnActivityResult.updateDataFinance(data, position, user);
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

    private void showDialogSavings(final List<ModelSavingsProgress> data, final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Pilih Opsi");
        final String[] tipe = {"Ubah", "Hapus"};
        builder.setItems(tipe, (dialog, which) -> {
            switch (which) {
                case 0:
                    mPositionItem = position;
                    mCallbackOnActivityResult.updateDataSavings(data, position, modelSavings);
                    dialog.dismiss();
                    break;
                case 1:
                    viewModelSavingsProgress.removeSavings(data.get(position));
                    savingsData.remove(position);
                    binding.rvList.getAdapter().notifyDataSetChanged();
                    dialog.dismiss();
                    break;
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

//    private void showDialogSavings(final List<ModelSavingsProgress> data, final int position) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//        builder.setTitle("Pilih Opsi");
//        final String[] tipe = {"Ubah", "Hapus"};
//        builder.setItems(tipe, (dialog, which) -> {
//            switch (which) {
//                case 0:
//                    mPositionItem = position;
//                    mCallbackOnActivityResult.updateDataFinance(data, position,user.getId());
//                    dialog.dismiss();
//                    break;
//                case 1:
//                    viewModelFinance.removeFinance(data.get(position));
//                    dataFinance.remove(position);
//                    binding.rvList.getAdapter().notifyDataSetChanged();
//                    dialog.dismiss();
//                    break;
//            }
//        });
//        AlertDialog dialog = builder.create();
//        dialog.show();
//    }


    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_finance, menu);
        MenuItem item = menu.findItem(R.id.view_list);
        item.setVisible(false);
    }


    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.filter:
                if (user.getCategory().equalsIgnoreCase(getString(R.string.jurnal_keuangan))) {
                    showDialogFilterFinance();
                } else {
                    showDialogFilterSavings();
                }
                // Not implemented here
                break;
//            case R.id.view_list:
//                if (type_layout_manager == TYPE_LAYOUT_MANAGER.GRID) {
//                    item.setIcon(R.drawable.ic_baseline_grid_view_24);
//                    type_layout_manager = TYPE_LAYOUT_MANAGER.VERTICAL;
//                } else if (type_layout_manager == TYPE_LAYOUT_MANAGER.VERTICAL) {
//                    item.setIcon(R.drawable.ic_baseline_format_list_bulleted_24);
//                    type_layout_manager = TYPE_LAYOUT_MANAGER.GRID;
//                }
//
//                if (user.getCategory().equalsIgnoreCase(getString(R.string.jurnal_keuangan)) && adapter != null) {
//                    adapter.setType(TYPE_LAYOUT_MANAGER.VERTICAL);
//                    adapter.notifyDataSetChanged();
//                } else {
//                    adapterSavings.setType(TYPE_LAYOUT_MANAGER.VERTICAL);
//                    adapterSavings.notifyDataSetChanged();
//                }
//                return true;

            default:
                break;
        }
        return true;
    }


    private void showDialogFilterFinance() {

        DialogFilterFinance dialog = new DialogFilterFinance(getContext(), getLayoutInflater(), new DialogFilterFinance.DialogFilterFinanceCallback() {
            @Override
            public void resultFilterType(@NonNull String result) {
                filterType = result;
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
                if (filterType == null) {
                    Toast.makeText(getContext(), "Mohon untuk pilih tipe terlebih dahulu!", Toast.LENGTH_SHORT).show();
                    return;
                }
                loadFilterByType();
            }
        });

        dialog.showDialogFilterFinance(financeFilter.filterType, financeFilter.filterNominal, financeFilter.filterPeriod, false);
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
                loadFilterByType();
            }
        });

        dialog.showDialogFilterSavings(financeFilter.filterNominal, financeFilter.filterPeriod, false);
    }

    @SuppressLint({"NewApi", "SetTextI18n", "ResourceType"})
    private void loadFilterByType() {
        prevNextWeek = 0;

        nextWeekEnabled = localizedWeekHelper.getMonthLastWeekDay(prevNextWeek) <= today.getTimeInMillis();

        if (filterPeriod.equalsIgnoreCase(getString(R.string.bulanan))) {
            loadDataByMonth(Tools.getFormattedMonthToTime(month));
//            viewModelFinance.getFinanceByTypeAndMonth(type, month, user.getId()).observe(getViewLifecycleOwner(), modelFinances -> {
//                if (modelFinances != null) loadDataFinance(modelFinances);
//            });
        } else {
            @ColorInt int colorSurface = ((MainActivity)getActivity()).getColorFromAttr(getContext(), R.attr.colorOnSurface);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (nextWeekEnabled)
                    binding.btnNext.setImageTintList(ColorStateList.valueOf(colorSurface));
                else
                    binding.btnNext.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.colorGrey50)));
            }
            binding.btnNext.setEnabled(nextWeekEnabled);
//            viewModelFinance.getFinanceByTypeAndWeek(type, getListDateWeek(), user.getId()).observe(getViewLifecycleOwner(), modelFinances -> {
//                if (modelFinances != null) loadDataFinance(modelFinances);
//            });

            loadDataByWeek();
        }

        if (filterPeriod.equalsIgnoreCase(getString(R.string.bulanan))) {
            binding.txtMonth.setText(Tools.getFormattedMonthTextSimple(date_ship_millis));
            binding.placeMonth.setEnabled(true);
            @ColorInt int colorSurface = ((MainActivity)getActivity()).getColorFromAttr(getContext(), R.attr.colorOnSurface);

            if (date_ship_millis < today.getTimeInMillis())
                binding.btnNext.setImageTintList(ColorStateList.valueOf(colorSurface));
            else
                binding.btnNext.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.colorGrey50)));
            binding.btnNext.setEnabled(date_ship_millis < today.getTimeInMillis());
        } else {
            binding.txtMonth.setText(getTextWeek());
            binding.placeMonth.setEnabled(false);
        }
    }

    @SuppressLint({"NewApi", "ResourceType"})
    private void loadDataByMonth(long date_ship_milis) {
        binding.txtMonth.setText(Tools.getFormattedMonthTextSimple(date_ship_milis));
        month = Tools.getFormattedMonthSimple(date_ship_milis);
        @ColorInt int colorSurface = ((MainActivity)getActivity()).getColorFromAttr(getContext(), R.attr.colorOnSurface);

        if (date_ship_milis != today.getTimeInMillis()) {
            binding.btnNext.setImageTintList(ColorStateList.valueOf(colorSurface));
            binding.btnNext.setEnabled(true);
        }
        if (date_ship_milis >= today.getTimeInMillis()) {
            binding.btnNext.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.colorGrey50)));
            binding.btnNext.setEnabled(false);
        }

        if (user.getCategory().equalsIgnoreCase(getString(R.string.jurnal_keuangan))) {
            //BY FINANCE
            binding.layoutFinanceJournal.linearlayoutFinanceJournal.setVisibility(View.VISIBLE);
            binding.layoutSavingsProgress.linearlayoutSavingsProgress.setVisibility(View.GONE);

            if (filterType.equalsIgnoreCase(getString(R.string.semuanya))) {
                viewModelFinance.getFinanceByMonth(Tools.getFormattedMonthSimple(date_ship_milis), user.getId(),user.getType_currency()).observe(getViewLifecycleOwner(), modelFinances -> {
                    if (modelFinances != null) {
                        loadDataFinance(modelFinances);
                    }
                });
            } else {
                viewModelFinance.getFinanceByTypeAndMonth(filterType, Tools.getFormattedMonthSimple(date_ship_milis), user.getId(),user.getType_currency()).observe(getViewLifecycleOwner(), modelFinances -> {
                    if (modelFinances != null) {
                        loadDataFinance(modelFinances);
                    }
                });
            }
        } else {
            //BY SAVINGS
            viewModelSavings.findAllSavingsByIdUser(mainActivity.user.getId(),mainActivity.user.getType_currency()).observe(getViewLifecycleOwner(), dataSavings -> {
                if (dataSavings != null) {
                    savingsTargetData = new ArrayList<>(dataSavings);
                }
            });

            binding.layoutFinanceJournal.linearlayoutFinanceJournal.setVisibility(View.GONE);
            binding.layoutSavingsProgress.linearlayoutSavingsProgress.setVisibility(View.VISIBLE);
            if (modelSavings != null) {
                viewModelSavingsProgress.getSavingsByMonth(Tools.getFormattedMonthSimple(date_ship_milis), modelSavings.getId(),modelSavings.getType_currency()).observe(getViewLifecycleOwner(), modelSavings -> {
                    if (modelSavings != null) loadDataSavings(modelSavings);
                });
            }
        }
    }

    private void loadDataByWeek() {
        if (user.getCategory().equalsIgnoreCase(getString(R.string.jurnal_keuangan))) {
            viewModelFinance.getFinanceByTypeAndWeek(filterType, getListDateWeek(), user.getId(),user.getType_currency()).observe(getViewLifecycleOwner(), modelFinances -> {
                if (modelFinances != null) {
                    loadDataFinance(modelFinances);
                }
            });
        } else {
            viewModelSavingsProgress.getSavingsByWeek(getListDateWeek(), modelSavings.getId(),modelSavings.getType_currency()).observe(getViewLifecycleOwner(), modelSavingsProgresses -> {
                if (modelSavingsProgresses != null) loadDataSavings(modelSavingsProgresses);
            });
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadDataFinance(List<ModelFinance> data) {
        financeData = new ArrayList<>(data);
        mainActivity.dataFinance = data;

        loadDataHeader(data);
        if (filterNominal != null)
            data = financeFilter.filterNominal(filterNominal, data);

        List<ModelNestedFinance> listNestedFinance = financeFilter.filterNestedFinance(data);
        listNestedFinance = financeFilter.filterPeriod("terbaru", listNestedFinance);
        adapter = new AdapterFinance(getActivity(), listNestedFinance, this::showDialogFinance);
        adapter.setLocale(locale);

        adapter.setType(TYPE_LAYOUT_MANAGER.VERTICAL);
        binding.rvList.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.rvList.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        binding.placeEmpty.setVisibility(adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

    private void loadDataSavings(List<ModelSavingsProgress> data) {
        savingsData = new ArrayList<>(data);
     //        modelSavings.setProcessValue(new SavingsFilter().totalValueByType(savingsData));
//        viewModelSavings.inputUpdateSavings("update",modelSavings);

        loadTotalSavingsTarget();

        if (filterNominal != null)
            data = savingsFilter.filterNominal(filterNominal, data);

        List<ModelNestedSavings> listNestedSavings = savingsFilter.filterNestedSavings(data);
        listNestedSavings = savingsFilter.filterPeriod("terbaru", listNestedSavings);
        adapterSavings = new AdapterSavings(listNestedSavings, this::showDialogSavings);
        adapterSavings.setLocale(locale);
        adapterSavings.setTotal_value((int) modelSavings.getTargetValue());
        adapterSavings.setType(TYPE_LAYOUT_MANAGER.VERTICAL);
        binding.rvList.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.rvList.setAdapter(adapterSavings);
        adapterSavings.notifyDataSetChanged();

        binding.placeEmpty.setVisibility(adapterSavings.getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

    private void loadDataHeader(List<ModelFinance> data) {
        long total = (financeFilter.totalIncome(data) - financeFilter.totalExpense(data));
        binding.layoutFinanceJournal.txtTotalIncome.setText(convertToCurrency(financeFilter.totalIncome(data),locale));
        binding.layoutFinanceJournal.txtTotalExpense.setText(convertToCurrency(financeFilter.totalExpense(data),locale));
        binding.layoutFinanceJournal.txtTotalAll.setText(convertToCurrency(total,locale));
        binding.layoutFinanceJournal.txtTotalAll.setTextColor(total < 0 ? ContextCompat.getColor(getContext(), R.color.red) : ContextCompat.getColor(getContext(), R.color.white));
    }

    @SuppressLint("SetTextI18n")
    private void loadDataHeaderSavings() {
        long restOfTheDay = Tools.getRestOfTheDay(Tools.getFormattedDateSimple(today.getTimeInMillis()), modelSavings.getDate_target());
        double percentage = calculatePercentage((double) modelSavings.getProcessValue(), (double) modelSavings.getTargetValue());
        String txtPercentage = percentage >= 100 ? getString(R.string.achieved) : percentage + "%";
        long recommendationSavingsDay = Tools.calculateRecommendationDay(modelSavings.getTargetValue(), restOfTheDay);

        binding.layoutSavingsProgress.txtTitle.setText(modelSavings.getTitle());
        binding.layoutSavingsProgress.txtProgress.setText(convertToCurrency(modelSavings.getProcessValue(),locale) + " s/d " + convertToCurrency(modelSavings.getTargetValue(),locale));
        binding.layoutSavingsProgress.progressSavings.setProgress((int) calculatePercentage(modelSavings.getProcessValue(), modelSavings.getTargetValue()));
        binding.layoutSavingsProgress.progressSavings.setMax(100);
        binding.layoutSavingsProgress.txtPercentage.setText(txtPercentage);
        if (restOfTheDay > 0)
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

                        new SweetAlertDialog(getContext(), SweetAlertDialog.WARNING_TYPE)
                                .setTitleText("Hapus")
                                .setContentText("Apakah anda yakin ingin hapus tabungan '" + modelSavings.getTitle() + "'?")
                                .setConfirmText("Ya")
                                .setConfirmClickListener(sweetAlertDialog -> {
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

    private List<String> getListDateWeek() {
        return localizedWeekHelper.getListWeek(localizedWeekHelper.getFirstDay(prevNextWeek - 7), localizedWeekHelper.getLastDay(prevNextWeek));
    }

    private String getTextWeek() {
        return Tools.convertDateFormatWeekText(localizedWeekHelper.getFirstDay(prevNextWeek - 7)) + " - " + Tools.convertDateFormatWeekText(localizedWeekHelper.getLastDay(prevNextWeek));
    }

    @Override
    public void result(ActivityResult result, Intent intent) {
        if (result.getResultCode() == REQUEST_CODE_FINANCE) {
            ModelFinance modelFinance = (ModelFinance) intent.getSerializableExtra("finance");
            for (int i = 0; i < financeData.size(); i++) {
                if (financeData.get(i).getId() == modelFinance.getId())
                    financeData.set(i, modelFinance);
            }

            Log.e("TAG", "result: " + new Gson().toJson(financeData));
            loadDataFinance(financeData);

        } else if (result.getResultCode() == REQUEST_CODE_SAVINGS) {
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

    private void loadTotalSavingsTarget() {
        viewModelSavingsProgress.findTotalProcessValueByIdSavings(mainActivity.modelSavings.getId(),mainActivity.user.getType_currency()).observe(getViewLifecycleOwner(), modelSavings1 -> {
            int progressValue = modelSavings1!=null?modelSavings1:0;
            if (progressValue>=0) {
                modelSavings.setProcessValue(progressValue);
                viewModelSavings.inputUpdateSavings("update",modelSavings);
                setSavingsTarget();
                loadDataHeaderSavings();
            }
        });
    }

    private void setSavingsTarget() {
        tinyDb.putObject("savings", modelSavings);
        mainActivity.modelSavings = modelSavings;
    }
    boolean isHidden;
    public void toggleView() {
        if (isHidden) {
            // Show the view with animation
            ObjectAnimator animatorFabOpen = getObjectAnimator(binding.btnToggle, View.ROTATION, 0, 300);
            animatorFabOpen.start();
            binding.headerView.setVisibility(View.VISIBLE);
            binding.placeDateMonth.setVisibility(View.VISIBLE);
            Tools.setBackgroundColorView(binding.llAppBar, ((MainActivity)getActivity()).modelPrimaryColor);
            binding.headerView.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.fade_in));
        } else {
            // Hide the view with animation
            Animation slideUp = AnimationUtils.loadAnimation(getContext(), R.anim.fade_out);
            slideUp.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {}

                @Override
                public void onAnimationEnd(Animation animation) {
                    binding.headerView.setVisibility(View.GONE);
                    binding.placeDateMonth.setVisibility(View.GONE);
                    @ColorInt int colorSurface = ((MainActivity)getActivity()).getColorFromAttr(getContext(), R.attr.colorSurface);

                    binding.llAppBar.setBackground(new ColorDrawable(colorSurface));
                }

                @Override
                public void onAnimationRepeat(Animation animation) {}
            });
            binding.headerView.startAnimation(slideUp);
            ObjectAnimator animatorFabClose = getObjectAnimator(binding.btnToggle, View.ROTATION, 180, 300);
            animatorFabClose.start();
        }

        isHidden = !isHidden;
    }
    // Fungsi untuk mendapatkan warna dari atribut tema
    String descriptionFinance,descriptionSavings;
    List<ModelFinance>dataFinanceNotification=new ArrayList<>();
    List<ModelSavingsProgress> dataSaving=new ArrayList<>();
    private void setUpNotification(){
        boolean isDailyNotification = tinyDb.getString("time_notification").equalsIgnoreCase("daily");

        boolean isCheckedFinance = tinyDb.getBoolean("isCheckedFinance");

        tinyDb.putBoolean("isCheckedFinance", isCheckedFinance);
        viewModelFinance.getAllFinanceByDate(getFormattedDateSimple(System.currentTimeMillis()),user.getId(), user.getType_currency()).observe(getViewLifecycleOwner(), modelFinances -> {
            dataFinance = modelFinances;
        });
        if (isDailyNotification) {
            viewModelFinance.getAllFinanceByDate(getFormattedDateSimple(System.currentTimeMillis()), user.getId(), user.getType_currency()).observe(getViewLifecycleOwner(), modelFinances -> {
                dataFinanceNotification = modelFinances;
                descriptionFinance = "Data pemasukan anda hari ini :" + Tools.convertToCurrency(new FinanceFilter().totalIncome(dataFinanceNotification), locale) + "\n" +
                        "Data pengeluaran anda hari ini :" + Tools.convertToCurrency(new FinanceFilter().totalExpense(dataFinanceNotification), locale);

            });

        } else {
            viewModelFinance.getFinanceByMonth(Tools.getFormattedMonthSimple(System.currentTimeMillis()), user.getId(), user.getType_currency()).observe(getViewLifecycleOwner(), modelFinances -> {
                dataFinanceNotification = modelFinances;
                descriptionFinance = "Data pemasukan anda bulan ini :" + Tools.convertToCurrency(new FinanceFilter().totalIncome(dataFinanceNotification), locale) + "\n" +
                        "Data pengeluaran anda bulan ini :" + Tools.convertToCurrency(new FinanceFilter().totalExpense(dataFinanceNotification), locale);

            });
        }

        ModelNotification modelNotification =new ModelNotification("Pengingat Pemasukan & Pengeluaran: " + user.getName(), descriptionFinance);

        mainActivity.helperNotification.reminderSet(isCheckedFinance,modelNotification,getString(R.string.jurnal_keuangan),200);

        if (mainActivity.modelSavings == null)
            mainActivity.modelSavings = new ModelSavings();

        boolean isCheckedSavings = tinyDb.getBoolean("isCheckedSavings");

        if (isDailyNotification) {

            viewModelSavingsProgress.findAllSavingsByDate(getFormattedDateSimple(System.currentTimeMillis()), mainActivity.modelSavings.getId(), mainActivity.modelSavings.getType_currency()).observe(getViewLifecycleOwner(), dataSavings -> {
                dataSaving = dataSavings;
                descriptionSavings = "Progress menabung anda hingga hari ini: " + Tools.convertToCurrency(new SavingsFilter().totalValueByType(dataSaving), locale);
            });
        } else {
            viewModelSavingsProgress.getSavingsByMonth(Tools.getFormattedMonthSimple(System.currentTimeMillis()), mainActivity.modelSavings.getId(), mainActivity.modelSavings.getType_currency()).observe(getViewLifecycleOwner(), dataSavings -> {
                dataSaving = dataSavings;
                descriptionSavings = "Progress menabung anda bulan ini: " + Tools.convertToCurrency(new SavingsFilter().totalValueByType(dataSaving), locale);
            });
        }



        ModelNotification modelNotificationSavings =new ModelNotification("Pengingat Progress Menabung: " + user.getName(),"Progress menabung anda hari ini: " + Tools.convertToCurrency(new SavingsFilter().totalValueByType(dataSaving), locale));
        mainActivity.helperNotification.reminderSet(isCheckedSavings,modelNotificationSavings,getString(R.string.menabung),100);

    }
}
