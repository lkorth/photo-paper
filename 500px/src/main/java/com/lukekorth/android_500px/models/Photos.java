package com.lukekorth.android_500px.models;

import android.content.Context;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.From;
import com.activeandroid.query.Select;
import com.lukekorth.android_500px.helpers.Settings;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

@Table(name = "Photos")
public class Photos extends Model {

    public static final String BASE_URL_500PX = "http://500px.com";

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    @Column(name = "photo_id")
    public int photo_id;

    @Column(name = "name")
    public String name;

    @Column(name = "description")
    public String description;

    @Column(name = "user_name")
    public String userName;

    @Column(name = "created_at")
    public long createdAt;

    @Column(name = "feature")
    public String feature;

    @Column(name = "category")
    public int category;

    @Column(name = "nsfw")
    public boolean nsfw;

    @Column(name = "image_url")
    public String imageUrl;

    @Column(name = "url_path")
    public String urlPath;

    @Column(name = "seen")
    public boolean seen;

    @Column(name = "seen_at")
    public long seenAt;

    @Column(name = "added_at")
    public long addedAt;

    public Photos() {
        super();
    }

    public static Photos create(JSONObject jsonPhoto, String feature, int desiredHeight, int desiredWidth) {
        try {
            int actualHeight = jsonPhoto.getInt("height");
            int actualWidth = jsonPhoto.getInt("width");
            if (isAcceptableSize(desiredHeight, desiredWidth, actualHeight, actualWidth)) {
                int id = jsonPhoto.getInt("id");
                if (!new Select().from(Photos.class).where("photo_id = ?", id).exists()) {
                    Photos photo = new Photos();
                    photo.photo_id = id;
                    photo.name = jsonPhoto.getString("name");
                    photo.description = jsonPhoto.getString("description");
                    photo.userName = jsonPhoto.getJSONObject("user").getString("fullname");

                    String createdAt = jsonPhoto.getString("created_at");
                    photo.createdAt = DATE_FORMAT.parse(createdAt.substring(0, createdAt.length() - 6)).getTime();

                    photo.feature = feature;
                    photo.category = jsonPhoto.getInt("category");
                    photo.nsfw = jsonPhoto.getBoolean("nsfw");
                    photo.imageUrl = jsonPhoto.getString("image_url");
                    photo.urlPath = jsonPhoto.getString("url");
                    photo.seen = false;
                    photo.seenAt = 0;
                    photo.addedAt = System.currentTimeMillis();

                    photo.save();

                    return photo;
                }
            }
        } catch (JSONException e) {
        } catch (ParseException e) {
        }

        return null;
    }

    public static Photos getNextPhoto(Context context) {
        Photos photo = getQuery(context).executeSingle();
        photo.seen = true;
        photo.seenAt = System.currentTimeMillis();
        photo.save();

        return photo;
    }

    public static Photos getCurrentPhoto() {
        return getSeenQuery().executeSingle();
    }

    public static List<Photos> getSeenPhotos() {
        return getSeenQuery().execute();
    }

    public static int unseenPhotoCount(Context context) {
        return getQuery(context).count();
    }

    private static From getQuery(Context context) {
        From query = new Select().from(Photos.class)
                .where("feature = ?", Settings.getFeature(context));

        int[] categories = Settings.getCategories(context);
        Object[] categoryArgs = new Object[categories.length];
        String placeHolders = "";
        for (int i = 0; i < categories.length; i++) {
            placeHolders += "?, ";
            categoryArgs[i] = categories[i];
        }
        placeHolders= placeHolders.substring(0, placeHolders.length() - 2);
        query.where("category IN (" + placeHolders + ")", categoryArgs);

        if (!Settings.allowNSFW(context)) {
            query.where("nsfw = ?", false);
        }

        return query.where("seen = ?", false)
                .orderBy("created_at DESC");
    }

    private static From getSeenQuery() {
        return new Select().from(Photos.class)
                .where("seen = ?", true)
                .orderBy("seen_at DESC");
    }

    private static boolean isAcceptableSize(int desiredHeight, int desiredWidth, int actualHeight, int actualWidth) {
        boolean scale;
        if (actualHeight >= desiredHeight && actualWidth >= desiredWidth) {
            scale = true;
        } else {
            double scaleHeight = 0;
            if (actualHeight < desiredHeight) {
                scaleHeight = (double) desiredHeight / actualHeight;
            }

            double scaleWidth = 0;
            if (actualWidth < desiredWidth) {
                scaleWidth = (double) desiredWidth / actualWidth;
            }

            scale = Math.max(scaleHeight, scaleWidth) <= 1.5;
        }

        double desiredAspectRatio = (double) desiredHeight / desiredWidth;
        double actualAspectRatio = (double) actualHeight / actualWidth;
        double percentDifference = (Math.abs(desiredAspectRatio - actualAspectRatio) / desiredAspectRatio) * 100;
        boolean aspectRatio = percentDifference < 30;

        return scale && aspectRatio;
    }

}
