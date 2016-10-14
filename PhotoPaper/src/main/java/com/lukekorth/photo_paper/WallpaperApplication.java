package com.lukekorth.photo_paper;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.GsonBuilder;
import com.jakewharton.picasso.OkHttp3Downloader;
import com.lukekorth.fivehundredpx.AccessToken;
import com.lukekorth.mailable_log.MailableLog;
import com.lukekorth.photo_paper.api.FiveHundredPxClient;
import com.lukekorth.photo_paper.helpers.ConsumerApiKeyInterceptor;
import com.lukekorth.photo_paper.helpers.ThreadBus;
import com.lukekorth.photo_paper.helpers.UserAgentInterceptor;
import com.lukekorth.photo_paper.models.User;
import com.lukekorth.photo_paper.models.UserUpdatedEvent;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import se.akerfeldt.okhttp.signpost.OkHttpOAuthConsumer;
import se.akerfeldt.okhttp.signpost.SigningInterceptor;

public class WallpaperApplication extends Application implements Thread.UncaughtExceptionHandler {

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
        DebugUtils.setup(this);

        Realm.init(this);
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder().build();
        Realm.setDefaultConfiguration(realmConfiguration);

        getBus().register(this);
    }

    private void migrate() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        int version = prefs.getInt(VERSION, 0);
        if (BuildConfig.VERSION_CODE > version) {
            String now = new Date().toString();
            if (version == 0) {
                editor.putString("install_date", now);
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
            Realm realm = Realm.getDefaultInstance();
            OkHttpClient.Builder okHttpBuilder = new OkHttpClient.Builder();
            if (User.isUserLoggedIn(realm)) {
                AccessToken accessToken = User.getLoggedInUserAccessToken(realm);
                OkHttpOAuthConsumer consumer = new OkHttpOAuthConsumer(BuildConfig.CONSUMER_KEY,
                        BuildConfig.CONSUMER_SECRET);
                consumer.setTokenWithSecret(accessToken.getToken(), accessToken.getTokenSecret());
                okHttpBuilder.addInterceptor(new SigningInterceptor(consumer));
            } else {
                okHttpBuilder.addInterceptor(new ConsumerApiKeyInterceptor());
            }

            okHttpBuilder.addInterceptor(new UserAgentInterceptor());

            if (BuildConfig.DEBUG) {
                okHttpBuilder.addNetworkInterceptor(DebugUtils.getDebugNetworkInterceptor());
            }

            sApiClient = new Retrofit.Builder()
                    .baseUrl("https://api.500px.com/v1/")
                    .addConverterFactory(GsonConverterFactory.create(new GsonBuilder()
                            .excludeFieldsWithoutExposeAnnotation()
                            .create()))
                    .client(okHttpBuilder.build())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(FiveHundredPxClient.class);

            realm.close();
        }

        return sApiClient;
    }

    public static FiveHundredPxClient getNonLoggedInApiClient() {
        if (sNonLoggedInApiClient == null) {
            OkHttpClient.Builder okHttpBuilder = new OkHttpClient.Builder()
                    .addInterceptor(new ConsumerApiKeyInterceptor())
                    .addInterceptor(new UserAgentInterceptor());

            if (BuildConfig.DEBUG) {
                okHttpBuilder.addNetworkInterceptor(DebugUtils.getDebugNetworkInterceptor());
            }

            sNonLoggedInApiClient = new Retrofit.Builder()
                    .baseUrl("https://api.500px.com/v1/")
                    .addConverterFactory(GsonConverterFactory.create(new GsonBuilder()
                            .excludeFieldsWithoutExposeAnnotation()
                            .create()))
                    .client(okHttpBuilder.build())
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
                    .downloader(new OkHttp3Downloader(context.getApplicationContext(), 512000000)) // 512mb
                    .indicatorsEnabled(BuildConfig.DEBUG)
                    .build();
        }
        return sPicasso;
    }
}

