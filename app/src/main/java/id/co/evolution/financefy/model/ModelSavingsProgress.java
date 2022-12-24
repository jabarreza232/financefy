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

    public ModelSavingsProgress(int id) {
        this.id = id;
    }

    public ModelSavingsProgress() {
    }

    public String getDate_progress_savings() {
        return date_progress_savings;
    }

    public void setDate_progress_savings(String date_progress_savings) {
        this.date_progress_savings = date_progress_savings;
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

    public int getId_savings() {
        return id_savings;
    }

    public void setId_savings(int id_savings) {
        this.id_savings = id_savings;
    }
    public String getPercentage(int totalValue){
        return String.valueOf(Tools.calculatePercentage(processValue, totalValue));
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
}
