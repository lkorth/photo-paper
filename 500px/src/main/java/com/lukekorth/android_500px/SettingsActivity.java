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
import android.text.TextUtils;
import android.text.format.DateUtils;

import com.lukekorth.android_500px.helpers.Utils;
import com.lukekorth.android_500px.models.Photos;
import com.lukekorth.android_500px.models.WallpaperChangedEvent;
import com.lukekorth.android_500px.services.ApiService;
import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.Set;

import fr.nicolaspomepuy.discreetapprate.AppRate;
import fr.nicolaspomepuy.discreetapprate.RetryPolicy;

public class SettingsActivity extends PreferenceActivity implements Preference.OnPreferenceChangeListener {

    private ListPreference mFeature;
    private MultiSelectListPreference mCategories;
    private ListPreference mInterval;
    private Preference mCurrentPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.layout.settings);

        mFeature = (ListPreference) findPreference("feature");
        mFeature.setOnPreferenceChangeListener(this);
        setFeatureSummary(mFeature.getValue());

        mCategories = (MultiSelectListPreference) findPreference("categories");
        mCategories.setOnPreferenceChangeListener(this);
        setCategoriesSummary(mCategories.getValues());

        mInterval = (ListPreference) findPreference("update_interval");
        mInterval.setOnPreferenceChangeListener(this);
        setIntervalSummary(mInterval.getValue());

        findPreference("use_only_wifi").setOnPreferenceChangeListener(this);
        findPreference("allow_nsfw").setOnPreferenceChangeListener(this);

        mCurrentPhoto = findPreference("current_photo");

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
        onWallpaperChanged(new WallpaperChangedEvent());
    }

    @Override
    protected void onDestroy() {
        WallpaperApplication.getBus().unregister(this);
        super.onDestroy();
    }

    @Subscribe
    public void onWallpaperChanged(WallpaperChangedEvent event) {
        Photos photo = Photos.getCurrentPhoto(this);
        if (photo != null) {
            CharSequence timeSet = DateUtils.getRelativeTimeSpanString(photo.seenAt, System.currentTimeMillis(), 0);
            mCurrentPhoto.setTitle(photo.name);
            mCurrentPhoto.setSummary("© " + photo.userName + " / 500px\nSet " + timeSet);
            Picasso.with(this)
                    .load(photo.imageUrl)
                    .error(android.R.drawable.stat_notify_error)
                    .into(new Target() {
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
                    });
        } else {
            mCurrentPhoto.setTitle(R.string.no_current_photo);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key = preference.getKey();
        if (key.equals(mFeature.getKey())) {
            setFeatureSummary((String) newValue);
        } else if (key.equals(mCategories.getKey())) {
            setCategoriesSummary((Set<String>) newValue);
        } else if (key.equals(mInterval.getKey())) {
            setIntervalSummary((String) newValue);
            Utils.setAlarm(this);
        }

        runApiService();

        return true;
    }

    private void runApiService() {
        startService(new Intent(this, ApiService.class));
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
