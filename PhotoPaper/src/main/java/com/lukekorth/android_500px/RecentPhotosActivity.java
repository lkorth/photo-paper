package com.lukekorth.android_500px;

import android.app.ActionBar;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.lukekorth.android_500px.helpers.Utils;
import com.lukekorth.android_500px.models.Photos;
import com.lukekorth.android_500px.models.WallpaperChangedEvent;
import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;

import java.util.List;

public class RecentPhotosActivity extends ListActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WallpaperApplication.getBus().register(this);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateHistory(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        WallpaperApplication.getBus().unregister(this);
    }

    @Subscribe
    public void updateHistory(WallpaperChangedEvent event) {
        setListAdapter(new PhotoAdapter(this, Photos.getRecentlySeenPhotos()));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }

        return false;
    }

    private static class PhotoAdapter extends BaseAdapter {

        private Context mContext;
        private List<Photos> mPhotos;
        private LayoutInflater mLayoutInflater;
        private Picasso mPicasso;
        private int mSize;

        public PhotoAdapter(Context context, List<Photos> photos) {
            mContext = context;
            mPhotos = photos;
            mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mPicasso = WallpaperApplication.getPicasso(context);
            mSize = Utils.dpToPx(context, 100);
        }

        @Override
        public int getCount() {
            return mPhotos.size();
        }

        @Override
        public Object getItem(int position) {
            return mPhotos.get(position);
        }

        @Override
        public long getItemId(int position) {
            return mPhotos.get(position).photo_id;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final Photos photo = mPhotos.get(position);

            ViewHolderItem viewHolderItem;
            if(convertView == null){
                convertView = mLayoutInflater.inflate(R.layout.photo, parent, false);
                viewHolderItem = new ViewHolderItem();
                viewHolderItem.mPhoto = (ImageView) convertView.findViewById(R.id.thumbnail);
                viewHolderItem.mName = (TextView) convertView.findViewById(R.id.name);
                viewHolderItem.mPhotographer = (TextView) convertView.findViewById(R.id.photographer);
                viewHolderItem.mSeenAt = (TextView) convertView.findViewById(R.id.seen);
                convertView.setTag(viewHolderItem);
            } else {
                viewHolderItem = (ViewHolderItem) convertView.getTag();
            }

            CharSequence timeSeen = DateUtils.getRelativeTimeSpanString(photo.seenAt, System.currentTimeMillis(), 0);
            viewHolderItem.mName.setText(photo.name);
            viewHolderItem.mPhotographer.setText("© " + photo.userName + " / 500px");
            viewHolderItem.mSeenAt.setText("Seen " + timeSeen);
            mPicasso.load(photo.imageUrl).resize(mSize, mSize).centerCrop().into(viewHolderItem.mPhoto);
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, ViewPhotoActivity.class)
                            .putExtra(ViewPhotoActivity.PHOTO_POSITION_KEY, position);
                    mContext.startActivity(intent);
                }
            });

            return convertView;
        }

        static class ViewHolderItem {
            ImageView mPhoto;
            TextView mName;
            TextView mPhotographer;
            TextView mSeenAt;
        }
    }

}
