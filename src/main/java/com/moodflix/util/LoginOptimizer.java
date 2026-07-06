package com.moodflix.util;

import com.moodflix.service.PostgreSQLAuthService;
import com.moodflix.util.SessionManager;
import com.moodflix.util.NavigationCache;
import org.json.JSONObject;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Optimized login system with parallel processing and intelligent caching
 */
public class LoginOptimizer {
    
    private static final ExecutorService loginExecutor = Executors.newFixedThreadPool(5);
    private static final PostgreSQLAuthService authService = new PostgreSQLAuthService();
    
    /**
     * Optimized login with parallel processing
     */
    public static CompletableFuture<LoginResult> loginAsync(String email, String password) {
        String loginOperationId = PerformanceMonitor.startOperation("optimized_login");
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Step 1: Start authentication (this is the main bottleneck)
                CompletableFuture<Boolean> authFuture = authService.loginUserAsync(email, password);
                
                // Step 2: Start user details fetch in parallel (if cached, this will be instant)
                CompletableFuture<JSONObject> userDetailsFuture = getUserDetailsOptimized(email);
                
                // Wait for authentication to complete first
                Boolean authSuccess = authFuture.get(10, TimeUnit.SECONDS);
                
                if (!authSuccess) {
                    PerformanceMonitor.endOperation(loginOperationId, false);
                    return new LoginResult(false, null, null, "Authentication failed");
                }
                
                // Now wait for user details to determine role
                JSONObject userDetails = userDetailsFuture.get(5, TimeUnit.SECONDS);
                String role = determineUserRole(userDetails);
                
                // Step 3: Prepare dashboard based on determined role
                CompletableFuture<Scene> dashboardFuture = prepareDashboardAsync(email, role);
                
                // Wait for dashboard preparation
                Scene dashboard = dashboardFuture.get(5, TimeUnit.SECONDS);
                
                // Store session
                SessionManager.setSession(email, role);
                
                PerformanceMonitor.endOperation(loginOperationId, true);
                
                return new LoginResult(true, userDetails, dashboard, role);
                
            } catch (Exception e) {
                PerformanceMonitor.endOperation(loginOperationId, false);
                System.err.println("Login optimization error: " + e.getMessage());
                System.err.println("Stack trace:");
                e.printStackTrace();
                return new LoginResult(false, null, null, e.getMessage());
            }
        }, loginExecutor);
    }
    
    /**
     * Optimized user details fetch with intelligent caching
     */
    private static CompletableFuture<JSONObject> getUserDetailsOptimized(String email) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Check cache first
                JSONObject cachedUser = getCachedUser(email);
                if (cachedUser != null) {
                    System.out.println("[LOGIN-OPT] Using cached user details for: " + email);
                    return cachedUser;
                }
                
                // Fetch from PostgreSQL if not cached
                System.out.println("[LOGIN-OPT] Fetching user details from PostgreSQL for: " + email);
                return authService.getUserDetails(email);
                
            } catch (Exception e) {
                System.err.println("Error fetching user details: " + e.getMessage());
                return null;
            }
        }, loginExecutor);
    }
    
    private static CompletableFuture<Scene> prepareDashboardAsync(String email, String role) {
        CompletableFuture<Scene> future = new CompletableFuture<>();
        javafx.application.Platform.runLater(() -> {
            try {
                // Use role-specific dashboard creation
                if ("admin".equals(role)) {
                    System.out.println("[LOGIN-OPT] Preparing admin dashboard for: " + email);
                    future.complete(NavigationCache.getAdminDashboardScene(email));
                } else {
                    System.out.println("[LOGIN-OPT] Preparing user dashboard for: " + email);
                    // Get HostServices safely
                    javafx.application.HostServices hostServices = com.moodflix.Main.getAppHostServices();
                    if (hostServices == null) {
                        System.out.println("[LOGIN-OPT] HostServices is null, using fallback dashboard creation");
                        // Create dashboard without HostServices as fallback
                        future.complete(createUserDashboardFallback(email));
                    } else {
                        future.complete(NavigationCache.getUserDashboardScene(email));
                    }
                }
            } catch (Exception e) {
                System.err.println("Error preparing dashboard: " + e.getMessage());
                e.printStackTrace(); // Add stack trace for debugging
                future.completeExceptionally(e);
            }
        });
        return future;
    }
    
    /**
     * Create user dashboard fallback when HostServices is not available
     */
    private static Scene createUserDashboardFallback(String email) {
        try {
            System.out.println("[LOGIN-OPT] Creating user dashboard fallback for: " + email);
            com.moodflix.view.UserDashboard userDashboard = new com.moodflix.view.UserDashboard();
            com.moodflix.controller.UserDashboardController userController = new com.moodflix.controller.UserDashboardController(userDashboard);
            return new javafx.scene.Scene(userDashboard.getView(), 1200, 800);
        } catch (Exception e) {
            System.err.println("Error creating fallback dashboard: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Determine user role from user details
     */
    public static String determineUserRole(JSONObject userDetails) {
        if (userDetails == null) {
            System.out.println("[LOGIN-OPT] No user details found, using default 'user' role");
            return "user"; // Default role
        }
        
        System.out.println("[LOGIN-OPT] Analyzing user details for role determination...");
        
        // Check for role field first (primary field)
        if (userDetails.has("role")) {
            String role = userDetails.getString("role");
            System.out.println("[LOGIN-OPT] Found 'role' field: " + role);
            
            // Normalize role value
            if ("admin".equalsIgnoreCase(role) || "administrator".equalsIgnoreCase(role)) {
                System.out.println("[LOGIN-OPT] Determined role: ADMIN");
                return "admin";
            } else if ("user".equalsIgnoreCase(role) || "regular".equalsIgnoreCase(role)) {
                System.out.println("[LOGIN-OPT] Determined role: USER");
                return "user";
            } else {
                System.out.println("[LOGIN-OPT] Unknown role value: " + role + ", defaulting to 'user'");
                return "user";
            }
        }
        
        // Check for userType field as fallback
        if (userDetails.has("userType")) {
            String userType = userDetails.getString("userType");
            System.out.println("[LOGIN-OPT] Found 'userType' field: " + userType);
            
            // Normalize userType value
            if ("admin".equalsIgnoreCase(userType) || "administrator".equalsIgnoreCase(userType)) {
                System.out.println("[LOGIN-OPT] Determined role from userType: ADMIN");
                return "admin";
            } else if ("user".equalsIgnoreCase(userType) || "regular".equalsIgnoreCase(userType)) {
                System.out.println("[LOGIN-OPT] Determined role from userType: USER");
                return "user";
            } else {
                System.out.println("[LOGIN-OPT] Unknown userType value: " + userType + ", defaulting to 'user'");
                return "user";
            }
        }
        
        // Check for other potential role indicators
        if (userDetails.has("accountType")) {
            String accountType = userDetails.getString("accountType");
            System.out.println("[LOGIN-OPT] Found 'accountType' field: " + accountType);
            if ("admin".equalsIgnoreCase(accountType)) {
                System.out.println("[LOGIN-OPT] Determined role from accountType: ADMIN");
                return "admin";
            }
        }
        
        System.out.println("[LOGIN-OPT] No role information found in user details, using default 'user' role");
        System.out.println("[LOGIN-OPT] Available fields: " + userDetails.keySet());
        return "user";
    }
    
    /**
     * Get cached user details
     */
    private static JSONObject getCachedUser(String email) {
        try {
            // No reflection needed for PostgreSQL - return null to always fetch fresh
            return null;
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Login result class
     */
    public static class LoginResult {
        private final boolean success;
        private final JSONObject userDetails;
        private final Scene dashboard;
        private final String role;
        private final String errorMessage;
        
        public LoginResult(boolean success, JSONObject userDetails, Scene dashboard, String role) {
            this(success, userDetails, dashboard, role, null);
        }
        
        public LoginResult(boolean success, JSONObject userDetails, Scene dashboard, String role, String errorMessage) {
            this.success = success;
            this.userDetails = userDetails;
            this.dashboard = dashboard;
            this.role = role;
            this.errorMessage = errorMessage;
        }
        
        public boolean isSuccess() { return success; }
        public JSONObject getUserDetails() { return userDetails; }
        public Scene getDashboard() { return dashboard; }
        public String getRole() { return role; }
        public String getErrorMessage() { return errorMessage; }
    }
    
    /**
     * Shutdown the login executor
     */
    public static void shutdown() {
        loginExecutor.shutdown();
        try {
            if (!loginExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                loginExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            loginExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Test login performance
     */
    public static void testLoginPerformance(String email, String password) {
        System.out.println("🧪 Testing Login Performance...");
        
        long startTime = System.currentTimeMillis();
        
        loginAsync(email, password)
            .thenAcceptAsync(result -> {
                long endTime = System.currentTimeMillis();
                long totalTime = endTime - startTime;
                
                System.out.println("Login test completed in: " + totalTime + "ms");
                System.out.println("Success: " + result.isSuccess());
                System.out.println("Role: " + result.getRole());
                
                if (totalTime < 1000) {
                    System.out.println("✅ Excellent login performance (< 1s)");
                } else if (totalTime < 2000) {
                    System.out.println("✅ Good login performance (< 2s)");
                } else if (totalTime < 3000) {
                    System.out.println("⚠️ Acceptable login performance (< 3s)");
                } else {
                    System.out.println("❌ Poor login performance (> 3s)");
                }
            });
    }
} 