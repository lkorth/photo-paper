package com.lukekorth.photo_paper.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.lukekorth.photo_paper.R;
import com.lukekorth.photo_paper.WallpaperApplication;
import com.lukekorth.photo_paper.helpers.Settings;
import com.lukekorth.photo_paper.models.ApiResponse;
import com.lukekorth.photo_paper.models.PhotoGalleryRequest;
import com.lukekorth.photo_paper.models.User;

import io.realm.Realm;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.lukekorth.photo_paper.helpers.Utils.dpToPx;

public class FavoriteButton extends Button implements View.OnClickListener {

    private int mUserId;
    private String mPhotoId;
    private boolean mFavorited;

    public FavoriteButton(Context context) {
        super(context);
    }

    public FavoriteButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FavoriteButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public FavoriteButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setup(String photoId, boolean favorited) {
        Realm realm = Realm.getDefaultInstance();

        if (User.isUserLoggedIn(realm)) {
            mUserId = User.getUser(realm).getId();
            mPhotoId = photoId;
            mFavorited = favorited;

            setCompoundDrawablesWithIntrinsicBounds(R.drawable.button_action_favorite, 0, 0, 0);
            if (mFavorited) {
                setBackgroundResource(R.drawable.button_action_bg_favorited);
            } else {
                setBackgroundResource(R.drawable.button_action_bg);
            }

            setOnClickListener(this);
            setVisibility(View.VISIBLE);
        }

        realm.close();
    }

    @Override
    public void onClick(View v) {
        if (mFavorited) {
            setBackgroundResource(R.drawable.button_action_bg);

            WallpaperApplication.getApiClient()
                    .unfavorite(mUserId, Settings.getFavoriteGalleryId(getContext()), PhotoGalleryRequest.remove(mPhotoId))
                    .enqueue(new Callback<ApiResponse>() {
                        @Override
                        public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                            if (response.isSuccess()) {
                                setup(mPhotoId, false);
                                FirebaseAnalytics.getInstance(getContext()).logEvent("unfavorited", null);
                            } else {
                                onFailure(null, null);
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse> call, Throwable t) {
                            setBackgroundResource(R.drawable.button_action_bg_favorited);
                        }
                    });
        } else {
            setBackgroundResource(R.drawable.button_action_bg_favorited);

            WallpaperApplication.getApiClient()
                    .favorite(mUserId, Settings.getFavoriteGalleryId(getContext()), PhotoGalleryRequest.add(mPhotoId))
                    .enqueue(new Callback<ApiResponse>() {
                        @Override
                        public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                            if (response.isSuccess()) {
                                setup(mPhotoId, true);
                                FirebaseAnalytics.getInstance(getContext()).logEvent("favorited", null);
                            } else {
                                onFailure(null, null);
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse> call, Throwable t) {
                            setBackgroundResource(R.drawable.button_action_bg);
                        }
                    });
        }
    }

    @Override
    public void setBackgroundResource(int resid) {
        super.setBackgroundResource(resid);
        setPadding(dpToPx(getContext(), 4), dpToPx(getContext(), 4), dpToPx(getContext(), 4), dpToPx(getContext(), 4));
    }
}
