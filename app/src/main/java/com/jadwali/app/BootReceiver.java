package com.jadwali.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context c, Intent i) {
        Scheduler.schedule(c, c.getSharedPreferences("j", 0).getString("json", "[]"));
    }
}
