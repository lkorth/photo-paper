package com.lukekorth.android_500px.services;

import android.app.IntentService;
import android.app.WallpaperManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.PowerManager;

import com.lukekorth.android_500px.WallpaperApplication;
import com.lukekorth.android_500px.helpers.Settings;
import com.lukekorth.android_500px.helpers.Utils;
import com.lukekorth.android_500px.models.Photos;
import com.lukekorth.android_500px.models.WallpaperChangedEvent;

import java.io.IOException;

public class WallpaperService extends IntentService {

    public static final int WALLPAPER_REQUEST_CODE = 1000;

    public WallpaperService() {
        super("WallpaperService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (Settings.isEnabled(this)) {
            PowerManager.WakeLock wakeLock = ((PowerManager) getSystemService(POWER_SERVICE))
                    .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "500pxApiService");
            wakeLock.acquire();

            WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);

            int width = wallpaperManager.getDesiredMinimumWidth();
            int height = wallpaperManager.getDesiredMinimumHeight();
            if (!Settings.useParallax(this)) {
                width = width / 2;
            }

            try {
                Bitmap bitmap = WallpaperApplication.getPicasso(this)
                        .load(Photos.getNextPhoto(this).imageUrl)
                        .centerCrop()
                        .resize(width, height)
                        .get();

                wallpaperManager.setBitmap(bitmap);
            } catch (IOException e) {
            }

            if (Utils.needMorePhotos(this)) {
                startService(new Intent(this, ApiService.class));
            }

            Utils.setAlarm(this);
            WallpaperApplication.getBus().post(new WallpaperChangedEvent());

            wakeLock.release();
        }
    }

}

