package com.lukekorth.photo_paper.interfaces;

import com.lukekorth.photo_paper.models.PhotoResponse;
import com.lukekorth.photo_paper.models.PhotosResponse;
import com.lukekorth.photo_paper.models.SearchResult;
import com.lukekorth.photo_paper.models.UsersResponse;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface FiveHundredPxClient {

    @GET("users")
    Call<UsersResponse> users();

    @GET("photos/{id}")
    Call<PhotoResponse> photo(@Path("id") String id);

    @GET("photos/search?image_size=2&rpp=100")
    Call<SearchResult> search(@Query("term") String term);

    @GET("photos/search?image_size=2048&rpp100")
    Call<PhotosResponse> getPhotosFromSearch(@Query("term") String term, @Query("categories") String categories, @Query("page") int page);

    @GET("photos?image_size=2048&rpp=100")
    Call<PhotosResponse> getPhotos(@Query("feature") String feature, @Query("categories") String categories, @Query("page") int page);

    @GET("photos?image_size=2048&rpp=100")
    Call<PhotosResponse> getPhotos(@Query("feature") String feature, @Query("categories") String categories, @Query("username") String userName, @Query("page") int page);

    @POST("photos/{id}/vote")
    /** 0 for dislike, 1 for like */
    Call<PhotoResponse> vote(@Path("id") String id, @Query("vote") int like);

    @POST("photos/{id}/favorite")
    Call<PhotoResponse> favorite(@Path("id") String id);

    @DELETE("photos/{id}/favorite")
    Call<PhotoResponse> unfavorite(@Path("id") String id);
}
