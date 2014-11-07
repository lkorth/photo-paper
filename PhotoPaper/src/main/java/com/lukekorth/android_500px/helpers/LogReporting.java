package com.lukekorth.android_500px.helpers;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.content.FileProvider;

import com.lukekorth.android_500px.BuildConfig;
import com.lukekorth.android_500px.R;
import com.lukekorth.android_500px.WallpaperApplication;

import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

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
            message.append("Wallpaper height: " + Utils.getWallpaperHeight(mContext));
            message.append("Wallpaper width: " + Utils.getWallpaperWidth(mContext));
            message.append("Screen height: " + Utils.getScreenHeight(mContext));
            message.append("Screen width: " + Utils.getScreenWidth(mContext));
            message.append("Supports parallax: " + Utils.supportsParallax(mContext));

			Map<String,?> keys = prefs.getAll();
			for(Map.Entry<String,?> entry : keys.entrySet()) {
                message.append(entry.getKey() + ": " + entry.getValue().toString() + "\n");
			}
			message.append("---------------------------");
            message.append("\n");
			message.append(getLog());

            File file = getFile();
			try {
				file.createNewFile();
				
				GZIPOutputStream gos = new GZIPOutputStream(new BufferedOutputStream(new PrintStream(file)));
				gos.write(message.toString().getBytes());
				gos.close();
			} catch (IOException e) {
                LoggerFactory.getLogger("LogBuilder").warn("IOException while building emailable log file. "
                        + e.getMessage());
            }

            buildEmailIntent(file);

            // Ensure we show the spinner and don't just flash the screen
            SystemClock.sleep(1000);

            return null;
		}

        private String getLog() {
            StringBuilder response = new StringBuilder();
            InputStream in = null;
            try {
                in = new FileInputStream(
                        ((WallpaperApplication) mContext.getApplicationContext()).getLogFilePath());
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                String line = reader.readLine();
                String currentTag;
                String lastTag = null;
                while(line != null) {
                    try {
                        currentTag = line.substring(line.indexOf("["), line.indexOf("]") + 1);
                        if (!currentTag.equals(lastTag)) {
                            lastTag = currentTag;
                            response.append("\n");
                        }
                    } catch (StringIndexOutOfBoundsException e) {
                    }

                    response.append(line + "\n");
                    line = reader.readLine();
                }

                return response.toString();
            } catch (FileNotFoundException e) {
                return "FileNotFoundException: " + e.toString();
            } catch (IOException e) {
                return "IOException: " + e.toString();
            } finally {
                if (in != null) {
                    try { in.close(); } catch (IOException e) {}
                }
            }
        }

        private File getFile() {
            File emailableLogsDir  = new File(mContext.getFilesDir(), "emailable_logs");
            emailableLogsDir.mkdir();
            return new File(emailableLogsDir, "photo-paper.log.gz");
        }

        private void buildEmailIntent(File file) {
            Uri fileUri = FileProvider.getUriForFile(mContext, "com.lukekorth.android_500px.fileprovider", file);

            mEmailIntent = new Intent(Intent.ACTION_SEND);
		    mEmailIntent.setType("text/plain");
            mEmailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{ "photo-paper@lukekorth.com" });
            mEmailIntent.putExtra(Intent.EXTRA_SUBJECT, "Photo Paper Debug Log");
		    mEmailIntent.putExtra(Intent.EXTRA_STREAM, fileUri);

            // grant permissions for all apps that can handle given intent
            List<ResolveInfo> infoList = mContext.getPackageManager()
                    .queryIntentActivities(mEmailIntent, PackageManager.MATCH_DEFAULT_ONLY);
            for (ResolveInfo resolveInfo : infoList) {
                mContext.grantUriPermission(resolveInfo.activityInfo.packageName, fileUri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
        }

        @Override
		protected void onPostExecute(Void args) {
			if(mLoading != null && mLoading.isShowing()) {
                mLoading.cancel();
            }
		    mContext.startActivity(Intent.createChooser(mEmailIntent, mContext.getString(R.string.send_email_via)));
		}
	}
}
