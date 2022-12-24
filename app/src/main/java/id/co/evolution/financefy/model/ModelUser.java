package id.co.evolution.financefy.model;

import android.net.Uri;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Objects;

import id.co.evolution.financefy.helper.Tools;

@Entity(tableName = "user")
public class ModelUser {
    @ColumnInfo(name = "id_user")
    @PrimaryKey(autoGenerate = true)
    int id;
    @ColumnInfo(name = "name")
    String name;
    @ColumnInfo(name = "type")
    String type;
    @ColumnInfo(name = "category")
    String category;


    public ModelUser() {
    }

    public ModelUser(String name, String type, String category) {
        this.name = name;
        this.type = type;
        this.category = category;

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ModelUser modelUser = (ModelUser) o;
        return Objects.equals(name, modelUser.name) && Objects.equals(type, modelUser.type) && Objects.equals(category, modelUser.category);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, type, category);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }


}
