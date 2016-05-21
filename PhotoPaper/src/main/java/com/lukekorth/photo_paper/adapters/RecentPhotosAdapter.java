package com.lukekorth.photo_paper.adapters;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lukekorth.photo_paper.R;
import com.lukekorth.photo_paper.ViewPhotoActivity;
import com.lukekorth.photo_paper.WallpaperApplication;
import com.lukekorth.photo_paper.databinding.PhotoCardBinding;
import com.lukekorth.photo_paper.helpers.Utils;
import com.lukekorth.photo_paper.models.Photos;
import com.squareup.picasso.Picasso;

import java.util.List;

import io.realm.Realm;

public class RecentPhotosAdapter extends RecyclerView.Adapter<RecentPhotosAdapter.ViewHolder> {

    private List<Photos> mPhotos;
    private Picasso mPicasso;
    private int mSize;

    public RecentPhotosAdapter(Context context, Realm realm) {
        mPhotos = Photos.getRecentlySeenPhotos(realm);
        mPicasso = WallpaperApplication.getPicasso(context);
        mSize = Utils.dpToPx(context, 100);
    }

    @Override
    public RecentPhotosAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        PhotoCardBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                R.layout.photo_card, parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Photos photo = mPhotos.get(position);
        holder.mBinding.setPhoto(photo);
        mPicasso.load(photo.imageUrl)
                .resize(mSize, mSize)
                .centerCrop()
                .placeholder(new ColorDrawable(photo.getPalette()))
                .into(holder.mBinding.thumbnail);
    }

    @Override
    public int getItemCount() {
        return mPhotos.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private PhotoCardBinding mBinding;

        ViewHolder(PhotoCardBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(v.getContext(), ViewPhotoActivity.class)
                    .putExtra(ViewPhotoActivity.PHOTO_POSITION_KEY, getPosition());
            v.getContext().startActivity(intent);
        }
    }
}
