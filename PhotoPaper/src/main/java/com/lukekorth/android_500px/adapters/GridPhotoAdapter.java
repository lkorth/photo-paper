package com.lukekorth.android_500px.adapters;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.lukekorth.android_500px.WallpaperApplication;
import com.lukekorth.android_500px.helpers.Utils;
import com.lukekorth.android_500px.models.Photo;
import com.lukekorth.android_500px.views.SquareImageView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import static android.widget.ImageView.ScaleType.CENTER_CROP;

public class GridPhotoAdapter extends BaseAdapter {

    public static final String TAG = "GridPhotoAdapter";

    private Context mContext;
    private List<Photo> mPhotos;
    private Picasso mPicasso;

    public GridPhotoAdapter(Context context, ArrayList<Photo> photos) {
        mContext = context;
        mPhotos = photos;
        mPicasso = WallpaperApplication.getPicasso(context);
    }

    public void setPhotos(List<Photo> photos) {
        mPhotos = photos;
    }

    public List<Photo> getPhotos() {
        return mPhotos;
    }

    @Override
    public int getCount() {
        return mPhotos.size();
    }

    @Override
    public Photo getItem(int position) {
        return mPhotos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SquareImageView view = (SquareImageView) convertView;
        if (view == null) {
            view = new SquareImageView(mContext);
            view.setScaleType(CENTER_CROP);
        }

        mPicasso.load(getItem(position).imageUrl)
                .placeholder(new ColorDrawable(Utils.generateRandomColor()))
                .fit()
                .tag(TAG)
                .into(view);

        return view;
    }
}
