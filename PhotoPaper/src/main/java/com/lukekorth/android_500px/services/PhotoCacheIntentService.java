package com.lukekorth.android_500px.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.SystemClock;

import com.lukekorth.android_500px.WallpaperApplication;
import com.lukekorth.android_500px.helpers.Utils;
import com.lukekorth.android_500px.models.Photos;
import com.squareup.picasso.Picasso;

import java.util.List;

public class PhotoCacheIntentService extends IntentService {

    public PhotoCacheIntentService() {
        super("PhotoCacheIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Picasso picasso = WallpaperApplication.getPicasso(this);
        List<Photos> photos = Photos.getUnseenPhotos(this);
        for (Photos photo : photos) {
            if (Utils.isCurrentNetworkOk(this)) {
                picasso.load(photo.imageUrl)
                        .tag("PhotoCacheIntentService")
                        .fetch();
                SystemClock.sleep(200);
            } else {
                picasso.cancelTag("PhotoCacheIntentService");
                break;
            }
        }
    }
}
