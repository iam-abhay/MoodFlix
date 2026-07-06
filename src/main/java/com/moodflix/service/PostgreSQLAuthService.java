package com.moodflix.service;

import com.moodflix.database.DatabaseConfig;
import com.moodflix.model.User;
import org.mindrot.jbcrypt.BCrypt;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PostgreSQLAuthService {
    
    public JSONObject signup(String email, String password) throws Exception {
        return signup(email, password, "user");
    }
    
    public JSONObject signup(String email, String password, String role) throws Exception {
        try (Connection conn = DatabaseConfig.getConnection()) {
            // Check if user already exists
            String checkQuery = "SELECT email FROM users WHERE email = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                checkStmt.setString(1, email);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    throw new RuntimeException("User with this email already exists");
                }
            }
            
            // Hash password
            String passwordHash = BCrypt.hashpw(password, BCrypt.gensalt());
            
            // Insert new user
            String insertQuery = "INSERT INTO users (email, password_hash, role) VALUES (?, ?, ?) RETURNING id";
            try (PreparedStatement stmt = conn.prepareStatement(insertQuery)) {
                stmt.setString(1, email);
                stmt.setString(2, passwordHash);
                stmt.setString(3, role);
                
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    int userId = rs.getInt("id");
                    
                    // Return success response similar to Firebase
                    JSONObject response = new JSONObject();
                    response.put("localId", String.valueOf(userId));
                    response.put("email", email);
                    response.put("displayName", "");
                    response.put("idToken", generateSimpleToken(userId, email));
                    response.put("registered", true);
                    
                    return response;
                }
            }
            
            throw new RuntimeException("Failed to create user");
            
        } catch (SQLException e) {
            throw new Exception("Database error during signup: " + e.getMessage(), e);
        }
    }
    
    public JSONObject login(String email, String password) throws Exception {
        try (Connection conn = DatabaseConfig.getConnection()) {
            String query = "SELECT id, email, password_hash, role FROM users WHERE email = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, email);
                
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    String storedHash = rs.getString("password_hash");
                    
                    if (BCrypt.checkpw(password, storedHash)) {
                        int userId = rs.getInt("id");
                        String role = rs.getString("role");
                        
                        // Return success response similar to Firebase
                        JSONObject response = new JSONObject();
                        response.put("localId", String.valueOf(userId));
                        response.put("email", email);
                        response.put("displayName", "");
                        response.put("idToken", generateSimpleToken(userId, email));
                        response.put("registered", true);
                        response.put("role", role);
                        
                        return response;
                    } else {
                        throw new RuntimeException("Invalid password");
                    }
                } else {
                    throw new RuntimeException("User not found");
                }
            }
        } catch (SQLException e) {
            throw new Exception("Database error during login: " + e.getMessage(), e);
        }
    }
    
    public User getUserByEmail(String email) throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection()) {
            String query = "SELECT id, email, role, profile_photo_url FROM users WHERE email = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, email);
                
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    User user = new User(rs.getString("email"), rs.getString("role"));
                    user.setProfilePhotoUrl(rs.getString("profile_photo_url"));
                    
                    // Get friends list
                    int userId = rs.getInt("id");
                    List<String> friends = getFriends(userId);
                    // Note: The User class might need to be updated to support setting friends
                    
                    return user;
                }
            }
        }
        return null;
    }
    
    public boolean updateProfile(String email, String profilePhotoUrl) throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection()) {
            String query = "UPDATE users SET profile_photo_url = ?, updated_at = CURRENT_TIMESTAMP WHERE email = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, profilePhotoUrl);
                stmt.setString(2, email);
                
                return stmt.executeUpdate() > 0;
            }
        }
    }
    
    public List<String> getFriends(int userId) throws SQLException {
        List<String> friends = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection()) {
            String query = """
                SELECT u.email FROM friends f 
                JOIN users u ON f.friend_id = u.id 
                WHERE f.user_id = ?
            """;
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, userId);
                
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    friends.add(rs.getString("email"));
                }
            }
        }
        return friends;
    }
    
    public boolean addFriend(String userEmail, String friendEmail) throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection()) {
            // Get user IDs
            String getUserIdQuery = "SELECT id FROM users WHERE email = ?";
            int userId = -1, friendId = -1;
            
            try (PreparedStatement stmt = conn.prepareStatement(getUserIdQuery)) {
                stmt.setString(1, userEmail);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) userId = rs.getInt("id");
                
                stmt.setString(1, friendEmail);
                rs = stmt.executeQuery();
                if (rs.next()) friendId = rs.getInt("id");
            }
            
            if (userId == -1 || friendId == -1) {
                return false;
            }
            
            // Add friendship (bidirectional)
            String insertQuery = "INSERT INTO friends (user_id, friend_id) VALUES (?, ?) ON CONFLICT DO NOTHING";
            try (PreparedStatement stmt = conn.prepareStatement(insertQuery)) {
                stmt.setInt(1, userId);
                stmt.setInt(2, friendId);
                stmt.executeUpdate();
                
                stmt.setInt(1, friendId);
                stmt.setInt(2, userId);
                stmt.executeUpdate();
                
                return true;
            }
        }
    }
    
    // Simple token generation (in production, use proper JWT)
    private String generateSimpleToken(int userId, String email) {
        return "token_" + userId + "_" + System.currentTimeMillis();
    }
    
    private static final ExecutorService asyncExecutor = Executors.newFixedThreadPool(3);
    
    /**
     * Get user details as JSONObject (compatible with old FirebaseService.getUserDetails)
     */
    public JSONObject getUserDetails(String email) {
        try (Connection conn = DatabaseConfig.getConnection()) {
            String query = "SELECT id, email, role, display_name, full_name, age, gender, profile_photo_url, created_at FROM users WHERE email = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, email);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    JSONObject user = new JSONObject();
                    user.put("email", rs.getString("email"));
                    user.put("role", rs.getString("role"));
                    user.put("displayName", rs.getString("display_name") != null ? rs.getString("display_name") : "");
                    user.put("fullName", rs.getString("full_name") != null ? rs.getString("full_name") : "");
                    user.put("age", rs.getString("age") != null ? rs.getString("age") : "");
                    user.put("gender", rs.getString("gender") != null ? rs.getString("gender") : "");
                    user.put("profilePicUrl", rs.getString("profile_photo_url") != null ? rs.getString("profile_photo_url") : "");
                    if (rs.getTimestamp("created_at") != null) {
                        user.put("createdAt", rs.getTimestamp("created_at").getTime());
                    }
                    user.put("userType", rs.getString("role"));
                    return user;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting user details: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Async version of getUserDetails
     */
    public CompletableFuture<JSONObject> getUserDetailsAsync(String email) {
        return CompletableFuture.supplyAsync(() -> getUserDetails(email), asyncExecutor);
    }
    
    /**
     * Login async - returns CompletableFuture<Boolean>
     */
    public CompletableFuture<Boolean> loginUserAsync(String email, String password) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                login(email, password);
                return true;
            } catch (Exception e) {
                System.err.println("Async login failed: " + e.getMessage());
                return false;
            }
        }, asyncExecutor);
    }
    
    /**
     * Get all users as JSONObject (used by AdminUserManagement)
     */
    public JSONObject getAllUsers() {
        JSONObject allUsers = new JSONObject();
        try (Connection conn = DatabaseConfig.getConnection()) {
            String query = "SELECT email, role, display_name, full_name, age, gender, profile_photo_url, created_at FROM users ORDER BY created_at DESC";
            try (PreparedStatement stmt = conn.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String email = rs.getString("email");
                    String safeEmail = email.replace(".", "_dot_").replace("@", "_at_");
                    JSONObject user = new JSONObject();
                    user.put("email", email);
                    user.put("role", rs.getString("role"));
                    user.put("displayName", rs.getString("display_name") != null ? rs.getString("display_name") : "");
                    user.put("fullName", rs.getString("full_name") != null ? rs.getString("full_name") : "");
                    user.put("age", rs.getString("age") != null ? rs.getString("age") : "");
                    user.put("gender", rs.getString("gender") != null ? rs.getString("gender") : "");
                    user.put("profilePicUrl", rs.getString("profile_photo_url") != null ? rs.getString("profile_photo_url") : "");
                    if (rs.getTimestamp("created_at") != null) {
                        user.put("createdAt", rs.getTimestamp("created_at").getTime());
                    }
                    allUsers.put(safeEmail, user);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting all users: " + e.getMessage());
        }
        return allUsers;
    }
    
    /**
     * Update user role
     */
    public void updateUserRole(String email, String newRole) throws Exception {
        try (Connection conn = DatabaseConfig.getConnection()) {
            String query = "UPDATE users SET role = ?, updated_at = CURRENT_TIMESTAMP WHERE email = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, newRole);
                stmt.setString(2, email);
                stmt.executeUpdate();
            }
        }
    }
    
    /**
     * Update user details (used by AdminUserManagement)
     */
    public void updateUserDetails(String email, String fullName, String age, String gender, String role) throws Exception {
        try (Connection conn = DatabaseConfig.getConnection()) {
            String query = "UPDATE users SET full_name = ?, age = ?, gender = ?, role = ?, updated_at = CURRENT_TIMESTAMP WHERE email = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, fullName);
                stmt.setString(2, age);
                stmt.setString(3, gender);
                stmt.setString(4, role);
                stmt.setString(5, email);
                stmt.executeUpdate();
            }
        }
    }
    
    /**
     * Update admin profile with PUT (display name, photo, age, gender)
     */
    public void updateAdminProfileWithPut(String email, String displayName, String picUrl, String age, String gender) throws Exception {
        try (Connection conn = DatabaseConfig.getConnection()) {
            String query = "UPDATE users SET display_name = ?, profile_photo_url = ?, age = ?, gender = ?, updated_at = CURRENT_TIMESTAMP WHERE email = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, displayName);
                stmt.setString(2, picUrl);
                stmt.setString(3, age);
                stmt.setString(4, gender);
                stmt.setString(5, email);
                stmt.executeUpdate();
            }
        }
    }
    
    /**
     * Delete user
     */
    public void deleteUser(String email) throws Exception {
        try (Connection conn = DatabaseConfig.getConnection()) {
            String query = "DELETE FROM users WHERE email = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, email);
                stmt.executeUpdate();
            }
        }
    }
    
    /**
     * Save feedback (used by FeedbackPageController)
     */
    public void saveFeedback(String email, String feedback, int rating) throws Exception {
        try (Connection conn = DatabaseConfig.getConnection()) {
            // Get user id
            int userId = getUserIdByEmail(conn, email);
            
            String query = "INSERT INTO feedback (user_id, message, rating) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, userId);
                stmt.setString(2, feedback);
                stmt.setInt(3, rating);
                stmt.executeUpdate();
            }
        }
    }
    
    /**
     * Get user activity as JSONObject (used by ActivityHistoryController)
     */
    public JSONObject getUserActivity(String email) {
        JSONObject activities = new JSONObject();
        try (Connection conn = DatabaseConfig.getConnection()) {
            int userId = getUserIdByEmail(conn, email);
            if (userId == -1) return activities;
            
            String query = "SELECT id, title, mood, type, activity_date, duration, rating FROM activities WHERE user_id = ? ORDER BY activity_date DESC";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, userId);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    JSONObject activity = new JSONObject();
                    activity.put("title", rs.getString("title"));
                    activity.put("mood", rs.getString("mood") != null ? rs.getString("mood") : "");
                    activity.put("type", rs.getString("type") != null ? rs.getString("type") : "");
                    activity.put("timestamp", rs.getTimestamp("activity_date").getTime());
                    activity.put("duration", rs.getInt("duration"));
                    activity.put("rating", rs.getInt("rating"));
                    activities.put("activity_" + rs.getInt("id"), activity);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting user activity: " + e.getMessage());
        }
        return activities;
    }
    
    /**
     * Log user activity
     */
    public void logActivity(String email, String title, String mood, String type) throws Exception {
        try (Connection conn = DatabaseConfig.getConnection()) {
            int userId = getUserIdByEmail(conn, email);
            String query = "INSERT INTO activities (user_id, title, mood, type, activity_date) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, userId);
                stmt.setString(2, title);
                stmt.setString(3, mood);
                stmt.setString(4, type);
                stmt.executeUpdate();
            }
        }
    }
    
    /**
     * Upload profile image - stores local file path as URL
     */
    public String uploadProfileImage(java.io.File file, String destFileName) {
        try {
            // For local PostgreSQL, store the file path as URL
            String localUrl = file.toURI().toString();
            return localUrl;
        } catch (Exception e) {
            System.err.println("Error handling profile image: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Get user ID by email (helper)
     */
    private int getUserIdByEmail(Connection conn, String email) throws SQLException {
        String query = "SELECT id FROM users WHERE email = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        }
        return -1;
    }
    
    /**
     * Clear cache - no-op for PostgreSQL (no cache to clear)
     */
    public static void clearCache() {
        System.out.println("[PostgreSQL] Cache cleared (no-op for direct DB access)");
    }
}