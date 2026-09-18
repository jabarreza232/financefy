package id.co.evolution.financefy.helper;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.util.Calendar;

import id.co.evolution.financefy.R;
import id.co.evolution.financefy.model.ModelNotification;

public class HelperNotification {
    Context context;
    TinyDb tinyDb;

    public HelperNotification(Context context) {
        this.context = context;
        tinyDb = new TinyDb(context);
    }

    @SuppressLint({"UnspecifiedImmutableFlag", "ScheduleExactAlarm"})
    public void reminderSet(boolean isChecked, ModelNotification modelNotification, String keyNotif, int requestCode) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, ReminderBroadcast.class);
        intent.putExtra("key", keyNotif);

        // Tambahkan FLAG_UPDATE_CURRENT agar data intent selalu terbarui
        int pendingFlags = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ?
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE :
                PendingIntent.FLAG_UPDATE_CURRENT;

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, pendingFlags);

        if (isChecked) {
            modelNotification.requestCode = requestCode;
            tinyDb.putObject(keyNotif, modelNotification);

            String timeNotif = tinyDb.getString("time_notification");
            boolean isMonthly = "monthly".equalsIgnoreCase(timeNotif);

            int hour = tinyDb.getInt("notif_hour", 8);
            int minute = tinyDb.getInt("notif_minute", 0);
            int day = tinyDb.getInt("notif_day", 1);

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());

            // Setup jam dan menit
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            if (isMonthly) {
                int maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
                calendar.set(Calendar.DAY_OF_MONTH, Math.min(day, maxDays));

                // Jika waktu sudah lewat, set untuk bulan depan
                if (calendar.before(Calendar.getInstance())) {
                    calendar.add(Calendar.MONTH, 1);
                    maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
                    calendar.set(Calendar.DAY_OF_MONTH, Math.min(day, maxDays));
                }
            } else {
                // Daily: Jika waktu sudah lewat hari ini, set untuk besok
                if (calendar.before(Calendar.getInstance())) {
                    calendar.add(Calendar.DAY_OF_MONTH, 1);
                }
            }

            if (alarmManager != null) {
                // GUNAKAN setExactAndAllowWhileIdle untuk akurasi jam 1 pagi
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                }
            }
        } else {
            if (alarmManager != null) {
                alarmManager.cancel(pendingIntent);
            }
            tinyDb.remove(keyNotif);
        }
    }
    public void rescheduleAllAlarmsOnBoot() {
        boolean isCheckedFinance = tinyDb.getBoolean("isCheckedFinance");
        if (isCheckedFinance) {
            ModelNotification financeNotif = tinyDb.getObject(context.getString(R.string.jurnal_keuangan), ModelNotification.class);
            if (financeNotif != null) {
                reminderSet(true, financeNotif, context.getString(R.string.jurnal_keuangan), 200);
            }
        }

        boolean isCheckedSavings = tinyDb.getBoolean("isCheckedSavings");
        if (isCheckedSavings) {
            ModelNotification savingsNotif = tinyDb.getObject(context.getString(R.string.menabung), ModelNotification.class);
            if (savingsNotif != null) {
                reminderSet(true, savingsNotif, context.getString(R.string.menabung), 100);
            }
        }
    }

    public static long getTimeInMillisForTomorrow() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        return calendar.getTimeInMillis();
    }
}
