package id.co.evolution.financefy.model;

import java.util.Comparator;
import java.util.List;

import id.co.evolution.financefy.helper.Tools;

public class ModelNestedSavings {
    String date;
    List<ModelSavingsProgress> savingsProgresses;


    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public List<ModelSavingsProgress> getSavingsProgresses() {
        return savingsProgresses;
    }

    public void setSavingsProgresses(List<ModelSavingsProgress> savingsProgresses) {
        this.savingsProgresses = savingsProgresses;
    }

    public String getDefaultDate(){
        return Tools.convertDateFormat(date);
    }

    public static Comparator<ModelNestedSavings> shortedPeriodLatestToLongest = new Comparator<ModelNestedSavings>() {
        @Override
        public int compare(ModelNestedSavings jc1, ModelNestedSavings jc2) {
            //TODO membuat filter date tahun, bulan, tanggal

            int result =Tools.getDateFromDateFormat(jc2.getDefaultDate(),"year") - Tools.getDateFromDateFormat(jc1.getDefaultDate(),"year");
            if(result == 0)
                result = Tools.getDateFromDateFormat(jc2.getDefaultDate(),"month") - Tools.getDateFromDateFormat(jc1.getDefaultDate(),"month");

            if(result ==0)
                result = Tools.getDateFromDateFormat(jc2.getDefaultDate(),"date") - Tools.getDateFromDateFormat(jc1.getDefaultDate(),"date");

            return  result;
        }
    };

    public static Comparator<ModelNestedSavings> shortedPeriodLongestToLatest = new Comparator<ModelNestedSavings>() {
        @Override
        public int compare(ModelNestedSavings jc1, ModelNestedSavings jc2) {

           //TODO membuat filter date tahun, bulan, tanggal
            int result =Tools.getDateFromDateFormat(jc1.getDefaultDate(),"year") - Tools.getDateFromDateFormat(jc2.getDefaultDate(),"year");
            if(result == 0)
                result = Tools.getDateFromDateFormat(jc1.getDefaultDate(),"month") - Tools.getDateFromDateFormat(jc2.getDefaultDate(),"month");

            if(result ==0)
                result = Tools.getDateFromDateFormat(jc1.getDefaultDate(),"date") - Tools.getDateFromDateFormat(jc2.getDefaultDate(),"date");

            return result;
        }
    };

}
