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
import com.lukekorth.photo_paper.models.Photo;
import com.lukekorth.photo_paper.models.PhotoResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.lukekorth.photo_paper.helpers.Utils.dpToPx;

public class LikeButton extends Button implements View.OnClickListener {

    private Photo mPhoto;

    public LikeButton(Context context) {
        super(context);
    }

    public LikeButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LikeButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public LikeButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setPhoto(Photo photo) {
        mPhoto = photo;

        setText(Integer.toString(mPhoto.votes));
        setCompoundDrawablesWithIntrinsicBounds(R.drawable.button_action_like, 0, 0, 0);
        if (mPhoto.voted) {
            setBackgroundResource(R.drawable.button_action_bg_liked);
        } else {
            setBackgroundResource(R.drawable.button_action_bg);
        }

        setOnClickListener(this);
        setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View v) {
        if (mPhoto.voted) {
            setBackgroundResource(R.drawable.button_action_bg);
        } else {
            setBackgroundResource(R.drawable.button_action_bg_liked);
        }

        Callback<PhotoResponse> callback = new Callback<PhotoResponse>() {
            @Override
            public void onResponse(Call<PhotoResponse> call, Response<PhotoResponse> response) {
                if (response.isSuccess()) {
                    Photo photo = response.body().photo;
                    photo.voted = !mPhoto.voted;
                    setPhoto(response.body().photo);

                    FirebaseAnalytics.getInstance(getContext()).logEvent((photo.voted ? "liked" : "unliked"), null);
                } else {
                    onFailure(null, null);
                }
            }

            @Override
            public void onFailure(Call<PhotoResponse> call, Throwable t) {
                if (mPhoto.voted) {
                    setBackgroundResource(R.drawable.button_action_bg_liked);
                } else {
                    setBackgroundResource(R.drawable.button_action_bg);
                }
            }
        };

        if (mPhoto.voted) {
            WallpaperApplication.getApiClient().unlike(mPhoto.id).enqueue(callback);
        } else {
            WallpaperApplication.getApiClient().like(mPhoto.id).enqueue(callback);
        };
    }

    @Override
    public void setBackgroundResource(int resid) {
        super.setBackgroundResource(resid);

        setPadding(dpToPx(getContext(), 2), dpToPx(getContext(), 4), dpToPx(getContext(), 8),
                dpToPx(getContext(), 4));
    }
}
