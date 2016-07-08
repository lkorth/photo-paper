package com.lukekorth.photo_paper.adapters;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lukekorth.photo_paper.R;
import com.lukekorth.photo_paper.WallpaperApplication;
import com.lukekorth.photo_paper.helpers.Settings;
import com.lukekorth.photo_paper.helpers.Utils;
import com.lukekorth.photo_paper.models.GalleryResponse;
import com.lukekorth.photo_paper.models.PhotoResponse;
import com.lukekorth.photo_paper.models.Photos;
import com.lukekorth.photo_paper.models.User;
import com.lukekorth.photo_paper.views.FavoriteButton;
import com.lukekorth.photo_paper.views.LikeButton;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PhotoPagerAdapter extends PagerAdapter implements RealmChangeListener {

    private Context mContext;
    private ViewPager mViewPager;
    private LayoutInflater mInflater;
    private Realm mRealm;
    private List<Photos> mPhotos;
    private int mNumberOfPhotos;
    private boolean mLoggedIn;

    public PhotoPagerAdapter(Context context, Realm realm, ViewPager viewPager, RealmResults<Photos> photos) {
        mContext = context;
        mViewPager = viewPager;
        mInflater = LayoutInflater.from(context);
        mRealm = realm;
        mPhotos = photos;
        mNumberOfPhotos = photos.size();
        mLoggedIn = User.isUserLoggedIn(mRealm);
        mRealm.addChangeListener(this);
    }

    @Override
    public void onChange() {
        notifyDataSetChanged();
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        int numberOfNewPhotos = mPhotos.size() - mNumberOfPhotos;
        mNumberOfPhotos = mPhotos.size();
        mViewPager.setCurrentItem(mViewPager.getCurrentItem() + numberOfNewPhotos, false);
    }

    @Override
    public Object instantiateItem(ViewGroup collection, int position) {
        final Photos photo = mPhotos.get(position);
        final View view = mInflater.inflate(R.layout.full_screen_photo, collection, false);
        collection.addView(view);

        WallpaperApplication.getPicasso(mContext)
                .load(photo.imageUrl)
                .resize(Utils.getScreenWidth(mContext), Utils.getScreenHeight(mContext))
                .centerInside()
                .into(((ImageViewTouch) view.findViewById(R.id.photo)));

        if (mLoggedIn) {
            WallpaperApplication.getApiClient().photo(photo.getPhotoId()).enqueue(new Callback<PhotoResponse>() {
                @Override
                public void onResponse(Call<PhotoResponse> call, Response<PhotoResponse> response) {
                    if (!response.isSuccess()) {
                        return;
                    }

                    ((LikeButton) view.findViewById(R.id.votes)).setPhoto(response.body().photo);
                }

                @Override
                public void onFailure(Call<PhotoResponse> call, Throwable t) {}
            });

            final String galleryId = Settings.getFavoriteGalleryId(mContext);
            if (galleryId != null) {
                WallpaperApplication.getApiClient().galleriesForPhoto(photo.getPhotoId(), User.getUser(mRealm).getId())
                        .enqueue(new Callback<GalleryResponse>() {
                            @Override
                            public void onResponse(Call<GalleryResponse> call, Response<GalleryResponse> response) {
                                if (!response.isSuccess()) {
                                    return;
                                }

                                boolean favorited = false;
                                for (GalleryResponse.Gallery gallery : response.body().galleries) {
                                    if (galleryId.equals(gallery.id)) {
                                        favorited = true;
                                        break;
                                    }
                                }
                                ((FavoriteButton) view.findViewById(R.id.favorites)).setup(photo.getPhotoId(), favorited);
                            }

                            @Override
                            public void onFailure(Call<GalleryResponse> call, Throwable t) {}
                        });
            }
        }

        return view;
    }

    @Override
    public void destroyItem(ViewGroup collection, int position, Object view) {
        collection.removeView((View) view);
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public int getCount() {
        return mPhotos.size();
    }

    public Photos getItem(int position) {
        return mPhotos.get(position);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mPhotos.get(position).getName();
    }
}
