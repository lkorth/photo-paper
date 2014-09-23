package com.lukekorth.android_500px;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.activeandroid.ActiveAndroid;
import com.lukekorth.android_500px.helpers.Cache;
import com.lukekorth.android_500px.helpers.ThreadBus;
import com.squareup.otto.Bus;
import com.squareup.picasso.Picasso;

public class WallpaperApplication extends com.activeandroid.app.Application {

    private static final String VERSION = "version";

    private static ThreadBus sBus;
    private static Picasso sPicasso;

    @Override
    public void onCreate() {
        super.onCreate();
        sBus = new ThreadBus();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (prefs.getInt(VERSION, 0) <= 1) {
            ActiveAndroid.getDatabase().delete("Photos", null, null);
            prefs.edit().putInt(VERSION, BuildConfig.VERSION_CODE).apply();
        }
    }

    public static Bus getBus() {
        return sBus;
    }

    public static Picasso getPicasso(Context context) {
        if (sPicasso == null) {
            sPicasso = new Picasso.Builder(context.getApplicationContext())
                    .downloader(Cache.createCacheDownloader(context.getApplicationContext()))
                    .build();
        }
        return sPicasso;
    }

}

