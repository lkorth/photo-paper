package com.lukekorth.photo_paper.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Photo {

    @Expose public String id;
    @Expose public String name;
    @Expose public String description;
    @Expose @SerializedName("created_at") public String createdAt;
    @Expose public int category;
    @Expose @SerializedName("votes_count") public int votes;
    @Expose public boolean nsfw;
    @Expose @SerializedName("highest_rating") public double highestRating;
    @Expose @SerializedName("times_viewed") public int views;
    @Expose @SerializedName("image_url") public String imageUrl;
    @Expose public String url;
    @Expose public User user;
    @Expose public boolean voted;
    @Expose public boolean favorited;

}
