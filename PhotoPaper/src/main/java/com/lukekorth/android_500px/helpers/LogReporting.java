package com.lukekorth.android_500px.helpers;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.SystemClock;
import android.preference.PreferenceManager;

import com.lukekorth.android_500px.BuildConfig;
import com.lukekorth.android_500px.R;
import com.lukekorth.mailable_log.MailableLog;

import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

public class LogReporting {

	private Context mContext;
	private ProgressDialog mLoading;
    private Intent mEmailIntent;
	
	public LogReporting(Context context) {
		mContext = context;
	}
	
	public void collectAndSendLogs() {
		mLoading = ProgressDialog.show(mContext, "", mContext.getString(R.string.loading), true);
		new GenerateLogFile().execute();
	}

	private class GenerateLogFile extends AsyncTask<Void, Void, Void> {

		@SuppressLint("NewApi")
		@Override
		protected Void doInBackground(Void... args) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
			StringBuilder message = new StringBuilder();
			
			message.append("Android version: " + Build.VERSION.SDK_INT + "\n");
            message.append("Device manufacturer: " + Build.MANUFACTURER + "\n");
            message.append("Device model: " + Build.MODEL + "\n");
            message.append("Device product: " + Build.PRODUCT + "\n");
			message.append("App version: " + BuildConfig.VERSION_NAME + "\n");
            message.append("Debug: " + BuildConfig.DEBUG + "\n");
            message.append("Wallpaper height: " + Utils.getWallpaperHeight(mContext) + "\n");
            message.append("Wallpaper width: " + Utils.getWallpaperWidth(mContext) + "\n");
            message.append("Screen height: " + Utils.getScreenHeight(mContext) + "\n");
            message.append("Screen width: " + Utils.getScreenWidth(mContext) + "\n");
            message.append("Supports parallax: " + Utils.supportsParallax(mContext) + "\n");

			Map<String,?> keys = prefs.getAll();
			for(Map.Entry<String,?> entry : keys.entrySet()) {
                message.append(entry.getKey() + ": " + entry.getValue().toString() + "\n");
			}
			message.append("---------------------------");
            message.append("\n");

            try {
                mEmailIntent = MailableLog.buildEmailIntent(mContext, "photo-paper@lukekorth.com",
                        "Photo Paper Debug Log", "photo-paper.log", message.toString());
            } catch (IOException e) {
                LoggerFactory.getLogger("LogReporting").error(e.getMessage());
            }

            // Ensure we show the spinner and don't just flash the screen
            SystemClock.sleep(1000);

            return null;
		}

        @Override
		protected void onPostExecute(Void args) {
			if(mLoading != null && mLoading.isShowing()) {
                mLoading.cancel();
            }
		    mContext.startActivity(mEmailIntent);
		}
	}
}
