package com.moodflix.util;

import javafx.scene.image.Image;
import java.io.InputStream;
import java.util.concurrent.ConcurrentHashMap;

/**
 * High-performance central image cache for MoodFlix.
 * Eliminates duplicate network requests, prevents JavaFX Application Thread blocking
 * via background loading, and minimizes memory footprint.
 */
public final class ImageCache {

    private static final ConcurrentHashMap<String, Image> cache = new ConcurrentHashMap<>();

    private ImageCache() {
        // Utility class
    }

    /**
     * Get image from cache or load it in the background if not present.
     */
    public static Image getImage(String url) {
        if (url == null || url.trim().isEmpty()) {
            return null;
        }
        return cache.computeIfAbsent(url, u -> {
            try {
                if (u.startsWith("http://") || u.startsWith("https://")) {
                    // Load in background
                    return new Image(u, true);
                } else {
                    // Local classpath resources
                    InputStream stream = ImageCache.class.getResourceAsStream(u);
                    if (stream != null) {
                        return new Image(stream);
                    }
                }
            } catch (Exception e) {
                System.err.println("[IMAGE-CACHE] Failed to load image: " + u + " (" + e.getMessage() + ")");
            }
            return null;
        });
    }

    /**
     * Get resized image from cache or load it in background.
     */
    public static Image getImage(String url, double requestedWidth, double requestedHeight, boolean preserveRatio, boolean smooth) {
        if (url == null || url.trim().isEmpty()) {
            return null;
        }
        String cacheKey = String.format("%s_%f_%f_%b_%b", url, requestedWidth, requestedHeight, preserveRatio, smooth);
        
        return cache.computeIfAbsent(cacheKey, key -> {
            try {
                if (url.startsWith("http://") || url.startsWith("https://")) {
                    // Load in background with requested sizing
                    return new Image(url, requestedWidth, requestedHeight, preserveRatio, smooth, true);
                } else {
                    // Local resource
                    InputStream stream = ImageCache.class.getResourceAsStream(url);
                    if (stream != null) {
                        return new Image(stream, requestedWidth, requestedHeight, preserveRatio, smooth);
                    }
                }
            } catch (Exception e) {
                System.err.println("[IMAGE-CACHE] Failed to load sized image: " + url + " (" + e.getMessage() + ")");
            }
            return null;
        });
    }

    /**
     * Clear all cached images to reclaim memory.
     */
    public static void clear() {
        cache.clear();
        System.out.println("[IMAGE-CACHE] Cleared all cached images.");
    }
}
