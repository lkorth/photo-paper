package com.lukekorth.android_500px.helpers;

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
        return getPrefs(context).getBoolean("enable", true);
    }

    public static int getUpdateInterval(Context context) {
        return Integer.parseInt(getPrefs(context).getString("update_interval", "3600"));
    }

    public static boolean useParallax(Context context) {
        return getPrefs(context).getBoolean("use_parallax", false);
    }

    public static boolean useOnlyWifi(Context context) {
        return getPrefs(context).getBoolean("use_only_wifi", true);
    }

    public static long getNextAlarm(Context context) {
        return getPrefs(context).getLong("next_alarm", 0);
    }

    public static void setNextAlarm(Context context, long wakeupTime) {
        getPrefs(context).edit().putLong("next_alarm", wakeupTime).apply();
    }

}
