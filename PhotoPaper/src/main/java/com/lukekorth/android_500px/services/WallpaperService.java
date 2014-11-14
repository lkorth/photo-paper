package com.lukekorth.android_500px.services;

import android.app.IntentService;
import android.app.WallpaperManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.PowerManager;
import android.os.SystemClock;

import com.lukekorth.android_500px.WallpaperApplication;
import com.lukekorth.android_500px.helpers.Settings;
import com.lukekorth.android_500px.helpers.Utils;
import com.lukekorth.android_500px.models.Photos;
import com.lukekorth.android_500px.models.WallpaperChangedEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class WallpaperService extends IntentService {

    public static final String SLEEP_KEY = "sleep";

    public WallpaperService() {
        super("WallpaperService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Logger logger = LoggerFactory.getLogger("WallpaperService");

        if (Settings.isEnabled(this)) {
            PowerManager.WakeLock wakeLock = ((PowerManager) getSystemService(POWER_SERVICE))
                    .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "500pxApiService");
            wakeLock.acquire();

            if (intent.getBooleanExtra(SLEEP_KEY, false)) {
                SystemClock.sleep(2000);
            }

            WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);

            int width = wallpaperManager.getDesiredMinimumWidth();
            int height = wallpaperManager.getDesiredMinimumHeight();
            if (Utils.supportsParallax(this) && !Settings.useParallax(this)) {
                width = width / 2;
            }

            logger.debug("Setting wallpaper to " + width + "px wide by " + height + "px tall");

            Photos photo = Photos.getNextPhoto(this);
            if (photo != null) {
                try {
                    Bitmap bitmap = WallpaperApplication.getPicasso(this)
                            .load(photo.imageUrl)
                            .centerCrop()
                            .resize(width, height)
                            .get();

                    wallpaperManager.setBitmap(bitmap);

                    photo.seen = true;
                    photo.seenAt = System.currentTimeMillis();
                    photo.save();

                    Settings.setUpdated(this);
                } catch (IOException e) {
                    logger.error(e.getMessage());
                    photo.failedCount = photo.failedCount + 1;
                    if (photo.failedCount > 3) {
                        photo.delete();
                    } else {
                        photo.save();
                    }
                    startService(new Intent(this, WallpaperService.class));
                }
            } else {
                logger.debug("Next photo was null");
            }

            if (Utils.needMorePhotos(this)) {
                logger.debug("Getting more photos via ApiService");
                startService(new Intent(this, ApiService.class));
            }

            WallpaperApplication.getBus().post(new WallpaperChangedEvent());

            wakeLock.release();
        } else {
            logger.debug("App not enabled");
        }
    }

}

