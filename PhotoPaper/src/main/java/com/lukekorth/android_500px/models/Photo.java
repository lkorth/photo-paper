package com.lukekorth.android_500px.models;

import com.google.gson.annotations.SerializedName;

public class Photo {

    public String id;
    public String name;
    public String description;
    @SerializedName("created_at") public String createdAt;
    public int category;
    public int width;
    public int height;
    @SerializedName("votes_count") public int votes;
    @SerializedName("favorites_count") public int favorites;
    public boolean nsfw;
    @SerializedName("highest_rating") public double highestRating;
    @SerializedName("image_url") public String imageUrl;
    public String url;
    public User user;

}
