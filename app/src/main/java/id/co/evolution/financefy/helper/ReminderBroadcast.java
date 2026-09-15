package id.co.evolution.financefy.helper;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import id.co.evolution.financefy.R;
import id.co.evolution.financefy.model.ModelNotification;
import id.co.evolution.financefy.model.ModelUser;

public class ReminderBroadcast extends BroadcastReceiver {
    TinyDb tinyDb;
    ModelUser user;

    @Override
    public void onReceive(Context context, Intent intent) {
        tinyDb = new TinyDb(context);
        user = tinyDb.getObject("user", ModelUser.class);

//        String title = intent.getStringExtra("title");
//        String description = intent.getStringExtra("description");
//        int id = intent.getIntExtra("id",0);
//
//        String title = tinyDb.getString("title");
//        String description = tinyDb.getString("description");
//



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M&&Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            ArrayList<Object>dataNotification=tinyDb.getListObject("dataNotification",ModelNotification.class);
            for (Object object:dataNotification){
                ModelNotification modelNotification = (ModelNotification) object;
                String title = modelNotification.title;
                String description = modelNotification.description;
                pushNotification(context,title,description, modelNotification.requestCode);
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            String keyNotif = intent.getStringExtra("key");

            ModelNotification modelNotification = tinyDb.getObject(keyNotif,ModelNotification.class);
            if(modelNotification!=null){
                String title = modelNotification.title;
                String description = modelNotification.description;
                pushNotification(context,title,description, modelNotification.requestCode);
            }
        }
    }
    private void pushNotification(Context context,String title,String description,int requestCode){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context,""+user.getId())
                .setSmallIcon(R.drawable.logo)
                .setContentTitle(title)
                .setContentText(description)
                .setPriority(NotificationCompat.PRIORITY_MAX);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(requestCode,builder.build());

    }
}
