package com.moodflix.service;

import com.moodflix.database.DatabaseConfig;
import com.moodflix.model.Content;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PostgreSQLWatchlistService {
    
    public void addToWatchlist(String userEmail, String contentTitle) throws Exception {
        try (Connection conn = DatabaseConfig.getConnection()) {
            // Get user ID and content ID
            int userId = getUserIdByEmail(userEmail, conn);
            int contentId = getContentIdByTitle(contentTitle, conn);
            
            if (userId == -1) {
                throw new Exception("User not found: " + userEmail);
            }
            if (contentId == -1) {
                throw new Exception("Content not found: " + contentTitle);
            }
            
            // Add to watchlist (ON CONFLICT DO NOTHING prevents duplicates)
            String query = "INSERT INTO watchlist (user_id, content_id) VALUES (?, ?) ON CONFLICT (user_id, content_id) DO NOTHING";
            
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, userId);
                stmt.setInt(2, contentId);
                
                stmt.executeUpdate();
                System.out.println("✅ Added to watchlist: " + contentTitle + " for user: " + userEmail);
            }
        } catch (SQLException e) {
            throw new Exception("Database error while adding to watchlist: " + e.getMessage(), e);
        }
    }
    
    public List<Content> getWatchlist(String userEmail) throws Exception {
        List<Content> watchlist = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection()) {
            int userId = getUserIdByEmail(userEmail, conn);
            
            if (userId == -1) {
                throw new Exception("User not found: " + userEmail);
            }
            
            String query = """
                SELECT c.title, c.mood, c.type, c.link, c.description, c.image_url 
                FROM watchlist w 
                JOIN content c ON w.content_id = c.id 
                WHERE w.user_id = ? 
                ORDER BY w.added_at DESC
            """;
            
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, userId);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Content content = new Content(
                            rs.getString("title"),
                            rs.getString("mood"),
                            rs.getString("type"),
                            rs.getString("link"),
                            rs.getString("description"),
                            rs.getString("image_url")
                        );
                        watchlist.add(content);
                    }
                }
            }
        } catch (SQLException e) {
            throw new Exception("Database error while fetching watchlist: " + e.getMessage(), e);
        }
        
        return watchlist;
    }
    
    public void removeFromWatchlist(String userEmail, String contentTitle) throws Exception {
        try (Connection conn = DatabaseConfig.getConnection()) {
            int userId = getUserIdByEmail(userEmail, conn);
            int contentId = getContentIdByTitle(contentTitle, conn);
            
            if (userId == -1) {
                throw new Exception("User not found: " + userEmail);
            }
            if (contentId == -1) {
                throw new Exception("Content not found: " + contentTitle);
            }
            
            String query = "DELETE FROM watchlist WHERE user_id = ? AND content_id = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, userId);
                stmt.setInt(2, contentId);
                
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("✅ Removed from watchlist: " + contentTitle + " for user: " + userEmail);
                } else {
                    System.out.println("⚠️ Content not found in watchlist: " + contentTitle);
                }
            }
        } catch (SQLException e) {
            throw new Exception("Database error while removing from watchlist: " + e.getMessage(), e);
        }
    }
    
    public boolean isInWatchlist(String userEmail, String contentTitle) throws Exception {
        try (Connection conn = DatabaseConfig.getConnection()) {
            int userId = getUserIdByEmail(userEmail, conn);
            int contentId = getContentIdByTitle(contentTitle, conn);
            
            if (userId == -1 || contentId == -1) {
                return false;
            }
            
            String query = "SELECT COUNT(*) as count FROM watchlist WHERE user_id = ? AND content_id = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, userId);
                stmt.setInt(2, contentId);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("count") > 0;
                    }
                }
            }
        } catch (SQLException e) {
            throw new Exception("Database error while checking watchlist: " + e.getMessage(), e);
        }
        
        return false;
    }
    
    public int getWatchlistCount(String userEmail) throws Exception {
        try (Connection conn = DatabaseConfig.getConnection()) {
            int userId = getUserIdByEmail(userEmail, conn);
            
            if (userId == -1) {
                return 0;
            }
            
            String query = "SELECT COUNT(*) as count FROM watchlist WHERE user_id = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, userId);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("count");
                    }
                }
            }
        } catch (SQLException e) {
            throw new Exception("Database error while counting watchlist: " + e.getMessage(), e);
        }
        
        return 0;
    }
    
    public void clearWatchlist(String userEmail) throws Exception {
        try (Connection conn = DatabaseConfig.getConnection()) {
            int userId = getUserIdByEmail(userEmail, conn);
            
            if (userId == -1) {
                throw new Exception("User not found: " + userEmail);
            }
            
            String query = "DELETE FROM watchlist WHERE user_id = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, userId);
                
                int rowsAffected = stmt.executeUpdate();
                System.out.println("✅ Cleared watchlist for user: " + userEmail + " (" + rowsAffected + " items removed)");
            }
        } catch (SQLException e) {
            throw new Exception("Database error while clearing watchlist: " + e.getMessage(), e);
        }
    }
    
    // Helper methods
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
    
    private int getContentIdByTitle(String title, Connection conn) throws SQLException {
        String query = "SELECT id FROM content WHERE title = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, title);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        }
        return -1;
    }
    
    /**
     * Get watchlist as JSON string (used by WatchlistPageController for backward compatibility)
     */
    public String getWatchlistJson(String userEmail) throws Exception {
        try (Connection conn = DatabaseConfig.getConnection()) {
            int userId = getUserIdByEmail(userEmail, conn);
            if (userId == -1) return null;
            
            String query = """
                SELECT c.title, c.mood, c.type, c.link, c.description, c.image_url 
                FROM watchlist w 
                JOIN content c ON w.content_id = c.id 
                WHERE w.user_id = ? 
                ORDER BY w.added_at DESC
            """;
            
            JSONObject result = new JSONObject();
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, userId);
                try (ResultSet rs = stmt.executeQuery()) {
                    int idx = 0;
                    while (rs.next()) {
                        JSONObject item = new JSONObject();
                        item.put("title", rs.getString("title") != null ? rs.getString("title") : "");
                        item.put("mood", rs.getString("mood") != null ? rs.getString("mood") : "");
                        item.put("type", rs.getString("type") != null ? rs.getString("type") : "");
                        item.put("link", rs.getString("link") != null ? rs.getString("link") : "");
                        item.put("description", rs.getString("description") != null ? rs.getString("description") : "");
                        item.put("imageUrl", rs.getString("image_url") != null ? rs.getString("image_url") : "");
                        result.put("item_" + idx, item);
                        idx++;
                    }
                }
            }
            return result.length() > 0 ? result.toString() : null;
        } catch (SQLException e) {
            throw new Exception("Database error while fetching watchlist JSON: " + e.getMessage(), e);
        }
    }
}