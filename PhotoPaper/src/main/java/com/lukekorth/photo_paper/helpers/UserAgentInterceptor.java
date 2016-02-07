package com.lukekorth.photo_paper.helpers;

import com.lukekorth.photo_paper.BuildConfig;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

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
