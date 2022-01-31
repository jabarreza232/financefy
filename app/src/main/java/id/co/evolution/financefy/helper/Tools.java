package id.co.evolution.financefy.helper;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Tools {
    public static String getFormattedMonthSimple(Long dateTime) {
        SimpleDateFormat newFormat = new SimpleDateFormat("MM-yyyy");
        return newFormat.format(new Date(dateTime));
    }
    public static String getFormattedMonthTextSimple(Long dateTime) {
        SimpleDateFormat newFormat = new SimpleDateFormat("MMMM, yyyy");
        return newFormat.format(new Date(dateTime));
    }
}
