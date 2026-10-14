package com.jadwali.app;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import org.json.JSONArray;
import org.json.JSONObject;

public class Scheduler {
    static final String CH = "reminders";

    static void channel(Context c) {
        NotificationManager nm = c.getSystemService(NotificationManager.class);
        nm.createNotificationChannel(new NotificationChannel(CH, "Reminders", NotificationManager.IMPORTANCE_HIGH));
    }

    static PendingIntent pi(Context c, int code, String title, String body) {
        Intent i = new Intent(c, AlarmReceiver.class);
        i.putExtra("title", title);
        i.putExtra("body", body);
        return PendingIntent.getBroadcast(c, code, i, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    static void schedule(Context c, String json) {
        try {
            SharedPreferences p = c.getSharedPreferences("j", 0);
            AlarmManager am = c.getSystemService(AlarmManager.class);
            int old = p.getInt("n", 0);
            for (int i = 0; i < old; i++) am.cancel(pi(c, i, "", ""));
            JSONArray arr = new JSONArray(json);
            long now = System.currentTimeMillis();
            boolean exact = Build.VERSION.SDK_INT < 31 || am.canScheduleExactAlarms();
            int n = 0;
            for (int k = 0; k < arr.length(); k++) {
                JSONObject o = arr.getJSONObject(k);
                long t = o.getLong("t");
                if (t <= now) continue;
                PendingIntent pi = pi(c, n, o.getString("title"), o.getString("body"));
                if (exact) am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, t, pi);
                else am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, t, pi);
                n++;
            }
            p.edit().putInt("n", n).putString("json", json).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void test(Context c, String t, String b) {
        AlarmManager am = c.getSystemService(AlarmManager.class);
        long at = System.currentTimeMillis() + 5000;
        PendingIntent p = pi(c, 9999, t, b);
        if (Build.VERSION.SDK_INT < 31 || am.canScheduleExactAlarms()) am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, p);
        else am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, p);
    }
}
