package com.lukekorth.android_500px.views;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.preference.ListPreference;
import android.preference.Preference;
import android.util.AttributeSet;

import com.lukekorth.android_500px.R;
import com.lukekorth.android_500px.WallpaperApplication;
import com.lukekorth.android_500px.helpers.Settings;
import com.lukekorth.android_500px.helpers.Utils;
import com.lukekorth.android_500px.models.User;
import com.lukekorth.android_500px.models.UserUpdatedEvent;
import com.lukekorth.android_500px.services.ApiService;
import com.squareup.otto.Subscribe;

public class FeatureListPreference extends ListPreference implements Preference.OnPreferenceChangeListener {

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
        WallpaperApplication.getBus().register(this);
        setOnPreferenceChangeListener(this);
        initEntries();
        setFeatureSummary(Settings.getFeature(getContext()));
    }

    @Override
    protected void onPrepareForRemoval() {
        super.onPrepareForRemoval();
        WallpaperApplication.getBus().unregister(this);
    }

    @Subscribe
    public void onUserUpdated(UserUpdatedEvent event) {
        if (event.getUser() == null &&
                getValue().equals(getContext().getResources().getStringArray(R.array.logged_in_feature_index)[0])) {
            setValue(getContext().getResources().getStringArray(R.array.feature_index)[0]);
            setFeatureSummary(getValue());
        }
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        initEntries();
        super.onPrepareDialogBuilder(builder);
    }

    private void initEntries() {
        if (User.isUserLoggedIn()) {
            setEntries(R.array.logged_in_features);
            setEntryValues(R.array.logged_in_feature_index);
        } else {
            setEntries(R.array.features);
            setEntryValues(R.array.feature_index);
        }
    }

    private void setFeatureSummary(String index) {
        if (User.isUserLoggedIn()) {
            setSummary(Utils.getListSummary(getContext(), R.array.logged_in_feature_index,
                    R.array.logged_in_features, index, getContext().getString(R.string.popular)));
        } else {
            setSummary(Utils.getListSummary(getContext(), R.array.feature_index, R.array.features,
                    index, getContext().getString(R.string.popular)));
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        Settings.setFeature(getContext(), newValue.toString());
        setFeatureSummary(newValue.toString());
        getContext().startService(new Intent(getContext(), ApiService.class));
        return true;
    }

}
