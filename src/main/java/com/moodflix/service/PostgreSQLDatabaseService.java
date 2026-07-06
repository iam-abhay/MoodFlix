package com.moodflix.service;

import com.moodflix.database.DatabaseConfig;
import com.moodflix.model.Activity;
import com.moodflix.model.Feedback;
import com.moodflix.model.MoodEntry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PostgreSQLDatabaseService {
    
    // Activity methods
    public void addActivity(Activity activity, String userEmail) throws Exception {
        try (Connection conn = DatabaseConfig.getConnection()) {
            int userId = getUserIdByEmail(userEmail, conn);
            
            if (userId == -1) {
                throw new Exception("User not found: " + userEmail);
            }
            
            String query = """
                INSERT INTO activities (user_id, title, mood, type, activity_date, duration, rating) 
                VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
            
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, userId);
                stmt.setString(2, activity.getTitle());
                stmt.setString(3, activity.getMood());
                stmt.setString(4, activity.getType());
                stmt.setTimestamp(5, Timestamp.valueOf(activity.getDate()));
                stmt.setInt(6, activity.getDuration());
                stmt.setInt(7, activity.getRating());
                
                stmt.executeUpdate();
                System.out.println("✅ Activity added: " + activity.getTitle());
            }
        } catch (SQLException e) {
            throw new Exception("Database error while adding activity: " + e.getMessage(), e);
        }
    }
    
    public List<Activity> getActivitiesByUser(String userEmail) throws Exception {
        List<Activity> activities = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection()) {
            int userId = getUserIdByEmail(userEmail, conn);
            
            if (userId == -1) {
                return activities; // Return empty list if user not found
            }
            
            String query = """
                SELECT id, title, mood, type, activity_date, duration, rating 
                FROM activities WHERE user_id = ? 
                ORDER BY activity_date DESC
            """;
            
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, userId);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Activity activity = new Activity(
                            String.valueOf(rs.getInt("id")),
                            rs.getString("title"),
                            rs.getString("mood"),
                            rs.getString("type"),
                            rs.getTimestamp("activity_date").toLocalDateTime(),
                            rs.getInt("duration"),
                            rs.getInt("rating"),
                            userEmail
                        );
                        activities.add(activity);
                    }
                }
            }
        } catch (SQLException e) {
            throw new Exception("Database error while fetching activities: " + e.getMessage(), e);
        }
        
        return activities;
    }
    
    public List<Activity> getRecentActivities(String userEmail, int limit) throws Exception {
        List<Activity> activities = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection()) {
            int userId = getUserIdByEmail(userEmail, conn);
            
            if (userId == -1) {
                return activities;
            }
            
            String query = """
                SELECT id, title, mood, type, activity_date, duration, rating 
                FROM activities WHERE user_id = ? 
                ORDER BY activity_date DESC 
                LIMIT ?
            """;
            
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, userId);
                stmt.setInt(2, limit);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Activity activity = new Activity(
                            String.valueOf(rs.getInt("id")),
                            rs.getString("title"),
                            rs.getString("mood"),
                            rs.getString("type"),
                            rs.getTimestamp("activity_date").toLocalDateTime(),
                            rs.getInt("duration"),
                            rs.getInt("rating"),
                            userEmail
                        );
                        activities.add(activity);
                    }
                }
            }
        } catch (SQLException e) {
            throw new Exception("Database error while fetching recent activities: " + e.getMessage(), e);
        }
        
        return activities;
    }
    
    // Feedback methods
    public void addFeedback(Feedback feedback) throws Exception {
        try (Connection conn = DatabaseConfig.getConnection()) {
            int userId = getUserIdByEmail(feedback.getUserEmail(), conn);
            
            if (userId == -1) {
                throw new Exception("User not found: " + feedback.getUserEmail());
            }
            
            String query = "INSERT INTO feedback (user_id, message, rating) VALUES (?, ?, ?)";
            
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, userId);
                stmt.setString(2, feedback.getMessage());
                stmt.setInt(3, feedback.getRating());
                
                stmt.executeUpdate();
                System.out.println("✅ Feedback added from user: " + feedback.getUserEmail());
            }
        } catch (SQLException e) {
            throw new Exception("Database error while adding feedback: " + e.getMessage(), e);
        }
    }
    
    public List<Feedback> getAllFeedback() throws Exception {
        List<Feedback> feedbackList = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection()) {
            String query = """
                SELECT u.email, f.message, f.rating, f.created_at 
                FROM feedback f 
                JOIN users u ON f.user_id = u.id 
                ORDER BY f.created_at DESC
            """;
            
            try (PreparedStatement stmt = conn.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {
                
                while (rs.next()) {
                    Feedback feedback = new Feedback(
                        rs.getString("email"),
                        rs.getString("message"),
                        rs.getInt("rating")
                    );
                    feedbackList.add(feedback);
                }
            }
        } catch (SQLException e) {
            throw new Exception("Database error while fetching feedback: " + e.getMessage(), e);
        }
        
        return feedbackList;
    }
    
    // Mood Entry methods
    public void addMoodEntry(MoodEntry moodEntry, String userEmail) throws Exception {
        try (Connection conn = DatabaseConfig.getConnection()) {
            int userId = getUserIdByEmail(userEmail, conn);
            
            if (userId == -1) {
                throw new Exception("User not found: " + userEmail);
            }
            
            String query = "INSERT INTO mood_entries (user_id, mood, entry_timestamp) VALUES (?, ?, ?)";
            
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, userId);
                stmt.setString(2, moodEntry.getMood());
                stmt.setTimestamp(3, Timestamp.valueOf(moodEntry.getTimestamp()));
                
                stmt.executeUpdate();
                System.out.println("✅ Mood entry added: " + moodEntry.getMood());
            }
        } catch (SQLException e) {
            throw new Exception("Database error while adding mood entry: " + e.getMessage(), e);
        }
    }
    
    public List<MoodEntry> getMoodEntries(String userEmail) throws Exception {
        List<MoodEntry> moodEntries = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection()) {
            int userId = getUserIdByEmail(userEmail, conn);
            
            if (userId == -1) {
                return moodEntries;
            }
            
            String query = """
                SELECT mood, entry_timestamp 
                FROM mood_entries 
                WHERE user_id = ? 
                ORDER BY entry_timestamp DESC
            """;
            
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, userId);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        MoodEntry moodEntry = new MoodEntry(
                            rs.getString("mood"),
                            rs.getTimestamp("entry_timestamp").toString()
                        );
                        moodEntries.add(moodEntry);
                    }
                }
            }
        } catch (SQLException e) {
            throw new Exception("Database error while fetching mood entries: " + e.getMessage(), e);
        }
        
        return moodEntries;
    }
    
    // Statistics methods
    public int getTotalUsers() throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection()) {
            String query = "SELECT COUNT(*) as count FROM users";
            
            try (PreparedStatement stmt = conn.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {
                
                if (rs.next()) {
                    return rs.getInt("count");
                }
            }
        }
        return 0;
    }
    
    public int getTotalActivities(String userEmail) throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection()) {
            int userId = getUserIdByEmail(userEmail, conn);
            
            if (userId == -1) {
                return 0;
            }
            
            String query = "SELECT COUNT(*) as count FROM activities WHERE user_id = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, userId);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("count");
                    }
                }
            }
        }
        return 0;
    }
    
    // Helper method
    private int getUserIdByEmail(String email, Connection conn) throws SQLException {
        String query = "SELECT id FROM users WHERE email = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, email);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        }
        return -1;
    }
}