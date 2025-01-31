package id.co.evolution.financefy.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;

import id.co.evolution.financefy.helper.Tools;

@Entity(tableName = "savings_progress")
public class ModelSavingsProgress implements Serializable {
    @ColumnInfo(name = "id_progress_savings")
    @PrimaryKey(autoGenerate = true)
    int id;
    @ColumnInfo(name = "title")
    String title;
    @ColumnInfo(name = "description")
    String description;
    @ColumnInfo(name = "process_value")
    long processValue;
    @ColumnInfo(name = "id_savings")
    int id_savings;
    @ColumnInfo(name = "date_progress_savings")
    String date_progress_savings;
    @ColumnInfo(name = "month")
    String month;
    @ColumnInfo(name = "type_currency")
    String type_currency;
    @ColumnInfo(name = "photo")
    String photo;

    @Ignore
    int totalValue;
    public ModelSavingsProgress(int id) {
        this.id = id;
    }

    public ModelSavingsProgress() {
    }

    public String getType_currency() {
        return type_currency;
    }

    public void setType_currency(String type_currency) {
        this.type_currency = type_currency;
    }

    public String getDate_progress_savings() {
        return date_progress_savings;
    }
    public String getDefaultDate(){
        return Tools.convertDateFormat(date_progress_savings);
    }

    public void setDate_progress_savings(String date_progress_savings) {
        this.date_progress_savings = date_progress_savings;
    }

    public int getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(int totalValue) {
        this.totalValue = totalValue;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getProcessValue() {
        return processValue;
    }

    public void setProcessValue(long processValue) {
        this.processValue = processValue;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public int getId_savings() {
        return id_savings;
    }

    public void setId_savings(int id_savings) {
        this.id_savings = id_savings;
    }
    public String getPercentage(int totalValue){
        double percentage = Tools.calculatePercentage(processValue, totalValue);
        return String.valueOf(percentage>=100? 100:percentage);
    }
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }
    public static Comparator<ModelSavingsProgress> shortedNominalMinToMax = (jc1, jc2) -> {
        long min = jc1.getProcessValue();
        long max = jc2.getProcessValue();
        return ((int) (min - max));
    };

    public static Comparator<ModelSavingsProgress> shortedNominalMaxToMin = (jc1, jc2) -> {
        long min = jc1.getProcessValue();
        long max = jc2.getProcessValue();
        return ((int) (max -min));
    };
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ModelSavingsProgress)) return false;
        ModelSavingsProgress that = (ModelSavingsProgress) o;
        return id == that.id && processValue == that.processValue && id_savings == that.id_savings && Objects.equals(title, that.title) && Objects.equals(date_progress_savings, that.date_progress_savings);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, processValue, id_savings, date_progress_savings);
    }


    public static Comparator<ModelSavingsProgress> shortedPeriodLatestToLongest = new Comparator<ModelSavingsProgress>() {
        @Override
        public int compare(ModelSavingsProgress jc1, ModelSavingsProgress jc2) {
            //TODO membuat filter date tahun, bulan, tanggal

            int result =Tools.getDateFromDateFormat(jc2.getDefaultDate(),"year") - Tools.getDateFromDateFormat(jc1.getDefaultDate(),"year");
            if(result == 0)
                result = Tools.getDateFromDateFormat(jc2.getDefaultDate(),"month") - Tools.getDateFromDateFormat(jc1.getDefaultDate(),"month");

            if(result ==0)
                result = Tools.getDateFromDateFormat(jc2.getDefaultDate(),"date") - Tools.getDateFromDateFormat(jc1.getDefaultDate(),"date");

            return  result;
        }
    };

    public static Comparator<ModelSavingsProgress> shortedPeriodLongestToLatest = new Comparator<ModelSavingsProgress>() {
        @Override
        public int compare(ModelSavingsProgress jc1, ModelSavingsProgress jc2) {

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
