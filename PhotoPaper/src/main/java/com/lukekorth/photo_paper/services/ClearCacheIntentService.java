package com.lukekorth.photo_paper.services;

import android.app.IntentService;
import android.content.Intent;

import com.lukekorth.photo_paper.WallpaperApplication;
import com.lukekorth.photo_paper.helpers.Settings;
import com.lukekorth.photo_paper.models.Photos;
import com.lukekorth.photo_paper.models.WallpaperChangedEvent;
import com.lukekorth.photo_paper.sync.SyncAdapter;
import com.squareup.picasso.PicassoTools;

import org.slf4j.LoggerFactory;

import io.realm.Realm;

public class ClearCacheIntentService extends IntentService {

    public ClearCacheIntentService() {
        super("ClearCacheIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        LoggerFactory.getLogger("ClearCacheIntentService").debug("Clearing photo database and cache");

        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.where(Photos.class).findAll().clear();
        realm.commitTransaction();
        realm.close();

        PicassoTools.clearCache(WallpaperApplication.getPicasso(this));
        Settings.clearUpdated(this);

        WallpaperApplication.getBus().post(new WallpaperChangedEvent());
        SyncAdapter.requestSync();
    }
}
