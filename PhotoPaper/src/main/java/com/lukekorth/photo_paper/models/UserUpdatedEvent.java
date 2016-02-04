package com.lukekorth.photo_paper.models;

public class UserUpdatedEvent {

    public User user;

    public UserUpdatedEvent(User user) {
        this.user = user;
    }
}
