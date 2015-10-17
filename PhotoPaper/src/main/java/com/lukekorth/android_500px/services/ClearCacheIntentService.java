package com.lukekorth.android_500px.services;

import android.app.IntentService;
import android.content.Intent;

import com.activeandroid.ActiveAndroid;
import com.lukekorth.android_500px.WallpaperApplication;
import com.lukekorth.android_500px.helpers.Settings;
import com.lukekorth.android_500px.models.WallpaperChangedEvent;
import com.squareup.picasso.PicassoTools;

import org.slf4j.LoggerFactory;

public class ClearCacheIntentService extends IntentService {

    public ClearCacheIntentService() {
        super("ClearCacheIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        LoggerFactory.getLogger("ClearCacheIntentService").debug("Clearing photo database and cache");

        ActiveAndroid.getDatabase().delete("Photos", null, null);
        PicassoTools.clearCache(WallpaperApplication.getPicasso(this));
        Settings.clearUpdated(this);

        WallpaperApplication.getBus().post(new WallpaperChangedEvent());
        startService(new Intent(this, ApiService.class));
    }
}
