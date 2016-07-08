package com.lukekorth.photo_paper.models;

import com.google.gson.annotations.Expose;

import java.util.List;

public class GalleryResponse {

    @Expose public List<Gallery> galleries;

    public class Gallery {

        @Expose public String id;
        @Expose public String name;
    }
}
