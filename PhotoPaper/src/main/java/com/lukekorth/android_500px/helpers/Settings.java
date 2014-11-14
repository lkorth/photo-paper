package com.lukekorth.android_500px.helpers;

import android.app.WallpaperManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.HashSet;
import java.util.Set;

public class Settings {

    private static SharedPreferences getPrefs(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static String getFeature(Context context) {
        return getPrefs(context).getString("feature", "popular");
    }

    public static int[] getCategories(Context context) {
        Set<String> defaultCategory = new HashSet<String>();
        defaultCategory.add("8");
        Set<String> prefCategories = getPrefs(context).getStringSet("categories", defaultCategory);

        int[] categories = new int[prefCategories.size()];
        int i = 0;
        for (String category : prefCategories) {
            categories[i] = Integer.parseInt(category);
            i++;
        }

        return categories;
    }

    public static boolean allowNSFW(Context context) {
        return getPrefs(context).getBoolean("allow_nsfw", false);
    }

    public static boolean isEnabled(Context context) {
        return getPrefs(context).getBoolean("enable", false);
    }

    public static int getUpdateInterval(Context context) {
        return Integer.parseInt(getPrefs(context).getString("update_interval", "3600"));
    }

    public static void setUpdateInterval(Context context, String interval) {
        getPrefs(context).edit().putString("update_interval", interval);
    }

    public static boolean useParallax(Context context) {
        return getPrefs(context).getBoolean("use_parallax", false);
    }

    public static boolean useOnlyWifi(Context context) {
        return getPrefs(context).getBoolean("use_only_wifi", true);
    }

    public static long getLastUpdated(Context context) {
        return getPrefs(context).getLong("last_updated", 0);
    }

    public static void setUpdated(Context context) {
        getPrefs(context).edit().putLong("last_updated", System.currentTimeMillis()).apply();
    }

    public static int getDesiredHeight(Context context) {
        int desiredHeight = getPrefs(context).getInt("desired_height", 0);
        if (desiredHeight == 0) {
            desiredHeight = getWallpaperManager(context).getDesiredMinimumHeight();
            setDesiredHeight(context, desiredHeight);
        }

        return desiredHeight;
    }

    public static void setDesiredHeight(Context context, int desiredHeight) {
        getPrefs(context).edit().putInt("desired_height", desiredHeight).apply();
    }

    public static int getDesiredWidth(Context context) {
        int desiredWidth = getPrefs(context).getInt("desired_width", 0);
        if (desiredWidth == 0) {
            desiredWidth = getWallpaperManager(context).getDesiredMinimumWidth();
            if (!Settings.useParallax(context)) {
                desiredWidth = desiredWidth / 2;
            }
            setDesiredWidth(context, desiredWidth);
        }

        return desiredWidth;
    }

    public static void setDesiredWidth(Context context, int desiredWidth) {
        getPrefs(context).edit().putInt("desired_width", desiredWidth).apply();
    }

    private static WallpaperManager getWallpaperManager(Context context) {
        return WallpaperManager.getInstance(context);
    }

}
