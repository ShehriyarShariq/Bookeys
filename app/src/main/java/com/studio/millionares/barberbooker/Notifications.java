package com.studio.millionares.barberbooker;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

public class Notifications{

    public Notifications(Context context, String type, String title, String content){

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel notificationChannel;

            if(type.equals(CONSTANTS.BOOKING_CREATION_CHANNEL)){
                notificationChannel = new NotificationChannel(CONSTANTS.BOOKING_CREATION_CHANNEL, CONSTANTS.BOOKING_CREATION_CHANNEL, NotificationManager.IMPORTANCE_DEFAULT);
            } else if(type.equals(CONSTANTS.BOOKING_REMINDER_CHANNEL)){
                notificationChannel = new NotificationChannel(CONSTANTS.BOOKING_REMINDER_CHANNEL, CONSTANTS.BOOKING_REMINDER_CHANNEL, NotificationManager.IMPORTANCE_HIGH);
            } else {
                notificationChannel = new NotificationChannel(CONSTANTS.OFFERS_CHANNEL, CONSTANTS.OFFERS_CHANNEL, NotificationManager.IMPORTANCE_DEFAULT);
            }

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, type)
                .setContentTitle(title)
                .setContentText(content)
                .setAutoCancel(true)
                .setSmallIcon(R.mipmap.ic_launcher_round);

        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(context);
        managerCompat.notify(1, builder.build());

    }

}