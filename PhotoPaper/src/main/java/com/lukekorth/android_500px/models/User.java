package com.lukekorth.android_500px.models;

import android.content.Context;
import android.content.Intent;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import com.fivehundredpx.api.auth.AccessToken;
import com.lukekorth.android_500px.services.UserInfoIntentService;

@Table(name = "User")
public class User extends Model {

    @Column(name = "user_id")
    public int id;

    @Column(name = "username")
    public String userName;

    @Column(name = "firstname")
    public String firstName;

    @Column(name = "lastname")
    public String lastName;

    @Column(name = "photo")
    public String photo;

    @Column(name = "access_token")
    public String accessToken;

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
