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
import id.co.evolution.financefy.model.ModelSavings;
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
    public List<ModelSavingsProgress> filterPeriodSavingProgress(@NonNull String filterPeriod, List<ModelSavingsProgress> list) {
        if (filterPeriod.equalsIgnoreCase("terlama")) {
            Collections.sort(list, ModelSavingsProgress.shortedPeriodLongestToLatest);
        } else {
            Collections.sort(list, ModelSavingsProgress.shortedPeriodLatestToLongest);
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


    public long totalValueByType(List<ModelSavingsProgress> data) {
        long value = 0;
        for (ModelSavingsProgress modelFinance : data)
                value += (long) modelFinance.getProcessValue();

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

    public List<ModelSavingsProgress> listAnalysis(List<ModelSavingsProgress> data) {
        List<ModelSavingsProgress> list = new ArrayList<>();
        HashSet<String> hashsetCategory = new HashSet<>();

        for (ModelSavingsProgress modelFinance : data) {
            hashsetCategory.add(modelFinance.getTitle());
        }

        for (String category : hashsetCategory) {
            ModelSavingsProgress modelFinance = new ModelSavingsProgress();
            modelFinance.setTitle(category);
            double totalValue = 0;
            String date = null;
            for (ModelSavingsProgress finance : data) {
                if (category.equalsIgnoreCase(finance.getTitle())) {
                    totalValue = totalValue + finance.getProcessValue();
                    date = finance.getDate_progress_savings();
                }
            }
            modelFinance.setProcessValue((long) totalValue);
            modelFinance.setTotalValue((int) totalValueByType(data));
            modelFinance.setDate_progress_savings(date);
            list.add(modelFinance);
        }
        return list;
    }
}
