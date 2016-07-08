package com.lukekorth.photo_paper.api;

import com.lukekorth.photo_paper.models.ApiResponse;
import com.lukekorth.photo_paper.models.GalleryResponse;
import com.lukekorth.photo_paper.models.PhotoGalleryRequest;
import com.lukekorth.photo_paper.models.PhotoResponse;
import com.lukekorth.photo_paper.models.PhotosResponse;
import com.lukekorth.photo_paper.models.SearchResult;
import com.lukekorth.photo_paper.models.UsersResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
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

    @GET("users/{user_id}/galleries/{gallery_id}/items?image_size=2048&rpp=100")
    Call<PhotosResponse> getFavorites(@Path("user_id") int userId, @Path("gallery_id") String galleryId, @Query("only") String categories, @Query("page") int page);

    @POST("photos/{id}/vote?vote=1")
    Call<PhotoResponse> like(@Path("id") String id);

    @DELETE("photos/{id}/vote")
    Call<PhotoResponse> unlike(@Path("id") String id);

    @PUT("users/{user_id}/galleries/{gallery_id}/items")
    Call<ApiResponse> favorite(@Path("user_id") int userId, @Path("gallery_id") String galleryId, @Body PhotoGalleryRequest request);

    @PUT("users/{user_id}/galleries/{gallery_id}/items")
    Call<ApiResponse> unfavorite(@Path("user_id") int userId, @Path("gallery_id") String galleryId, @Body PhotoGalleryRequest request);

    @GET("users/{id}/galleries?privacy=both&rpp=100")
    Call<GalleryResponse> galleries(@Path("id") int userId);

    @GET("photos/{id}/galleries?rpp=100")
    Call<GalleryResponse> galleriesForPhoto(@Path("id") String id, @Query("user_id") int userId);
}
