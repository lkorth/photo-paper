package com.lukekorth.android_500px.helpers;

import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.lukekorth.android_500px.models.Photos;

import java.util.Random;

public class Utils {

    public static boolean shouldGetPhotos(Context context) {
        return Settings.isEnabled(context) && Utils.needMorePhotos(context) &&
                isCurrentNetworkOk(context);
    }

    public static boolean shouldUpdateWallpaper(Context context) {
        return Settings.isEnabled(context) && (((Settings.getLastUpdated(context) +
                 (Settings.getUpdateInterval(context) * 1000)) < System.currentTimeMillis()));
    }

    public static boolean isCurrentNetworkOk(Context context) {
        return !Settings.useOnlyWifi(context) ||
                (Settings.useOnlyWifi(context) && Utils.isConnectedToWifi(context));
    }

    public static boolean needMorePhotos(Context context) {
        int remainingPhotos = Photos.unseenPhotoCount(context);
        return remainingPhotos <= 10 && remainingPhotos < 144;
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

    public static String getListSummary(Context context, int indexArrayId, int valueArrayId,
                                        String index, String defaultValue) {
        String[] indexArray = context.getResources().getStringArray(indexArrayId);
        String[] valueArray = context.getResources().getStringArray(valueArrayId);
        int i;
        for (i = 0; i < indexArray.length; i++) {
            if (indexArray[i].equals(index)) {
                return valueArray[i];
            }
        }
        return defaultValue;
    }

    public static int generateRandomColor() {
        Random random = new Random();
        int red = random.nextInt(256);
        int green = random.nextInt(256);
        int blue = random.nextInt(256);

        red = (red + Color.red(-7637147)) / 2;
        green = (green + Color.green(-7637147)) / 2;
        blue = (blue + Color.blue(-7637147)) / 2;

        return Color.rgb(red, green, blue);
    }
}
