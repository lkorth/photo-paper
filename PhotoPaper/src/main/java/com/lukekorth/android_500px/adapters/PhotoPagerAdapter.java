package com.lukekorth.android_500px.adapters;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lukekorth.android_500px.R;
import com.lukekorth.android_500px.WallpaperApplication;
import com.lukekorth.android_500px.helpers.Utils;
import com.lukekorth.android_500px.models.Photos;

import java.util.ArrayList;
import java.util.List;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;

public class PhotoPagerAdapter extends PagerAdapter {

    private Context mContext;
    private LayoutInflater mInflater;
    private List<Photos> mPhotos;

    public PhotoPagerAdapter(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mPhotos = new ArrayList<>();
    }

    public void setPhotos(List<Photos> photos) {
        mPhotos = photos;
        notifyDataSetChanged();
    }

    @Override
    public Object instantiateItem(ViewGroup collection, int position) {
        Photos photo = mPhotos.get(position);
        View view = mInflater.inflate(R.layout.full_screen_photo, collection, false);
        collection.addView(view);

        WallpaperApplication.getPicasso(mContext)
                .load(photo.imageUrl)
                .resize(Utils.getScreenWidth(mContext), Utils.getScreenHeight(mContext))
                .centerInside()
                .into(((ImageViewTouch) view.findViewById(R.id.photo)));

        return view;
    }

    @Override
    public void destroyItem(ViewGroup collection, int position, Object view) {
        collection.removeView((View) view);
    }

    @Override
    public int getCount() {
        return mPhotos.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mPhotos.get(position).name;
    }
}
