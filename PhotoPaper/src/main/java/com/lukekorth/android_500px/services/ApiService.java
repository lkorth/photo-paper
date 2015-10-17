package com.lukekorth.android_500px.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.PowerManager;

import com.lukekorth.android_500px.R;
import com.lukekorth.android_500px.WallpaperApplication;
import com.lukekorth.android_500px.helpers.Settings;
import com.lukekorth.android_500px.helpers.Utils;
import com.lukekorth.android_500px.interfaces.FiveHundredPxClient;
import com.lukekorth.android_500px.models.Photos;
import com.lukekorth.android_500px.models.User;
import com.lukekorth.android_500px.models.WallpaperChangedEvent;
import com.squareup.otto.Bus;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import retrofit.client.Response;
import retrofit.mime.TypedByteArray;

public class ApiService extends IntentService {

    private Logger mLogger;
    private Bus mBus;
    private int mErrorCount = 0;
    private int mPage = 1;
    private int mTotalPages = 1;

    public ApiService() {
        super("ApiService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mLogger = LoggerFactory.getLogger("ApiService");
        if (Utils.shouldGetPhotos(this)) {
            mLogger.debug("Attempting to fetch new photos");

            PowerManager.WakeLock wakeLock = ((PowerManager) getSystemService(POWER_SERVICE))
                    .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "500pxApiService");
            wakeLock.acquire();

            long startTime = System.currentTimeMillis();

            mBus = WallpaperApplication.getBus();

            while (Utils.needMorePhotos(this) && mPage <= mTotalPages && mErrorCount < 5 &&
                    Utils.isCurrentNetworkOk(this) && (System.currentTimeMillis() - startTime) < 300000) {
                getPhotos();
            }

            startService(new Intent(this, PhotoCacheIntentService.class));

            wakeLock.release();
            mLogger.debug("Done fetching photos");
        } else {
            mBus.post(new WallpaperChangedEvent());
            mLogger.debug("Not getting photos at this time");
        }
    }

    private void getPhotos() {
        try {
            String feature = Settings.getFeature(this);
            String search = feature.equals("search") ? Settings.getSearchQuery(this) : "";

            FiveHundredPxClient client = WallpaperApplication.getFiveHundredPxClient();
            Response response;
            switch (feature) {
                case "search":
                    response = client.getPhotosFromSearch(Settings.getSearchQuery(this), getCategoriesForRequest(), mPage);
                    break;
                case "user_favorites":
                    response = client.getPhotos(feature, getCategoriesForRequest(), User.getUser().userName, mPage);
                    break;
                default:
                    response = client.getPhotos(feature, getCategoriesForRequest(), mPage);
                    break;
            }

            int responseCode = response.getStatus();
            mLogger.debug("Response code: " + responseCode);
            if (responseCode != 200) {
                mErrorCount++;
                return;
            }

            JSONObject body = new JSONObject(new String(((TypedByteArray) response.getBody()).getBytes()));
            mPage = body.getInt("current_page") + 1;
            mTotalPages = body.getInt("total_pages");

            JSONArray photos = body.getJSONArray("photos");
            int desiredHeight = Settings.getDesiredHeight(this);
            int desiredWidth = Settings.getDesiredWidth(this);
            for (int i = 0; i < photos.length(); i++) {
                if (Photos.create(photos.getJSONObject(i), feature, search, desiredHeight, desiredWidth) != null) {
                    mBus.post(new WallpaperChangedEvent());
                }
            }
        } catch (JSONException e) {
            mLogger.error(e.getMessage());
            mErrorCount++;
        }
    }

    private String getCategoriesForRequest() {
        String[] allCategories = getResources().getStringArray(R.array.categories);
        int[] categories = Settings.getCategories(this);
        String filter = "";
        for (int category : categories) {
            filter += allCategories[category] + ",";
        }

        try {
            return URLEncoder.encode(filter.substring(0, filter.length() - 1), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }
}
