package com.lukekorth.android_500px.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SearchResult {

    @SerializedName("photos")
    private List<Photos> mPhotos;

    public List<Photos> getPhotos() {
        return mPhotos;
    }
}
