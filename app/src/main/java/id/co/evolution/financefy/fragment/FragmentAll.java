package id.co.evolution.financefy.fragment;

import static id.co.evolution.financefy.helper.Tools.convertToCurrency;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.whiteelephant.monthpicker.MonthPickerDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import id.co.evolution.financefy.MainActivity;
import id.co.evolution.financefy.R;
import id.co.evolution.financefy.adapter.AdapterFilter;
import id.co.evolution.financefy.adapter.AdapterFinance;
import id.co.evolution.financefy.asynctask.FilterMaxMonthAsynctask;
import id.co.evolution.financefy.asynctask.FilterMinYearAsynctask;
import id.co.evolution.financefy.callback.CallbackOnActivityResult;
import id.co.evolution.financefy.databinding.FragmentAllBinding;
import id.co.evolution.financefy.dialog.DialogFilterFinance;
import id.co.evolution.financefy.dialog.DialogMonthPicker;
import id.co.evolution.financefy.helper.FinanceFilter;
import id.co.evolution.financefy.helper.LocalizedWeekHelper;
import id.co.evolution.financefy.helper.TinyDb;
import id.co.evolution.financefy.helper.Tools;
import id.co.evolution.financefy.model.ModelFilter;
import id.co.evolution.financefy.model.ModelFinance;
import id.co.evolution.financefy.model.ModelNestedFinance;
import id.co.evolution.financefy.model.ModelUser;
import id.co.evolution.financefy.model.ModelUserWithFinance;
import id.co.evolution.financefy.repository.FinanceRepository;
import id.co.evolution.financefy.repository.UserRepository;
import id.co.evolution.financefy.viewmodel.ViewModelFinance;
import id.co.evolution.financefy.viewmodel.ViewModelUser;

@AndroidEntryPoint
public class FragmentAll extends Fragment implements CallbackOnActivityResult.OnCallbackResult {
    Calendar today;
    Calendar prevNextMonth;
    List<ModelFinance> dataFinance = new ArrayList<>();
    List<ModelFinance> financeData;
    FragmentAllBinding binding;
    ViewModelFinance viewModelFinance;
    ViewModelUser viewModelUser;
    AdapterFinance adapter;
    AdapterFinance.TYPE_LAYOUT_MANAGER type_layout_manager = AdapterFinance.TYPE_LAYOUT_MANAGER.GRID;
    @Inject
    FinanceFilter financeFilter;
    String filterType, filterNominal, filterPeriod;
    String month;
    @Inject
    LocalizedWeekHelper localizedWeekHelper;
    int prevNextWeek = 0;
    boolean nextWeekEnabled;
    @Inject
    FinanceRepository financeRepository;
    @Inject
    UserRepository userRepository;
    CallbackOnActivityResult mCallbackOnActivityResult;
    int mPositionItem;
    long date_ship_millis;
    ModelUser user;
    @Inject
    TinyDb tinyDb;

