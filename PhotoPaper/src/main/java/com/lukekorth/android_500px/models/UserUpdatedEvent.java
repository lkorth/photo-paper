package com.lukekorth.android_500px.models;

public class UserUpdatedEvent {

    private User mUser;

    public UserUpdatedEvent(User user) {
        mUser = user;
    }

    public User getUser() {
        return mUser;
    }
}
