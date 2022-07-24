package id.co.evolution.financefy.helper;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.google.gson.Gson;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LocalizedWeekHelper {

    public LocalizedWeekHelper() {
    }

    public String getFirstDay(int prevNext) {
        Calendar calendar = Calendar.getInstance();
        while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
            calendar.add(Calendar.DATE, 1);
        }
        if (prevNext < 0) {
            calendar.add(Calendar.DAY_OF_MONTH, prevNext);
        } else if (prevNext > 0) {
            calendar.add(Calendar.DAY_OF_MONTH, prevNext);
        }
        return Tools.getFormattedDateDefault(calendar.getTime().getTime());
    }

    public String getLastDay(int prevNext) {
        Calendar calendar = Calendar.getInstance();
        while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
            calendar.add(Calendar.DATE, 1);
        }
        calendar.add(Calendar.DATE, -1);
        if (prevNext < 0) {
            calendar.add(Calendar.DAY_OF_MONTH, prevNext);
        } else if (prevNext > 0) {
            calendar.add(Calendar.DAY_OF_MONTH, prevNext);
        }
        return Tools.getFormattedDateDefault(calendar.getTime().getTime());
    }

    public long getFirstMonthWeekDay(int prevNext) {
        Calendar calendar = Calendar.getInstance();
        while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
            calendar.add(Calendar.DATE, -1);
        }
        if (prevNext < 0) {
            calendar.add(Calendar.DAY_OF_MONTH, prevNext);
        } else if (prevNext > 0) {
            calendar.add(Calendar.DAY_OF_MONTH, prevNext);
        }
        return calendar.getTime().getTime();
    }

    public long getMonthLastWeekDay(int prevNext) {
        Calendar calendar = Calendar.getInstance();
        while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
            calendar.add(Calendar.DATE, 1);
        }
        calendar.add(Calendar.DATE, -1);
        if (prevNext < 0) {
            calendar.add(Calendar.DAY_OF_MONTH, prevNext);
        } else if (prevNext > 0) {
            calendar.add(Calendar.DAY_OF_MONTH, prevNext);
        }
        return calendar.getTime().getTime();
    }

    public List<String> getListWeek(String startWeek, String endWeek) {
        List<String> list = new ArrayList<>();
        int startDate = Integer.parseInt(startWeek.split("-")[2]);
        int endDate = Integer.parseInt(endWeek.split("-")[2]);
        int startYear = Integer.parseInt(startWeek.split("-")[0]);
        int endYear = Integer.parseInt(endWeek.split("-")[0]);
        int startMonth = Integer.parseInt(startWeek.split("-")[1]);
        int endMonth = Integer.parseInt(endWeek.split("-")[1]);

        Log.e("TAG", "getLastDayMonth: " + Tools.getFirstLastDate(endWeek, true));
        if (startMonth != endMonth) {
            for (int i = startDate; i <= Tools.getFirstLastDate(startWeek, false); i++) {
                String date = startYear + "-" + startMonth+"-"+i;
                list.add(Tools.convertDateFormatWeek(date));
            }

            for (int i = 1; i <= endDate; i++) {
                String date = endYear + "-" + endMonth+"-"+i;
                list.add(Tools.convertDateFormatWeek(date));
            }
        } else {
            for (int i = startDate; i <= endDate; i++) {
                String date = startYear + "-" + startMonth;
                date += "-" + i;
                list.add(Tools.convertDateFormatWeek(date));
            }
        }

        Log.e("TAG", "getListWeek: " + new Gson().toJson(list));

        return list;
    }
}
