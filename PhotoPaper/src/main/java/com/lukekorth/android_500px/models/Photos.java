package com.lukekorth.android_500px.models;

import android.content.Context;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.From;
import com.activeandroid.query.Select;
import com.google.gson.annotations.SerializedName;
import com.lukekorth.android_500px.helpers.Settings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

@Table(name = "Photos")
public class Photos extends Model {

    public static final String BASE_URL_500PX = "http://500px.com";

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    @SerializedName("id")
    @Column(name = "photo_id")
    public int photo_id;

    @SerializedName("name")
    @Column(name = "name")
    public String name;

    @SerializedName("description")
    @Column(name = "description")
    public String description;

    @Column(name = "user_name")
    public String userName;

    @Column(name = "created_at")
    public long createdAt;

    @Column(name = "feature")
    public String feature;

    @Column(name = "search")
    public String search;

    @SerializedName("category")
    @Column(name = "category")
    public int category;

    @SerializedName("image_url")
    @Column(name = "image_url")
    public String imageUrl;

    @SerializedName("url")
    @Column(name = "url_path")
    public String urlPath;

    @Column(name = "palette")
    public int palette;

    @Column(name = "seen")
    public boolean seen;

    @Column(name = "seen_at")
    public long seenAt;

    @Column(name = "failed_count")
    public int failedCount;

    @Column(name = "added_at")
    public long addedAt;

    public Photos() {
        super();
    }

    public static Photos create(Photo photo, String feature, String search) {
        Logger logger = LoggerFactory.getLogger("Photos");

        if (photo.nsfw) {
            logger.debug("Photo was nsfw");
            return null;
        }

        if (new Select().from(Photos.class).where("photo_id = ?", photo.id).exists()) {
            logger.debug("Photo already exists");
            return null;
        }

        try {
            Photos photoModel = new Photos();
            photoModel.photo_id = Integer.parseInt(photo.id);
            photoModel.name = photo.name;
            photoModel.description = photo.description;
            photoModel.userName = photo.user.fullName;

            String createdAt = photo.createdAt;
            photoModel.createdAt = DATE_FORMAT.parse(createdAt.substring(0, createdAt.length() - 6)).getTime();

            photoModel.feature = feature;
            photoModel.search = search;
            photoModel.category = photo.category;
            photoModel.imageUrl = photo.imageUrl;
            photoModel.urlPath = photo.url;
            photoModel.seen = false;
            photoModel.seenAt = 0;
            photoModel.addedAt = System.currentTimeMillis();

            photoModel.save();

            return photoModel;
        } catch (ParseException e) {
            logger.error(e.getMessage());
        }

        return null;
    }

    public static Photos getPhoto(int id) {
        return new Select().from(Photos.class)
                .where("photo_id = ?", id)
                .executeSingle();
    }

    public static Photos getNextPhoto(Context context) {
        return getQuery(context).executeSingle();
    }

    public static Photos getCurrentPhoto() {
        return getRecentlySeenQuery().executeSingle();
    }

    public static List<Photos> getRecentlySeenPhotos() {
        return getRecentlySeenQuery().execute();
    }

    public static List<Photos> getUnseenPhotos(Context context) {
        return getQuery(context).execute();
    }

    public static int unseenPhotoCount(Context context) {
        return getQuery(context).count();
    }

    private static From getQuery(Context context) {
        String feature = Settings.getFeature(context);
        From query = new Select().from(Photos.class)
                .where("feature = ?", feature);
        if (feature.equals("search")) {
            query.where("search = ?", Settings.getSearchQuery(context));
        }

        int[] categories = Settings.getCategories(context);
        Object[] categoryArgs = new Object[categories.length];
        String placeHolders = "";
        for (int i = 0; i < categories.length; i++) {
            placeHolders += "?, ";
            categoryArgs[i] = categories[i];
        }
        placeHolders = placeHolders.substring(0, placeHolders.length() - 2);
        query.where("category IN (" + placeHolders + ")", categoryArgs);

        return query.where("seen = ?", false)
                .orderBy("failed_count ASC, created_at ASC");
    }

    private static From getRecentlySeenQuery() {
        return new Select().from(Photos.class)
                .where("seen = ?", true)
                .where("seen_at > ?", System.currentTimeMillis() - 604800000) // 7 days in milliseconds
                .orderBy("seen_at DESC");
    }
}
