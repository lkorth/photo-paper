package com.lukekorth.photo_paper.helpers;

import android.content.Context;

import com.jakewharton.picasso.OkHttp3Downloader;
import com.lukekorth.photo_paper.BuildConfig;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.PicassoTools;

public class PicassoHelper {

    public static Picasso getPicasso(Context context) {
        return new Picasso.Builder(context.getApplicationContext())
                .downloader(new OkHttp3Downloader(context.getApplicationContext(), 512000000)) // 512mb
                .indicatorsEnabled(BuildConfig.DEBUG)
                .build();
    }

    public static void clearCache(Context context) {
        PicassoTools.clearCache(PicassoHelper.getPicasso(context));
    }
}
