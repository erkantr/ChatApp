package com.chatapp.model;

public class User {

    private String id;
    private String username;
    private String imageURL;

    public User(String id,String username, String imageURL) {
        this.username = username;
        this.imageURL = imageURL;
        this.id = id;
    }

    public User() {

    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
