package id.co.evolution.financefy.fragment;

import android.arch.lifecycle.Observer;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.whiteelephant.monthpicker.MonthPickerDialog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutionException;

import butterknife.BindView;
import butterknife.ButterKnife;
import id.co.evolution.financefy.App;
import id.co.evolution.financefy.R;
import id.co.evolution.financefy.activity.UpdateFinance;
import id.co.evolution.financefy.adapter.AdapterFinance;
import id.co.evolution.financefy.asynctask.FilterMaxMonthAsynctask;
import id.co.evolution.financefy.helper.Tools;
import id.co.evolution.financefy.model.ModelFinance;

public class All extends Fragment {
    @BindView(R.id.rv_list)
    RecyclerView rvList;
    @BindView(R.id.place_month)
    RelativeLayout placeMonth;
    @BindView(R.id.txt_month)
    TextView txtMonth;
    Calendar today;
    List<ModelFinance> dataFinance = new ArrayList<>();
    @BindView(R.id.empty)
    ImageView imgEmpty;

    public All() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_all, container, false);
        ButterKnife.bind(this, view);
        today = Calendar.getInstance();
        loadData();
        today.get(Calendar.YEAR);
        today.get(Calendar.MONTH);
        long date_ship_milis = today.getTimeInMillis();
        txtMonth.setText(Tools.getFormattedMonthTextSimple(date_ship_milis));
        placeMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialogMonthPicker();
            }
        });
        return view;
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
                        txtMonth.setText(Tools.getFormattedMonthTextSimple(date_ship_milis));
                        loadDataByMonth(App.getDatabase(getActivity()).financeDao().loadAllbyMonth(Tools.getFormattedMonthSimple(date_ship_milis)));
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


    @Override
    public void onPause() {
        super.onPause();
        loadData();

    }

    @Override
    public void onStart() {
        super.onStart();
        loadData();
    }

    private void loadData() {
        App.getDatabase(getActivity()).financeDao().getAll().observe(getActivity(), new Observer<List<ModelFinance>>() {
            @Override
            public void onChanged(@Nullable List<ModelFinance> modelFinances) {
                dataFinance = modelFinances;
                AdapterFinance adapter = new AdapterFinance(getActivity(), dataFinance, new AdapterFinance.MethodCallback() {
                    @Override
                    public void onClick(List<ModelFinance> data, int position) {
                        showDialog(data, position);
                    }
                });
                Log.e("jumlah", adapter.getItemCount() + "");
                rvList.setLayoutManager(new LinearLayoutManager(getActivity()));
                rvList.setAdapter(adapter);
                if (adapter.getItemCount() == 0) {
                    imgEmpty.setVisibility(View.VISIBLE);
                } else {
                    imgEmpty.setVisibility(View.GONE);
                }
            }
        });
    }

    private void loadDataByMonth(List<ModelFinance> data) {
        dataFinance = data;
        AdapterFinance adapter = new AdapterFinance(getActivity(), dataFinance, new AdapterFinance.MethodCallback() {
            @Override
            public void onClick(List<ModelFinance> data, int position) {
                showDialog(data, position);
            }
        });
        Log.e("jumlah", adapter.getItemCount() + "");
        rvList.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvList.setAdapter(adapter);
        rvList.getAdapter().notifyDataSetChanged();
        if (adapter.getItemCount() == 0) {
            imgEmpty.setVisibility(View.VISIBLE);
        } else {
            imgEmpty.setVisibility(View.GONE);
        }
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
                        dataFinance.remove(position);
                        rvList.getAdapter().notifyDataSetChanged();
                        dialog.dismiss();
                        break;
                }
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

}
