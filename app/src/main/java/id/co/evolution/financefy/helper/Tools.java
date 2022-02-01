package id.co.evolution.financefy.helper;

import java.text.ParseException;
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
    public static String getFormattedDateSimple(Long dateTime) {
        SimpleDateFormat newFormat = new SimpleDateFormat("MMMM dd, yyyy");
        return newFormat.format(new Date(dateTime));
    }

    public static String convertDateFormat(String date){
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
}
