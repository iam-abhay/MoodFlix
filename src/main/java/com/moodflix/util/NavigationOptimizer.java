package com.moodflix.util;

import javafx.scene.Scene;
import javafx.application.Platform;
import java.util.concurrent.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.concurrent.atomic.AtomicInteger;
import com.moodflix.Main;

/**
 * Advanced navigation optimization system for instant page transitions
 */
public class NavigationOptimizer {
    
    // Preloaded scenes cache
    private static final Map<String, Scene> preloadedScenes = new ConcurrentHashMap<>();
    private static final Map<String, CompletableFuture<Scene>> loadingScenes = new ConcurrentHashMap<>();
    
    // Navigation metrics
    private static final AtomicInteger instantNavigations = new AtomicInteger(0);
    private static final AtomicInteger preloadedNavigations = new AtomicInteger(0);
    private static final AtomicInteger slowNavigations = new AtomicInteger(0);
    
    // Thread pool for background loading
    private static final ExecutorService navigationExecutor = Executors.newFixedThreadPool(4);
    
    // Preload prediction based on user behavior
    private static final Map<String, String[]> likelyNextPages = new ConcurrentHashMap<>();
    
    static {
        initializePredictions();
    }
    
    /**
     * Initialize navigation predictions based on common user flows
     */
    private static void initializePredictions() {
        // User dashboard likely next pages
        likelyNextPages.put("UserDashboard", new String[]{
            "ProfilePage", "WatchlistPage", "FeedbackPage", "ActivityHistoryPage"
        });
        
        // Admin dashboard likely next pages
        likelyNextPages.put("AdminDashboard", new String[]{
            "AdminUserManagementPage", "AdminProfilePage", "UserStatsPage"
        });
        
        // Profile page likely next pages
        likelyNextPages.put("ProfilePage", new String[]{
            "UserDashboard", "AdminDashboard"
        });
        
        // Watchlist likely next pages
        likelyNextPages.put("WatchlistPage", new String[]{
            "UserDashboard", "ProfilePage"
        });
    }
    
