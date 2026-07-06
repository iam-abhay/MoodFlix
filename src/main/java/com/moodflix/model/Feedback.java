package com.moodflix.model;

public class Feedback {
    private String userEmail;
    private String message;
    private int rating;

    public Feedback(String userEmail, String message, int rating) {
        this.userEmail = userEmail;
        this.message = message;
        this.rating = rating;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getMessage() {
        return message;
    }

    public int getRating() {
        return rating;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }
}
