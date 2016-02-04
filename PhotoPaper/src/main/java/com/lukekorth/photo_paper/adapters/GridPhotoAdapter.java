package com.lukekorth.photo_paper.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.lukekorth.photo_paper.R;
import com.lukekorth.photo_paper.WallpaperApplication;
import com.lukekorth.photo_paper.helpers.Utils;
import com.lukekorth.photo_paper.models.Photo;
import com.lukekorth.photo_paper.views.SquareImageView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import static android.widget.ImageView.ScaleType.CENTER_CROP;

public class GridPhotoAdapter extends BaseAdapter {

    public static final String TAG = "GridPhotoAdapter";

    private Context mContext;
    private List<Photo> mPhotos;
    private Picasso mPicasso;
    private int mOneDpInPx;

    public GridPhotoAdapter(Context context, ArrayList<Photo> photos) {
        mContext = context;
        mPhotos = photos;
        mPicasso = WallpaperApplication.getPicasso(context);
        mOneDpInPx = Utils.dpToPx(mContext, 0.5);
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
            view.setPadding(mOneDpInPx, mOneDpInPx, mOneDpInPx, mOneDpInPx);
        }

        mPicasso.load(getItem(position).imageUrl)
                .placeholder(R.color.grey)
                .fit()
                .tag(TAG)
                .into(view);

        return view;
    }
}
