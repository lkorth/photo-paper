package com.lukekorth.photo_paper.helpers;

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

    public static void setFeature(Context context, String feature) {
        getPrefs(context).edit().putString("feature", feature).apply();
    }

    public static String getSearchQuery(Context context) {
        return getPrefs(context).getString("search_query", "");
    }

    public static void setSearchQuery(Context context, String query) {
        getPrefs(context).edit().putString("search_query", query).apply();
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

    public static void setCategories(Context context, Set<String> categories) {
        getPrefs(context).edit().putStringSet("categories", categories).apply();
    }

    public static boolean isEnabled(Context context) {
        return getPrefs(context).getBoolean("enable", false);
    }

    public static int getUpdateInterval(Context context) {
        return Integer.parseInt(getPrefs(context).getString("update_interval", "3600"));
    }

    public static void setUpdateInterval(Context context, String interval) {
        getPrefs(context).edit().putString("update_interval", interval).apply();
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

    public static void clearUpdated(Context context) {
        getPrefs(context).edit().putLong("last_updated", 0).apply();
    }
}
