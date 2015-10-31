package com.lukekorth.android_500px;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.activeandroid.ActiveAndroid;
import com.fivehundredpx.api.auth.AccessToken;
import com.lukekorth.android_500px.helpers.Cache;
import com.lukekorth.android_500px.helpers.ConsumerApiKeyInterceptor;
import com.lukekorth.android_500px.helpers.ThreadBus;
import com.lukekorth.android_500px.helpers.UserAgentInterceptor;
import com.lukekorth.android_500px.interfaces.FiveHundredPxClient;
import com.lukekorth.android_500px.models.User;
import com.lukekorth.android_500px.models.UserUpdatedEvent;
import com.lukekorth.mailable_log.MailableLog;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

import retrofit.GsonConverterFactory;
import retrofit.Retrofit;
import se.akerfeldt.okhttp.signpost.OkHttpOAuthConsumer;
import se.akerfeldt.okhttp.signpost.SigningInterceptor;

public class WallpaperApplication extends com.activeandroid.app.Application  implements Thread.UncaughtExceptionHandler {

    private static final String VERSION = "version";

    private Thread.UncaughtExceptionHandler mDefaultExceptionHandler;

    private static FiveHundredPxClient sApiClient;
    private static FiveHundredPxClient sNonLoggedInApiClient;
    private static ThreadBus sBus;
    private static Picasso sPicasso;

    @Override
    public void onCreate() {
        super.onCreate();

        migrate();
        MailableLog.init(this, BuildConfig.DEBUG);
        mDefaultExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
        getBus().register(this);
    }

    private void migrate() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        int version = prefs.getInt(VERSION, 0);
        if (BuildConfig.VERSION_CODE > version) {
            String now = new Date().toString();
            if (version == 0) {
                ActiveAndroid.getDatabase().delete("Photos", null, null);
                editor.putString("install_date", now);
            } else if (version == 1) {
                ActiveAndroid.getDatabase().delete("Photos", null, null);
            }

            editor.putString("upgrade_date", now);
            editor.putInt(VERSION, BuildConfig.VERSION_CODE);
            editor.apply();

            MailableLog.clearLog(this);
        }
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        Logger logger = LoggerFactory.getLogger("Exception");

        logger.error("thread.toString(): " + thread.toString());
        logger.error("Exception: " + ex.toString());
        logger.error("Exception stacktrace:");
        for (StackTraceElement trace : ex.getStackTrace()) {
            logger.error(trace.toString());
        }

        logger.error("");

        logger.error("cause.toString(): " + ex.getCause().toString());
        logger.error("Cause: " + ex.getCause().toString());
        logger.error("Cause stacktrace:");
        for (StackTraceElement trace : ex.getCause().getStackTrace()) {
            logger.error(trace.toString());
        }

        mDefaultExceptionHandler.uncaughtException(thread, ex);
    }

    @Subscribe
    public void onUserUpdated(UserUpdatedEvent event) {
        sApiClient = null;
    }

    public static FiveHundredPxClient getApiClient() {
        if (sApiClient == null) {
            OkHttpClient client = new OkHttpClient();
            if (User.isUserLoggedIn()) {
                AccessToken accessToken = User.getLoggedInUserAccessToken();
                OkHttpOAuthConsumer consumer = new OkHttpOAuthConsumer(BuildConfig.CONSUMER_KEY,
                        BuildConfig.CONSUMER_SECRET);
                consumer.setTokenWithSecret(accessToken.getToken(), accessToken.getTokenSecret());
                client.interceptors().add(new SigningInterceptor(consumer));
            } else {
                client.interceptors().add(new ConsumerApiKeyInterceptor());
            }

            client.interceptors().add(new UserAgentInterceptor());

            sApiClient = new Retrofit.Builder()
                    .baseUrl("https://api.500px.com/v1/")
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(FiveHundredPxClient.class);
        }

        return sApiClient;
    }

    public static FiveHundredPxClient getNonLoggedInApiClient() {
        if (sNonLoggedInApiClient == null) {
            OkHttpClient client = new OkHttpClient();
            client.interceptors().add(new ConsumerApiKeyInterceptor());
            client.interceptors().add(new UserAgentInterceptor());

            sNonLoggedInApiClient = new Retrofit.Builder()
                    .baseUrl("https://api.500px.com/v1/")
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(FiveHundredPxClient.class);
        }

        return sNonLoggedInApiClient;
    }

    public static Bus getBus() {
        if (sBus == null) {
            sBus = new ThreadBus();
        }
        return sBus;
    }

    public static Picasso getPicasso(Context context) {
        if (sPicasso == null) {
            sPicasso = new Picasso.Builder(context.getApplicationContext())
                    .downloader(Cache.createCacheDownloader(context.getApplicationContext()))
                    .indicatorsEnabled(BuildConfig.DEBUG)
                    .build();
        }
        return sPicasso;
    }

}

