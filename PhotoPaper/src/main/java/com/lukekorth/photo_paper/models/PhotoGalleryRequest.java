package com.lukekorth.photo_paper.models;

import com.google.gson.annotations.Expose;

import java.util.Collections;
import java.util.List;

public class PhotoGalleryRequest {

    @Expose private Add add;
    @Expose private Remove remove;

    public static PhotoGalleryRequest add(String photoId) {
        PhotoGalleryRequest photoGalleryRequest = new PhotoGalleryRequest();
        photoGalleryRequest.add = new Add(photoId);
        return photoGalleryRequest;
    }

    public static PhotoGalleryRequest remove(String photoId) {
        PhotoGalleryRequest photoGalleryRequest = new PhotoGalleryRequest();
        photoGalleryRequest.remove = new Remove(photoId);
        return photoGalleryRequest;
    }

    private static class Add {

        @Expose private List<String> photos;

        private Add(String photoId) {
            photos = Collections.singletonList(photoId);
        }
    }

    private static class Remove {

        @Expose private List<String> photos;

        private Remove(String photoId) {
            photos = Collections.singletonList(photoId);
        }
    }
}
