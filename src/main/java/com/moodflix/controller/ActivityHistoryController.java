package com.moodflix.controller;

import com.moodflix.Main;
import com.moodflix.model.Activity;
import com.moodflix.service.PostgreSQLAuthService;
import com.moodflix.util.SessionManager;
import com.moodflix.view.ActivityHistoryPage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.application.Platform;
import org.json.JSONObject;
import org.json.JSONArray;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class ActivityHistoryController {
    private final ActivityHistoryPage view;
    private final ObservableList<Activity> activityList = FXCollections.observableArrayList();
    private final PostgreSQLAuthService authService = new PostgreSQLAuthService();

    private String targetUserEmail; // For admin viewing specific user's activities

    public ActivityHistoryController(ActivityHistoryPage view) {
        this.view = view;
        this.targetUserEmail = null;
        setupEventHandlers();
        loadActivities();
    }
    
    // Constructor for admin viewing specific user's activities
    public ActivityHistoryController(ActivityHistoryPage view, String targetUserEmail) {
        this.view = view;
        this.targetUserEmail = targetUserEmail;
        setupEventHandlers();
        
        // Set admin mode header
        view.setAdminMode(targetUserEmail);
        
        loadActivities();
    }

    private void setupEventHandlers() {
        // Refresh button
        view.getRefreshBtn().setOnAction(e -> {
            System.out.println("🔄 Refreshing activity history...");
            loadActivities();
        });

        // Back button
        view.getBackBtn().setOnAction(e -> {
            System.out.println("⬅️ Going back to dashboard...");
            String operationId = com.moodflix.util.PerformanceMonitor.startOperation("activity_back_navigation");
            
            // Use optimized back navigation
            String userEmail = com.moodflix.util.SessionManager.getEmail();
            com.moodflix.util.BackNavigationOptimizer.smartBackNavigation(userEmail);
            
            com.moodflix.util.PerformanceMonitor.endOperation(operationId, true);
        });

        // Filter button
        view.getFilterBtn().setOnAction(e -> {
            System.out.println("🔍 Applying filters...");
            applyFilters();
        });

        // Clear filter button
        view.getClearFilterBtn().setOnAction(e -> {
            System.out.println("🗑️ Clearing filters...");
            clearFilters();
        });
    }

    private void loadActivities() {
        // Use targetUserEmail if available (for admin viewing), otherwise use current session user
        String userEmail = targetUserEmail != null ? targetUserEmail : SessionManager.getEmail();
        if (userEmail == null || userEmail.isEmpty()) {
            view.getStatusLabel().setText(" No user session found. Please login again.");
            return;
        }

        view.getStatusLabel().setText("📊 Loading activities...");
        
        // Run in background thread
        new Thread(() -> {
            try {
                System.out.println("📊 Loading activities for user: " + userEmail);
                
                // Get activities from PostgreSQL
                JSONObject activitiesJson = authService.getUserActivity(userEmail);
                
                Platform.runLater(() -> {
                    activityList.clear();
                    
                    if (activitiesJson != null && !activitiesJson.toString().equals("null")) {
                        int count = 0;
                        Iterator<String> keys = activitiesJson.keys();
                        
                        while (keys.hasNext()) {
                            String key = keys.next();
                            try {
                                JSONObject activityObj = activitiesJson.getJSONObject(key);
                                
                                Activity activity = new Activity(
                                    key,
                                    activityObj.optString("title", "Unknown"),
                                    activityObj.optString("mood", "Unknown"),
                                    activityObj.optString("type", "Unknown"),
                                    parseDateTime(activityObj.optString("timestamp", "")),
                                    activityObj.optInt("duration", 0),
                                    activityObj.optInt("rating", 0),
                                    userEmail
                                );
                                
                                activityList.add(activity);
                                count++;
                                
                            } catch (Exception ex) {
                                System.err.println("Error parsing activity " + key + ": " + ex.getMessage());
                            }
                        }
                        
                        System.out.println(" Loaded " + count + " activities");
                        if (targetUserEmail != null) {
                            view.getStatusLabel().setText("Loaded " + count + " activities for " + targetUserEmail);
                        } else {
                            view.getStatusLabel().setText("Loaded " + count + " activities");
                        }
                        
                        // Update statistics
                        updateStatistics();
                        
                    } else {
                        System.out.println("📭 No activities found for user: " + userEmail);
                        if (targetUserEmail != null) {
                            view.getStatusLabel().setText("📭 No activities found for " + targetUserEmail);
                        } else {
                            view.getStatusLabel().setText("📭 No activities found");
                        }
                    }
                    
                    view.getActivityTable().setItems(activityList);
                });
                
            } catch (Exception ex) {
                System.err.println(" Error loading activities: " + ex.getMessage());
                ex.printStackTrace();
                
                Platform.runLater(() -> {
                    view.getStatusLabel().setText("Error loading activities: " + ex.getMessage());
                });
            }
        }).start();
    }

    private void applyFilters() {
        String selectedMood = view.getFilterMoodBox().getValue();
        String selectedType = view.getFilterTypeBox().getValue();
        LocalDate startDate = view.getStartDatePicker().getValue();
        LocalDate endDate = view.getEndDatePicker().getValue();

        System.out.println("🔍 Applying filters - Mood: " + selectedMood + ", Type: " + selectedType + 
                         ", Start: " + startDate + ", End: " + endDate);

        List<Activity> filteredList = activityList.stream()
            .filter(activity -> {
                // Filter by mood
                if (!"All".equals(selectedMood) && !selectedMood.equals(activity.getMood())) {
                    return false;
                }
                
                // Filter by type
                if (!"All".equals(selectedType) && !selectedType.equals(activity.getType())) {
                    return false;
                }
                
                // Filter by date range
                if (startDate != null && activity.getDate() != null) {
                    LocalDate activityDate = activity.getDate().toLocalDate();
                    if (activityDate.isBefore(startDate)) {
                        return false;
                    }
                }
                
                if (endDate != null && activity.getDate() != null) {
                    LocalDate activityDate = activity.getDate().toLocalDate();
                    if (activityDate.isAfter(endDate)) {
                        return false;
                    }
                }
                
                return true;
            })
            .collect(Collectors.toList());

        ObservableList<Activity> filteredObservableList = FXCollections.observableArrayList(filteredList);
        view.getActivityTable().setItems(filteredObservableList);
        
        view.getStatusLabel().setText("🔍 Showing " + filteredList.size() + " filtered activities");
        
        // Update statistics for filtered data
        updateStatisticsForList(filteredList);
    }

    private void clearFilters() {
        view.getFilterMoodBox().setValue("All");
        view.getFilterTypeBox().setValue("All");
        view.getStartDatePicker().setValue(null);
        view.getEndDatePicker().setValue(null);
        
        view.getActivityTable().setItems(activityList);
        view.getStatusLabel().setText(" Filters cleared");
        
        // Update statistics for all data
        updateStatistics();
    }

    private void updateStatistics() {
        updateStatisticsForList(activityList);
    }

    private void updateStatisticsForList(List<Activity> activities) {
        int totalActivities = activities.size();
        int totalWatchTime = activities.stream()
            .mapToInt(Activity::getDuration)
            .sum();
        
        // Calculate favorite mood
        String favoriteMood = activities.stream()
            .collect(Collectors.groupingBy(Activity::getMood, Collectors.counting()))
            .entrySet().stream()
            .max((e1, e2) -> e1.getValue().compareTo(e2.getValue()))
            .map(entry -> entry.getKey())
            .orElse("None");
        
        // Calculate favorite type
        String favoriteType = activities.stream()
            .collect(Collectors.groupingBy(Activity::getType, Collectors.counting()))
            .entrySet().stream()
            .max((e1, e2) -> e1.getValue().compareTo(e2.getValue()))
            .map(entry -> entry.getKey())
            .orElse("None");

        // Update UI
        Platform.runLater(() -> {
            view.getTotalActivitiesLabel().setText(String.valueOf(totalActivities));
            
            int hours = totalWatchTime / 60;
            int minutes = totalWatchTime % 60;
            if (hours > 0) {
                view.getTotalWatchTimeLabel().setText(hours + "h " + minutes + "m");
            } else {
                view.getTotalWatchTimeLabel().setText(minutes + "m");
            }
        });
        
        System.out.println("📊 Statistics updated - Total: " + totalActivities + 
                         ", Watch Time: " + totalWatchTime + " minutes" +
                         ", Favorite Mood: " + favoriteMood +
                         ", Favorite Type: " + favoriteType);
    }

    private LocalDateTime parseDateTime(String timestamp) {
        try {
            if (timestamp != null && !timestamp.isEmpty()) {
                long time = Long.parseLong(timestamp);
                return LocalDateTime.ofEpochSecond(time / 1000, 0, java.time.ZoneOffset.UTC);
            }
        } catch (Exception ex) {
            System.err.println("Error parsing timestamp: " + timestamp);
        }
        return LocalDateTime.now();
    }

    private void navigateBack() {
        try {
            String operationId = com.moodflix.util.PerformanceMonitor.startOperation("activity_back_navigation");
            
            // Check if user is admin and force admin role if needed
            String userEmail = com.moodflix.util.SessionManager.getEmail();
            if (userEmail != null) {
                // Check if user has admin privileges by looking at their role in PostgreSQL
                try {
                    com.moodflix.service.PostgreSQLAuthService service = new com.moodflix.service.PostgreSQLAuthService();
                    org.json.JSONObject userDetails = service.getUserDetails(userEmail);
                    if (userDetails != null && userDetails.has("role")) {
                        String role = userDetails.getString("role");
                        if ("admin".equalsIgnoreCase(role)) {
                            // Force admin role in session
                            com.moodflix.util.SessionManager.setSession(userEmail, "admin");
                        }
                    }
                } catch (Exception ex) {
                    System.err.println("Error checking user role: " + ex.getMessage());
                }
            }
            
            // Create new dashboard scene based on user role
            try {
                com.moodflix.service.PostgreSQLAuthService service = new com.moodflix.service.PostgreSQLAuthService();
                org.json.JSONObject userDetails = service.getUserDetails(userEmail);
                if (userDetails != null && userDetails.has("role")) {
                    String role = userDetails.getString("role");
                    if ("admin".equalsIgnoreCase(role)) {
                        // Navigate to admin dashboard
                        com.moodflix.view.AdminDashboard adminDashboard = new com.moodflix.view.AdminDashboard();
                        com.moodflix.controller.AdminDashboardController adminController = new com.moodflix.controller.AdminDashboardController(adminDashboard);
                        javafx.scene.Scene adminScene = new javafx.scene.Scene(adminDashboard.getView(), 1200, 800);
                        com.moodflix.Main.setScene(adminScene);
                    } else {
                        // Navigate to user dashboard
                        com.moodflix.view.UserDashboard userDashboard = new com.moodflix.view.UserDashboard();
                        com.moodflix.controller.UserDashboardController userController = new com.moodflix.controller.UserDashboardController(userDashboard);
                        javafx.scene.Scene userScene = new javafx.scene.Scene(userDashboard.getView());
                        com.moodflix.Main.setScene(userScene);
                    }
                } else {
                    // Default to user dashboard
                    com.moodflix.view.UserDashboard userDashboard = new com.moodflix.view.UserDashboard();
                    com.moodflix.controller.UserDashboardController userController = new com.moodflix.controller.UserDashboardController(userDashboard);
                    javafx.scene.Scene userScene = new javafx.scene.Scene(userDashboard.getView());
                    com.moodflix.Main.setScene(userScene);
                }
            } catch (Exception ex) {
                System.err.println("Error determining dashboard type: " + ex.getMessage());
                // Fallback to user dashboard
                com.moodflix.view.UserDashboard userDashboard = new com.moodflix.view.UserDashboard();
                com.moodflix.controller.UserDashboardController userController = new com.moodflix.controller.UserDashboardController(userDashboard);
                javafx.scene.Scene userScene = new javafx.scene.Scene(userDashboard.getView());
                com.moodflix.Main.setScene(userScene);
            }
            
            com.moodflix.util.PerformanceMonitor.endOperation(operationId, true);
        } catch (Exception ex) {
            System.err.println("Error navigating back: " + ex.getMessage());
            // Fallback to login page
            com.moodflix.view.LoginPage loginView = new com.moodflix.view.LoginPage();
            com.moodflix.controller.LoginPageController loginController = new com.moodflix.controller.LoginPageController(loginView);
            com.moodflix.Main.setScene(new javafx.scene.Scene(loginView.getView()));
        }
    }
} 