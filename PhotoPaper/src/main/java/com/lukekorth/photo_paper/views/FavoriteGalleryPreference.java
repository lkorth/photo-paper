package com.lukekorth.photo_paper.views;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.preference.Preference;
import android.util.AttributeSet;

import com.lukekorth.fivehundredpx.FiveHundredException;
import com.lukekorth.photo_paper.R;
import com.lukekorth.photo_paper.WallpaperApplication;
import com.lukekorth.photo_paper.helpers.Settings;
import com.lukekorth.photo_paper.models.GalleryResponse;
import com.lukekorth.photo_paper.models.User;
import com.lukekorth.photo_paper.models.UserUpdatedEvent;
import com.squareup.otto.Subscribe;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import io.realm.Realm;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FavoriteGalleryPreference extends Preference implements Preference.OnPreferenceClickListener {

    private Realm mRealm;

    public FavoriteGalleryPreference(Context context) {
        super(context);
        init();
    }

    public FavoriteGalleryPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FavoriteGalleryPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public FavoriteGalleryPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        mRealm = Realm.getDefaultInstance();
        WallpaperApplication.getBus().register(this);
        setOnPreferenceClickListener(this);
        onUserUpdated(null);
    }

    @Override
    protected void onPrepareForRemoval() {
        super.onPrepareForRemoval();
        WallpaperApplication.getBus().unregister(this);
        mRealm.close();
    }

    @Subscribe
    public void onUserUpdated(UserUpdatedEvent event) {
        updateSummary();

        if (User.isUserLoggedIn(mRealm)) {
            setEnabled(true);
        } else {
            setEnabled(false);
        }
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        final ProgressDialog dialog = ProgressDialog.show(getContext(), "", getContext().getString(R.string.loading), true);
        dialog.show();

        WallpaperApplication.getApiClient().galleries(User.getUser(mRealm).getId()).enqueue(new Callback<GalleryResponse>() {
            @Override
            public void onResponse(Call<GalleryResponse> call, Response<GalleryResponse> response) {
                if (!response.isSuccess()) {
                    onFailure(call, new FiveHundredException(response.code()));
                    return;
                }

                dialog.dismiss();
                showSelectionDialog(response.body().galleries);
            }

            @Override
            public void onFailure(Call<GalleryResponse> call, Throwable t) {
                dialog.dismiss();

                new AlertDialog.Builder(getContext())
                        .setTitle(R.string.gallery_fetch_error)
                        .setMessage(R.string.gallery_fetch_error_message)
                        .setPositiveButton(android.R.string.ok, null)
                        .show();
            }
        });

        return true;
    }

    private void showSelectionDialog(final List<GalleryResponse.Gallery> galleries) {
        CharSequence items[] = new CharSequence[galleries.size()];
        int previouslySelected = -1;
        String selectedGallery = Settings.getFavoriteGallery(getContext());
        for (int i = 0; i < galleries.size(); i++) {
            if (galleries.get(i).name.equals(selectedGallery)) {
                previouslySelected = i;
            }

            items[i] = galleries.get(i).name;
        }

        final AtomicInteger selected = new AtomicInteger(-1);
        new AlertDialog.Builder(getContext())
                .setTitle(R.string.favorite_gallery_summary)
                .setSingleChoiceItems(items, previouslySelected, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        selected.set(i);
                    }
                })
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Settings.setFavoriteGallery(FavoriteGalleryPreference.this.getContext(),
                                galleries.get(selected.get()).name);
                        Settings.setFavoriteGalleryId(FavoriteGalleryPreference.this.getContext(),
                                galleries.get(selected.get()).id);

                        updateSummary();
                        dialogInterface.dismiss();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .show();
    }

    private void updateSummary() {
        String gallery = Settings.getFavoriteGallery(getContext());
        if (gallery == null || !User.isUserLoggedIn(mRealm)) {
            setSummary(R.string.favorite_gallery_summary);
        } else {
            setSummary(gallery);
        }
    }
}
