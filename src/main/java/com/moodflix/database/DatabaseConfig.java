package com.moodflix.database;

import com.moodflix.config.AppConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;

public final class DatabaseConfig {

    private static HikariDataSource dataSource = null;
    private static Connection mockConnection = null;

    private DatabaseConfig() {
        // Utility class
    }

    public static void setMockConnection(Connection conn) {
        mockConnection = conn;
    }

    private static synchronized HikariDataSource getDataSource() {
        if (dataSource == null) {
            dataSource = createDataSource();
        }
        return dataSource;
    }

    private static HikariDataSource createDataSource() {
        AppConfig.load();

        String host = AppConfig.getDbHost();
        String port = AppConfig.getDbPort();
        String dbName = AppConfig.getDbName();
        String user = AppConfig.getDbUser();
        String password = AppConfig.getDbPassword();

        String jdbcUrl = String.format("jdbc:postgresql://%s:%s/%s", host, port, dbName);

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(user);
        config.setPassword(password != null ? password : "");
        config.setPoolName("MoodFlixPool");
        config.setMaximumPoolSize(20);
        config.setMinimumIdle(5);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        config.setConnectionTestQuery("SELECT 1");
        config.setValidationTimeout(5000);
        config.setLeakDetectionThreshold(10000);

        return new HikariDataSource(config);
    }

    public static Connection getConnection() throws SQLException {
        if (mockConnection != null) {
            return mockConnection;
        }
        return getDataSource().getConnection();
    }

    public static void closeDataSource() {
        if (mockConnection != null) {
            return;
        }
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            dataSource = null;
        }
    }

    public static boolean isConfigured() {
        if (mockConnection != null) {
            return true;
        }
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    public static void validateConfiguration() {
        if (mockConnection != null) {
            return;
        }
        if (AppConfig.getDbPassword().isBlank()) {
            System.err.println("⚠️  Database password is not configured. Set MOODFLIX_DB_PASSWORD or db.password in application.properties.");
        }

        if (!isConfigured()) {
            System.err.println("DATABASE CONFIGURATION ERROR!");
            System.err.println("Please verify your PostgreSQL settings in moodflix/src/main/resources/application.properties or via environment variables.");
            System.err.println("Required values:");
            System.err.println("  MOODFLIX_DB_HOST=" + AppConfig.getDbHost());
            System.err.println("  MOODFLIX_DB_PORT=" + AppConfig.getDbPort());
            System.err.println("  MOODFLIX_DB_NAME=" + AppConfig.getDbName());
            System.err.println("  MOODFLIX_DB_USER=" + AppConfig.getDbUser());
            System.err.println("  MOODFLIX_DB_PASSWORD=<hidden>");
            throw new RuntimeException("Database not configured properly.");
        }
    }
}
