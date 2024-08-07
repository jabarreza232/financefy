package id.co.evolution.financefy.helper;

import static android.content.Context.ALARM_SERVICE;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import id.co.evolution.financefy.model.ModelNotification;

public class HelperNotification {
    Context context;
    TinyDb tinyDb;

    public HelperNotification(Context context) {
        this.context = context;
        tinyDb = new TinyDb(context);
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    public void reminderSet(boolean isChecked,ModelNotification modelNotification,String keyNotif,int requestCode) {
        ReminderBroadcast reminderBroadcast = new ReminderBroadcast();
        Intent intent = new Intent(context, reminderBroadcast.getClass());
        ArrayList<Object>dataNotification;
        if(tinyDb.getListObject("dataNotification",ModelNotification.class)!=null){
            dataNotification = tinyDb.getListObject("dataNotification",ModelNotification.class);
        }else{
            dataNotification=new ArrayList<>();
        }

        PendingIntent pendingIntent = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M&&Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            modelNotification.requestCode=requestCode;
            if(dataNotification.size()<2){
                dataNotification.add(modelNotification);
            }else{
                for (int i = 0; i < dataNotification.size(); i++) {
                    ModelNotification modelNotification1 = (ModelNotification) dataNotification.get(i);
                    if(modelNotification1.requestCode==modelNotification.requestCode)
                    dataNotification.set(i,modelNotification);

                }
            }
            tinyDb.putListObject("dataNotification",dataNotification);
            pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_IMMUTABLE);
        }else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            intent.putExtra("key",keyNotif);
            modelNotification.requestCode = requestCode;
            tinyDb.putObject(keyNotif,modelNotification);
            pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_IMMUTABLE);
        }
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        // Set the alarm to start at approximately 2:00 p.m.
        Calendar calendar = Calendar.getInstance();

        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 8);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        // Jika waktu yang ditetapkan sudah berlalu, setel alarm untuk hari berikutnya
        if (Calendar.getInstance().after(calendar)) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);

        if (pendingIntent != null && alarmManager != null && !isChecked) {
            alarmManager.cancel(pendingIntent);

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                dataNotification.remove(modelNotification);
                tinyDb.putListObject("dataNotification", dataNotification);
            }
        }
    }
    public static long getTimeInMillisForTomorrow() {
        // Mendapatkan instance dari Calendar
        Calendar calendar = Calendar.getInstance();

        // Mengatur waktu kalender ke waktu saat ini
        calendar.setTimeInMillis(System.currentTimeMillis());

        // Menambah satu hari ke waktu saat ini
        calendar.add(Calendar.DAY_OF_YEAR, 1);

        // Mengatur jam, menit, dan detik ke waktu yang sama pada hari esok
        calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY));
        calendar.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE));
        calendar.set(Calendar.SECOND, calendar.get(Calendar.SECOND));
        calendar.set(Calendar.MILLISECOND, calendar.get(Calendar.MILLISECOND));

        // Mengembalikan timeInMillis untuk hari esok
        return calendar.getTimeInMillis();
    }

}
