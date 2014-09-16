package com.lukekorth.android_500px.helpers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.lukekorth.android_500px.models.Photos;
import com.lukekorth.android_500px.services.WallpaperService;

public class Utils {

    public static boolean needMorePhotos(Context context) {
        return (Photos.unseenPhotoCount(context) == 0 ||
                (86400 / Photos.unseenPhotoCount(context)) > Settings.getUpdateInterval(context));
    }

    public static boolean isConnectedToWifi(Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        return (activeNetwork != null && activeNetwork.isConnectedOrConnecting() &&
                activeNetwork.getType() == ConnectivityManager.TYPE_WIFI);
    }

    public static void setAlarm(Context context) {
        long wakeupTime = System.currentTimeMillis() + (Settings.getUpdateInterval(context) * 1000);
        Settings.setNextAlarm(context, wakeupTime);

        PendingIntent wakeupIntent = PendingIntent.getService(context,
                WallpaperService.WALLPAPER_REQUEST_CODE,
                new Intent(context, WallpaperService.class),
                PendingIntent.FLAG_CANCEL_CURRENT);

        ((AlarmManager) context.getSystemService(Context.ALARM_SERVICE))
                .set(AlarmManager.RTC_WAKEUP, wakeupTime, wakeupIntent);
    }

}
