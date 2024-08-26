package id.co.evolution.financefy.helper;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.Property;
import android.view.View;
import android.widget.ImageView;

import androidx.core.content.ContextCompat;

import id.co.evolution.financefy.dummy.DummyPrimaryColor.PRIMARY_COLOR;

import java.io.File;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import id.co.evolution.financefy.R;
import id.co.evolution.financefy.model.ModelPrimaryColor;

public class Tools {
    public static int REQUEST_CODE_CALLBACK = 3;

    public enum TYPE_FILTER {
        SEMUANYA,
        PEMASUKAN,
        PENGELUARAN
    }
    public enum CATEGORY_INCOME {
        HASIL_USAHA,
        BONUS,
        GAJI
    }

    public enum CATEGORY_EXPENSE {
        BELANJA_UMUM,
        MAkANAN,
        PULSA_HP,
        TRANSPORTASI,
        TAGIHAN,
        PAKET_INTERNET
    }

    public enum TYPE {
        CLICKED,
        EDIT,
        REMOVED
    }
   public static ModelPrimaryColor modelPrimaryColor=new ModelPrimaryColor("purple",R.color.colorPrimary,R.color.colorPrimaryDark);

    public static void setThemeActivity(Resources.Theme theme,ModelPrimaryColor modelPrimaryColor){
        if(modelPrimaryColor!=null){

            if(modelPrimaryColor.getName().equalsIgnoreCase(PRIMARY_COLOR.Orange.toString()))
                theme.applyStyle(R.style.AppThemeOrange,true);
            else if(modelPrimaryColor.getName().equalsIgnoreCase(PRIMARY_COLOR.Brown.toString()))
                theme.applyStyle(R.style.AppThemeBrown,true);
            else if(modelPrimaryColor.getName().equalsIgnoreCase(PRIMARY_COLOR.Green.toString()))
                theme.applyStyle(R.style.AppThemeGreen,true);
            else if(modelPrimaryColor.getName().equalsIgnoreCase(PRIMARY_COLOR.Red.toString()))
                theme.applyStyle(R.style.AppThemeRed,true);
            else theme.applyStyle(R.style.AppTheme,true);

        }
    }
    public static void setThemeNoActionBarActivity(Resources.Theme theme,ModelPrimaryColor modelPrimaryColor){
        if(modelPrimaryColor!=null){

            if(modelPrimaryColor.getName().equalsIgnoreCase(PRIMARY_COLOR.Orange.toString()))
                theme.applyStyle(R.style.AppThemeOrangeNoActionBar,true);
            else if(modelPrimaryColor.getName().equalsIgnoreCase(PRIMARY_COLOR.Brown.toString()))
                theme.applyStyle(R.style.AppThemeBrownNoActionBar,true);
            else if(modelPrimaryColor.getName().equalsIgnoreCase(PRIMARY_COLOR.Green.toString()))
                theme.applyStyle(R.style.AppThemeGreenNoActionBar,true);
             else if(modelPrimaryColor.getName().equalsIgnoreCase(PRIMARY_COLOR.Red.toString()))
                theme.applyStyle(R.style.AppThemeRedNoActionBar,true);
            else theme.applyStyle(R.style.AppThemeNoActionBar,true);

        }
    }
    public static void setBackgroundColorView(View view,ModelPrimaryColor modelPrimaryColor){
        if(modelPrimaryColor!=null){
            view.setBackgroundColor(ContextCompat.getColor(view.getContext(),modelPrimaryColor.getColorPrimary()));
        }
    }

