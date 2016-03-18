package com.lukekorth.photo_paper;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;

import com.lukekorth.photo_paper.adapters.RecentPhotosAdapter;
import com.lukekorth.photo_paper.models.WallpaperChangedEvent;
import com.squareup.otto.Subscribe;

import io.realm.Realm;

public class RecentPhotosActivity extends AppCompatActivity {

    private Realm mRealm;
    private RecyclerView mRecyclerView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recent_photos);

        mRealm = Realm.getDefaultInstance();

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(new RecentPhotosAdapter(this, mRealm));

        WallpaperApplication.getBus().register(this);

        ActionBar actionBar = getSupportActionBar();
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
        mRealm.close();
    }

    @Subscribe
    public void onWallpaperChanged(WallpaperChangedEvent event) {
        mRecyclerView.getAdapter().notifyDataSetChanged();
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
