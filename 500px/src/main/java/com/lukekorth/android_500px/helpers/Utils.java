package com.lukekorth.android_500px.helpers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.lukekorth.android_500px.models.Photos;
import com.lukekorth.android_500px.services.WallpaperService;

public class Utils {

    public static boolean shouldGetPhotos(Context context) {
        return Settings.isEnabled(context) && Utils.needMorePhotos(context) && isCurrentNetworkOk(context);
    }

    public static boolean isCurrentNetworkOk(Context context) {
        return !Settings.useOnlyWifi(context) || (Settings.useOnlyWifi(context) && Utils.isConnectedToWifi(context));
    }

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

    public static int dpToPx(Context context, int dp) {
        return Math.round(dp * (context.getResources().getDisplayMetrics().xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public static int getWallpaperHeight(Context context) {
        return WallpaperManager.getInstance(context).getDesiredMinimumHeight();
    }

    public static int getWallpaperWidth(Context context) {
        return WallpaperManager.getInstance(context).getDesiredMinimumWidth();
    }

    public static int getScreenHeight(Context context) {
        return getScreenResolution(context).y;
    }

    public static int getScreenWidth(Context context) {
        return getScreenResolution(context).x;
    }

    private static Point getScreenResolution(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Point point = new Point();
        windowManager.getDefaultDisplay().getSize(point);
        return point;
    }

    public static boolean supportsParallax(Context context) {
        return ((double) getWallpaperWidth(context) / getScreenWidth(context)) >= 2;
    }

    public static void setAlarm(Context context) {
        setAlarm(context, Settings.getUpdateInterval(context));
    }

    public static void setAlarm(Context context, int updateInterval) {
        long wakeupTime = System.currentTimeMillis() + (updateInterval * 1000);
        Settings.setNextAlarm(context, wakeupTime);

        ((AlarmManager) context.getSystemService(Context.ALARM_SERVICE))
                .set(AlarmManager.RTC_WAKEUP, wakeupTime, getAlarmIntent(context));
    }

    public static void cancelAlarm(Context context) {
        ((AlarmManager) context.getSystemService(Context.ALARM_SERVICE)).cancel(getAlarmIntent(context));
    }

    private static PendingIntent getAlarmIntent(Context context) {
        return PendingIntent.getService(context, WallpaperService.WALLPAPER_REQUEST_CODE,
                new Intent(context, WallpaperService.class), PendingIntent.FLAG_CANCEL_CURRENT);
    }

}
