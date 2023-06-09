package id.co.evolution.financefy.model;

import android.content.Context;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.Objects;

import id.co.evolution.financefy.R;
import id.co.evolution.financefy.helper.Tools;

@Entity(tableName = "savings")
public class ModelSavings implements Serializable {
    @ColumnInfo(name = "id_savings")
    @PrimaryKey(autoGenerate = true)
    int id;
    @ColumnInfo(name = "title")
    String title;
    @ColumnInfo(name = "target_value")
    long targetValue;
    @ColumnInfo(name = "process_value")
    long processValue;
    @ColumnInfo(name = "id_savings_user")
    int id_savings_user;
    @ColumnInfo(name = "date_target")
    String date_target;
    @ColumnInfo(name = "type_currency")
    String type_currency;


    public ModelSavings(int id) {
        this.id = id;
    }

    public ModelSavings() {
        id= 0;
        type_currency="IDR";
    }

    public ModelSavings(String title, long targetValue, long processValue, int id_savings_user, String date_target) {
        this.title = title;
        this.targetValue = targetValue;
        this.processValue = processValue;
        this.id_savings_user = id_savings_user;
        this.date_target = date_target;
    }

    public String getType_currency() {
        return type_currency;
    }

    public void setType_currency(String type_currency) {
        this.type_currency = type_currency;
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
    public String getPercentage( Context context){
        double percentage = Tools.calculatePercentage(processValue, targetValue);
        return percentage >= 100 ? context.getString(R.string.achieved) : percentage + "%";
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

    public long getTargetValue() {
        return targetValue;
    }

    public void setTargetValue(long targetValue) {
        this.targetValue = targetValue;
    }

    public int getId_savings_user() {
        return id_savings_user;
    }

    public void setId_savings_user(int id_savings_user) {
        this.id_savings_user = id_savings_user;
    }

    public String getDate_target() {
        return date_target;
    }

    public void setDate_target(String date_target) {
        this.date_target = date_target;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ModelSavings)) return false;
        ModelSavings that = (ModelSavings) o;
        return id == that.id && targetValue == that.targetValue && processValue == that.processValue && id_savings_user == that.id_savings_user && Objects.equals(title, that.title) && Objects.equals(date_target, that.date_target);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, targetValue, processValue, id_savings_user, date_target);
    }
}
