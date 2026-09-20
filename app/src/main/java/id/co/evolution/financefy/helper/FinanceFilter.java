package id.co.evolution.financefy.helper;

import android.util.Log;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import javax.inject.Inject;

import id.co.evolution.financefy.model.ModelFilter;
import id.co.evolution.financefy.model.ModelFinance;
import id.co.evolution.financefy.model.ModelNestedFinance;

public class FinanceFilter {
    public List<ModelFilter> filterType = new ArrayList<>();
    public List<ModelFilter> filterNominal = new ArrayList<>();
    public List<ModelFilter> filterPeriod = new ArrayList<>();


    @Inject
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

    public List<ModelNestedFinance> filterPeriod(@NonNull String filterPeriod, List<ModelNestedFinance> list) {
        if (filterPeriod.equalsIgnoreCase("terlama")) {
            Collections.sort(list, ModelNestedFinance.shortedPeriodLongestToLatest);
        } else {
            Collections.sort(list, ModelNestedFinance.shortedPeriodLatestToLongest);
        }

        return list;
    }

    public List<ModelFinance> filterPeriodFinance(@NonNull String filterPeriod, List<ModelFinance> list) {
        if (filterPeriod.equalsIgnoreCase("terlama")) {
            Collections.sort(list, ModelFinance.shortedPeriodLongestToLatest);
        } else {
            Collections.sort(list, ModelFinance.shortedPeriodLatestToLongest);
        }

        return list;
    }

    public void setUpFilter() {
        filterType.add(new ModelFilter("Pengeluaran"));
        filterType.add(new ModelFilter("Pemasukan"));
        filterType.add(new ModelFilter("Semua"));

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
                if (date.equals(modelFinance.getDate()))
                    dataFinance.add(modelFinance);
            }

            modelNestedFinance.setFinances(dataFinance);
            listData.add(modelNestedFinance);
        }

        return listData;
    }

    public long totalExpense(List<ModelFinance> data) {
        long expense = 0;
        for (ModelFinance modelFinance : data)
            if (modelFinance.getTipe().equalsIgnoreCase("pengeluaran"))
                expense += (long) modelFinance.getJumlahValue();

        return expense;
    }

    public long totalIncome(List<ModelFinance> data) {
        long income = 0;
        for (ModelFinance modelFinance : data)
            if (modelFinance.getTipe().equalsIgnoreCase("pemasukan"))
                income += (long) modelFinance.getJumlahValue();

        return income;
    }

    public long totalValueByType(List<ModelFinance> data, String type) {
        if (type == null) return 0;
        boolean isAll = type.equalsIgnoreCase("Semua") || type.equalsIgnoreCase("Semuanya") || type.equalsIgnoreCase("SEMUANYA");
        long value = 0;
        for (ModelFinance modelFinance : data) {
            if (isAll || modelFinance.getTipe().equalsIgnoreCase(type)) {
                value += (long) modelFinance.getJumlahValue();
            }
        }
        return value;
    }

    public List<ModelFinance> listIncome(List<ModelFinance> data) {
        List<ModelFinance> list = new ArrayList<>();
        for (ModelFinance modelFinance : data) {
            if (modelFinance.getTipe().equalsIgnoreCase("pemasukan")) {
                list.add(modelFinance);
            }
        }
        return list;
    }
    public List<ModelFinance> listExpense(List<ModelFinance> data) {
        List<ModelFinance> list = new ArrayList<>();
        for (ModelFinance modelFinance : data) {
            if (modelFinance.getTipe().equalsIgnoreCase("pengeluaran")) {
                list.add(modelFinance);
            }
        }
        return list;
    }

    public List<ModelFinance> listAnalysis(List<ModelFinance> data, String type) {
        List<ModelFinance> list = new ArrayList<>();
        HashSet<String> hashsetCategory = new HashSet<>();

        for (ModelFinance modelFinance : data) {
            hashsetCategory.add(modelFinance.getKategori());
        }

        long totalBase = totalValueByType(data, type);

        for (String category : hashsetCategory) {
            ModelFinance modelFinance = new ModelFinance();
            modelFinance.setKategori(category);
            double totalValue = 0;
            String date = null;
            String actualTipe = null;
            for (ModelFinance finance : data) {
                if (category.equals(finance.getKategori()) || category.contains(finance.getKategori())) {
                    totalValue = totalValue + finance.getJumlahValue();
                    date = finance.getDate();
                    actualTipe = finance.getTipe();
                }
            }
            modelFinance.setJumlah(totalValue);
            modelFinance.setTotalValue(totalBase);
            modelFinance.setTipe(actualTipe != null ? actualTipe : type);
            modelFinance.setDate(date);
            list.add(modelFinance);
        }
        return list;
    }
}
