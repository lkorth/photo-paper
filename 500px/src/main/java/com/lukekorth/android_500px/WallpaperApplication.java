package com.lukekorth.android_500px;

import android.content.Context;

import com.lukekorth.android_500px.helpers.Cache;
import com.lukekorth.android_500px.helpers.ThreadBus;
import com.squareup.otto.Bus;
import com.squareup.picasso.Picasso;

public class WallpaperApplication extends com.activeandroid.app.Application {

    private static ThreadBus sBus;
    private static Picasso sPicasso;

    @Override
    public void onCreate() {
        super.onCreate();
        sBus = new ThreadBus();
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

