package com.lukekorth.android_500px.models;

import com.google.gson.annotations.SerializedName;

public class PhotosResponse {

    public String feature;
    @SerializedName("current_page") public int currentPage;
    @SerializedName("total_pages") public int totalPages;
    public Photo[] photos;
}
