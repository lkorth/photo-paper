package com.lukekorth.android_500px;

import android.app.ActionBar;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;

import com.lukekorth.android_500px.models.Photos;

import java.util.List;

public class ViewPhotoActivity extends FragmentActivity implements ViewPager.OnPageChangeListener {

    public static final String PHOTO_POSITION_KEY = "com.lukekorth.android_500px.ViewPhotoActivity.PHOTO_POSITION_KEY";

    private List<Photos> mPhotos;
    private int mCurrentPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_photo);

        mPhotos = Photos.getSeenPhotos();

        mCurrentPhoto = getIntent().getIntExtra(PHOTO_POSITION_KEY, 0);

        ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
        viewPager.setOnPageChangeListener(this);
        viewPager.setAdapter(new PhotoPagerAdapter(getSupportFragmentManager()));
        viewPager.setCurrentItem(mCurrentPhoto);

        setPhotoName(mCurrentPhoto);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.view_photo, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.open_in_500px:
                launch500PxForPhoto(mCurrentPhoto);
                return true;
        }

        return false;
    }

    private void setPhotoName(int position) {
        setTitle(mPhotos.get(position).name + " " + getString(R.string.by) + " " +
                mPhotos.get(position).userName);
    }

    private void launch500PxForPhoto(int position) {
        Intent intent = new Intent(Intent.ACTION_VIEW,
                Uri.parse(Photos.BASE_URL_500PX + mPhotos.get(position).urlPath))
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void onPageSelected(int position) {
        setPhotoName(position);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

    @Override
    public void onPageScrollStateChanged(int state) {}

    private class PhotoPagerAdapter extends FragmentPagerAdapter {

        public PhotoPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            PhotoFragment fragment = new PhotoFragment();
            Bundle bundle = new Bundle();
            bundle.putInt(PhotoFragment.PHOTO_ID_KEY, mPhotos.get(position).photo_id);
            fragment.setArguments(bundle);
            return fragment;
        }

        @Override
        public int getCount() {
            return mPhotos.size();
        }
    }

}
