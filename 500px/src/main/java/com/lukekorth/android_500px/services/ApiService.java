package com.lukekorth.android_500px.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;

import com.activeandroid.ActiveAndroid;
import com.lukekorth.android_500px.R;
import com.lukekorth.android_500px.helpers.Settings;
import com.lukekorth.android_500px.helpers.Utils;
import com.lukekorth.android_500px.models.Photos;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class ApiService extends IntentService {

    private static final String API_BASE_URL = "https://api.500px.com/v1/";
    private static final String CONSUMER_KEY = "3JkjLiYvQN9bYufEc9h9OxdjUmYG26FlEFmOM9G9";

    private int mPage = 1;

    public ApiService() {
        super("ApiService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (shouldGetPhotos() && Settings.isEnabled(this)) {
            PowerManager.WakeLock wakeLock = ((PowerManager) getSystemService(POWER_SERVICE))
                    .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "500pxApiService");
            wakeLock.acquire();

            while (Utils.needMorePhotos(this)) {
                getPhotos();
            }

            if (Settings.getNextAlarm(this) == 0) {
                startService(new Intent(this, WallpaperService.class));
            }

            wakeLock.release();
        }
    }

    private boolean shouldGetPhotos() {
        return (Utils.needMorePhotos(this) &&
                (!Settings.useOnlyWifi(this) || (Settings.useOnlyWifi(this) && Utils.isConnectedToWifi(this))));
    }

    private void getPhotos() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .header("User-Agent", "com.lukekorth.android_500px")
                .url(API_BASE_URL + "photos?feature=" + Settings.getFeature(this) + "&only=" +
                        getCategoriesForRequest() + "&page=" + mPage +
                        "&sort=created_at&image_size=5&rpp=100&consumer_key=" +
                        CONSUMER_KEY)
                .build();

        mPage++;

        ActiveAndroid.beginTransaction();
        try {
            Response response = client.newCall(request).execute();
            JSONArray json = new JSONObject(response.body().string()).getJSONArray("photos");

            Photos photo;
            for (int i = 0; i < json.length(); i++) {
                photo = Photos.create(json.getJSONObject(i), Settings.getFeature(this));
                if (photo != null) {
                    Picasso.with(this)
                            .load(photo.imageUrl)
                            .fetch();
                }
            }
            ActiveAndroid.setTransactionSuccessful();
        } catch (JSONException e) {
            Log.d("ApiService", e.getMessage());
        } catch (IOException e) {
            Log.d("ApiService", e.getMessage());
        } finally {
            ActiveAndroid.endTransaction();
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
