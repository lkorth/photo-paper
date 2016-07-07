package com.lukekorth.photo_paper;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.text.format.DateUtils;

import com.lukekorth.fivehundredpx.AccessToken;
import com.lukekorth.fivehundredpx.FiveHundredPxOAuthActivity;
import com.lukekorth.photo_paper.helpers.LogReporting;
import com.lukekorth.photo_paper.helpers.Settings;
import com.lukekorth.photo_paper.helpers.Utils;
import com.lukekorth.photo_paper.models.ActivityResumedEvent;
import com.lukekorth.photo_paper.models.Photos;
import com.lukekorth.photo_paper.models.RemainingPhotosChangedEvent;
import com.lukekorth.photo_paper.models.User;
import com.lukekorth.photo_paper.models.UserUpdatedEvent;
import com.lukekorth.photo_paper.models.WallpaperChangedEvent;
import com.lukekorth.photo_paper.services.ClearCacheIntentService;
import com.lukekorth.photo_paper.services.PhotoDownloadIntentService;
import com.lukekorth.photo_paper.services.WallpaperService;
import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.Set;

import fr.nicolaspomepuy.discreetapprate.AppRate;
import fr.nicolaspomepuy.discreetapprate.RetryPolicy;
import io.realm.Realm;

public class SettingsFragment extends PreferenceFragment
        implements Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {

    private static final int FIVE_HUNDRED_PX_LOGIN = 20;

    private Realm mRealm;
    private Preference mLogin;
    private MultiSelectListPreference mCategories;
    private ListPreference mInterval;
    private Preference mCurrentPhoto;
    private Preference mNextPhoto;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);

        mRealm = Realm.getDefaultInstance();

        mCurrentPhoto = findPreference("current_photo");
        mNextPhoto = findPreference("next_photo");
        mLogin = findPreference("login");
        mCategories = (MultiSelectListPreference) findPreference("categories");
        mInterval = (ListPreference) findPreference("update_interval");

        mNextPhoto.setOnPreferenceClickListener(this);
        mLogin.setOnPreferenceClickListener(this);
        findPreference("clear_cache").setOnPreferenceClickListener(this);
        findPreference("contact").setOnPreferenceClickListener(this);

        mCategories.setOnPreferenceChangeListener(this);
        mInterval.setOnPreferenceChangeListener(this);
        findPreference("enable").setOnPreferenceChangeListener(this);
        findPreference("use_only_wifi").setOnPreferenceChangeListener(this);

        setCategoriesSummary(mCategories.getValues());
        mInterval.setSummary(mInterval.getEntry());
        findPreference("version").setSummary(BuildConfig.VERSION_NAME);

        if (!Utils.supportsParallax(getActivity())) {
            ((PreferenceCategory) findPreference("settings"))
                    .removePreference(findPreference("use_parallax"));
        }

        WallpaperApplication.getBus().register(this);
        onUserUpdated(new UserUpdatedEvent());
    }

    @Override
    public void onStart() {
        super.onStart();

        AppRate.with(getActivity())
                .text(R.string.rate_app)
                .initialLaunchCount(3)
                .retryPolicy(RetryPolicy.EXPONENTIAL)
                .checkAndShow();
    }

    @Override
    public void onResume() {
        super.onResume();
        onWallpaperChanged(null);
        WallpaperApplication.getBus().post(new ActivityResumedEvent());

        if (Utils.shouldUpdateWallpaper(getActivity())) {
            runWallpaperService(false);
        }
    }

    @Override
    public void onDestroy() {
        WallpaperApplication.getBus().unregister(this);
        mRealm.close();
        super.onDestroy();
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

    private Target mUserImageCallback = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            mLogin.setIcon(new BitmapDrawable(bitmap));
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {
        }
    };

    @Subscribe
    public void onUserUpdated(UserUpdatedEvent event) {
        User user = User.getUser(mRealm);
        if (user != null) {
            mLogin.setTitle(user.getUserName());
            mLogin.setSummary(getString(R.string.click_to_logout));

            WallpaperApplication.getPicasso(getActivity())
                    .load(user.getPhoto())
                    .error(android.R.drawable.stat_notify_error)
                    .into(mUserImageCallback);
        } else {
            mLogin.setTitle(R.string.login_with_500px);
            mLogin.setSummary(R.string.login_with_500px_summary);
            mLogin.setIcon(null);
        }
    }

    @Subscribe
    public void onWallpaperChanged(WallpaperChangedEvent event) {
        Photos photo = Photos.getCurrentPhoto(mRealm);
        if (photo != null) {
            CharSequence timeSet = DateUtils.getRelativeTimeSpanString(photo.getSeenAt(), System.currentTimeMillis(), 0);
            mCurrentPhoto.setTitle(photo.getName());
            mCurrentPhoto.setSummary("© " + photo.getUserName() + " / 500px\n" + getString(R.string.set) +
                    " " + timeSet);
            WallpaperApplication.getPicasso(getActivity())
                    .load(photo.imageUrl)
                    .placeholder(new ColorDrawable(photo.getPalette()))
                    .error(android.R.drawable.stat_notify_error)
                    .into(mCurrentImageCallback);
        } else {
            mCurrentPhoto.setTitle(R.string.no_current_photo);
            mCurrentPhoto.setSummary("");
            mCurrentPhoto.setIcon(null);
        }

        onRemainingPhotosChanged(null);
    }

    @Subscribe
    public void onRemainingPhotosChanged(RemainingPhotosChangedEvent event) {
        if (Settings.isEnabled(getActivity())) {
            int photosRemaining = Photos.unseenPhotoCount(getActivity(), mRealm);
            if (photosRemaining > 0) {
                mNextPhoto.setEnabled(true);

                long nextPhotoTime = Settings.getLastUpdated(getActivity()) + (Settings.getUpdateInterval(getActivity()) * 1000);
                if (nextPhotoTime + 59000 < System.currentTimeMillis()) {
                    mNextPhoto.setTitle(getString(R.string.next_photo) + " " + getString(R.string.next_unlock));
                } else {
                    CharSequence nextTime = DateUtils.getRelativeTimeSpanString(nextPhotoTime, System.currentTimeMillis(), 0);
                    mNextPhoto.setTitle(getString(R.string.next_photo) + " " + nextTime);
                }
                mNextPhoto.setSummary(photosRemaining + " " + getString(R.string.remaining_photos));
            } else {
                mNextPhoto.setEnabled(false);
                mNextPhoto.setTitle(R.string.no_photos_remaining);
                mNextPhoto.setSummary(R.string.select_different_feature_or_categories);
            }
        } else {
            mNextPhoto.setTitle(R.string.disabled);
            mNextPhoto.setSummary(R.string.enable_below);
        }
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference.getKey().equals("clear_cache")) {
            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.are_you_sure)
                    .setMessage(R.string.are_you_sure_message)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            getActivity().startService(new Intent(getActivity(), ClearCacheIntentService.class));
                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .show();
        } else if (preference.getKey().equals(mNextPhoto.getKey())) {
            mNextPhoto.setTitle(R.string.loading);
            mNextPhoto.setSummary("");
            runWallpaperService(true);
            return true;
        } else if (preference.getKey().equals("login")) {
            if (User.isUserLoggedIn(mRealm)) {
                User.logout(mRealm);
                WallpaperApplication.getBus().post(new UserUpdatedEvent());
            } else {
                startActivityForResult(new Intent(getActivity(), FiveHundredPxOAuthActivity.class),
                        FIVE_HUNDRED_PX_LOGIN);
            }
        } else if (preference.getKey().equals("contact")) {
            new LogReporting(getActivity()).collectAndSendLogs();
            return true;
        }

        return false;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key = preference.getKey();
        if (key.equals(mCategories.getKey())) {
            Settings.setCategories(getActivity(), (Set<String>) newValue);
            setCategoriesSummary((Set<String>) newValue);
        } else if (key.equals(mInterval.getKey())) {
            Settings.setUpdateInterval(getActivity(), newValue.toString());
            mInterval.setSummary(Utils.getListSummary(getActivity(), R.array.interval_index,
                    R.array.intervals, newValue.toString(), getString(R.string.one_hour)));
            onWallpaperChanged(null);
        }

        PhotoDownloadIntentService.downloadPhotos(getActivity());

        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FIVE_HUNDRED_PX_LOGIN) {
            if (resultCode == Activity.RESULT_OK) {
                User.newUser(getActivity(), mRealm,
                        (AccessToken) data.getParcelableExtra(FiveHundredPxOAuthActivity.ACCESS_TOKEN));
            } else if (resultCode != Activity.RESULT_CANCELED) {
                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.login_error)
                        .setMessage(R.string.login_error_message)
                        .setPositiveButton(android.R.string.ok, null)
                        .show();
            }
        }
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

    private void runWallpaperService(boolean skip) {
        Intent intent = new Intent(getActivity(), WallpaperService.class)
                .putExtra(WallpaperService.SKIP_WALLPAPER_KEY, skip);
        getActivity().startService(intent);
    }
}
