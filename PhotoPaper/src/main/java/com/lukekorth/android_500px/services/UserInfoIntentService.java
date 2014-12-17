package com.lukekorth.android_500px.services;

import android.app.IntentService;
import android.content.Intent;

import com.fivehundredpx.api.PxApi;
import com.lukekorth.android_500px.BuildConfig;
import com.lukekorth.android_500px.WallpaperApplication;
import com.lukekorth.android_500px.models.User;
import com.lukekorth.android_500px.models.UserUpdatedEvent;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

public class UserInfoIntentService extends IntentService {

    public UserInfoIntentService() {
        super("UserInfoIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        PxApi pxApi = new PxApi(User.getLoggedInUserAccessToken(), BuildConfig.CONSUMER_KEY,
                BuildConfig.CONSUMER_SECRET);
        try {
            JSONObject jsonUser = pxApi.get("/users").getJSONObject("user");

            User user = User.getUser();
            user.id = jsonUser.getInt("id");
            user.userName = jsonUser.getString("username");
            user.firstName = jsonUser.getString("firstname");
            user.lastName = jsonUser.getString("lastname");
            user.photo = jsonUser.getString("userpic_url");
            user.save();

            WallpaperApplication.getBus().post(new UserUpdatedEvent(user));
        } catch (JSONException e) {
            LoggerFactory.getLogger("UserInfoIntentService").error(e.getMessage());
        }
    }
}
