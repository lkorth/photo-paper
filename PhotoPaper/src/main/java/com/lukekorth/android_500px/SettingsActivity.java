package com.lukekorth.android_500px;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.text.TextUtils;
import android.text.format.DateUtils;

import com.lukekorth.android_500px.helpers.LogReporting;
import com.lukekorth.android_500px.helpers.Settings;
import com.lukekorth.android_500px.helpers.Utils;
import com.lukekorth.android_500px.models.Photos;
import com.lukekorth.android_500px.models.WallpaperChangedEvent;
import com.lukekorth.android_500px.services.ApiService;
import com.lukekorth.android_500px.services.WallpaperService;
import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.Set;

import fr.nicolaspomepuy.discreetapprate.AppRate;
import fr.nicolaspomepuy.discreetapprate.RetryPolicy;

public class SettingsActivity extends PreferenceActivity implements
        Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {

    private ListPreference mFeature;
    private MultiSelectListPreference mCategories;
    private ListPreference mInterval;
    private Preference mCurrentPhoto;
    private Preference mNextPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.layout.settings);

        mCurrentPhoto = findPreference("current_photo");
        mNextPhoto = findPreference("next_photo");
        mFeature = (ListPreference) findPreference("feature");
        mCategories = (MultiSelectListPreference) findPreference("categories");
        mInterval = (ListPreference) findPreference("update_interval");

        mNextPhoto.setOnPreferenceClickListener(this);
        mCurrentPhoto.setOnPreferenceClickListener(this);
        findPreference("contact").setOnPreferenceClickListener(this);

        mFeature.setOnPreferenceChangeListener(this);
        mCategories.setOnPreferenceChangeListener(this);
        mInterval.setOnPreferenceChangeListener(this);
        findPreference("enable").setOnPreferenceChangeListener(this);
        findPreference("use_only_wifi").setOnPreferenceChangeListener(this);
        findPreference("allow_nsfw").setOnPreferenceChangeListener(this);

        setFeatureSummary(mFeature.getValue());
        setCategoriesSummary(mCategories.getValues());
        setIntervalSummary(mInterval.getValue());
        findPreference("version").setSummary(BuildConfig.VERSION_NAME);

        if (Utils.supportsParallax(this)) {
            findPreference("use_parallax").setOnPreferenceChangeListener(this);
        } else {
            ((PreferenceCategory) findPreference("settings"))
                    .removePreference(findPreference("use_parallax"));
        }

        WallpaperApplication.getBus().register(this);
        runApiService();

        AppRate.with(this)
                .text(R.string.rate_app)
                .initialLaunchCount(3)
                .retryPolicy(RetryPolicy.EXPONENTIAL)
                .checkAndShow();
    }

    @Override
    protected void onResume() {
        super.onResume();
        onWallpaperChanged(null);

        if (Utils.shouldUpdateWallpaper(this)) {
            runWallpaperService();
        }
    }

    @Override
    protected void onDestroy() {
        WallpaperApplication.getBus().unregister(this);
        super.onDestroy();
    }

    @Subscribe
    public void onWallpaperChanged(WallpaperChangedEvent event) {
        Photos photo = Photos.getCurrentPhoto();
        if (photo != null) {
            CharSequence timeSet = DateUtils.getRelativeTimeSpanString(photo.seenAt, System.currentTimeMillis(), 0);
            mCurrentPhoto.setTitle(photo.name);
            mCurrentPhoto.setSummary("© " + photo.userName + " / 500px\nSet " + timeSet);
            WallpaperApplication.getPicasso(this)
                    .load(photo.imageUrl)
                    .error(android.R.drawable.stat_notify_error)
                    .into(mCurrentImageCallback);
        } else {
            mCurrentPhoto.setTitle(R.string.no_current_photo);
        }

        if (Settings.isEnabled(this)) {
            long nextPhotoTime = Settings.getLastUpdated(this) + (Settings.getUpdateInterval(this) * 1000);
            if (nextPhotoTime + 59000 < System.currentTimeMillis()) {
                mNextPhoto.setTitle(getString(R.string.next_photo) + " " + getString(R.string.next_unlock));
            } else {
                CharSequence nextTime = DateUtils.getRelativeTimeSpanString(nextPhotoTime, System.currentTimeMillis(), 0);
                mNextPhoto.setTitle(getString(R.string.next_photo) + " " + nextTime);
            }
            mNextPhoto.setSummary("Click to set it now");
        } else {
            mNextPhoto.setTitle(R.string.disabled);
            mNextPhoto.setSummary(R.string.enable_below);
        }
    }

    private Target mCurrentImageCallback = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            mCurrentPhoto.setIcon(new BitmapDrawable(bitmap));
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {
        }
    };

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference.getKey().equals(mCurrentPhoto.getKey())) {
            startActivity(new Intent(this, HistoryActivity.class));
            return true;
        } else if (preference.getKey().equals(mNextPhoto.getKey())) {
            mNextPhoto.setTitle(R.string.loading);
            mNextPhoto.setSummary("");
            runWallpaperService();
            return true;
        } else if (preference.getKey().equals("contact")) {
            new LogReporting(this).collectAndSendLogs();
            return true;
        }

        return false;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key = preference.getKey();
        if (key.equals(mFeature.getKey())) {
            setFeatureSummary((String) newValue);
        } else if (key.equals(mCategories.getKey())) {
            setCategoriesSummary((Set<String>) newValue);
        } else if (key.equals(mInterval.getKey())) {
            Settings.setUpdated(this);
            setIntervalSummary((String) newValue);
            onWallpaperChanged(null);
        } else if (key.equals("use_parallax")) {
            Settings.setDesiredWidth(this, 0);
        }

        runApiService();

        return true;
    }

    private void runApiService() {
        startService(new Intent(this, ApiService.class));
    }

    private void runWallpaperService() {
        startService(new Intent(this, WallpaperService.class));
    }

    private void setFeatureSummary(String index) {
        String summary = getSummary(R.array.feature_index, R.array.features, index);
        if (TextUtils.isEmpty(summary)) {
            summary = getString(R.string.popular);
        }
        mFeature.setSummary(summary);
    }

    private void setCategoriesSummary(Set<String> indexes) {
        if (indexes.size() == 0) {
            mCategories.setSummary(getString(R.string.landscapes));
        } else {
            String[] valueArray = getResources().getStringArray(R.array.categories);
            String summary = "";
            for (String index : indexes) {
                summary += valueArray[Integer.parseInt(index)] + ", ";
            }
            mCategories.setSummary(summary.substring(0, summary.length() - 2));
        }
    }

    private void setIntervalSummary(String index) {
        String summary = getSummary(R.array.interval_index, R.array.intervals, index);
        if (TextUtils.isEmpty(summary)) {
            summary = getString(R.string.one_hour);
        }
        mInterval.setSummary(summary);
    }

    private String getSummary(int indexArrayId, int valueArrayId, String index) {
        String[] indexArray = getResources().getStringArray(indexArrayId);
        String[] valueArray = getResources().getStringArray(valueArrayId);
        int i;
        for (i = 0; i < indexArray.length; i++) {
            if (indexArray[i].equals(index)) {
                return valueArray[i];
            }
        }
        return "";
    }

}
