package com.lukekorth.android_500px.helpers;

import android.content.Context;
import android.os.Build;
import android.os.StatFs;

import com.squareup.picasso.Downloader;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.UrlConnectionDownloader;

import java.io.File;

public class Cache {

    private static final String CACHE_PATH = "wallpaper-cache";
    private static final int MIN_DISK_CACHE_SIZE = 32 * 1024 * 1024;  // 32MB
    private static final int MAX_DISK_CACHE_SIZE = 512 * 1024 * 1024; // 256MB

    private static final float  MAX_AVAILABLE_SPACE_USE_FRACTION = 0.9f;
    private static final float  MAX_TOTAL_SPACE_USE_FRACTION     = 0.25f;

    public static Downloader createCacheDownloader(Context context) {
        try {
            Class.forName("com.squareup.okhttp.OkHttpClient");
            File cacheDir = createDefaultCacheDir(context, CACHE_PATH);
            long cacheSize = calculateDiskCacheSize(cacheDir);
            OkHttpDownloader downloader = new OkHttpDownloader(cacheDir, cacheSize);
            return downloader;
        } catch (ClassNotFoundException e) {
            return new UrlConnectionDownloader(context);
        }
    }

    private static File createDefaultCacheDir(Context context, String path) {
        File cacheDir = context.getApplicationContext().getExternalCacheDir();
        if (cacheDir == null)
            cacheDir = context.getApplicationContext().getCacheDir();
        File cache = new File(cacheDir, path);
        if (!cache.exists()) {
            cache.mkdirs();
        }
        return cache;
    }

    /**
     * Calculates bonded min max cache size. Min value is {@link #MIN_DISK_CACHE_SIZE}
     *
     * @param dir cache dir
     * @return disk space in bytes
     */
    private static long calculateDiskCacheSize(File dir) {
        long size = Math.min(calculateAvailableCacheSize(dir), MAX_DISK_CACHE_SIZE);
        return Math.max(size, MIN_DISK_CACHE_SIZE);
    }

    /**
     * Calculates minimum of available or total fraction of disk space
     *
     * @param dir
     * @return space in bytes
     */
    private static long calculateAvailableCacheSize(File dir) {
        long size = 0;
        try {
            StatFs statFs = new StatFs(dir.getAbsolutePath());
            long totalBytes;
            long availableBytes;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                availableBytes = statFs.getAvailableBytes();
                totalBytes = statFs.getTotalBytes();
            } else {
                int blockSize = statFs.getBlockSize();
                availableBytes = ((long) statFs.getAvailableBlocks()) * blockSize;
                totalBytes = ((long) statFs.getBlockCount()) * blockSize;
            }
            // Target at least 90% of available or 25% of total space
            size = (long) Math.min(availableBytes * MAX_AVAILABLE_SPACE_USE_FRACTION, totalBytes * MAX_TOTAL_SPACE_USE_FRACTION);
        } catch (IllegalArgumentException ignored) {
            // ignored
        }
        return size;
    }

}
