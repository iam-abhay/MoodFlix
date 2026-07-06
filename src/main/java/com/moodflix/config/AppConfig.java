package com.moodflix.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Central configuration loaded from application.properties with environment-variable overrides.
 */
public final class AppConfig {

    private static final Properties PROPERTIES = new Properties();
    private static volatile boolean loaded;

    private AppConfig() {
    }

    public static void load() {
        if (loaded) {
            return;
        }
        synchronized (AppConfig.class) {
            if (loaded) {
                return;
            }
            try (InputStream in = AppConfig.class.getClassLoader().getResourceAsStream("application.properties")) {
                if (in != null) {
                    PROPERTIES.load(in);
                }
            } catch (IOException e) {
                System.err.println("Warning: could not load application.properties — using defaults and env vars.");
            }
            loaded = true;
        }
    }

    public static String get(String key, String defaultValue) {
        load();
        String envKey = toEnvKey(key);
        String envValue = System.getenv(envKey);
        if (envValue != null && !envValue.isBlank()) {
            return envValue.trim();
        }
        return PROPERTIES.getProperty(key, defaultValue);
    }

    public static int getInt(String key, int defaultValue) {
        String value = get(key, String.valueOf(defaultValue));
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        return Boolean.parseBoolean(get(key, String.valueOf(defaultValue)));
    }

    public static String getDbHost() {
        return get("db.host", "localhost");
    }

    public static String getDbPort() {
        return get("db.port", "5432");
    }

    public static String getDbName() {
        return get("db.name", "moodflix");
    }

    public static String getDbUser() {
        return get("db.user", "postgres");
    }

    public static String getDbPassword() {
        return get("db.password", "");
    }

    public static String getOmdbApiKey() {
        return get("api.omdb.key", "");
    }

    public static String getUnsplashApiKey() {
        return get("api.unsplash.key", "");
    }

    public static boolean isSeedDemoDataEnabled() {
        return getBoolean("app.seed.demo.data", true);
    }

    public static boolean isDevMode() {
        return getBoolean("app.dev.mode", false);
    }

    public static int getConnectTimeoutMs() {
        return getInt("http.connect.timeout.ms", 5000);
    }

    public static int getReadTimeoutMs() {
        return getInt("http.read.timeout.ms", 8000);
    }

    public static int getMaxRetries() {
        return getInt("http.max.retries", 2);
    }

    private static String toEnvKey(String propertyKey) {
        return "MOODFLIX_" + propertyKey.replace('.', '_').replace('-', '_').toUpperCase();
    }
}
