package com.lukekorth.photo_paper.services;

import android.app.IntentService;
import android.content.Intent;

import com.lukekorth.photo_paper.WallpaperApplication;
import com.lukekorth.photo_paper.models.User;
import com.lukekorth.photo_paper.models.UserUpdatedEvent;
import com.lukekorth.photo_paper.sync.AccountCreator;

import org.slf4j.LoggerFactory;

import java.io.IOException;

public class UserInfoIntentService extends IntentService {

    public UserInfoIntentService() {
        super("UserInfoIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        ((WallpaperApplication) getApplication()).onUserUpdated(null);

        try {
            User userResponse = WallpaperApplication.getApiClient()
                    .users()
                    .execute()
                    .body()
                    .user;

            User user = User.getUser();
            user.id = userResponse.id;
            user.userName = userResponse.userName;
            user.firstName = userResponse.firstName;
            user.lastName = userResponse.lastName;
            user.photo = userResponse.photo;
            user.save();

            AccountCreator.createAccount(this);

            WallpaperApplication.getBus().post(new UserUpdatedEvent(user));
        } catch (IOException e) {
            LoggerFactory.getLogger("UserInfoIntentService").error(e.getMessage());
        }
    }
}
