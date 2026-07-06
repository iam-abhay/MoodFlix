package com.moodflix.model;

public class MoodEntry {
    private String mood;
    private String timestamp;

    public MoodEntry(String mood, String timestamp) {
        this.mood = mood;
        this.timestamp = timestamp;
    }

    public String getMood() {
        return mood;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setMood(String mood) {
        this.mood = mood;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
