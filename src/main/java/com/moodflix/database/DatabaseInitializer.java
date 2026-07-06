package com.moodflix.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {
    
    public static void createTables() {
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Create users table
            String createUsersTable = """
                CREATE TABLE IF NOT EXISTS users (
                    id SERIAL PRIMARY KEY,
                    email VARCHAR(255) UNIQUE NOT NULL,
                    password_hash VARCHAR(255) NOT NULL,
                    role VARCHAR(50) NOT NULL DEFAULT 'user',
                    display_name VARCHAR(255),
                    full_name VARCHAR(255),
                    age VARCHAR(10),
                    gender VARCHAR(20),
                    profile_photo_url TEXT,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """;
            stmt.execute(createUsersTable);
            
            // Add columns if they don't exist (for existing databases)
            String[] alterQueries = {
                "ALTER TABLE users ADD COLUMN IF NOT EXISTS display_name VARCHAR(255)",
                "ALTER TABLE users ADD COLUMN IF NOT EXISTS full_name VARCHAR(255)",
                "ALTER TABLE users ADD COLUMN IF NOT EXISTS age VARCHAR(10)",
                "ALTER TABLE users ADD COLUMN IF NOT EXISTS gender VARCHAR(20)"
            };
            for (String alterQuery : alterQueries) {
                try { stmt.execute(alterQuery); } catch (Exception ignored) {}
            }
            
            // Create content table
            String createContentTable = """
                CREATE TABLE IF NOT EXISTS content (
                    id SERIAL PRIMARY KEY,
                    title VARCHAR(500) NOT NULL,
                    mood VARCHAR(100) NOT NULL,
                    type VARCHAR(50) NOT NULL,
                    link TEXT,
                    description TEXT,
                    image_url TEXT,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """;
            stmt.execute(createContentTable);
            
            // Create activities table
            String createActivitiesTable = """
                CREATE TABLE IF NOT EXISTS activities (
                    id SERIAL PRIMARY KEY,
                    user_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
                    title VARCHAR(500) NOT NULL,
                    mood VARCHAR(100),
                    type VARCHAR(50),
                    activity_date TIMESTAMP NOT NULL,
                    duration INTEGER DEFAULT 0,
                    rating INTEGER CHECK (rating >= 1 AND rating <= 5),
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """;
            stmt.execute(createActivitiesTable);
            
            // Create feedback table
            String createFeedbackTable = """
                CREATE TABLE IF NOT EXISTS feedback (
                    id SERIAL PRIMARY KEY,
                    user_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
                    message TEXT NOT NULL,
                    rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """;
            stmt.execute(createFeedbackTable);
            
            // Create mood_entries table
            String createMoodEntriesTable = """
                CREATE TABLE IF NOT EXISTS mood_entries (
                    id SERIAL PRIMARY KEY,
                    user_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
                    mood VARCHAR(100) NOT NULL,
                    entry_timestamp TIMESTAMP NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """;
            stmt.execute(createMoodEntriesTable);
            
            // Create watchlist table
            String createWatchlistTable = """
                CREATE TABLE IF NOT EXISTS watchlist (
                    id SERIAL PRIMARY KEY,
                    user_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
                    content_id INTEGER REFERENCES content(id) ON DELETE CASCADE,
                    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    UNIQUE(user_id, content_id)
                )
            """;
            stmt.execute(createWatchlistTable);
            
            // Create friends table for user relationships
            String createFriendsTable = """
                CREATE TABLE IF NOT EXISTS friends (
                    id SERIAL PRIMARY KEY,
                    user_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
                    friend_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    UNIQUE(user_id, friend_id)
                )
            """;
            stmt.execute(createFriendsTable);
            
            // Create indexes for better performance
            String[] indexes = {
                "CREATE INDEX IF NOT EXISTS idx_users_email ON users(email)",
                "CREATE INDEX IF NOT EXISTS idx_content_title ON content(title)",
                "CREATE INDEX IF NOT EXISTS idx_content_mood ON content(mood)",
                "CREATE INDEX IF NOT EXISTS idx_content_type ON content(type)",
                "CREATE INDEX IF NOT EXISTS idx_content_mood_type ON content(mood, type)",
                "CREATE INDEX IF NOT EXISTS idx_activities_user_id ON activities(user_id)",
                "CREATE INDEX IF NOT EXISTS idx_activities_date ON activities(activity_date)",
                "CREATE INDEX IF NOT EXISTS idx_activities_analytics ON activities(user_id, mood, type)",
                "CREATE INDEX IF NOT EXISTS idx_watchlist_user_id ON watchlist(user_id)",
                "CREATE INDEX IF NOT EXISTS idx_mood_entries_user_id ON mood_entries(user_id)"
            };
            
            for (String indexQuery : indexes) {
                stmt.execute(indexQuery);
            }
            
            System.out.println("✅ Database tables created successfully!");
            
        } catch (SQLException e) {
            System.err.println("❌ Failed to create database tables: " + e.getMessage());
            throw new RuntimeException("Database setup failed", e);
        }
    }
    
    public static void initializeDatabase() {
        System.out.println("🔧 Initializing PostgreSQL database...");
        DatabaseConfig.validateConfiguration();
        createTables();
        System.out.println("✅ Database initialization completed!");
    }
}