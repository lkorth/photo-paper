package com.lukekorth.android_500px.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.SystemClock;

import com.lukekorth.android_500px.R;
import com.lukekorth.android_500px.WallpaperApplication;
import com.lukekorth.android_500px.helpers.Settings;
import com.lukekorth.android_500px.helpers.Utils;
import com.lukekorth.android_500px.models.Photos;
import com.lukekorth.android_500px.models.PhotosResponse;
import com.lukekorth.android_500px.models.RemainingPhotosChangedEvent;
import com.lukekorth.android_500px.models.User;
import com.lukekorth.android_500px.models.WallpaperChangedEvent;
import com.squareup.otto.Bus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import retrofit.Call;
import retrofit.Response;

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

            long startTime = System.currentTimeMillis();

            mBus = WallpaperApplication.getBus();

            while (Utils.needMorePhotos(this) && mPage <= mTotalPages && mErrorCount < 5 &&
                    Utils.isCurrentNetworkOk(this) && (System.currentTimeMillis() - startTime) < 300000) {
                getPhotos();
            }

            startService(new Intent(this, PhotoCacheIntentService.class));

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

            Call<PhotosResponse> call;
            switch (feature) {
                case "search":
                    call = WallpaperApplication.getNonLoggedInApiClient()
                            .getPhotosFromSearch(Settings.getSearchQuery(this), getCategoriesForRequest(), mPage);
                    break;
                case "user_favorites":
                    call = WallpaperApplication.getApiClient()
                            .getPhotos(feature, getCategoriesForRequest(), User.getUser().userName, mPage);
                    break;
                default:
                    call = WallpaperApplication.getApiClient()
                            .getPhotos(feature, getCategoriesForRequest(), mPage);
                    break;
            }

            Response<PhotosResponse> response = call.execute();
            int responseCode = response.code();
            mLogger.debug("Response code: " + responseCode);
            if (responseCode != 200) {
                mErrorCount++;
                return;
            }

            mPage = response.body().currentPage + 1;
            mTotalPages = response.body().totalPages;

            for (int i = 0; i < response.body().photos.length; i++) {
                if (Photos.create(response.body().photos[i], response.body().feature, search) != null) {
                    mBus.post(new RemainingPhotosChangedEvent());
                }
                SystemClock.sleep(25);
            }
        } catch (IOException e) {
            mLogger.error(e.getMessage());
            mErrorCount++;
        }

        SystemClock.sleep(5000);
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
