package com.moodflix.util;

import com.moodflix.view.UserDashboard;
import com.moodflix.view.AdminDashboard;
import com.moodflix.controller.UserDashboardController;
import com.moodflix.controller.AdminDashboardController;
import javafx.scene.Scene;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Navigation cache to store dashboard instances and avoid recreation
 */
public class NavigationCache {
    
    private static final ConcurrentHashMap<String, CachedDashboard> dashboardCache = new ConcurrentHashMap<>();
    private static final long CACHE_TTL_MS = 10 * 60 * 1000; // 10 minutes
    
    private static class CachedDashboard {
        final Scene scene;
        final long timestamp;
        final String userEmail;
        
        CachedDashboard(Scene scene, String userEmail) {
            this.scene = scene;
            this.timestamp = System.currentTimeMillis();
            this.userEmail = userEmail;
        }
        
        boolean isExpired() {
            return (System.currentTimeMillis() - timestamp) > CACHE_TTL_MS;
        }
        
        boolean isForUser(String email) {
            return userEmail != null && userEmail.equals(email);
        }
    }
    
    /**
     * Get cached user dashboard or create new one
     */
    public static Scene getUserDashboardScene(String userEmail) {
        String cacheKey = "user_dashboard_" + userEmail;
        CachedDashboard cached = dashboardCache.get(cacheKey);
        
        if (cached != null && !cached.isExpired() && cached.isForUser(userEmail)) {
            System.out.println("[NAV-CACHE] Returning cached user dashboard for: " + userEmail);
            return cached.scene;
        }
        
        // Create new dashboard
        System.out.println("[NAV-CACHE] Creating new user dashboard for: " + userEmail);
        long startTime = System.currentTimeMillis();
        
        // Get HostServices safely
        javafx.application.HostServices hostServices = com.moodflix.Main.getAppHostServices();
        UserDashboard userDashboard;
        
        if (hostServices != null) {
            userDashboard = new UserDashboard(hostServices);
        } else {
            System.out.println("[NAV-CACHE] HostServices is null, creating dashboard without it");
            userDashboard = new UserDashboard(); // Uses default constructor with null HostServices
        }
        
        UserDashboardController userController = new UserDashboardController(userDashboard);
        Scene scene = new Scene(userDashboard.getView(), 1200, 800);
        
        long endTime = System.currentTimeMillis();
        System.out.println("[NAV-CACHE] User dashboard creation took: " + (endTime - startTime) + "ms");
        
        // Cache the new dashboard
        dashboardCache.put(cacheKey, new CachedDashboard(scene, userEmail));
        
        return scene;
    }
    
    /**
     * Get cached admin dashboard or create new one
     */
    public static Scene getAdminDashboardScene(String adminEmail) {
        String cacheKey = "admin_dashboard_" + adminEmail;
        CachedDashboard cached = dashboardCache.get(cacheKey);
        
        if (cached != null && !cached.isExpired() && cached.isForUser(adminEmail)) {
            System.out.println("[NAV-CACHE] Returning cached admin dashboard for: " + adminEmail);
            return cached.scene;
        }
        
        // Create new dashboard
        System.out.println("[NAV-CACHE] Creating new admin dashboard for: " + adminEmail);
        long startTime = System.currentTimeMillis();
        
        AdminDashboard adminDashboard = new AdminDashboard();
        AdminDashboardController adminController = new AdminDashboardController(adminDashboard);
        Scene scene = new Scene(adminDashboard.getView(), 1200, 800);
        
        long endTime = System.currentTimeMillis();
        System.out.println("[NAV-CACHE] Admin dashboard creation took: " + (endTime - startTime) + "ms");
        
        // Cache the new dashboard
        dashboardCache.put(cacheKey, new CachedDashboard(scene, adminEmail));
        
        return scene;
    }
    
    /**
     * Get appropriate dashboard based on user role
     */
    public static Scene getDashboardScene(String userEmail) {
        if (SessionManager.isAdmin()) {
            return getAdminDashboardScene(userEmail);
        } else {
            return getUserDashboardScene(userEmail);
        }
    }
    
    /**
     * Clear cache for specific user
     */
    public static void clearUserCache(String userEmail) {
        dashboardCache.remove("user_dashboard_" + userEmail);
        dashboardCache.remove("admin_dashboard_" + userEmail);
        System.out.println("[NAV-CACHE] Cleared cache for user: " + userEmail);
    }
    
    /**
     * Clear all cache
     */
    public static void clearAllCache() {
        dashboardCache.clear();
        System.out.println("[NAV-CACHE] Cleared all navigation cache");
    }
    
    /**
     * Get cache statistics
     */
    public static String getCacheStats() {
        int totalCached = dashboardCache.size();
        int expiredCount = 0;
        
        for (CachedDashboard cached : dashboardCache.values()) {
            if (cached.isExpired()) {
                expiredCount++;
            }
        }
        
        return String.format("Navigation Cache - Total: %d, Expired: %d, Active: %d", 
                           totalCached, expiredCount, totalCached - expiredCount);
    }
    
    /**
     * Clean up expired entries
     */
    public static void cleanupExpired() {
        dashboardCache.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }
} 