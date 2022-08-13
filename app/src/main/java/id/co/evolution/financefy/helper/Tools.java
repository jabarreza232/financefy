package id.co.evolution.financefy.helper;

import android.os.Build;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tools {
    public static int REQUEST_CODE_CALLBACK = 3;
    public enum TYPE_FILTER{
        SEMUANYA,
        PEMASUKAN,
        PENGELUARAN
    }


    public static String getFormattedMonthSimple(Long dateTime) {
        SimpleDateFormat newFormat = new SimpleDateFormat("MM-yyyy");
        return newFormat.format(new Date(dateTime));
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

        return Double.parseDouble(value.replaceAll("[Rp,.]", ""));
    }

    public static String numberFormat(long number) {
        DecimalFormat format;
        String lengthNumber = String.valueOf(number);
        String result = "";
        if (lengthNumber.length() == 4) {
            format = new DecimalFormat("#,###");
            result = format.format(number);
        } else if (lengthNumber.length() == 5) {
            format = new DecimalFormat("##,###");
            result = format.format(number);
        } else if (lengthNumber.length() == 6) {
            format = new DecimalFormat("###,###");
            result = format.format(number);
        } else if (lengthNumber.length() == 7) {
            format = new DecimalFormat("#,###,###");
            result = format.format(number);
        } else if (lengthNumber.length() == 8) {
            format = new DecimalFormat("##,###,###");
            result = format.format(number);
        } else if (lengthNumber.length() == 9) {
            format = new DecimalFormat("###,###,###");
            result = format.format(number);
        } else if (lengthNumber.length() == 10) {
            format = new DecimalFormat("#,###,###,###");
            result = format.format(number);
        } else if (lengthNumber.length() == 11) {
            format = new DecimalFormat("##,###,###,###");
            result = format.format(number);
        } else if (lengthNumber.length() == 12) {
            format = new DecimalFormat("###,###,###,###");
            result = format.format(number);
        } else if (lengthNumber.length() == 13) {
            format = new DecimalFormat("#,###,###,###,###");
            result = format.format(number);
        } else if (lengthNumber.length() == 14) {
            format = new DecimalFormat("##,###,###,###,###");
            result = format.format(number);
        } else if (lengthNumber.length() == 15) {
            format = new DecimalFormat("###,###,###,###,###");
            result = format.format(number);
        } else if (lengthNumber.length() == 16) {
            format = new DecimalFormat("#,###,###,###,###,###");
            result = format.format(number);
        } else {
            result = String.valueOf(number);
        }

        return result;
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
        return value.replaceAll("[Rp,.]", "");
    }

    public static String convertToCurrency(int currency) {
        Locale localeID = new Locale("in", "ID");
        String formatted = NumberFormat.getCurrencyInstance(localeID).format(((double) currency));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            formatted = formatted.replaceAll(",00", "");
        }
        return formatted;
    }

    public static String convertToCurrency(long currency) {
        Locale localeID = new Locale("in", "ID");
        String formatted = NumberFormat.getCurrencyInstance(localeID).format(currency);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            formatted = formatted.replaceAll(",00", "");
        }
        return formatted;
    }

    public static String convertToCurrency(double currency) {
        Locale localeID = new Locale("in", "ID");
        String formatted = NumberFormat.getCurrencyInstance(localeID).format(currency);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            formatted = formatted.replaceAll(",00", "");
        }
        return formatted;
    }

    public static String convertToCurrency(String currency) {
        double parsed = Double.parseDouble(currency);
        Locale localeID = new Locale("in", "ID");
        String formatted = NumberFormat.getCurrencyInstance(localeID).format((parsed));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            formatted = formatted.replaceAll(",00", "");
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
