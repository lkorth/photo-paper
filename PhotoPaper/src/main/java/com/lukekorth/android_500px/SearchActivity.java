package com.lukekorth.android_500px;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.SearchView;

import com.lukekorth.android_500px.helpers.Settings;
import com.lukekorth.android_500px.models.SearchCompleteEvent;
import com.lukekorth.android_500px.services.ApiService;
import com.lukekorth.android_500px.views.SquareImageView;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.otto.Subscribe;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.widget.ImageView.ScaleType.CENTER_CROP;

public class SearchActivity extends Activity implements SearchView.OnQueryTextListener,
        AbsListView.OnScrollListener, View.OnClickListener {

    private static final String QUERY_KEY = "com.lukekorth.android_500px.SearchActivity.QUERY_KEY";

    private OkHttpClient mOkHttpClient;

    private SearchView mSearchView;
    private PhotoAdapter mPhotoAdapter;
    private String mCurrentQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.search_grid_view);

        if (savedInstanceState != null) {
            mCurrentQuery = savedInstanceState.getString(QUERY_KEY);
            if (!TextUtils.isEmpty(mCurrentQuery)) {
                setTitle(mCurrentQuery);
            }
        }

        mOkHttpClient = new OkHttpClient();
        mPhotoAdapter = new PhotoAdapter(this);

        GridView gridView = (GridView) findViewById(R.id.grid_view);
        gridView.setAdapter(mPhotoAdapter);
        gridView.setEmptyView(findViewById(R.id.no_search_results));
        gridView.setOnScrollListener(this);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        WallpaperApplication.getBus().register(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        WallpaperApplication.getBus().unregister(this);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(QUERY_KEY, mCurrentQuery);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search, menu);

        mSearchView = (SearchView) menu.findItem(R.id.search).getActionView();
        mSearchView.setQuery(mCurrentQuery, false);
        if (TextUtils.isEmpty(mCurrentQuery)) {
            mSearchView.setIconified(false);
        }
        mSearchView.setOnQueryTextListener(this);
        mSearchView.setOnSearchClickListener(this);

        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(mSearchView.getWindowToken(), 0);

        mCurrentQuery = query;
        setTitle(query);

        // first call clears the search, second call closes search view
        mSearchView.setIconified(true);
        mSearchView.setIconified(true);

        performSearch();
        setProgressBarIndeterminateVisibility(true);

        return true;
    }

    private void performSearch() {
        if (TextUtils.isEmpty(mCurrentQuery)) {
            mPhotoAdapter.setPhotos(new ArrayList<String>());
        }

        Request request = new Request.Builder()
                .header("User-Agent", "com.lukekorth.android_500px")
                .url(ApiService.API_BASE_URL + "photos/search?term=" + Uri.encode(mCurrentQuery) +
                        "&rpp=100" + "&image_size=2&consumer_key=" + BuildConfig.CONSUMER_KEY)
                .build();

        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                WallpaperApplication.getBus().post(new SearchCompleteEvent(new ArrayList<String>()));
            }

            @Override
            public void onResponse(Response response) throws IOException {
                try {
                    if (response.code() == 200) {
                        JSONArray photos = new JSONObject(response.body().string()).getJSONArray("photos");
                        ArrayList<String> data = new ArrayList<String>();
                        for (int i = 0; i < photos.length(); i++) {
                            data.add(photos.getJSONObject(i).getString("image_url"));
                        }

                        WallpaperApplication.getBus().post(new SearchCompleteEvent(data));
                    } else {
                        WallpaperApplication.getBus().post(new SearchCompleteEvent(new ArrayList<String>()));
                    }
                } catch (IOException e) {
                    WallpaperApplication.getBus().post(new SearchCompleteEvent(new ArrayList<String>()));
                } catch (JSONException e) {
                    WallpaperApplication.getBus().post(new SearchCompleteEvent(new ArrayList<String>()));
                }

            }
        });
    }

    @Subscribe
    public void onSearchComplete(SearchCompleteEvent event) {
        mPhotoAdapter.setPhotos(event.getPhotos());
        mPhotoAdapter.notifyDataSetChanged();
        setProgressBarIndeterminateVisibility(false);
    }

    @Override
    public void onClick(View v) {
        if (v == mSearchView && !TextUtils.isEmpty(mCurrentQuery)) {
            mSearchView.setQuery(mCurrentQuery, false);
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == SCROLL_STATE_IDLE || scrollState == SCROLL_STATE_TOUCH_SCROLL) {
            WallpaperApplication.getPicasso(this).resumeTag(this);
        } else {
            WallpaperApplication.getPicasso(this).pauseTag(this);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.save_search:
                Settings.setSearchQuery(this, mCurrentQuery);
                startService(new Intent(this, ApiService.class));
                finish();
                return true;
        }

        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {}

    private class PhotoAdapter extends BaseAdapter {

        private Context mContext;
        private List<String> mPhotos;

        public PhotoAdapter(Context context) {
            mContext = context;
            mPhotos = new ArrayList<String>();
        }

        public void setPhotos(List<String> photos) {
            mPhotos = photos;
        }

        @Override
        public int getCount() {
            return mPhotos.size();
        }

        @Override
        public String getItem(int position) {
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
            }

            WallpaperApplication.getPicasso(mContext)
                    .load(getItem(position))
                    .fit()
                    .tag(mContext)
                    .into(view);

            return view;
        }

    }
}
