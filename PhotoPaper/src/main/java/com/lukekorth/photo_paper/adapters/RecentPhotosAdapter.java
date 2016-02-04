package com.lukekorth.photo_paper.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.lukekorth.photo_paper.R;
import com.lukekorth.photo_paper.ViewPhotoActivity;
import com.lukekorth.photo_paper.WallpaperApplication;
import com.lukekorth.photo_paper.helpers.Utils;
import com.lukekorth.photo_paper.models.Photos;
import com.squareup.picasso.Picasso;

import java.util.List;

public class RecentPhotosAdapter extends RecyclerView.Adapter<RecentPhotosAdapter.ViewHolder> {

    private Context mContext;
    private List<Photos> mPhotos;
    private Picasso mPicasso;
    private int mSize;

    public RecentPhotosAdapter(Context context) {
        mContext = context;
        mPhotos = Photos.getRecentlySeenPhotos();
        mPicasso = WallpaperApplication.getPicasso(context);
        mSize = Utils.dpToPx(context, 100);
    }

    @Override
    public RecentPhotosAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.photo_card, parent, false);
        return new ViewHolder(mContext, view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Photos photo = mPhotos.get(position);
        CharSequence timeSeen = DateUtils.getRelativeTimeSpanString(photo.seenAt, System.currentTimeMillis(), 0);

        holder.mName.setText(photo.name);
        holder.mPhotographer.setText("© " + photo.userName + " / 500px");
        holder.mSeenAt.setText("Seen " + timeSeen);
        mPicasso.load(photo.imageUrl)
                .resize(mSize, mSize)
                .centerCrop()
                .placeholder(new ColorDrawable(photo.palette))
                .into(holder.mPhoto);
    }

    @Override
    public int getItemCount() {
        return mPhotos.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public Context mContext;
        public ImageView mPhoto;
        public TextView mName;
        public TextView mPhotographer;
        public TextView mSeenAt;

        public ViewHolder(Context context, View itemView) {
            super(itemView);

            mContext = context;
            itemView.setOnClickListener(this);

            mPhoto = (ImageView) itemView.findViewById(R.id.thumbnail);
            mName = (TextView) itemView.findViewById(R.id.name);
            mPhotographer = (TextView) itemView.findViewById(R.id.photographer);
            mSeenAt = (TextView) itemView.findViewById(R.id.seen);
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(mContext, ViewPhotoActivity.class)
                    .putExtra(ViewPhotoActivity.PHOTO_POSITION_KEY, getPosition());
            mContext.startActivity(intent);
        }
    }
}
