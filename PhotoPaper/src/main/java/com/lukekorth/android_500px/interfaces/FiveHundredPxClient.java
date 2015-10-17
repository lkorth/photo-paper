package com.lukekorth.android_500px.interfaces;

import com.lukekorth.android_500px.models.PhotosResponse;
import com.lukekorth.android_500px.models.SearchResult;
import com.lukekorth.android_500px.models.UsersResponse;

import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Query;

public interface FiveHundredPxClient {

    @GET("users")
    Call<UsersResponse> users();

    @GET("photos/search?image_size=2&rpp=100")
    Call<SearchResult> search(@Query("term") String term);

    @GET("photos/search?image_size=2048&rpp100")
    Call<PhotosResponse> getPhotosFromSearch(@Query("term") String term, @Query("categories") String categories, @Query("page") int page);

    @GET("photos?image_size=2048&rpp=100")
    Call<PhotosResponse> getPhotos(@Query("feature") String feature, @Query("categories") String categories, @Query("page") int page);

    @GET("photos?image_size=2048&rpp=100")
    Call<PhotosResponse> getPhotos(@Query("feature") String feature, @Query("categories") String categories, @Query("username") String userName, @Query("page") int page);
}
