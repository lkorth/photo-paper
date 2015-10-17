package com.lukekorth.android_500px.helpers;

import com.lukekorth.android_500px.BuildConfig;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import oauth.signpost.http.HttpRequest;
import retrofit.client.Header;
import retrofit.client.Request;

public class HttpRequestAdapter implements HttpRequest {

    private static final String DEFAULT_CONTENT_TYPE = "application/json";

    private Request mRequest;
    private String mContentType;

    public HttpRequestAdapter(Request request) {
        this(request, DEFAULT_CONTENT_TYPE);
    }

    public HttpRequestAdapter(Request request, String contentType) {
        mRequest = request;
        mContentType = contentType;
    }

    @Override
    public Map<String, String> getAllHeaders() {
        HashMap<String, String> headers = new HashMap<>();
        for (Header header : mRequest.getHeaders()) {
            headers.put(header.getName(), header.getValue());
        }
        headers.put("User-Agent", BuildConfig.APPLICATION_ID);
        return headers;
    }

    @Override
    public String getContentType() {
        return mContentType;
    }

    @Override
    public String getHeader(String key) {
        for (Header header : mRequest.getHeaders()) {
            if (key.equals(header.getName())) {
                return header.getValue();
            }
        }
        return null;
    }

    @Override
    public InputStream getMessagePayload() throws IOException {
        throw new RuntimeException(new UnsupportedOperationException());
    }

    @Override
    public String getMethod() {
        return mRequest.getMethod();
    }

    @Override
    public String getRequestUrl() {
        return mRequest.getUrl();
    }

    @Override
    public void setHeader(String key, String value) {
        ArrayList<Header> headers = new ArrayList<>();
        headers.addAll(mRequest.getHeaders());
        headers.add(new Header(key, value));
        mRequest = new Request(mRequest.getMethod(), mRequest.getUrl(), headers, mRequest.getBody());
    }

    @Override
    public void setRequestUrl(String url) {
        throw new RuntimeException(new UnsupportedOperationException());
    }

    @Override
    public Object unwrap() {
        return mRequest;
    }
}
