package com.lukekorth.android_500px.helpers;

import com.lukekorth.android_500px.BuildConfig;

import retrofit.RequestInterceptor;

public class FiveHundredPxClientRequestInterceptor implements RequestInterceptor {

    @Override
    public void intercept(RequestFacade request) {
        request.addHeader("User-Agent", BuildConfig.APPLICATION_ID);
        request.addQueryParam("consumer_key", BuildConfig.CONSUMER_KEY);
    }
}
