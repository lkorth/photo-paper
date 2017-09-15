package com.lukekorth.photo_paper.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.lukekorth.photo_paper.helpers.AlarmHelper;
import com.lukekorth.photo_paper.helpers.Settings;
import com.lukekorth.photo_paper.helpers.Utils;
import com.lukekorth.photo_paper.services.WallpaperService;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            if (Settings.isEnabled(context)) {
                if (Utils.shouldUpdateWallpaper(context)) {
                    context.startService(new Intent(context, WallpaperService.class));
                }

                AlarmHelper.scheduleWallpaperAlarm(context);
            }
        }
    }
}
