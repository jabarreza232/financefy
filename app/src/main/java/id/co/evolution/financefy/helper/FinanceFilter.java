package id.co.evolution.financefy.helper;

import android.os.Build;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import id.co.evolution.financefy.model.ModelFilter;
import id.co.evolution.financefy.model.ModelFinance;
import id.co.evolution.financefy.model.ModelNestedFinance;

public class FinanceFilter {
    public List<ModelFilter> filterType = new ArrayList<>();
    public List<ModelFilter> filterNominal = new ArrayList<>();
    public List<ModelFilter> filterPeriod = new ArrayList<>();

    public FinanceFilter() {
        setUpFilter();
    }

    public List<ModelFinance> filterNominal(String filterNominal, List<ModelFinance> list) {
        if (filterNominal.split("-")[0].equalsIgnoreCase("terendah")) {
            Collections.sort(list, ModelFinance.shortedNominalMinToMax);
        } else {
            Collections.sort(list, ModelFinance.shortedNominalMaxToMin);
        }

        return list;
    }

    public List<ModelNestedFinance> filterPeriod(String filterPeriod, List<ModelNestedFinance> list) {
        if (filterPeriod.split("-")[0].equalsIgnoreCase("terlama")) {
            Collections.sort(list, ModelNestedFinance.shortedPeriodLongestToLatest);
        } else {
            Collections.sort(list, ModelNestedFinance.shortedPeriodLatestToLongest);
        }

        return list;
    }

    public void setUpFilter() {
        filterType.add(new ModelFilter("Pengeluaran"));
        filterType.add(new ModelFilter("Pemasukan"));
        filterType.add(new ModelFilter("Semuanya"));

        filterNominal.add(new ModelFilter("Tertinggi-Terendah"));
        filterNominal.add(new ModelFilter("Terendah-Tertinggi"));

        filterPeriod.add(new ModelFilter("Mingguan"));
        filterPeriod.add(new ModelFilter("Bulanan"));
    }

    public void resetFilter() {
        filterNominal.clear();
        filterType.clear();
        filterPeriod.clear();
        setUpFilter();
    }

    public List<ModelNestedFinance> filterNestedFinance(List<ModelFinance> data) {
        List<ModelNestedFinance> listData = new ArrayList<>();
        HashSet<String> hashset = new HashSet<>();

        for (ModelFinance modelFinance : data) {
            hashset.add(modelFinance.getDate());
        }
        for (String date : hashset) {
            Log.e("TAG", "filterNestedFinance: " + date);
            ModelNestedFinance modelNestedFinance = new ModelNestedFinance();
            modelNestedFinance.setDate(date);
            List<ModelFinance> dataFinance = new ArrayList<>();
            for (ModelFinance modelFinance : data) {
                if (date.contains(modelFinance.getDate()))
                    dataFinance.add(modelFinance);
            }
            modelNestedFinance.setFinances(dataFinance);
            listData.add(modelNestedFinance);
        }
        return listData;
    }

    public int totalExpense(List<ModelFinance> data) {
        int income = 0;
        for (ModelFinance modelFinance : data)
            if (modelFinance.getTipe().equalsIgnoreCase("pengeluaran"))
                income += Integer.parseInt(modelFinance.getJumlah().replaceAll("[Rp,.]", ""));

        return income;
    }

    public int totalIncome(List<ModelFinance> data) {
        int income = 0;
        for (ModelFinance modelFinance : data)
            if (modelFinance.getTipe().equalsIgnoreCase("pemasukan"))
                income += Integer.parseInt(modelFinance.getJumlah().replaceAll("[Rp,.]", ""));

        return income;
    }
}
