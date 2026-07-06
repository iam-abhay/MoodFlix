package com.moodflix.util;

import javafx.scene.Scene;
import javafx.application.Platform;
import java.util.concurrent.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import com.moodflix.Main;

/**
 * Specialized back navigation optimization for instant dashboard returns
 */
public class BackNavigationOptimizer {
    
    // Preloaded dashboard scenes
    private static final Map<String, Scene> dashboardScenes = new ConcurrentHashMap<>();
    private static final Map<String, CompletableFuture<Scene>> loadingDashboards = new ConcurrentHashMap<>();
    
    // Back navigation metrics
    private static final AtomicInteger instantBackNavigations = new AtomicInteger(0);
    private static final AtomicInteger cachedBackNavigations = new AtomicInteger(0);
    private static final AtomicInteger slowBackNavigations = new AtomicInteger(0);
    
    // Thread pool for background dashboard loading
    private static final ExecutorService dashboardExecutor = Executors.newFixedThreadPool(2);
    
    /**
     * Navigate back to dashboard instantly with optimization
     */
    public static void navigateBackToDashboard(String userEmail, String userRole) {
        long startTime = System.currentTimeMillis();
        String dashboardKey = userEmail + "_" + userRole;
        
        // Check if dashboard is preloaded
        Scene preloadedDashboard = dashboardScenes.get(dashboardKey);
        if (preloadedDashboard != null) {
            Main.setScene(preloadedDashboard);
            instantBackNavigations.incrementAndGet();
            System.out.println("[BACK-OPT] Instant back navigation to " + userRole + " dashboard (" + 
                (System.currentTimeMillis() - startTime) + "ms)");
            return;
        }
        
        // Check if dashboard is currently loading
        CompletableFuture<Scene> loadingDashboard = loadingDashboards.get(dashboardKey);
        if (loadingDashboard != null && !loadingDashboard.isDone()) {
            loadingDashboard.thenAccept(scene -> {
                Platform.runLater(() -> {
                    Main.setScene(scene);
                    cachedBackNavigations.incrementAndGet();
                    System.out.println("[BACK-OPT] Cached back navigation to " + userRole + " dashboard (" + 
                        (System.currentTimeMillis() - startTime) + "ms)");
                });
            });
            return;
        }
        
        // Create dashboard immediately
        try {
            Scene dashboardScene = createDashboardScene(userEmail, userRole);
            Main.setScene(dashboardScene);
            slowBackNavigations.incrementAndGet();
            System.out.println("[BACK-OPT] Slow back navigation to " + userRole + " dashboard (" + 
                (System.currentTimeMillis() - startTime) + "ms)");
            
            // Preload dashboard for future use
            preloadDashboard(userEmail, userRole);
            
        } catch (Exception e) {
            System.err.println("[BACK-OPT] Back navigation failed: " + e.getMessage());
            throw e;
        }
    }
    
    public static void preloadDashboard(String userEmail, String userRole) {
        String dashboardKey = userEmail + "_" + userRole;
        
        if (dashboardScenes.containsKey(dashboardKey) || loadingDashboards.containsKey(dashboardKey)) {
            return; // Already loading or loaded
        }
        
        CompletableFuture<Scene> future = new CompletableFuture<>();
        Platform.runLater(() -> {
            try {
                System.out.println("[BACK-OPT] Preloading " + userRole + " dashboard for " + userEmail + " on FX Thread...");
                Scene scene = createDashboardScene(userEmail, userRole);
                dashboardScenes.put(dashboardKey, scene);
                loadingDashboards.remove(dashboardKey);
                System.out.println("[BACK-OPT] " + userRole + " dashboard preloaded successfully");
                future.complete(scene);
            } catch (Exception e) {
                System.err.println("[BACK-OPT] Failed to preload " + userRole + " dashboard: " + e.getMessage());
                loadingDashboards.remove(dashboardKey);
                future.completeExceptionally(e);
            }
        });
        
        loadingDashboards.put(dashboardKey, future);
    }
    
