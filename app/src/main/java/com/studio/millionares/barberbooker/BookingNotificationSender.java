package com.studio.millionares.barberbooker;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BookingNotificationSender extends BroadcastReceiver {

    /*
        NOT BEING USED CURRENTLY
    */

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManager notificationManager  = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Notification notification = intent.getParcelableExtra("notification");
        int id = intent.getIntExtra("notifID", 0);
        notificationManager.notify(id, notification);

    }
}
