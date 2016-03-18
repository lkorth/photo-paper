package com.lukekorth.photo_paper;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import com.lukekorth.photo_paper.adapters.PhotoPagerAdapter;
import com.lukekorth.photo_paper.models.Photos;

import io.realm.Realm;

public class ViewPhotoActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener {

    public static final String PHOTO_POSITION_KEY = "com.lukekorth.photo_paper.ViewPhotoActivity.PHOTO_POSITION_KEY";

    private ActionBar mActionBar;
    private Realm mRealm;
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

        mRealm = Realm.getDefaultInstance();

        mCurrentPhoto = getIntent().getIntExtra(PHOTO_POSITION_KEY, 0);

        mAdapter = new PhotoPagerAdapter(this, mRealm, Photos.getRecentlySeenPhotos(mRealm));

        ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
        viewPager.setOnPageChangeListener(this);
        viewPager.setAdapter(mAdapter);
        viewPager.setCurrentItem(mCurrentPhoto);

        mActionBar = getSupportActionBar();
        if (mActionBar != null) {
            mActionBar.setDisplayHomeAsUpEnabled(true);
        }
        setPhotoName(mCurrentPhoto);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRealm.close();
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
            mActionBar.setTitle("  " + mAdapter.getItem(position).getName());
            mActionBar.setSubtitle("   " + mAdapter.getItem(position).getUserName());
        }
    }

    private void sharePhoto(int position) {
        Intent intent = new Intent(Intent.ACTION_SEND)
                .setType("text/plain")
                .putExtra(Intent.EXTRA_TEXT, Photos.BASE_URL_500PX + mAdapter.getItem(position).getUrlPath());

        startActivity(Intent.createChooser(intent, getString(R.string.share)));
    }

    private void launch500PxForPhoto(int position) {
        Intent intent = new Intent(Intent.ACTION_VIEW,
                Uri.parse(Photos.BASE_URL_500PX + mAdapter.getItem(position).getUrlPath()));

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
