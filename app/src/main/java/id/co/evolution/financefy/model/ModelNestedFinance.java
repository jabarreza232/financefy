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


    public static Comparator<ModelNestedFinance> shortedPeriodLatestToLongest = new Comparator<ModelNestedFinance>() {
        @Override
        public int compare(ModelNestedFinance jc1, ModelNestedFinance jc2) {
            return  Integer.parseInt(Tools.convertDateFormat(jc2.getDate()).split("-")[0]) - Integer.parseInt(Tools.convertDateFormat(jc1.getDate()).split("-")[0]);
        }
    };

    public static Comparator<ModelNestedFinance> shortedPeriodLongestToLatest = new Comparator<ModelNestedFinance>() {
        @Override
        public int compare(ModelNestedFinance jc1, ModelNestedFinance jc2) {
            return  Integer.parseInt(Tools.convertDateFormat(jc1.getDate()).split("-")[0]) - Integer.parseInt(Tools.convertDateFormat(jc2.getDate()).split("-")[0]);
        }
    };
}
