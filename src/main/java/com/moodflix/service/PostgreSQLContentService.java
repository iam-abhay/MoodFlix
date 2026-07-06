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

public class PostgreSQLContentService {
    
    public void uploadContent(Content content) throws Exception {
        System.out.println("📤 Uploading content: " + content.getTitle());
        
        try (Connection conn = DatabaseConfig.getConnection()) {
            String query = """
                INSERT INTO content (title, mood, type, link, description, image_url) 
                VALUES (?, ?, ?, ?, ?, ?)
            """;
            
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, content.getTitle());
                stmt.setString(2, content.getMood());
                stmt.setString(3, content.getType());
                stmt.setString(4, content.getLink());
                stmt.setString(5, content.getDescription());
                stmt.setString(6, content.getImageUrl());
                
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("✅ Content uploaded successfully: " + content.getTitle());
                } else {
                    throw new Exception("Failed to upload content");
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Failed to upload content: " + e.getMessage());
            throw new Exception("Database error during content upload: " + e.getMessage(), e);
        }
    }
    
    public List<Content> getAllContent() throws Exception {
        List<Content> contentList = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection()) {
            String query = "SELECT title, mood, type, link, description, image_url FROM content ORDER BY created_at DESC";
            
            try (PreparedStatement stmt = conn.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {
                
                while (rs.next()) {
                    Content content = new Content(
                        rs.getString("title"),
                        rs.getString("mood"),
                        rs.getString("type"),
                        rs.getString("link"),
                        rs.getString("description"),
                        rs.getString("image_url")
                    );
                    contentList.add(content);
                }
            }
        } catch (SQLException e) {
            throw new Exception("Database error while fetching content: " + e.getMessage(), e);
        }
        
        return contentList;
    }
    
    public List<Content> getContentByMood(String mood) throws Exception {
        List<Content> contentList = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection()) {
            String query = "SELECT title, mood, type, link, description, image_url FROM content WHERE mood = ? ORDER BY created_at DESC";
            
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, mood);
                
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
                        contentList.add(content);
                    }
                }
            }
        } catch (SQLException e) {
            throw new Exception("Database error while fetching content by mood: " + e.getMessage(), e);
        }
        
        return contentList;
    }
    
    public List<Content> getContentByType(String type) throws Exception {
        List<Content> contentList = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection()) {
            String query = "SELECT title, mood, type, link, description, image_url FROM content WHERE type = ? ORDER BY created_at DESC";
            
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, type);
                
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
                        contentList.add(content);
                    }
                }
            }
        } catch (SQLException e) {
            throw new Exception("Database error while fetching content by type: " + e.getMessage(), e);
        }
        
        return contentList;
    }
    
    public List<Content> searchContent(String searchTerm) throws Exception {
        List<Content> contentList = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection()) {
            String query = """
                SELECT title, mood, type, link, description, image_url FROM content 
                WHERE title ILIKE ? OR description ILIKE ? 
                ORDER BY created_at DESC
            """;
            
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                String searchPattern = "%" + searchTerm + "%";
                stmt.setString(1, searchPattern);
                stmt.setString(2, searchPattern);
                
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
                        contentList.add(content);
                    }
                }
            }
        } catch (SQLException e) {
            throw new Exception("Database error while searching content: " + e.getMessage(), e);
        }
        
        return contentList;
    }
    
    public boolean deleteContent(String title) throws Exception {
        try (Connection conn = DatabaseConfig.getConnection()) {
            String query = "DELETE FROM content WHERE title = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, title);
                
                int rowsAffected = stmt.executeUpdate();
                return rowsAffected > 0;
            }
        } catch (SQLException e) {
            throw new Exception("Database error while deleting content: " + e.getMessage(), e);
        }
    }
    
    public Content getContentByTitle(String title) throws Exception {
        try (Connection conn = DatabaseConfig.getConnection()) {
            String query = "SELECT title, mood, type, link, description, image_url FROM content WHERE title = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, title);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return new Content(
                            rs.getString("title"),
                            rs.getString("mood"),
                            rs.getString("type"),
                            rs.getString("link"),
                            rs.getString("description"),
                            rs.getString("image_url")
                        );
                    }
                }
            }
        } catch (SQLException e) {
            throw new Exception("Database error while fetching content: " + e.getMessage(), e);
        }
        
        return null;
    }
    
    public int getContentCount() throws Exception {
        try (Connection conn = DatabaseConfig.getConnection()) {
            String query = "SELECT COUNT(*) as count FROM content";
            
            try (PreparedStatement stmt = conn.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {
                
                if (rs.next()) {
                    return rs.getInt("count");
                }
            }
        } catch (SQLException e) {
            throw new Exception("Database error while counting content: " + e.getMessage(), e);
        }
        
        return 0;
    }
    
    /**
     * Get all content as JSON string (compatible with old FirebaseContentService.getAllContentJson)
     */
    public String getAllContentJson() throws Exception {
        JSONObject allContent = new JSONObject();
        try (Connection conn = DatabaseConfig.getConnection()) {
            String query = "SELECT id, title, mood, type, link, description, image_url FROM content ORDER BY created_at DESC";
            try (PreparedStatement stmt = conn.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    JSONObject item = new JSONObject();
                    item.put("title", rs.getString("title"));
                    item.put("mood", rs.getString("mood"));
                    item.put("type", rs.getString("type"));
                    item.put("link", rs.getString("link") != null ? rs.getString("link") : "");
                    item.put("description", rs.getString("description") != null ? rs.getString("description") : "");
                    item.put("imageUrl", rs.getString("image_url") != null ? rs.getString("image_url") : "");
                    allContent.put("content_" + rs.getInt("id"), item);
                }
            }
        }
        if (allContent.isEmpty()) return null;
        return allContent.toString();
    }
    
    /**
     * Update content by title key (compatible with old FirebaseContentService.updateContent)
     */
    public void updateContent(String key, Content content) throws Exception {
        try (Connection conn = DatabaseConfig.getConnection()) {
            // key could be "content_ID" format or just the title
            String query;
            PreparedStatement stmt;
            if (key.startsWith("content_")) {
                try {
                    int id = Integer.parseInt(key.replace("content_", ""));
                    query = "UPDATE content SET title = ?, mood = ?, type = ?, link = ?, description = ?, image_url = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
                    stmt = conn.prepareStatement(query);
                    stmt.setString(1, content.getTitle());
                    stmt.setString(2, content.getMood());
                    stmt.setString(3, content.getType());
                    stmt.setString(4, content.getLink());
                    stmt.setString(5, content.getDescription());
                    stmt.setString(6, content.getImageUrl());
                    stmt.setInt(7, id);
                } catch (NumberFormatException e) {
                    // Fallback to title-based update
                    query = "UPDATE content SET mood = ?, type = ?, link = ?, description = ?, image_url = ?, updated_at = CURRENT_TIMESTAMP WHERE title = ?";
                    stmt = conn.prepareStatement(query);
                    stmt.setString(1, content.getMood());
                    stmt.setString(2, content.getType());
                    stmt.setString(3, content.getLink());
                    stmt.setString(4, content.getDescription());
                    stmt.setString(5, content.getImageUrl());
                    stmt.setString(6, content.getTitle());
                }
            } else {
                query = "UPDATE content SET mood = ?, type = ?, link = ?, description = ?, image_url = ?, updated_at = CURRENT_TIMESTAMP WHERE title = ?";
                stmt = conn.prepareStatement(query);
                stmt.setString(1, content.getMood());
                stmt.setString(2, content.getType());
                stmt.setString(3, content.getLink());
                stmt.setString(4, content.getDescription());
                stmt.setString(5, content.getImageUrl());
                stmt.setString(6, content.getTitle());
            }
            stmt.executeUpdate();
            stmt.close();
            System.out.println("✅ Content updated: " + content.getTitle());
        }
    }
    
    /**
     * Get filtered content by mood and/or type (compatible with old FirebaseContentService.getFilteredContent)
     * Returns JSON string
     */
    public String getFilteredContent(String mood, String type) throws Exception {
        JSONObject filtered = new JSONObject();
        try (Connection conn = DatabaseConfig.getConnection()) {
            StringBuilder queryBuilder = new StringBuilder("SELECT id, title, mood, type, link, description, image_url FROM content WHERE 1=1");
            List<String> params = new ArrayList<>();
            
            if (mood != null && !mood.isEmpty()) {
                queryBuilder.append(" AND mood = ?");
                params.add(mood);
            }
            if (type != null && !type.isEmpty()) {
                queryBuilder.append(" AND type = ?");
                params.add(type);
            }
            queryBuilder.append(" ORDER BY created_at DESC");
            
            try (PreparedStatement stmt = conn.prepareStatement(queryBuilder.toString())) {
                for (int i = 0; i < params.size(); i++) {
                    stmt.setString(i + 1, params.get(i));
                }
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    JSONObject item = new JSONObject();
                    item.put("title", rs.getString("title"));
                    item.put("mood", rs.getString("mood"));
                    item.put("type", rs.getString("type"));
                    item.put("link", rs.getString("link") != null ? rs.getString("link") : "");
                    item.put("description", rs.getString("description") != null ? rs.getString("description") : "");
                    item.put("imageUrl", rs.getString("image_url") != null ? rs.getString("image_url") : "");
                    filtered.put("content_" + rs.getInt("id"), item);
                }
            }
        }
        if (filtered.isEmpty()) return null;
        return filtered.toString();
    }
    
    /**
     * Delete content by ID key
     */
    public void deleteContentByKey(String key) throws Exception {
        try (Connection conn = DatabaseConfig.getConnection()) {
            if (key.startsWith("content_")) {
                try {
                    int id = Integer.parseInt(key.replace("content_", ""));
                    String query = "DELETE FROM content WHERE id = ?";
                    try (PreparedStatement stmt = conn.prepareStatement(query)) {
                        stmt.setInt(1, id);
                        stmt.executeUpdate();
                    }
                    return;
                } catch (NumberFormatException ignored) {}
            }
            // Fallback to title-based delete
            deleteContent(key);
        }
    }
    
    /**
     * Invalidate cache - no-op for PostgreSQL
     */
    public static void invalidateCache() {
        System.out.println("[PostgreSQL] Content cache invalidated (no-op for direct DB access)");
    }
    
    /**
     * Get filtered content as List<Content> (used by UserDashboardController)
     */
    public List<Content> getFilteredContentList(String mood, String type) throws Exception {
        if (mood != null && !mood.isEmpty() && (type == null || type.isEmpty())) {
            return getContentByMood(mood);
        } else if ((mood == null || mood.isEmpty()) && type != null && !type.isEmpty()) {
            return getContentByType(type);
        } else if (mood != null && !mood.isEmpty() && type != null && !type.isEmpty()) {
            // Both mood and type specified
            List<Content> contentList = new ArrayList<>();
            try (Connection conn = DatabaseConfig.getConnection()) {
                String query = "SELECT title, mood, type, link, description, image_url FROM content WHERE mood = ? AND type = ? ORDER BY created_at DESC";
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setString(1, mood);
                    stmt.setString(2, type);
                    try (ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            contentList.add(new Content(
                                rs.getString("title"), rs.getString("mood"), rs.getString("type"),
                                rs.getString("link"), rs.getString("description"), rs.getString("image_url")
                            ));
                        }
                    }
                }
            }
            return contentList;
        } else {
            return getAllContent();
        }
    }
}