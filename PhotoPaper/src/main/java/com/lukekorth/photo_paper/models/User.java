package com.lukekorth.photo_paper.models;

import android.content.Context;
import android.content.Intent;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import com.fivehundredpx.api.auth.AccessToken;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.lukekorth.photo_paper.services.UserInfoIntentService;

@Table(name = "User")
public class User extends Model {

    @Expose
    @Column(name = "user_id")
    public int id;

    @Expose
    @SerializedName("username")
    @Column(name = "username")
    public String userName;

    @Expose
    @SerializedName("firstname")
    @Column(name = "firstname")
    public String firstName;

    @Expose
    @SerializedName("lastname")
    @Column(name = "lastname")
    public String lastName;

    @Expose
    @SerializedName("fullname")
    public String fullName;

    @Expose
    @SerializedName("userpic_url")
    @Column(name = "photo")
    public String photo;

    @Expose
    @Column(name = "access_token")
    public String accessToken;

    @Expose
    @Column(name = "access_token_secret")
    public String accessTokenSecret;

    public static User getUser() {
        return new Select().from(User.class)
                .executeSingle();
    }

    public static boolean isUserLoggedIn() {
        return new Select().from(User.class)
                .exists();
    }

    public static void logout() {
        new Select().from(User.class)
                .executeSingle()
                .delete();
    }

    public static void newUser(Context context, AccessToken accessToken) {
        User user = getUser();
        if (user != null) {
            user.delete();
        }

        user = new User();
        user.accessToken = accessToken.getToken();
        user.accessTokenSecret = accessToken.getTokenSecret();
        user.save();

        context.startService(new Intent(context, UserInfoIntentService.class));
    }

    public static AccessToken getLoggedInUserAccessToken() {
        User user = getUser();
        if (user == null) {
            return null;
        }

        return new AccessToken(user.accessToken, user.accessTokenSecret);
    }
}
