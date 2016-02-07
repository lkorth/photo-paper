package com.lukekorth.photo_paper.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PhotosResponse {

    @Expose public String feature;
    @Expose @SerializedName("current_page") public int currentPage;
    @Expose @SerializedName("total_pages") public int totalPages;
    @Expose public Photo[] photos;
}
