package com.lukekorth.android_500px;

import com.lukekorth.android_500px.helpers.ThreadBus;
import com.squareup.otto.Bus;

public class WallpaperApplication extends com.activeandroid.app.Application {

    private static ThreadBus sBus;

    @Override
    public void onCreate() {
        super.onCreate();
        sBus = new ThreadBus();
    }

    public static Bus getBus() {
        return sBus;
    }

}

