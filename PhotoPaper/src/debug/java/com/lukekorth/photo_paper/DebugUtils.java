package com.lukekorth.photo_paper;

import android.content.Context;

import com.facebook.stetho.Stetho;
import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.uphyca.stetho_realm.RealmInspectorModulesProvider;

import okhttp3.Interceptor;

public class DebugUtils {

    public static void setup(Context context) {
        Stetho.initialize(Stetho.newInitializerBuilder(context)
                .enableDumpapp(Stetho.defaultDumperPluginsProvider(context))
                .enableWebKitInspector(RealmInspectorModulesProvider.builder(context)
                        .withMetaTables()
                        .build())
                .build());
    }

    public static Interceptor getDebugNetworkInterceptor() {
        return new StethoInterceptor();
    }
}
