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
import id.co.evolution.financefy.model.ModelNestedSavings;
import id.co.evolution.financefy.model.ModelSavingsProgress;

public class SavingsFilter {
    public List<ModelFilter> filterNominal = new ArrayList<>();
    public List<ModelFilter> filterPeriod = new ArrayList<>();


    @Inject
    public SavingsFilter() {
        setUpFilter();
    }

    public List<ModelSavingsProgress> filterNominal(String filterNominal, List<ModelSavingsProgress> list) {
        if (filterNominal.split("-")[0].equalsIgnoreCase("terendah")) {
            Collections.sort(list, ModelSavingsProgress.shortedNominalMinToMax);
        } else {
            Collections.sort(list, ModelSavingsProgress.shortedNominalMaxToMin);
        }

        return list;
    }

    public List<ModelNestedSavings> filterPeriod(@NonNull String filterPeriod, List<ModelNestedSavings> list) {
        if (filterPeriod.equalsIgnoreCase("terlama")) {
            Collections.sort(list, ModelNestedSavings.shortedPeriodLongestToLatest);
        } else {
            Collections.sort(list, ModelNestedSavings.shortedPeriodLatestToLongest);
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
        filterNominal.add(new ModelFilter("Tertinggi-Terendah"));
        filterNominal.add(new ModelFilter("Terendah-Tertinggi"));

        filterPeriod.add(new ModelFilter("Mingguan"));
        filterPeriod.add(new ModelFilter("Bulanan"));
    }

    public void resetFilter() {
        filterNominal.clear();
        filterPeriod.clear();
        setUpFilter();
    }

    public List<ModelNestedSavings> filterNestedSavings(List<ModelSavingsProgress> data) {
        List<ModelNestedSavings> listData = new ArrayList<>();
        HashSet<String> hashset = new HashSet<>();

        for (ModelSavingsProgress modelSavings : data) {
            hashset.add(modelSavings.getDate_progress_savings());
        }

        for (String date : hashset) {
            Log.e("TAG", "filterNestedFinance: " + date);
            ModelNestedSavings modelNestedFinance = new ModelNestedSavings();
            modelNestedFinance.setDate(date);
            List<ModelSavingsProgress> dataSavings = new ArrayList<>();
            for (ModelSavingsProgress modelFinance : data) {
                if (date.equals(modelFinance.getDate_progress_savings()))
                    dataSavings.add(modelFinance);
            }

            modelNestedFinance.setSavingsProgresses(dataSavings);
            listData.add(modelNestedFinance);
        }

        return listData;
    }


    public long totalValueByType(List<ModelFinance> data, String type) {
        long value = 0;
        for (ModelFinance modelFinance : data)
            if (modelFinance.getTipe().equalsIgnoreCase(type))
                value += (long) modelFinance.getJumlahValue();

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

    public List<ModelFinance> listAnalysis(List<ModelFinance> data, String type) {
        List<ModelFinance> list = new ArrayList<>();
        HashSet<String> hashsetCategory = new HashSet<>();

        for (ModelFinance modelFinance : data) {
            hashsetCategory.add(modelFinance.getKategori());
        }

        for (String category : hashsetCategory) {
            ModelFinance modelFinance = new ModelFinance();
            modelFinance.setKategori(category);
            double totalValue = 0;
            String date = null;
            for (ModelFinance finance : data) {
                if (category.contains(finance.getKategori())) {
                    totalValue = totalValue + finance.getJumlahValue();
                    date = finance.getDate();
                }
            }
            modelFinance.setJumlah(Tools.convertToCurrency(totalValue));
            modelFinance.setTotalValue(totalValueByType(data, type));
            modelFinance.setTipe(type);
            modelFinance.setDate(date);
            list.add(modelFinance);
        }
        return list;
    }
}
