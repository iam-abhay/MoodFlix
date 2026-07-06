package com.moodflix.model;

public class User {
    private String email;
    private String role;
    private String profilePhotoUrl;
    private java.util.List<String> friends;

    public User(String email, String role) {
        this.email = email;
        this.role = role;
        this.friends = new java.util.ArrayList<>();
        this.profilePhotoUrl = null;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public String getProfilePhotoUrl() {
        return profilePhotoUrl;
    }

    public void setProfilePhotoUrl(String profilePhotoUrl) {
        this.profilePhotoUrl = profilePhotoUrl;
    }

    public java.util.List<String> getFriends() {
        return friends;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setFriends(java.util.List<String> friends) {
        this.friends = friends;
    }

    public void addFriend(String friendEmail) {
        if (!friends.contains(friendEmail)) {
            friends.add(friendEmail);
        }
    }

    public void removeFriend(String friendEmail) {
        friends.remove(friendEmail);
    }
}
