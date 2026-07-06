package com.moodflix.model;

public class Content {
    private String title;
    private String mood;
    private String type; // Movie, Series, Song, Short
    private String link;
    private String description;
    private String imageUrl;

    public Content(String title, String mood, String type, String link, String description, String imageUrl) {
        this.title = title;
        this.mood = mood;
        this.type = type;
        this.link = link;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getMood() {
        return mood;
    }

    public String getType() {
        return type;
    }

    public String getLink() {
        return link;
    }

    public String getDescription() {
        return description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setMood(String mood) {
        this.mood = mood;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
