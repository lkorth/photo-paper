package com.lukekorth.android_500px.models;

import java.util.ArrayList;

public class SearchCompleteEvent {

    private ArrayList<String> mPhotos;

    public SearchCompleteEvent(ArrayList<String> photos) {
        mPhotos = photos;
    }

    public ArrayList<String> getPhotos() {
        return mPhotos;
    }
}
