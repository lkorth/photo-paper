package com.lukekorth.photo_paper.services;

import android.app.IntentService;
import android.content.Intent;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.lukekorth.photo_paper.WallpaperApplication;
import com.lukekorth.photo_paper.helpers.Settings;
import com.lukekorth.photo_paper.models.User;
import com.lukekorth.photo_paper.models.UserUpdatedEvent;

import org.slf4j.LoggerFactory;

import java.io.IOException;

import io.realm.Realm;

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

            Realm realm = Realm.getDefaultInstance();
            User user = User.getUser(realm);
            realm.beginTransaction();
            user.setId(userResponse.getId());
            user.setUserName(userResponse.getUserName());
            user.setFirstName(userResponse.getFirstName());
            user.setLastName(userResponse.getLastName());
            user.setPhoto(userResponse.getPhoto());
            realm.commitTransaction();
            realm.close();

            Settings.setFavoriteGallery(this, null);
            Settings.setFavoriteGalleryId(this, null);

            FirebaseAnalytics.getInstance(this).logEvent(FirebaseAnalytics.Event.LOGIN, null);

            WallpaperApplication.getBus().post(new UserUpdatedEvent());
        } catch (IOException e) {
            LoggerFactory.getLogger("UserInfoIntentService").error(e.getMessage());
        }
    }
}
