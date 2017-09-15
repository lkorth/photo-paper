package com.lukekorth.photo_paper.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

import com.lukekorth.photo_paper.R;
import com.lukekorth.photo_paper.WallpaperApplication;
import com.lukekorth.photo_paper.helpers.PicassoHelper;
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
import java.util.concurrent.TimeUnit;

import io.realm.Realm;
import retrofit2.Call;
import retrofit2.Response;

public class PhotoDownloadIntentService extends IntentService {

    private Logger mLogger;
    private Realm mRealm;
    private Bus mBus;
    private int mErrorCount;
    private int mPage;
    private int mTotalPages;

    public PhotoDownloadIntentService() {
        super(PhotoDownloadIntentService.class.getName());
    }

    public static void downloadPhotos(Context context) {
        context.startService(new Intent(context, PhotoDownloadIntentService.class));
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mLogger = LoggerFactory.getLogger("PhotoDownloadIntentService");
        mBus = WallpaperApplication.getBus();
        mErrorCount = 0;
        mPage = 1;
        mTotalPages = 1;
        mRealm = Realm.getDefaultInstance();

        if (Utils.shouldGetPhotos(this, mRealm)) {
            mLogger.debug("Attempting to fetch new photos");

            long startTime = System.currentTimeMillis();

            while (Photos.unseenPhotoCount(this, mRealm) < 100 && mPage <= mTotalPages &&
                    mErrorCount < 5 && Utils.isCurrentNetworkOk(this) &&
                    (System.currentTimeMillis() - startTime) < 300000) {
                getPhotos();
            }

            mLogger.debug("Done fetching photos");

            cachePhotos();
        } else {
            mBus.post(new RemainingPhotosChangedEvent());
            mLogger.debug("Not getting photos at this time");
        }

        mRealm.beginTransaction();
        mRealm.where(Photos.class)
                .equalTo("seen", true)
                .lessThan("seenAt", System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30))
                .findAll()
                .deleteAllFromRealm();
        mRealm.commitTransaction();

        mRealm.close();
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
                    call = WallpaperApplication.getApiClient().getFavorites(User.getUser(mRealm).getId(),
                            Settings.getFavoriteGalleryId(this), getCategoriesForRequest(), mPage);
                    break;
                default:
                    call = WallpaperApplication.getApiClient().getPhotos(feature, getCategoriesForRequest(), mPage);
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
                if (Photos.create(mRealm, response.body().photos[i], feature, search) != null) {
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

        Picasso picasso = PicassoHelper.getPicasso(this);
        List<Photos> photos = Photos.getUnseenPhotos(this, mRealm);
        for (Photos photo : photos) {
            if (Utils.isCurrentNetworkOk(this)) {
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
