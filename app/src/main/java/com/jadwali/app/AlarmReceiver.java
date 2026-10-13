package com.jadwali.app;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context c, Intent i) {
        Scheduler.channel(c);
        PendingIntent open = PendingIntent.getActivity(c, 0, new Intent(c, MainActivity.class), PendingIntent.FLAG_IMMUTABLE);
        Notification n = new Notification.Builder(c, Scheduler.CH)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(i.getStringExtra("title"))
            .setContentText(i.getStringExtra("body"))
            .setAutoCancel(true)
            .setContentIntent(open)
            .build();
        c.getSystemService(NotificationManager.class).notify((int) System.currentTimeMillis(), n);
    }
}
