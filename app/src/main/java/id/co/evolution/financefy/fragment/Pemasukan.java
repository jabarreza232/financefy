package id.co.evolution.financefy.fragment;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
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
import java.util.concurrent.ExecutionException;

import id.co.evolution.financefy.App;
import id.co.evolution.financefy.MainActivity;
import id.co.evolution.financefy.R;
import id.co.evolution.financefy.activity.UpdateFinance;
import id.co.evolution.financefy.adapter.AdapterFinance;
import id.co.evolution.financefy.asynctask.FilterMaxMonthAsynctask;
import id.co.evolution.financefy.databinding.FragmentPemasukanBinding;
import id.co.evolution.financefy.helper.Tools;
import id.co.evolution.financefy.model.ModelFinance;
import id.co.evolution.financefy.viewmodel.ViewModelFinance;

public class Pemasukan extends Fragment {
    Calendar today;
    FragmentPemasukanBinding binding;
    public static List<ModelFinance> dataFinance = new ArrayList<>();
    ViewModelFinance viewModelFinance;

    public Pemasukan() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_pemasukan, container, false);
        dataFinance = ((MainActivity) requireActivity()).dataFinance;

        today = Calendar.getInstance();
        today.get(Calendar.YEAR);
        today.get(Calendar.MONTH);
        long date_ship_milis = today.getTimeInMillis();
        binding.txtMonth.setText(Tools.getFormattedMonthTextSimple(date_ship_milis));

        viewModelFinance = new ViewModelProvider(this).get(ViewModelFinance.class);

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
        return binding.getRoot();
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


    private void loadDataByMonth(List<ModelFinance> data) {
        Collections.sort(data, (modelFinance, modelFinance2) -> Integer.parseInt(Tools.convertDateFormat(modelFinance.getDate()).split("-")[0]) - Integer.parseInt(Tools.convertDateFormat(modelFinance2.getDate()).split("-")[0]));

        AdapterFinance adapter = new AdapterFinance(getActivity(), loadDataPemasukan(data), new AdapterFinance.MethodCallback() {
            @Override
            public void onClick(List<ModelFinance> data, int position) {
                showDialog(data, position);
            }
        });
        Log.e("jumlah", adapter.getItemCount() + "");
        adapter.setType(AdapterFinance.TYPE_LAYOUT_MANAGER.VERTICAL);

        binding.rvList.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.rvList.setAdapter(adapter);
        binding.rvList.getAdapter().notifyDataSetChanged();
        if (adapter.getItemCount() == 0) {
            binding.empty.setVisibility(View.VISIBLE);
        } else {
            binding.empty.setVisibility(View.GONE);

        }
    }

    private List<ModelFinance> loadDataPemasukan(List<ModelFinance> finance) {
        List<ModelFinance> data = new ArrayList<>();
        for (ModelFinance modelFinance : finance) {
            if (modelFinance.getTipe().equalsIgnoreCase("pemasukan")) {
                data.add(modelFinance);
            }
        }
        return data;
    }

    private void showDialog(final List<ModelFinance> data, final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Pilih Opsi");
        final String[] tipe = {"Ubah", "Hapus"};
        builder.setItems(tipe, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        Intent i = new Intent(getActivity(), UpdateFinance.class);
                        i.putExtra("id", data.get(position).getId());
                        startActivity(i);
                        dialog.dismiss();
                        break;
                    case 1:
                        App.getDatabase(getActivity()).financeDao().delete(data.get(position));
                        App.getDatabase(getActivity()).financeDao().getAll();
                        dataFinance.remove(position);
                        binding.rvList.getAdapter().notifyDataSetChanged();
                        dialog.dismiss();
                        break;
                }
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();

    }
}
