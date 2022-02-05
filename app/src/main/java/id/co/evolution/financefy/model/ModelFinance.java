package id.co.evolution.financefy.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "finance")
public class ModelFinance implements Serializable {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_finance")
    int id;
    @ColumnInfo(name = "count")
    String jumlah;
    @ColumnInfo(name = "category")
    String kategori;
    @ColumnInfo(name = "description")
    String keterangan;
    @ColumnInfo(name = "date")
    String date;
    @ColumnInfo(name = "type")
    String tipe;
    @ColumnInfo(name = "month")
    String month;

    public ModelFinance() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getJumlah() {
        return jumlah;
    }

    public void setJumlah(String jumlah) {
        this.jumlah = jumlah;
    }

    public String getKategori() {
        return kategori;
    }

    public void setKategori(String kategori) {
        this.kategori = kategori;
    }

    public String getKeterangan() {
        return keterangan;
    }

    public void setKeterangan(String keterangan) {
        this.keterangan = keterangan;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTipe() {
        return tipe;
    }

    public void setTipe(String tipe) {
        this.tipe = tipe;
    }
}
