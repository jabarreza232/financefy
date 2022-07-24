package id.co.evolution.financefy.model;

import java.util.Comparator;
import java.util.List;

import id.co.evolution.financefy.helper.Tools;

public class ModelNestedFinance {
    String date;
    List<ModelFinance> finances;


    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public List<ModelFinance> getFinances() {
        return finances;
    }

    public void setFinances(List<ModelFinance> finances) {
        this.finances = finances;
    }

    public String getDefaultDate(){
        return Tools.convertDateFormat(date);
    }

    public static Comparator<ModelNestedFinance> shortedPeriodLatestToLongest = new Comparator<ModelNestedFinance>() {
        @Override
        public int compare(ModelNestedFinance jc1, ModelNestedFinance jc2) {
            int result =Tools.getDateFromDateFormat(jc2.getDefaultDate(),"year") - Tools.getDateFromDateFormat(jc1.getDefaultDate(),"year");
            if(result == 0)
                result = Tools.getDateFromDateFormat(jc2.getDefaultDate(),"month") - Tools.getDateFromDateFormat(jc1.getDefaultDate(),"month");

            if(result ==0)
                result = Tools.getDateFromDateFormat(jc2.getDefaultDate(),"date") - Tools.getDateFromDateFormat(jc1.getDefaultDate(),"date");

            return  result;
        }
    };

    public static Comparator<ModelNestedFinance> shortedPeriodLongestToLatest = new Comparator<ModelNestedFinance>() {
        @Override
        public int compare(ModelNestedFinance jc1, ModelNestedFinance jc2) {
            int result =Tools.getDateFromDateFormat(jc2.getDefaultDate(),"year") - Tools.getDateFromDateFormat(jc1.getDefaultDate(),"year");
            if(result == 0)
                result = Tools.getDateFromDateFormat(jc2.getDefaultDate(),"month") - Tools.getDateFromDateFormat(jc1.getDefaultDate(),"month");

            if(result ==0)
                result = Tools.getDateFromDateFormat(jc2.getDefaultDate(),"date") - Tools.getDateFromDateFormat(jc1.getDefaultDate(),"date");

            return result;
        }
    };
}
