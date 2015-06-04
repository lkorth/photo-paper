package com.lukekorth.android_500px;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;

import com.lukekorth.android_500px.models.WallpaperChangedEvent;
import com.squareup.otto.Subscribe;

public class RecentPhotosActivity extends Activity {

    private RecyclerView mRecyclerView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recent_photos);

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(new RecentPhotosAdapter(this));

        WallpaperApplication.getBus().register(this);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        onWallpaperChanged(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        WallpaperApplication.getBus().unregister(this);
    }

    @Subscribe
    public void onWallpaperChanged(WallpaperChangedEvent event) {
        mRecyclerView.swapAdapter(new RecentPhotosAdapter(this), false);
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
}
