package com.lukekorth.android_500px.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.lukekorth.android_500px.helpers.Utils;
import com.lukekorth.android_500px.services.WallpaperService;

public class UserPresentReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Utils.shouldUpdateWallpaper(context)) {
            context.startService(new Intent(context, WallpaperService.class)
                .putExtra(WallpaperService.SLEEP_KEY, true));
        }
    }

}
