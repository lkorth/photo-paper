package com.lukekorth.photo_paper.helpers;

import com.lukekorth.photo_paper.BuildConfig;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

public class UserAgentInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request()
                .newBuilder()
                .header("User-Agent", BuildConfig.APPLICATION_ID)
                .build();

        return chain.proceed(request);
    }
}
