package com.lukekorth.photo_paper.models;

import android.content.Context;
import android.content.Intent;

import com.fivehundredpx.api.auth.AccessToken;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.lukekorth.photo_paper.services.UserInfoIntentService;

import io.realm.Realm;
import io.realm.RealmObject;

public class User extends RealmObject {

    @Expose
    private int id;

    @Expose
    @SerializedName("username")
    private String userName;

    @Expose
    @SerializedName("firstname")
    private String firstName;

    @Expose
    @SerializedName("lastname")
    private String lastName;

    @Expose
    @SerializedName("fullname")
    private String fullName;

    @Expose
    @SerializedName("userpic_url")
    private String photo;

    @Expose
    private String accessToken;

    @Expose
    private String accessTokenSecret;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getAccessTokenSecret() {
        return accessTokenSecret;
    }

    public void setAccessTokenSecret(String accessTokenSecret) {
        this.accessTokenSecret = accessTokenSecret;
    }

    public static User getUser(Realm realm) {
        return realm.where(User.class).findFirst();
    }

    public static boolean isUserLoggedIn(Realm realm) {
        return (realm.where(User.class).findFirst() != null);
    }

    public static void logout(Realm realm) {
        realm.beginTransaction();
        realm.where(User.class).findAll().clear();
        realm.commitTransaction();
    }

    public static void newUser(Context context, Realm realm, AccessToken accessToken) {
        User.logout(realm);

        realm.beginTransaction();
        User user = realm.createObject(User.class);
        user.setAccessToken(accessToken.getToken());
        user.setAccessTokenSecret(accessToken.getTokenSecret());
        realm.commitTransaction();

        context.startService(new Intent(context, UserInfoIntentService.class));
    }

    public static AccessToken getLoggedInUserAccessToken(Realm realm) {
        User user = getUser(realm);
        return (user != null ? new AccessToken(user.getAccessToken(), user.getAccessTokenSecret()) : null);
    }
}
