package com.studio.millionares.barberbooker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotifReminder_Receiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String title = intent.getStringExtra("title");
        String content = intent.getStringExtra("content");

        Notifications notifs = new Notifications(context, CONSTANTS.BOOKING_REMINDER_CHANNEL, title, content);
    }

}