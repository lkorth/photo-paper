package com.lukekorth.photo_paper.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.lukekorth.photo_paper.helpers.Utils;
import com.lukekorth.photo_paper.services.WallpaperService;

public class UserPresentReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Utils.shouldUpdateWallpaper(context)) {
            context.startService(new Intent(context, WallpaperService.class)
                .putExtra(WallpaperService.USER_PRESENT_RECEIVER_KEY, true));
        }
    }
}