    public FragmentAll() {
        // Required empty public constructor
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

        filterType = getString(R.string.semuanya);
        filterPeriod = getString(R.string.bulanan);

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
        viewModelFinance = new ViewModelProvider(this).get(ViewModelFinance.class);
        viewModelUser = new ViewModelProvider(this).get(ViewModelUser.class);

        viewModelUser.init(userRepository);
        viewModelFinance.init(financeRepository);
        user = tinyDb.getObject("user", ModelUser.class);

        if (user != null) {
            viewModelUser.getFinanceByUserId(user.getId()).observe(getViewLifecycleOwner(), new Observer<ModelUserWithFinance>() {
                @Override
                public void onChanged(ModelUserWithFinance modelUserWithFinances) {
                    Log.e("TAG", "onChanged: " + new Gson().toJson(modelUserWithFinances));

                    loadDataFinanceByMonth(date_ship_millis);
                    initiateSayHaloWithTime();
                }
            });

        } else {
            viewModelUser.getAllUser().observe(getViewLifecycleOwner(), modelUserWithFinances -> {
                if (modelUserWithFinances.size() == 0) {
                    user = new ModelUser("Reza", "Menabung", "Pribadi", 0);
                    viewModelUser.inputUpdateUser("create", user);

                } else {
                    for (ModelUser modelUser : modelUserWithFinances)
                        user = modelUser;
                }

//                user = modelUserWithFinances.get(0);
//            Log.e("TAG", "onChanged: "+ new Gson().toJson(modelUserWithFinances));
                loadDataFinanceByMonth(date_ship_millis);
                initiateSayHaloWithTime();
            });
        }


        binding.placeMonth.setOnClickListener(v -> showDialogMonthPicker());

        binding.btnNext.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.colorGrey50)));
        binding.btnNext.setEnabled(false);

        binding.btnPrev.setOnClickListener(v -> {
            if (filterPeriod.equalsIgnoreCase(getString(R.string.bulanan))) {
                prevNextMonth.get(Calendar.YEAR);
                prevNextMonth.add(Calendar.MONTH, -1);
                long date_ship_milisecond = prevNextMonth.getTimeInMillis();
                this.date_ship_millis = date_ship_milisecond;
                loadDataFinanceByMonth(date_ship_milisecond);
            } else {

                prevNextWeek -= 7;
                /*TODO
                    jika sudah sampai minggu saat ini di bulan sekarang  maka tombol next tidak berfungsi
                 */
                nextWeekEnabled = localizedWeekHelper.getMonthLastWeekDay(prevNextWeek) <= today.getTimeInMillis();
                if (nextWeekEnabled)
                    binding.btnNext.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.white)));
                else
                    binding.btnNext.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.colorGrey50)));

                binding.btnNext.setEnabled(nextWeekEnabled);
                binding.txtMonth.setText(getTextWeek());
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
                this.date_ship_millis = date_ship_milisecond;
                loadDataFinanceByMonth(date_ship_milisecond);
            } else {
                prevNextWeek += 7;

                nextWeekEnabled = localizedWeekHelper.getMonthLastWeekDay(prevNextWeek) <= today.getTimeInMillis();
                binding.btnNext.setEnabled(nextWeekEnabled);

                if (nextWeekEnabled)
                    binding.btnNext.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.white)));
                else
                    binding.btnNext.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.colorGrey50)));

                binding.txtMonth.setText(getTextWeek());
                binding.txtMonth.setEnabled(false);


                viewModelFinance.getFinanceByTypeAndWeek(filterType, getListDateWeek(), user.getId()).observe(getViewLifecycleOwner(), modelFinances -> {
                    if (modelFinances != null) {
                        loadData(modelFinances);
                    }
                });
            }
        });


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
                FragmentAll.this.date_ship_millis = date_ship_milis;
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
                loadFilterByType(filterType);
            }
        });

        dialog.showDialogFilterFinance(financeFilter.filterType, financeFilter.filterNominal, financeFilter.filterPeriod, false);
    }

    @SuppressLint({"NewApi", "SetTextI18n"})
    private void loadFilterByType(String type) {
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
            if (date_ship_millis < today.getTimeInMillis())
                binding.btnNext.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.white)));
            else
                binding.btnNext.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.colorGrey50)));
            binding.btnNext.setEnabled(date_ship_millis < today.getTimeInMillis());
        } else {
            binding.txtMonth.setText(getTextWeek());
            binding.placeMonth.setEnabled(false);
        }
    }

    @SuppressLint("NewApi")
    private void loadDataFinanceByMonth(long date_ship_milis) {
        binding.txtMonth.setText(Tools.getFormattedMonthTextSimple(date_ship_milis));
        month = Tools.getFormattedMonthSimple(date_ship_milis);

        if (date_ship_milis != today.getTimeInMillis()) {
            binding.btnNext.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.white)));
            binding.btnNext.setEnabled(true);
        }
        if (date_ship_milis >= today.getTimeInMillis()) {
            binding.btnNext.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.colorGrey50)));
            binding.btnNext.setEnabled(false);
        }

        if (filterType.equalsIgnoreCase(getString(R.string.semuanya))) {
            viewModelFinance.getFinanceByMonth(Tools.getFormattedMonthSimple(date_ship_milis), user.getId()).observe(getViewLifecycleOwner(), modelFinances -> {
                if (modelFinances != null) {
                    loadData(modelFinances);
                }
            });
        } else {
            viewModelFinance.getFinanceByTypeAndMonth(filterType, Tools.getFormattedMonthSimple(date_ship_milis), user.getId()).observe(getViewLifecycleOwner(), modelFinances -> {
                if (modelFinances != null) {
                    loadData(modelFinances);
                }
            });
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadData(List<ModelFinance> data) {
        financeData = new ArrayList<>(data);
        loadDataHeader(data);
        if (filterNominal != null)
            data = financeFilter.filterNominal(filterNominal, data);

        List<ModelNestedFinance> listNestedFinance = financeFilter.filterNestedFinance(data);
        listNestedFinance = financeFilter.filterPeriod("terbaru", listNestedFinance);
        adapter = new AdapterFinance(getActivity(), listNestedFinance, this::showDialog);

        adapter.setType(type_layout_manager);
        binding.rvList.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.rvList.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        binding.placeEmpty.setVisibility(adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

    private void loadDataHeader(List<ModelFinance> data) {
        long total = (financeFilter.totalIncome(data) - financeFilter.totalExpense(data));
        binding.txtTotalIncome.setText(convertToCurrency(financeFilter.totalIncome(data)));
        binding.txtTotalExpense.setText(convertToCurrency(financeFilter.totalExpense(data)));
        binding.txtTotalAll.setText(convertToCurrency(total));
        binding.txtTotalAll.setTextColor(total < 0 ? ContextCompat.getColor(getContext(), R.color.red) : ContextCompat.getColor(getContext(), R.color.green));
    }

    private List<String> getListDateWeek() {
        return localizedWeekHelper.getListWeek(localizedWeekHelper.getFirstDay(prevNextWeek - 7), localizedWeekHelper.getLastDay(prevNextWeek));
    }

    private String getTextWeek() {
        return Tools.convertDateFormatWeekText(localizedWeekHelper.getFirstDay(prevNextWeek - 7)) + " - " + Tools.convertDateFormatWeekText(localizedWeekHelper.getLastDay(prevNextWeek));
    }

    @Override
    public void result(Intent intent) {
        ModelFinance modelFinance = (ModelFinance) intent.getSerializableExtra("finance");
        for (int i = 0; i < financeData.size(); i++) {
            if (financeData.get(i).getId() == modelFinance.getId())
                financeData.set(i, modelFinance);
        }

        Log.e("TAG", "result: " + new Gson().toJson(financeData));
        loadData(financeData);
    }
}
