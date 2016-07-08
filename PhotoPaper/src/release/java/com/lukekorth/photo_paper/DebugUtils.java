package com.lukekorth.photo_paper;

import android.content.Context;

import okhttp3.Interceptor;

public class DebugUtils {

    public static void setup(Context context) {}

    public static Interceptor getDebugNetworkInterceptor() {
        return null;
    }
}
