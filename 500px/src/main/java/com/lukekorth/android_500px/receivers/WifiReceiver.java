package com.lukekorth.android_500px.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.lukekorth.android_500px.helpers.Utils;
import com.lukekorth.android_500px.services.ApiService;

public class WifiReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Utils.isConnectedToWifi(context) && Utils.needMorePhotos(context)) {
            context.startService(new Intent(context, ApiService.class));
        }
    }

}
