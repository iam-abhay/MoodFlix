package com.moodflix.controller;

import com.moodflix.service.PostgreSQLAuthService;
import com.moodflix.util.SessionManager;
import com.moodflix.view.UserStatsPage;
import javafx.application.Platform;
import org.json.JSONObject;
import java.util.*;

public class UserStatsPageController {
    private final UserStatsPage view;
    private final PostgreSQLAuthService authService = new PostgreSQLAuthService();

    public UserStatsPageController(UserStatsPage view) {
        this(view, com.moodflix.util.SessionManager.getEmail());
    }

    public UserStatsPageController(UserStatsPage view, String targetEmail) {
        this.view = view;
        loadUserStats(targetEmail);
        setupBackButton();
    }

    private void loadUserStats() {
        loadUserStats(com.moodflix.util.SessionManager.getEmail());
    }

    private void loadUserStats(String userEmail) {
        if (userEmail == null || userEmail.isEmpty()) return;
        new Thread(() -> {
            try {
                org.json.JSONObject activitiesJson = authService.getUserActivity(userEmail);
                java.util.List<org.json.JSONObject> activities = new java.util.ArrayList<>();
                if (activitiesJson != null && !activitiesJson.toString().equals("null")) {
                    for (String key : activitiesJson.keySet()) {
                        activities.add(activitiesJson.getJSONObject(key));
                    }
                }
                int totalActivities = activities.size();
                int totalWatchTime = 0;
                java.util.Map<String, Integer> moodCount = new java.util.HashMap<>();
                java.util.Map<String, Integer> typeCount = new java.util.HashMap<>();
                java.util.Map<String, Integer> titleCount = new java.util.HashMap<>();
                for (org.json.JSONObject act : activities) {
                    totalWatchTime += act.optInt("duration", 0);
                    String mood = act.optString("mood", "Unknown");
                    String type = act.optString("type", "Unknown");
                    String title = act.optString("title", "Unknown");
                    moodCount.put(mood, moodCount.getOrDefault(mood, 0) + 1);
                    typeCount.put(type, typeCount.getOrDefault(type, 0) + 1);
                    titleCount.put(title, titleCount.getOrDefault(title, 0) + 1);
                }
                String favoriteMood = moodCount.entrySet().stream().max(java.util.Map.Entry.comparingByValue()).map(java.util.Map.Entry::getKey).orElse("None");
                String favoriteType = typeCount.entrySet().stream().max(java.util.Map.Entry.comparingByValue()).map(java.util.Map.Entry::getKey).orElse("None");
                String mostWatched = titleCount.entrySet().stream().max(java.util.Map.Entry.comparingByValue()).map(java.util.Map.Entry::getKey).orElse("None");
                int hours = totalWatchTime / 60;
                int minutes = totalWatchTime % 60;
                String watchTimeStr = (hours > 0) ? (hours + "h " + minutes + "m") : (minutes + "m");
                javafx.application.Platform.runLater(() -> {
                    view.getTotalActivitiesLabel().setText(String.valueOf(totalActivities));
                    view.getTotalWatchTimeLabel().setText(watchTimeStr);
                    view.getFavoriteMoodLabel().setText(favoriteMood);
                    view.getFavoriteTypeLabel().setText(favoriteType);
                    view.getMostWatchedLabel().setText(mostWatched);
                });
            } catch (Exception ex) {
                javafx.application.Platform.runLater(() -> {
                    view.getTotalActivitiesLabel().setText("?");
                    view.getTotalWatchTimeLabel().setText("?");
                    view.getFavoriteMoodLabel().setText("?");
                    view.getFavoriteTypeLabel().setText("?");
                    view.getMostWatchedLabel().setText("?");
                });
            }
        }).start();
    }

    private void setupBackButton() {
        view.getBackBtn().setOnAction(e -> {
            String operationId = com.moodflix.util.PerformanceMonitor.startOperation("stats_back_navigation");
            
            // Use optimized back navigation
            String userEmail = com.moodflix.util.SessionManager.getEmail();
            com.moodflix.util.BackNavigationOptimizer.smartBackNavigation(userEmail);
            
            com.moodflix.util.PerformanceMonitor.endOperation(operationId, true);
        });
    }
} 