    /**
     * Navigate instantly with preloaded scene or create new one
     */
    public static void navigateInstantly(String pageName, Supplier<Scene> sceneCreator) {
        long startTime = System.currentTimeMillis();
        
        // Check if scene is preloaded
        Scene preloadedScene = preloadedScenes.get(pageName);
        if (preloadedScene != null) {
            Main.setScene(preloadedScene);
            instantNavigations.incrementAndGet();
            System.out.println("[NAV-OPT] Instant navigation to " + pageName + " (" + 
                (System.currentTimeMillis() - startTime) + "ms)");
            return;
        }
        
        // Check if scene is currently loading
        CompletableFuture<Scene> loadingScene = loadingScenes.get(pageName);
        if (loadingScene != null && !loadingScene.isDone()) {
            loadingScene.thenAccept(scene -> {
                Platform.runLater(() -> {
                    Main.setScene(scene);
                    preloadedNavigations.incrementAndGet();
                    System.out.println("[NAV-OPT] Preloaded navigation to " + pageName + " (" + 
                        (System.currentTimeMillis() - startTime) + "ms)");
                });
            });
            return;
        }
        
        // Create scene immediately
        try {
            Scene scene = sceneCreator.get();
            Main.setScene(scene);
            slowNavigations.incrementAndGet();
            System.out.println("[NAV-OPT] Slow navigation to " + pageName + " (" + 
                (System.currentTimeMillis() - startTime) + "ms)");
            
            // Preload likely next pages
            preloadLikelyPages(pageName);
            
        } catch (Exception e) {
            System.err.println("[NAV-OPT] Navigation failed to " + pageName + ": " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Preload a specific page in background
     */
    public static void preloadPage(String pageName, Supplier<Scene> sceneCreator) {
        if (preloadedScenes.containsKey(pageName) || loadingScenes.containsKey(pageName)) {
            return; // Already loading or loaded
        }
        
        CompletableFuture<Scene> future = CompletableFuture.supplyAsync(() -> {
            try {
                System.out.println("[NAV-OPT] Preloading " + pageName + "...");
                Scene scene = sceneCreator.get();
                preloadedScenes.put(pageName, scene);
                loadingScenes.remove(pageName);
                System.out.println("[NAV-OPT] " + pageName + " preloaded successfully");
                return scene;
            } catch (Exception e) {
                System.err.println("[NAV-OPT] Failed to preload " + pageName + ": " + e.getMessage());
                loadingScenes.remove(pageName);
                throw e;
            }
        }, navigationExecutor);
        
        loadingScenes.put(pageName, future);
    }
    
    /**
     * Preload likely next pages based on current page
     */
    private static void preloadLikelyPages(String currentPage) {
        String[] likelyPages = likelyNextPages.get(currentPage);
        if (likelyPages == null) return;
        
        for (String pageName : likelyPages) {
            preloadPage(pageName, () -> createSceneForPage(pageName));
        }
    }
    
    /**
     * Create scene for specific page
     */
    private static Scene createSceneForPage(String pageName) {
        try {
            switch (pageName) {
                case "ProfilePage":
                    String userEmail = SessionManager.getEmail();
                    com.moodflix.view.ProfilePage profileView = new com.moodflix.view.ProfilePage(userEmail);
                    new com.moodflix.controller.ProfilePageController(profileView, userEmail);
                    return new Scene(profileView.getView());
                    
                case "WatchlistPage":
                    com.moodflix.view.WatchlistPage watchlistView = new com.moodflix.view.WatchlistPage();
                    new com.moodflix.controller.WatchlistPageController(watchlistView);
                    return new Scene(watchlistView.getView());
                    
                case "FeedbackPage":
                    com.moodflix.view.FeedbackPage feedbackView = new com.moodflix.view.FeedbackPage();
                    new com.moodflix.controller.FeedbackPageController(feedbackView);
                    return new Scene(feedbackView.getView());
                    
                case "ActivityHistoryPage":
                    com.moodflix.view.ActivityHistoryPage activityView = new com.moodflix.view.ActivityHistoryPage();
                    new com.moodflix.controller.ActivityHistoryController(activityView);
                    return new Scene(activityView.getView());
                    
                case "AdminUserManagementPage":
                    com.moodflix.view.AdminUserManagementPage userMgmtView = new com.moodflix.view.AdminUserManagementPage();
                    new com.moodflix.controller.AdminUserManagementController(userMgmtView);
                    return new Scene(userMgmtView.getView(), 1200, 800);
                    
                case "AdminProfilePage":
                    String adminEmail = SessionManager.getEmail();
                    com.moodflix.view.AdminProfilePage adminProfileView = new com.moodflix.view.AdminProfilePage(adminEmail);
                    new com.moodflix.controller.AdminProfilePageController(adminProfileView, adminEmail);
                    return new Scene(adminProfileView.getView(), 1200, 800);
                    
                case "UserStatsPage":
                    com.moodflix.view.UserStatsPage userStatsView = new com.moodflix.view.UserStatsPage();
                    new com.moodflix.controller.UserStatsPageController(userStatsView);
                    return new Scene(userStatsView.getView(), 1200, 800);
                    
                default:
                    throw new IllegalArgumentException("Unknown page: " + pageName);
            }
        } catch (Exception e) {
            System.err.println("[NAV-OPT] Failed to create scene for " + pageName + ": " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Get navigation statistics
     */
    public static String getNavigationStats() {
        int instant = instantNavigations.get();
        int preloaded = preloadedNavigations.get();
        int slow = slowNavigations.get();
        int total = instant + preloaded + slow;
        
        double instantRate = total > 0 ? (double) instant / total * 100 : 0;
        double preloadedRate = total > 0 ? (double) preloaded / total * 100 : 0;
        
        return String.format(
            "Navigation Performance:\n" +
            "- Instant Navigations: %d (%.1f%%)\n" +
            "- Preloaded Navigations: %d (%.1f%%)\n" +
            "- Slow Navigations: %d (%.1f%%)\n" +
            "- Total Navigations: %d\n" +
            "- Preloaded Scenes: %d\n" +
            "- Loading Scenes: %d",
            instant, instantRate,
            preloaded, preloadedRate,
            slow, total > 0 ? (double) slow / total * 100 : 0,
            total,
            preloadedScenes.size(),
            loadingScenes.size()
        );
    }
    
    /**
     * Clear all preloaded scenes
     */
    public static void clearPreloadedScenes() {
        preloadedScenes.clear();
        loadingScenes.clear();
        System.out.println("[NAV-OPT] All preloaded scenes cleared");
    }
    
    /**
     * Clear specific preloaded scene
     */
    public static void clearPreloadedScene(String pageName) {
        preloadedScenes.remove(pageName);
        loadingScenes.remove(pageName);
        System.out.println("[NAV-OPT] Preloaded scene cleared: " + pageName);
    }
    
    /**
     * Shutdown navigation executor
     */
    public static void shutdown() {
        navigationExecutor.shutdown();
        try {
            if (!navigationExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                navigationExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            navigationExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
} 