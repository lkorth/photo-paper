package com.lukekorth.photo_paper.helpers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

import com.lukekorth.photo_paper.services.WallpaperService;

public class AlarmHelper {

    public static boolean isAlarmSet(Context context) {
        return (PendingIntent.getService(context, 0,
                new Intent(context, WallpaperService.class), PendingIntent.FLAG_NO_CREATE)) != null;
    }

    public static void scheduleWallpaperAlarm(Context context) {
        scheduleWallpaperAlarm(context, Settings.getUpdateInterval(context));
    }

    public static void scheduleWallpaperAlarm(Context context, int duration) {
        removeWallpaperAlarm(context);
        long interval = duration * 1000;
        getAlarmManager(context).setInexactRepeating(AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + interval, interval, getPendingIntent(context));
    }

    public static void removeWallpaperAlarm(Context context) {
        PendingIntent pendingIntent = getPendingIntent(context);
        getAlarmManager(context).cancel(getPendingIntent(context));
        pendingIntent.cancel();
    }

    private static AlarmManager getAlarmManager(Context context) {
        return (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    private static PendingIntent getPendingIntent(Context context) {
        return PendingIntent.getService(context, 0, new Intent(context, WallpaperService.class), 0);
    }
}
