package com.moodflix.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Activity {
    private String id;
    private String title;
    private String mood;
    private String type;
    private LocalDateTime date;
    private int duration; // in minutes
    private int rating; // 1-5 stars
    private String userId;

    public Activity(String id, String title, String mood, String type, LocalDateTime date, int duration, int rating, String userId) {
        this.id = id;
        this.title = title;
        this.mood = mood;
        this.type = type;
        this.date = date;
        this.duration = duration;
        this.rating = rating;
        this.userId = userId;
    }

    // Default constructor
    public Activity() {
        this.date = LocalDateTime.now();
        this.duration = 0;
        this.rating = 0;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMood() {
        return mood;
    }

    public void setMood(String mood) {
        this.mood = mood;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    // Formatted getters for TableView
    public String getFormattedDate() {
        if (date != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
            return date.format(formatter);
        }
        return "N/A";
    }

    public String getFormattedDuration() {
        if (duration <= 0) {
            return "N/A";
        }
        
        int hours = duration / 60;
        int minutes = duration % 60;
        
        if (hours > 0) {
            return String.format("%dh %dm", hours, minutes);
        } else {
            return String.format("%dm", minutes);
        }
    }

    public String getFormattedRating() {
        if (rating <= 0) {
            return "N/A";
        }
        return "★".repeat(rating) + "☆".repeat(5 - rating);
    }

    @Override
    public String toString() {
        return "Activity{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", mood='" + mood + '\'' +
                ", type='" + type + '\'' +
                ", date=" + date +
                ", duration=" + duration +
                ", rating=" + rating +
                ", userId='" + userId + '\'' +
                '}';
    }
} 