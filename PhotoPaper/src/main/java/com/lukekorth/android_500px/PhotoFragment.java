package com.lukekorth.android_500px;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.lukekorth.android_500px.models.Photos;

public class PhotoFragment extends Fragment {

    public static final String PHOTO_ID_KEY = "com.lukekorth.android_500px.PhotoFragment.PHOTO_ID_KEY";

    private Photos mPhoto;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.full_screen_photo, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mPhoto = Photos.getPhoto(getArguments().getInt(PHOTO_ID_KEY));
        WallpaperApplication.getPicasso(getActivity())
                .load(mPhoto.imageUrl)
                .into((ImageView) getView().findViewById(R.id.photo));
    }
}
