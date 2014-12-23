package com.lukekorth.android_500px;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.lukekorth.android_500px.models.Photos;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.JELLY_BEAN;

public class PhotoFragment extends Fragment {

    public static final String PHOTO_ID_KEY = "com.lukekorth.android_500px.PhotoFragment.PHOTO_ID_KEY";

    private Photos mPhoto;
    private boolean mPhotoSet;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPhoto = Photos.getPhoto(getArguments().getInt(PHOTO_ID_KEY));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.full_screen_photo, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();

        mPhotoSet = false;

        final ViewTreeObserver viewTreeObserver = getView().getViewTreeObserver();
        if (viewTreeObserver.isAlive()) {
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (SDK_INT >= JELLY_BEAN) {
                        viewTreeObserver.removeOnGlobalLayoutListener(this);
                    }

                    if (!mPhotoSet) {
                        ImageViewTouch photoView = (ImageViewTouch) getView().findViewById(R.id.photo);
                        WallpaperApplication.getPicasso(getActivity())
                                .load(mPhoto.imageUrl)
                                .resize(photoView.getWidth(), photoView.getHeight())
                                .centerInside()
                                .into(photoView);
                        mPhotoSet = true;
                    }
                }
            });
        }
    }

}
