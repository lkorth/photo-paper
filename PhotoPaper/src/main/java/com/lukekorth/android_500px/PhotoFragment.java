package com.lukekorth.android_500px;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lukekorth.android_500px.helpers.Utils;
import com.lukekorth.android_500px.models.Photos;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;

public class PhotoFragment extends Fragment {

    public static final String PHOTO_ID_KEY = "com.lukekorth.android_500px.PhotoFragment.PHOTO_ID_KEY";

    private Photos mPhoto;
    private ImageViewTouch mImageView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPhoto = Photos.getPhoto(getArguments().getInt(PHOTO_ID_KEY));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.full_screen_photo, container, false);
        mImageView = (ImageViewTouch) view.findViewById(R.id.photo);

        return view;
   }

    @Override
    public void onResume() {
        super.onResume();

        WallpaperApplication.getPicasso(getActivity())
                .load(mPhoto.imageUrl)
                .resize(Utils.getScreenWidth(getActivity()), Utils.getScreenHeight(getActivity()))
                .centerInside()
                .into(mImageView);
    }

}
