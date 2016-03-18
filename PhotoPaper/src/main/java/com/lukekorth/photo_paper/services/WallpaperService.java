package com.lukekorth.photo_paper.services;

import android.app.IntentService;
import android.app.WallpaperManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.PowerManager;
import android.os.SystemClock;
import android.support.v7.graphics.Palette;

import com.lukekorth.photo_paper.R;
import com.lukekorth.photo_paper.WallpaperApplication;
import com.lukekorth.photo_paper.helpers.Settings;
import com.lukekorth.photo_paper.helpers.Utils;
import com.lukekorth.photo_paper.models.Photos;
import com.lukekorth.photo_paper.models.WallpaperChangedEvent;
import com.lukekorth.photo_paper.sync.SyncAdapter;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.RequestCreator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class WallpaperService extends IntentService {

    public static final String USER_PRESENT_RECEIVER_KEY = "com.lukekorth.photo_paper.services.WallpaperService.SLEEP";
    public static final String SKIP_WALLPAPER_KEY = "com.lukekorth.photo_paper.services.WallpaperService.SKIP";

    public WallpaperService() {
        super("WallpaperService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Logger logger = LoggerFactory.getLogger("WallpaperService");

        if (!Utils.shouldUpdateWallpaper(this) && !intent.getBooleanExtra(SKIP_WALLPAPER_KEY, false)) {
            logger.debug("App is not enabled or wallpaper does not need to be updated");
            return;
        }

        PowerManager.WakeLock wakeLock = ((PowerManager) getSystemService(POWER_SERVICE))
                .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "500pxApiService");
        wakeLock.acquire();

        if (intent.getBooleanExtra(USER_PRESENT_RECEIVER_KEY, false)) {
            logger.debug("User present, waiting 2 seconds and then setting wallpaper");
            SystemClock.sleep(2000);
        }

        WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);
        int width = wallpaperManager.getDesiredMinimumWidth();
        int height = wallpaperManager.getDesiredMinimumHeight();
        if (Utils.supportsParallax(this) && !Settings.useParallax(this)) {
            width = width / 2;
        }

        Photos photo = Photos.getNextPhoto(this);
        if (photo != null) {
            try {
                logger.debug("Setting wallpaper to " + width + "px wide by " + height + "px tall");

                RequestCreator request = WallpaperApplication.getPicasso(this)
                        .load(photo.imageUrl)
                        .centerCrop()
                        .resize(width, height);

                if (Settings.useOnlyWifi(this)) {
                    request.networkPolicy(NetworkPolicy.OFFLINE);
                }

                Bitmap bitmap = request.get();

                wallpaperManager.setBitmap(bitmap);

                photo.palette = Palette.generate(bitmap).getMutedColor(getResources().getColor(R.color.brown));
                photo.seen = true;
                photo.seenAt = System.currentTimeMillis();
                photo.save();

                Settings.setUpdated(this);
            } catch (IOException e) {
                logger.error(e.getMessage());
                photo.failedCount = photo.failedCount + 1;
                if (photo.failedCount > 10) {
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
            logger.debug("Getting more photos via Sync Adapter");
            SyncAdapter.requestSync();
        }

        WallpaperApplication.getBus().post(new WallpaperChangedEvent());

        wakeLock.release();
    }
}

