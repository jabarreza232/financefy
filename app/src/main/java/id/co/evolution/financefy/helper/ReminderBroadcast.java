package id.co.evolution.financefy.helper;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import id.co.evolution.financefy.MainActivity;
import id.co.evolution.financefy.R;
import id.co.evolution.financefy.model.ModelNotification;
import id.co.evolution.financefy.model.ModelUser;

public class ReminderBroadcast extends BroadcastReceiver {
    TinyDb tinyDb;
    ModelUser user;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) return;

        // Handle reboot
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            HelperNotification helperNotification = new HelperNotification(context);
            helperNotification.rescheduleAllAlarmsOnBoot();
            return;
        }

        tinyDb = new TinyDb(context);
        user = tinyDb.getObject("user", ModelUser.class);

        String keyNotif = intent.getStringExtra("key");

        if (keyNotif != null) {
            ModelNotification modelNotification = tinyDb.getObject(keyNotif, ModelNotification.class);

            if (modelNotification != null) {
                pushNotification(context, modelNotification.title, modelNotification.description, modelNotification.requestCode);

                // If monthly frequency, reschedule for next month
                String timeNotif = tinyDb.getString("time_notification");
                if ("monthly".equalsIgnoreCase(timeNotif)) {
                    HelperNotification helperNotification = new HelperNotification(context);
                    helperNotification.reminderSet(true, modelNotification, keyNotif, modelNotification.requestCode);
                }
            }
        }
    }

    private void pushNotification(Context context, String title, String description, int requestCode) {
        String channelId = (user != null) ? String.valueOf(user.getId()) : "financefy_channel";
        String channelName = "Financefy Reminders";

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    channelName,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifikasi untuk pengingat keuangan dan tabungan");

            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        Intent openAppIntent = new Intent(context, MainActivity.class);
        openAppIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        int pendingFlags = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ?
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE :
                PendingIntent.FLAG_UPDATE_CURRENT;

        PendingIntent pendingIntent = PendingIntent.getActivity(context, requestCode, openAppIntent, pendingFlags);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.logo)
                .setContentTitle(title)
                .setContentText(description)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        if (notificationManager != null) {
            notificationManager.notify(requestCode, builder.build());
        }
    }
}
