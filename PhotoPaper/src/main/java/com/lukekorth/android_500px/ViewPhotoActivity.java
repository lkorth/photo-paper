package com.lukekorth.android_500px;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import com.lukekorth.android_500px.adapters.PhotoPagerAdapter;
import com.lukekorth.android_500px.models.Photos;

import java.util.List;

public class ViewPhotoActivity extends Activity implements ViewPager.OnPageChangeListener {

    public static final String PHOTO_POSITION_KEY = "com.lukekorth.android_500px.ViewPhotoActivity.PHOTO_POSITION_KEY";

    private ActionBar mActionBar;
    private List<Photos> mPhotos;
    private int mCurrentPhoto;
    private PhotoPagerAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }

        setContentView(R.layout.view_photo);

        mPhotos = Photos.getRecentlySeenPhotos();
        mCurrentPhoto = getIntent().getIntExtra(PHOTO_POSITION_KEY, 0);

        mAdapter = new PhotoPagerAdapter(this);
        mAdapter.setPhotos(mPhotos);

        ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
        viewPager.setOnPageChangeListener(this);
        viewPager.setAdapter(mAdapter);
        viewPager.setCurrentItem(mCurrentPhoto);

        mActionBar = getActionBar();
        if (mActionBar != null) {
            mActionBar.setDisplayHomeAsUpEnabled(true);
        }
        setPhotoName(mCurrentPhoto);
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
            case R.id.share_photo:
                sharePhoto(mCurrentPhoto);
                return true;
            case R.id.open_in_500px:
                launch500PxForPhoto(mCurrentPhoto);
                return true;
        }

        return false;
    }

    private void setPhotoName(int position) {
        if (mActionBar != null) {
            mActionBar.setTitle("  " + mPhotos.get(position).name);
            mActionBar.setSubtitle("   " + mPhotos.get(position).userName);
        }
    }

    private void sharePhoto(int position) {
        Intent intent = new Intent(Intent.ACTION_SEND)
                .setType("text/plain")
                .putExtra(Intent.EXTRA_TEXT, Photos.BASE_URL_500PX + mPhotos.get(position).urlPath);

        startActivity(Intent.createChooser(intent, getString(R.string.share)));
    }

    private void launch500PxForPhoto(int position) {
        Intent intent = new Intent(Intent.ACTION_VIEW,
                Uri.parse(Photos.BASE_URL_500PX + mPhotos.get(position).urlPath));

        startActivity(intent);
    }

    @Override
    public void onPageSelected(int position) {
        mCurrentPhoto = position;
        setPhotoName(position);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

    @Override
    public void onPageScrollStateChanged(int state) {}
}
