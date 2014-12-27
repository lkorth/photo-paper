package com.lukekorth.android_500px.services;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.PowerManager;
import android.os.SystemClock;

import com.lukekorth.android_500px.BuildConfig;
import com.lukekorth.android_500px.R;
import com.lukekorth.android_500px.WallpaperApplication;
import com.lukekorth.android_500px.helpers.Settings;
import com.lukekorth.android_500px.helpers.Utils;
import com.lukekorth.android_500px.models.Photos;
import com.lukekorth.android_500px.models.User;
import com.lukekorth.android_500px.models.WallpaperChangedEvent;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.otto.Bus;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;

public class ApiService extends IntentService {

    public static final String API_BASE_URL = "https://api.500px.com/v1/";

    private Logger mLogger;
    private BroadcastReceiver mWifiReceiver;
    private boolean mIsCurrentNetworkOk;
    private OkHttpClient mOkHttpClient;
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

            registerWifiReceiver();
            mIsCurrentNetworkOk = Utils.isCurrentNetworkOk(this);
            mOkHttpClient = new OkHttpClient();
            while (Utils.needMorePhotos(this) && mPage <= mTotalPages && mErrorCount < 5 &&
                    mIsCurrentNetworkOk && (System.currentTimeMillis() - startTime) < 300000) {
                getPhotos();
            }

            unregisterReceiver(mWifiReceiver);
            wakeLock.release();
            mLogger.debug("Done fetching photos");
        } else {
            mLogger.debug("Not getting photos at this time");
        }
    }

    private void getPhotos() {
        try {
            Request request = new Request.Builder()
                    .header("User-Agent", "com.lukekorth.android_500px")
                    .url(buildRequestUrl())
                    .build();

            Response response = mOkHttpClient.newCall(request).execute();

            int responseCode = response.code();
            mLogger.debug("Response code: " + responseCode);
            if (responseCode != 200) {
                mErrorCount++;
                return;
            }

            JSONObject body = new JSONObject(response.body().string());
            mPage = body.getInt("current_page") + 1;
            mTotalPages = body.getInt("total_pages");

            JSONArray photos = body.getJSONArray("photos");
            String feature = Settings.getFeature(this);
            int desiredHeight = Settings.getDesiredHeight(this);
            int desiredWidth = Settings.getDesiredWidth(this);
            Photos photo;
            Picasso picasso = WallpaperApplication.getPicasso(this);
            for (int i = 0; i < photos.length(); i++) {
                if (!mIsCurrentNetworkOk) {
                    break;
                }

                String search = "";
                if (feature.equals("search")) {
                    search = Settings.getSearchQuery(this);
                }

                photo = Photos.create(photos.getJSONObject(i), feature, search, desiredHeight,
                        desiredWidth);
                if (photo != null) {
                    mLogger.debug("Photo added, caching");
                    picasso.load(photo.imageUrl).fetch();
                    mBus.post(new WallpaperChangedEvent());
                    SystemClock.sleep(200);
                }
            }
        } catch (JSONException e) {
            mLogger.error(e.getMessage());
            mErrorCount++;
        } catch (IOException e) {
            mLogger.error(e.getMessage());
            mErrorCount++;
        } catch (OAuthExpectationFailedException e) {
            mLogger.error(e.getMessage());
            mErrorCount++;
        } catch (OAuthCommunicationException e) {
            mLogger.error(e.getMessage());
            mErrorCount++;
        } catch (OAuthMessageSignerException e) {
            mLogger.error(e.getMessage());
            mErrorCount++;
        }
    }

    private void registerWifiReceiver() {
        mWifiReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mIsCurrentNetworkOk = Utils.isCurrentNetworkOk(context);
                mLogger.debug("Received connectivity change. Network ok: " + mIsCurrentNetworkOk);
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(mWifiReceiver, filter);
    }

    private String buildRequestUrl() throws OAuthCommunicationException,
            OAuthExpectationFailedException, OAuthMessageSignerException {
        String feature = Settings.getFeature(this);
        String url;
        if (feature.equals("search")) {
            url = API_BASE_URL + "photos/search?term=" + Settings.getSearchQuery(this) +
                    "&page=" + mPage + "&image_size=5&rpp=100";
        } else {
            url = API_BASE_URL + "photos?feature=" + feature + "&only=" +
                        getCategoriesForRequest() + "&page=" + mPage + "&image_size=5&rpp=100";
        }

        if (feature.equals("user_favorites")) {
            User user = User.getUser();
            url += "&username=" + user.userName;

            mLogger.debug("Getting photos. Url: " + url);

            CommonsHttpOAuthConsumer consumer = new CommonsHttpOAuthConsumer(
                    BuildConfig.CONSUMER_KEY, BuildConfig.CONSUMER_SECRET);
            consumer.setTokenWithSecret(user.accessToken, user.accessTokenSecret);
            url = consumer.sign(url);
        } else {
            mLogger.debug("Getting photos. Url: " + url);

            url += "&consumer_key=" + BuildConfig.CONSUMER_KEY;
        }

        return url;
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
