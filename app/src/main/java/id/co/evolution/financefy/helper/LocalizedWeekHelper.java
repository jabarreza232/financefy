package id.co.evolution.financefy.helper;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
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
        int startMonth = Integer.parseInt(startWeek.split("-")[1]);

        for (int i = startDate; i <= endDate; i++) {
            String date = startYear + "-" + startMonth;
            date += "-" + i;
            list.add(Tools.convertDateFormatWeek(date));
        }
        return list;
    }
}