    public static void setBackgroundTintView(View view,ModelPrimaryColor modelPrimaryColor){
        if(modelPrimaryColor!=null){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                view.setBackgroundTintList(ContextCompat.getColorStateList(view.getContext(),modelPrimaryColor.getColorPrimary()));
            }
        }
    }
    public static void setImageTintView(ImageView view, ModelPrimaryColor modelPrimaryColor){
        if(modelPrimaryColor!=null){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                view.setColorFilter(ContextCompat.getColor(view.getContext(),modelPrimaryColor.getColorPrimary()));
            }
        }
    }
    public static void setBackgroundTintView(View view,int color){
        if(modelPrimaryColor!=null){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                view.setBackgroundTintList(ContextCompat.getColorStateList(view.getContext(),color));
            }
        }
    }
    // Fungsi untuk menghapus cache internal aplikasi
    public static void clearCache(Context context) {
        try {
            File cacheDir = context.getCacheDir();
            deleteDir(cacheDir);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Fungsi untuk menghitung ukuran cache internal aplikasi
    public static long getCacheSize(Context context) {
        try {
            File cacheDir = context.getCacheDir();
            return getDirSize(cacheDir);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    // Fungsi rekursif untuk menghapus direktori
    private static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        } else if (dir != null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }

    // Fungsi rekursif untuk menghitung ukuran direktori
    private static long getDirSize(File dir) {
        long size = 0;
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            if (children != null) {
                for (String child : children) {
                    size += getDirSize(new File(dir, child));
                }
            }
        } else if (dir != null && dir.isFile()) {
            size += dir.length();
        }

        Log.d("TAG", "Directory: " + dir.getAbsolutePath() + " Size: " + size);
        return size;
    }

    public static String getFormattedMonthSimple(Long dateTime) {
        SimpleDateFormat newFormat = new SimpleDateFormat("MM-yyyy");
        return newFormat.format(new Date(dateTime));
    }
    public static SpannableString changeTitleColor(String text,int color){
        SpannableString title = new SpannableString(text);
        title.setSpan(new ForegroundColorSpan(color), 0, title.length(), 0);
        return title;
    }
    public static String changeTitleColor(String text,String color){
        return "<font color='"+color+"'>"+text+"</font>";
    }

    public static long getRestOfTheDay(String startDate, String endDate) {
        SimpleDateFormat df = new SimpleDateFormat("MMMM dd, yyyy");

        try {
            Date start = df.parse(startDate);
            Date end = df.parse(endDate);
            long diff = end.getTime() - start.getTime();
            return TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
        } catch (ParseException e) {
            return 0;
        }
    }

    public static long calculateRecommendationDay(long target, long day) {
        if(day>0)
        return target / day;
        else return target;
    }

    public static long calculateRecommendationMonth(long recommendationPerDay) {
        return recommendationPerDay * 30;
    }

    public static long calculateRecommendationYear(long recommendationPerDay) {
        return recommendationPerDay *365;
    }

    public static long calculateRecommendationMonth(long target, long day,int maximumDayOfMonth) {
        return calculateRecommendationDay(target, day) * maximumDayOfMonth;
    }

    public static long calculateRecommendationYear(long target, long day,int maximumDayOfYear) {
        return calculateRecommendationDay(target, day) * maximumDayOfYear;
    }

    public static long getFormattedMonthToTime(String dateTime) {
        SimpleDateFormat df = new SimpleDateFormat("MM-yyyy");

        try {
            return df.parse(dateTime).getTime();
        } catch (ParseException e) {
            return 0;
        }
    }

    public static String getFormattedYearSimple(Long dateTime) {
        SimpleDateFormat newFormat = new SimpleDateFormat("yyyy");
        return newFormat.format(new Date(dateTime));
    }

    public static ObjectAnimator getObjectAnimator(View view, Property property, float values, long duration) {
        return ObjectAnimator.ofFloat(view, property, values).setDuration(duration);
    }

    public static String getFormattedMonthTextSimple(Long dateTime) {
        SimpleDateFormat newFormat = new SimpleDateFormat("MMMM, yyyy");
        return newFormat.format(new Date(dateTime));
    }

    public static String getFormattedDateSimple(Long dateTime) {
        SimpleDateFormat newFormat = new SimpleDateFormat("MMMM dd, yyyy");
        return newFormat.format(new Date(dateTime));
    }

    public static String getFormattedDateDefault(Long dateTime) {
        SimpleDateFormat newFormat = new SimpleDateFormat("yyyy-MM-dd");
        return newFormat.format(new Date(dateTime));
    }

    public static boolean isSpecialCharacterInMyString(String value) {
        Pattern p = Pattern.compile("[!@#$%&x/()_+=|<>?{}\\[\\]~-]");
        Matcher m = p.matcher(value);
        return m.find();
    }

    public static String replaceStringNumberFormat(String value) {

        return value.replaceAll("[,.]", "");
    }

    public static double replaceCurrencyStringToDouble(String value) {

        return Double.parseDouble(value.replaceAll("[Rp,.$]", ""));
    }

    public static long replaceCurrencyStringToLong(String value) {

        return Long.parseLong(value.replaceAll("[Rp,.$]", ""));
    }

    public static String getSpecialCharacterInMyString(String value) {


        return value.replaceAll("[^+\\-*x/]", "");
    }

    public static double calculatePercentage(double value, double total) {
        double values = value * 100 / total;

        return Math.round(values * 100.0) / 100.0;
    }

    public static String removeLastChar(String s) {
        return s.substring(0, s.length() - 1);
    }

    public static String getLastChar(String s) {
        return s.substring(s.length() - 1);
    }

    public static String getLastDateChar(String s) {
        return s.substring(s.length() - 2);
    }

    public static int getFirstLastDate(String date, boolean isFirst) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date convertedDate = null;
        try {
            convertedDate = dateFormat.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar c = Calendar.getInstance();
        c.setTime(convertedDate);

        return isFirst ? c.getActualMinimum(Calendar.DAY_OF_MONTH) : c.getActualMaximum(Calendar.DAY_OF_MONTH);
    }


    public static String convertCurrencyToValue(String value) {
        return value.replaceAll("[Rp,.$]", "");
    }

    public static String convertToCurrency(int currency,Locale locale) {
        String formatted = NumberFormat.getCurrencyInstance(locale).format(((double) currency));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            formatted = formatted.replaceAll(",00", "");
        }
        return formatted;
    }
    public static Locale getLocaleIDN(){
        return new Locale("in", "ID");
    }
    public static Locale getLocaleUS(){
        return Locale.US;
    }
    public static String convertToCurrency(long currency,Locale locale) {
        String formatted = NumberFormat.getCurrencyInstance(locale).format(currency);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P&&locale.equals(getLocaleIDN())) {
            formatted = formatted.replaceAll(",00", "");
        }
        if (formatted.endsWith(".00") && locale.equals(getLocaleUS())) {
            int centsIndex = formatted.lastIndexOf(".00");
            if (centsIndex != -1) {
                formatted = formatted.substring(0, centsIndex);
            }
        }

        return formatted;
    }

    public static String convertToCurrency(double currency,Locale locale) {
        String formatted = NumberFormat.getCurrencyInstance(locale).format(currency);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P&&locale.equals(getLocaleIDN())) {
            formatted = formatted.replaceAll(",00", "");
        }
        if (formatted.endsWith(".00") && locale.equals(getLocaleUS())) {
            int centsIndex = formatted.lastIndexOf(".00");
            if (centsIndex != -1) {
                formatted = formatted.substring(0, centsIndex);
            }
        }

        return formatted;
    }

    public static String convertToCurrency(String currency,Locale locale) {

        double parsed = Double.parseDouble(currency);
        String formatted = NumberFormat.getCurrencyInstance(locale).format((parsed));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P&&locale.equals(getLocaleIDN())) {
            formatted = formatted.replaceAll(",00", "");
        }
        if (formatted.endsWith(".00") && locale.equals(getLocaleUS())) {
            int centsIndex = formatted.lastIndexOf(".00");
            if (centsIndex != -1) {
                formatted = formatted.substring(0, centsIndex);
            }
        }

        return formatted;
    }

    public static String convertDateFormat(String date) {
        final String OLD_FORMAT = "MMMM dd, yyyy";
        final String NEW_FORMAT = "dd-MM-yyyy";

// August 12, 2010
        String newDateString;

        SimpleDateFormat sdf = new SimpleDateFormat(OLD_FORMAT);
        Date d = null;
        try {
            d = sdf.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        sdf.applyPattern(NEW_FORMAT);
        newDateString = sdf.format(d);

        return newDateString;
    }

    public static Integer getDateFromDateFormat(String date, String type) {
        if (type.equalsIgnoreCase("year"))
            return Integer.parseInt(date.split("-")[2]);
        else if (type.equalsIgnoreCase("month"))
            return Integer.parseInt(date.split("-")[1]);
        else
            return Integer.parseInt(date.split("-")[0]);
    }

    public static String convertDateFormatWeek(String date) {
        final String OLD_FORMAT = "yyyy-MM-dd";
        final String NEW_FORMAT = "MMMM dd, yyyy";

// August 12, 2010
        String newDateString;

        SimpleDateFormat sdf = new SimpleDateFormat(OLD_FORMAT);
        Date d = null;
        try {
            d = sdf.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        sdf.applyPattern(NEW_FORMAT);
        newDateString = sdf.format(d);

        return newDateString;
    }

    public static String convertDateFormatWeekText(String date) {
        final String OLD_FORMAT = "yyyy-MM-dd";
        final String NEW_FORMAT = "dd MMM yyyy";

// August 12, 2010
        String newDateString;

        SimpleDateFormat sdf = new SimpleDateFormat(OLD_FORMAT);
        Date d = null;
        try {
            d = sdf.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        sdf.applyPattern(NEW_FORMAT);
        newDateString = sdf.format(d);

        return newDateString;
    }


    public static String convertDateFormatAnalysis(int size, String date) {
        final String OLD_FORMAT = "dd-MM-yyyy";
        final String NEW_FORMAT;

        NEW_FORMAT = size <= 10 ? "dd MMM" : "dd";

// August 12, 2010
        String newDateString;

        SimpleDateFormat sdf = new SimpleDateFormat(OLD_FORMAT);
        Date d = null;
        try {
            d = sdf.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        sdf.applyPattern(NEW_FORMAT);
        newDateString = sdf.format(d);

        return newDateString;
    }
}
