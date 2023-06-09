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
            pendingIntent = PendingIntent.getBroadcast(context, 200, intent, PendingIntent.FLAG_IMMUTABLE);
        }else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            intent.putExtra("key",keyNotif);
            modelNotification.requestCode = requestCode;
            tinyDb.putObject(keyNotif,modelNotification);
            pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_MUTABLE);
        }
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        // Set the alarm to start at approximately 2:00 p.m.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 8);

        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);

        if (pendingIntent != null && alarmManager != null && !isChecked) {
            alarmManager.cancel(pendingIntent);

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                dataNotification.remove(modelNotification);
                tinyDb.putListObject("dataNotification", dataNotification);
            }
        }
    }
}
