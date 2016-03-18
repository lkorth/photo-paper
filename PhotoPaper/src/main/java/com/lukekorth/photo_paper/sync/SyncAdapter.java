package com.lukekorth.photo_paper.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;

import com.lukekorth.photo_paper.R;
import com.lukekorth.photo_paper.WallpaperApplication;
import com.lukekorth.photo_paper.helpers.Settings;
import com.lukekorth.photo_paper.helpers.Utils;
import com.lukekorth.photo_paper.models.Photos;
import com.lukekorth.photo_paper.models.PhotosResponse;
import com.lukekorth.photo_paper.models.RemainingPhotosChangedEvent;
import com.lukekorth.photo_paper.models.User;
import com.squareup.otto.Bus;
import com.squareup.picasso.Picasso;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import io.realm.Realm;
import retrofit2.Call;
import retrofit2.Response;

public class SyncAdapter extends AbstractThreadedSyncAdapter {

    private Logger mLogger;
    private Realm mRealm;
    private Bus mBus;
    private int mErrorCount;
    private int mPage;
    private int mTotalPages;

    public static void requestSync() {
        WallpaperApplication.getBus().post(new RemainingPhotosChangedEvent());
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ContentResolver.requestSync(AccountCreator.getAccount(), "com.lukekorth.android_500px.sync.provider",
                        new Bundle());
            }
        }, 1000);
    }

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        init();
    }

    public SyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        init();
    }

    private void init() {
        mLogger = LoggerFactory.getLogger("SyncAdapter");
        mBus = WallpaperApplication.getBus();
        mErrorCount = 0;
        mPage = 1;
        mTotalPages = 1;
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider,
                              SyncResult syncResult) {
        mRealm = Realm.getDefaultInstance();

        if (Utils.shouldGetPhotos(getContext(), mRealm)) {
            mLogger.debug("Attempting to fetch new photos");

            long startTime = System.currentTimeMillis();

            while (Photos.unseenPhotoCount(getContext(), mRealm) < 100 && mPage <= mTotalPages &&
                    mErrorCount < 5 && Utils.isCurrentNetworkOk(getContext()) &&
                    (System.currentTimeMillis() - startTime) < 300000) {
                getPhotos();
            }

            mLogger.debug("Done fetching photos");

            cachePhotos();
        } else {
            mBus.post(new RemainingPhotosChangedEvent());
            mLogger.debug("Not getting photos at this time");
        }

        mRealm.close();
    }

    private void getPhotos() {
        try {
            String feature = Settings.getFeature(getContext());
            String search = feature.equals("search") ? Settings.getSearchQuery(getContext()) : "";

            Call<PhotosResponse> call;
            switch (feature) {
                case "search":
                    call = WallpaperApplication.getNonLoggedInApiClient()
                            .getPhotosFromSearch(Settings.getSearchQuery(getContext()), getCategoriesForRequest(), mPage);
                    break;
                case "user_favorites":
                    call = WallpaperApplication.getApiClient()
                            .getPhotos(feature, getCategoriesForRequest(), User.getUser(mRealm).getUserName(), mPage);
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
                if (Photos.create(mRealm, response.body().photos[i], response.body().feature, search) != null) {
                    mLogger.debug("Added photo");
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

    private void cachePhotos() {
        mLogger.debug("Caching photos");

        Picasso picasso = WallpaperApplication.getPicasso(getContext());
        List<Photos> photos = Photos.getUnseenPhotos(getContext(), mRealm);
        for (Photos photo : photos) {
            if (Utils.isCurrentNetworkOk(getContext())) {
                picasso.load(photo.imageUrl)
                        .tag("PhotoCacheIntentService")
                        .fetch();
                SystemClock.sleep(50);
            } else {
                picasso.cancelTag("PhotoCacheIntentService");
                break;
            }
        }

        mLogger.debug("Done caching photos");
    }

    private String getCategoriesForRequest() {
        String[] allCategories = getContext().getResources().getStringArray(R.array.categories);
        int[] categories = Settings.getCategories(getContext());
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
