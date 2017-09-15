package com.lukekorth.photo_paper.models;

import android.content.Context;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.lukekorth.photo_paper.helpers.Settings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;
import io.realm.annotations.Required;

public class Photos extends RealmObject {

    public static final String BASE_URL_500PX = "http://500px.com";

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    @Expose
    @SerializedName("id")
    @Required
    private String id;

    @Expose
    private String name;

    @Expose
    private String description;

    @Expose
    private String userName;

    @Expose
    private long createdAt;

    @Expose
    private String feature;

    @Expose
    private String search;

    @Expose
    private int category;

    @Expose
    @SerializedName("highest_rating")
    private double highestRating;

    @Expose
    @SerializedName("times_viewed")
    private int views;

    @Expose
    @SerializedName("image_url")
    public String imageUrl;

    @Expose
    @SerializedName("url")
    private String urlPath;

    @Expose
    private int palette;

    @Expose
    private boolean seen;

    @Expose
    private long seenAt;

    @Expose
    private int failedCount;

    @Expose
    private long addedAt;

    public String getPhotoId() {
        return id;
    }

    public void setPhotoId(String photoId) {
        this.id = photoId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public String getFeature() {
        return feature;
    }

    public void setFeature(String feature) {
        this.feature = feature;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    public double getHighestRating() {
        return highestRating;
    }

    public void setHighestRating(double highestRating) {
        this.highestRating = highestRating;
    }

    public int getViews() {
        return views;
    }

    public void setViews(int views) {
        this.views = views;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getUrlPath() {
        return urlPath;
    }

    public void setUrlPath(String urlPath) {
        this.urlPath = urlPath;
    }

    public int getPalette() {
        return palette;
    }

    public void setPalette(int palette) {
        this.palette = palette;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public long getSeenAt() {
        return seenAt;
    }

    public void setSeenAt(long seenAt) {
        this.seenAt = seenAt;
    }

    public int getFailedCount() {
        return failedCount;
    }

    public void setFailedCount(int failedCount) {
        this.failedCount = failedCount;
    }

    public long getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(long addedAt) {
        this.addedAt = addedAt;
    }

    public static Photos create(Realm realm, Photo photo, String feature, String search) {
        Logger logger = LoggerFactory.getLogger("Photos");

        if (photo.nsfw) {
            logger.debug("Photo was nsfw");
            return null;
        }

        if (realm.where(Photos.class).equalTo("id", photo.id).count() > 0) {
            logger.debug("Photo already exists");
            return null;
        }

        try {
            realm.beginTransaction();

            Photos photoModel = realm.createObject(Photos.class);
            photoModel.setPhotoId(photo.id);
            photoModel.setName(photo.name);
            photoModel.setDescription(photo.description);
            photoModel.setUserName(photo.user.getFullName());
            photoModel.setCreatedAt(DATE_FORMAT.parse(photo.createdAt.substring(0, photo.createdAt.length() - 6)).getTime());
            photoModel.setFeature(feature);
            photoModel.setSearch(search);
            photoModel.setCategory(photo.category);
            photoModel.setHighestRating(photo.highestRating);
            photoModel.setViews(photo.views);
            photoModel.setImageUrl(photo.imageUrl);
            photoModel.setUrlPath(photo.url);
            photoModel.setSeen(false);
            photoModel.setSeenAt(0);
            photoModel.setAddedAt(photoModel.addedAt = System.currentTimeMillis());

            realm.commitTransaction();

            return photoModel;
        } catch (ParseException e) {
            logger.error(e.getMessage());
        }

        return null;
    }

    public static Photos getPhoto(Realm realm, int id) {
        return realm.where(Photos.class)
                .equalTo("id", id)
                .findFirst();
    }

    public static Photos getNextPhoto(Context context, Realm realm) {
        RealmResults<Photos> photos = getQuery(context, realm);
        if (photos.isEmpty()) {
            return null;
        } else {
            return photos.first();
        }
    }

    public static Photos getCurrentPhoto(Realm realm) {
        RealmResults<Photos> photos = getRecentlySeenPhotos(realm);
        if (photos.isEmpty()) {
            return null;
        } else {
            return photos.first();
        }
    }

    public static RealmResults<Photos> getRecentlySeenPhotos(Realm realm) {
        return realm.where(Photos.class)
                .equalTo("seen", true)
                .greaterThan("seenAt", System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7))
                .findAllSorted("seenAt", Sort.DESCENDING);
    }

    public static List<Photos> getUnseenPhotos(Context context, Realm realm) {
        return getQuery(context, realm);
    }

    public static int unseenPhotoCount(Context context, Realm realm) {
        return getQuery(context, realm).size();
    }

    private static RealmResults<Photos> getQuery(Context context, Realm realm) {
        String feature = Settings.getFeature(context);
        RealmQuery<Photos> query = realm.where(Photos.class);
        if (feature.equals("search")) {
            query.equalTo("search", Settings.getSearchQuery(context));
        } else {
            query.equalTo("feature", feature);
        }

        query.beginGroup();
        int[] categories = Settings.getCategories(context);
        for (int i = 0; i < categories.length - 1; i++) {
            query.equalTo("category", categories[i])
                    .or();
        }
        query.equalTo("category", categories[categories.length - 1])
                .endGroup();

        return query.equalTo("seen", false)
                .findAllSorted(new String[] { "failedCount", "highestRating", "views", "createdAt" },
                        new Sort[] { Sort.ASCENDING, Sort.DESCENDING, Sort.DESCENDING, Sort.ASCENDING });
    }
}
