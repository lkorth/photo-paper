package com.lukekorth.photo_paper.views;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.preference.ListPreference;
import android.preference.Preference;
import android.util.AttributeSet;

import com.lukekorth.photo_paper.R;
import com.lukekorth.photo_paper.SearchActivity;
import com.lukekorth.photo_paper.WallpaperApplication;
import com.lukekorth.photo_paper.helpers.Settings;
import com.lukekorth.photo_paper.helpers.Utils;
import com.lukekorth.photo_paper.models.ActivityResumedEvent;
import com.lukekorth.photo_paper.models.User;
import com.lukekorth.photo_paper.models.UserUpdatedEvent;
import com.lukekorth.photo_paper.sync.SyncAdapter;
import com.squareup.otto.Subscribe;

import io.realm.Realm;

public class FeatureListPreference extends ListPreference implements Preference.OnPreferenceChangeListener {

    private Realm mRealm;

    public FeatureListPreference(Context context) {
        super(context);
        init();
    }

    public FeatureListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public FeatureListPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public FeatureListPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        mRealm = Realm.getDefaultInstance();
        WallpaperApplication.getBus().register(this);
        setOnPreferenceChangeListener(this);
        initEntries();
    }

    @Override
    protected void onPrepareForRemoval() {
        super.onPrepareForRemoval();
        WallpaperApplication.getBus().unregister(this);
        mRealm.close();
    }

    @Subscribe
    public void onActivityResumed(ActivityResumedEvent event) {
        setFeatureSummary(Settings.getFeature(getContext()));
    }

    @Subscribe
    public void onUserUpdated(UserUpdatedEvent event) {
        if (User.isUserLoggedIn(mRealm) && getValue().equals("your_favorites")) {
            setValue("your_favorites");
            setFeatureSummary(getValue());
        }
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        initEntries();
        super.onPrepareDialogBuilder(builder);
    }

    private void initEntries() {
        if (User.isUserLoggedIn(mRealm)) {
            setEntries(R.array.logged_in_features);
            setEntryValues(R.array.logged_in_feature_index);
        } else {
            setEntries(R.array.features);
            setEntryValues(R.array.feature_index);
        }
    }

    private void setFeatureSummary(String index) {
        if (Settings.getFeature(getContext()).equals("search")) {
            setSummary(getContext().getString(R.string.search) + ": " +
                    Settings.getSearchQuery(getContext()));
        } else if (User.isUserLoggedIn(mRealm)) {
            setSummary(Utils.getListSummary(getContext(), R.array.logged_in_feature_index,
                    R.array.logged_in_features, index, getContext().getString(R.string.popular)));
        } else {
            setSummary(Utils.getListSummary(getContext(), R.array.feature_index, R.array.features,
                    index, getContext().getString(R.string.popular)));
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (!newValue.toString().equals("search")) {
            Settings.setFeature(getContext(), newValue.toString());
            setFeatureSummary(newValue.toString());
            SyncAdapter.requestSync();
            return true;
        } else {
            getContext().startActivity(new Intent(getContext(), SearchActivity.class));
            return false;
        }
    }
}
