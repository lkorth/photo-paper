package com.lukekorth.android_500px.models;

import java.util.List;

public class SearchCompleteEvent {

    private List<String> mPhotos;

    public SearchCompleteEvent(List<String> photos) {
        mPhotos = photos;
    }

    public List<String> getPhotos() {
        return mPhotos;
    }
}