    /**
     * Create dashboard scene based on user role
     */
    private static Scene createDashboardScene(String userEmail, String userRole) {
        try {
            if ("admin".equalsIgnoreCase(userRole)) {
                // Create admin dashboard
                com.moodflix.view.AdminDashboard adminDashboard = new com.moodflix.view.AdminDashboard();
                com.moodflix.controller.AdminDashboardController adminController = new com.moodflix.controller.AdminDashboardController(adminDashboard);
                return new Scene(adminDashboard.getView(), 1200, 800);
            } else {
                // Create user dashboard
                com.moodflix.view.UserDashboard userDashboard = new com.moodflix.view.UserDashboard();
                com.moodflix.controller.UserDashboardController userController = new com.moodflix.controller.UserDashboardController(userDashboard);
                return new Scene(userDashboard.getView(), 1200, 800);
            }
        } catch (Exception e) {
            System.err.println("[BACK-OPT] Failed to create dashboard scene: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Smart back navigation that determines user role automatically
     */
    public static void smartBackNavigation(String userEmail) {
        try {
            // Determine user role from session or Firebase
            String userRole = SessionManager.getRole();
            if (userRole == null || userRole.isEmpty()) {
                // Asynchronous fallback: check PostgreSQL for user role
                System.out.println("[BACK-OPT] User role not in session, checking PostgreSQL asynchronously...");
                
                // First, navigate to user dashboard immediately (fast fallback)
                navigateBackToDashboard(userEmail, "user");
                
                // Then check PostgreSQL in background and update if needed
                CompletableFuture.runAsync(() -> {
                    try {
                        com.moodflix.service.PostgreSQLAuthService authService = new com.moodflix.service.PostgreSQLAuthService();
                        org.json.JSONObject userDetails = authService.getUserDetails(userEmail);
                        if (userDetails != null && userDetails.has("role")) {
                            String actualRole = userDetails.getString("role");
                            // Update session with correct role
                            SessionManager.setSession(userEmail, actualRole);
                            System.out.println("[BACK-OPT] Updated user role to: " + actualRole);
                            
                            // If role is admin, preload admin dashboard for future use
                            if ("admin".equals(actualRole)) {
                                preloadDashboard(userEmail, "admin");
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("[BACK-OPT] Background role check failed: " + e.getMessage());
                    }
                }, dashboardExecutor);
                
                return;
            }
            
            navigateBackToDashboard(userEmail, userRole);
            
        } catch (Exception e) {
            System.err.println("[BACK-OPT] Smart back navigation failed: " + e.getMessage());
            // Fallback to user dashboard
            navigateBackToDashboard(userEmail, "user");
        }
    }
    
    /**
     * Preload both user and admin dashboards for a user
     */
    public static void preloadAllDashboards(String userEmail) {
        // Preload user dashboard
        preloadDashboard(userEmail, "user");
        
        // Preload admin dashboard (in case user has admin privileges)
        preloadDashboard(userEmail, "admin");
    }
    
    /**
     * Get back navigation statistics
     */
    public static String getBackNavigationStats() {
        int instant = instantBackNavigations.get();
        int cached = cachedBackNavigations.get();
        int slow = slowBackNavigations.get();
        int total = instant + cached + slow;
        
        double instantRate = total > 0 ? (double) instant / total * 100 : 0;
        double cachedRate = total > 0 ? (double) cached / total * 100 : 0;
        
        return String.format(
            "Back Navigation Performance:\n" +
            "- Instant Back Navigations: %d (%.1f%%)\n" +
            "- Cached Back Navigations: %d (%.1f%%)\n" +
            "- Slow Back Navigations: %d (%.1f%%)\n" +
            "- Total Back Navigations: %d\n" +
            "- Preloaded Dashboards: %d\n" +
            "- Loading Dashboards: %d",
            instant, instantRate,
            cached, cachedRate,
            slow, total > 0 ? (double) slow / total * 100 : 0,
            total,
            dashboardScenes.size(),
            loadingDashboards.size()
        );
    }
    
    /**
     * Clear all preloaded dashboards
     */
    public static void clearPreloadedDashboards() {
        dashboardScenes.clear();
        loadingDashboards.clear();
        System.out.println("[BACK-OPT] All preloaded dashboards cleared");
    }
    
    /**
     * Clear specific user's preloaded dashboards
     */
    public static void clearUserDashboards(String userEmail) {
        dashboardScenes.entrySet().removeIf(entry -> entry.getKey().startsWith(userEmail + "_"));
        loadingDashboards.entrySet().removeIf(entry -> entry.getKey().startsWith(userEmail + "_"));
        System.out.println("[BACK-OPT] Preloaded dashboards cleared for user: " + userEmail);
    }
    
    /**
     * Shutdown dashboard executor
     */
    public static void shutdown() {
        dashboardExecutor.shutdown();
        try {
            if (!dashboardExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                dashboardExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            dashboardExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
} 