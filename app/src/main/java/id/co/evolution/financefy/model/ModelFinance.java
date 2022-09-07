package id.co.evolution.financefy.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.Comparator;

import id.co.evolution.financefy.helper.Tools;

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
    @ColumnInfo(name = "id_finance_user")
    int id_finance_user;
    @Ignore
    long totalValue;


    public ModelFinance() {
    }

    public int getId_finance_user() {
        return id_finance_user;
    }

    public void setId_finance_user(int id_finance_user) {
        this.id_finance_user = id_finance_user;
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

    public double getJumlahValue() {
        return Tools.replaceCurrencyStringToDouble(jumlah);
    }

    public void setJumlah(String jumlah) {
        this.jumlah = jumlah;
    }

    public String getKategori() {
        return kategori;
    }

    public String getKategoriLowerCase() {
        return kategori.toLowerCase();
    }

    public String getKategoriWithSeparator() {
        return kategori.replace(" ","_").toLowerCase();
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

    public String getDefaultDate() {
        return Tools.convertDateFormat(date);
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

    public long getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(long totalValue) {
        this.totalValue = totalValue;
    }


    public static Comparator<ModelFinance> shortedNominalMinToMax = (jc1, jc2) -> {
        String min = jc1.getJumlah().replaceAll("[Rp,.]", "");
        String max = jc2.getJumlah().replaceAll("[Rp,.]", "");
        return ((int) (Long.parseLong(min) - Long.parseLong(max)));
    };

    public static Comparator<ModelFinance> shortedNominalMaxToMin = (jc1, jc2) -> {
        String min = jc1.getJumlah().replaceAll("[Rp,.]", "");
        String max = jc2.getJumlah().replaceAll("[Rp,.]", "");
        return ((int) (Long.parseLong(max) - Long.parseLong(min)));
    };
    public static Comparator<ModelFinance> shortedPeriodLatestToLongest = (jc1, jc2) -> {
        int result = Tools.getDateFromDateFormat(jc2.getDefaultDate(), "year") - Tools.getDateFromDateFormat(jc1.getDefaultDate(), "year");
        if (result == 0)
            result = Tools.getDateFromDateFormat(jc2.getDefaultDate(), "month") - Tools.getDateFromDateFormat(jc1.getDefaultDate(), "month");

        if (result == 0)
            result = Tools.getDateFromDateFormat(jc2.getDefaultDate(), "date") - Tools.getDateFromDateFormat(jc1.getDefaultDate(), "date");

        return result;
    };

    public static Comparator<ModelFinance> shortedPeriodLongestToLatest = (jc1, jc2) -> {
        int result = Tools.getDateFromDateFormat(jc2.getDefaultDate(), "year") - Tools.getDateFromDateFormat(jc1.getDefaultDate(), "year");
        if (result == 0)
            result = Tools.getDateFromDateFormat(jc2.getDefaultDate(), "month") - Tools.getDateFromDateFormat(jc1.getDefaultDate(), "month");

        if (result == 0)
            result = Tools.getDateFromDateFormat(jc2.getDefaultDate(), "date") - Tools.getDateFromDateFormat(jc1.getDefaultDate(), "date");

        return result;
    };
}
