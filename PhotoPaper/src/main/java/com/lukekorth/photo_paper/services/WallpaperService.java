package com.lukekorth.photo_paper.services;

import android.app.IntentService;
import android.app.WallpaperManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.PowerManager;
import android.support.v7.graphics.Palette;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.lukekorth.photo_paper.R;
import com.lukekorth.photo_paper.WallpaperApplication;
import com.lukekorth.photo_paper.helpers.PicassoHelper;
import com.lukekorth.photo_paper.helpers.Settings;
import com.lukekorth.photo_paper.helpers.Utils;
import com.lukekorth.photo_paper.models.Photos;
import com.lukekorth.photo_paper.models.WallpaperChangedEvent;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.RequestCreator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import io.realm.Realm;

public class WallpaperService extends IntentService {

    public WallpaperService() {
        super("WallpaperService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Logger logger = LoggerFactory.getLogger("WallpaperService");

        if (!Settings.isEnabled(this)) {
            logger.debug("App is not enabled");
            return;
        }

        PowerManager.WakeLock wakeLock = ((PowerManager) getSystemService(POWER_SERVICE))
                .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "500pxApiService");
        wakeLock.acquire(TimeUnit.MINUTES.toMillis(1));

        WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);
        int width = wallpaperManager.getDesiredMinimumWidth();
        int height = wallpaperManager.getDesiredMinimumHeight();
        if (Utils.supportsParallax(this) && !Settings.useParallax(this)) {
            width = width / 2;
        }

        Realm realm = Realm.getDefaultInstance();
        Photos photo = Photos.getNextPhoto(this, realm);
        if (photo != null) {
            try {
                logger.debug("Setting wallpaper to " + width + "px wide by " + height + "px tall");

                RequestCreator request = PicassoHelper.getPicasso(this)
                        .load(photo.imageUrl)
                        .centerCrop()
                        .resize(width, height);

                if (Settings.useOnlyWifi(this) && !Utils.isConnectedToWifi(this)) {
                    request.networkPolicy(NetworkPolicy.OFFLINE);
                }

                Bitmap bitmap = request.get();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    wallpaperManager.setBitmap(bitmap, null, false, WallpaperManager.FLAG_SYSTEM);
                    wallpaperManager.setBitmap(bitmap, null, false, WallpaperManager.FLAG_LOCK);
                } else {
                    wallpaperManager.setBitmap(bitmap);
                }

                realm.beginTransaction();
                photo.setPalette(Palette.generate(bitmap).getMutedColor(getResources().getColor(R.color.brown)));
                photo.setSeen(true);
                photo.setSeenAt(System.currentTimeMillis());
                realm.commitTransaction();

                Settings.setUpdated(this);

                FirebaseAnalytics.getInstance(this).logEvent("wallpaper_updated", null);
            } catch (IOException e) {
                logger.error(e.toString());
                for (StackTraceElement trace : e.getStackTrace()) {
                    logger.error(trace.toString());
                }

                if (e.getCause() != null) {
                    logger.error(e.getCause().toString());
                    for (StackTraceElement trace : e.getCause().getStackTrace()) {
                        logger.error(trace.toString());
                    }
                }

                realm.beginTransaction();
                if (photo.getFailedCount() > 10) {
                    photo.deleteFromRealm();
                } else {
                    photo.setFailedCount(photo.getFailedCount() + 1);
                }
                realm.commitTransaction();

                startService(new Intent(this, WallpaperService.class));
            }
        } else {
            logger.debug("Next photo was null");
        }

        if (Utils.needMorePhotos(this, realm)) {
            logger.debug("Getting more photos via PhotoDownloadIntentService");
            PhotoDownloadIntentService.downloadPhotos(this);
        }

        WallpaperApplication.getBus().post(new WallpaperChangedEvent());

        realm.close();
        wakeLock.release();
    }
}

