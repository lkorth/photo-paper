package com.lukekorth.android_500px.interfaces;

import com.lukekorth.android_500px.models.SearchResult;

import retrofit.Callback;
import retrofit.client.Response;
import retrofit.http.GET;
import retrofit.http.Query;

public interface FiveHundredPxClient {

    @GET("/photos/search?image_size=2&rpp=100")
    void search(@Query("term") String term, Callback<SearchResult> callback);

    @GET("/photos/search?image_size=5&rpp100")
    Response getPhotosFromSearch(@Query("term") String term, @Query("categories") String categories, @Query("page") int page);

    @GET("/photos?image_size=5&rpp=100")
    Response getPhotos(@Query("feature") String feature, @Query("categories") String categories, @Query("page") int page);

    @GET("/photos?image_size=5&rpp=100")
    Response getPhotos(@Query("feature") String feature, @Query("categories") String categories, @Query("username") String userName, @Query("page") int page);
}
