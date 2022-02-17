package id.co.evolution.financefy.model;

import java.util.List;

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
}
