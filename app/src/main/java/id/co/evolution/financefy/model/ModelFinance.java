package id.co.evolution.financefy.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Locale;

import id.co.evolution.financefy.helper.Tools;

@Entity(tableName = "finance")
public class ModelFinance implements Serializable {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_finance")
    int id;
    @ColumnInfo(name = "amount")
    double jumlah;
    @ColumnInfo(name = "category")
    String kategori;
    @ColumnInfo(name = "description")
    String keterangan;
    @ColumnInfo(name = "photo")
    String photo;
    @ColumnInfo(name = "date")
    String date;
    @ColumnInfo(name = "type")
    String tipe;
    @ColumnInfo(name = "month")
    String month;
    @ColumnInfo(name = "id_finance_user")
    int id_finance_user;
    @ColumnInfo(name = "type_currency")
    String type_currency;
    @Ignore
    long totalValue;


    public ModelFinance() {
    }

    public ModelFinance(double jumlah,String tipe, String kategori, long totalValue) {
        this.jumlah = jumlah;
        this.tipe =tipe;
        this.kategori = kategori;
        this.totalValue = totalValue;
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

    public String getType_currency() {
        return type_currency;
    }

    public void setType_currency(String type_currency) {
        this.type_currency = type_currency;
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

    public double getJumlah() {
        return jumlah;
    }

    public String getJumlahDesc(Locale locale) {
        return Tools.convertToCurrency(jumlah,locale);
    }

    public double getJumlahValue() {
        return jumlah;
    }

    public void setJumlah(double jumlah) {
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

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public static Comparator<ModelFinance> shortedNominalMinToMax = (jc1, jc2) -> {
        double min = jc1.getJumlah();
        double max = jc2.getJumlah();
        return ((int) ((long) min - (long) max));
    };

    public static Comparator<ModelFinance> shortedNominalMaxToMin = (jc1, jc2) -> {
        double min = jc1.getJumlah();
        double max = jc2.getJumlah();
        return ((int) ((long) max - (long) min));
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ModelFinance that = (ModelFinance) o;
        return kategori.equals(that.kategori);
    }


}
