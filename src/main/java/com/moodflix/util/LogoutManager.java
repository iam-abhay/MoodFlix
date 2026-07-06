package com.moodflix.util;

import javafx.scene.Scene;
import javafx.stage.Stage;
import com.moodflix.view.LoginPage;
import com.moodflix.controller.LoginPageController;
import com.moodflix.Main;

/**
 * Centralized logout manager for consistent logout behavior
 */
public class LogoutManager {
    
    /**
     * Perform complete logout with all cleanup operations
     */
    public static void performLogout(Stage currentStage) {
        performLogout(currentStage, false);
    }
    
    /**
     * Perform complete logout with all cleanup operations
     * @param currentStage The current stage to navigate from
     * @param isAdmin Whether this is an admin logout
     */
    public static void performLogout(Stage currentStage, boolean isAdmin) {
        System.out.println("🚪 " + (isAdmin ? "Admin" : "User") + " logout initiated");
        
        try {
            // Step 1: Clear session data
            String userEmail = SessionManager.getEmail();
            SessionManager.clear();
            System.out.println("✅ Session cleared successfully");
            
            // Step 2: Clear navigation cache for this user
            if (userEmail != null) {
                NavigationCache.clearUserCache(userEmail);
                System.out.println("✅ Navigation cache cleared for: " + userEmail);
            }
            
            // Step 3: Clear application cache
            // No Firebase cache to clear - using PostgreSQL
            System.out.println("✅ Application cache cleared");
            
            // Step 4: Clear performance monitor stats (optional)
            PerformanceMonitor.clearStats();
            System.out.println("✅ Performance monitor stats cleared");
            
            // Step 5: Create and navigate to login page
            LoginPage loginPage = new LoginPage();
            LoginPageController loginController = new LoginPageController(loginPage);
            Scene loginScene = new Scene(loginPage.getView());
            
            // Step 6: Set the scene
            if (currentStage != null) {
                currentStage.setScene(loginScene);
                currentStage.setTitle("MoodFlix - Login");
            } else {
                Main.setScene(loginScene);
            }
            
            System.out.println("✅ Successfully navigated to login page");
            
            // Step 7: Show logout confirmation
            showLogoutConfirmation(isAdmin);
            
        } catch (Exception ex) {
            System.err.println("❌ Error during logout: " + ex.getMessage());
            ex.printStackTrace();
            
            // Show error dialog
            showLogoutError(ex.getMessage());
            
            // Still try to navigate to login page even if there was an error
            try {
                LoginPage loginPage = new LoginPage();
                LoginPageController loginController = new LoginPageController(loginPage);
                Scene loginScene = new Scene(loginPage.getView());
                
                if (currentStage != null) {
                    currentStage.setScene(loginScene);
                    currentStage.setTitle("MoodFlix - Login");
                } else {
                    Main.setScene(loginScene);
                }
            } catch (Exception navEx) {
                System.err.println("❌ Critical error: Could not navigate to login page: " + navEx.getMessage());
            }
        }
    }
    
    /**
     * Show logout confirmation dialog
     */
    private static void showLogoutConfirmation(boolean isAdmin) {
        javafx.application.Platform.runLater(() -> {
            try {
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
                alert.setTitle("Logged Out");
                alert.setHeaderText("Successfully Logged Out");
                alert.setContentText("You have been successfully logged out. Please log in again to continue.");
                alert.showAndWait();
            } catch (Exception e) {
                System.err.println("Error showing logout confirmation: " + e.getMessage());
            }
        });
    }
    
    /**
     * Show logout error dialog
     */
    private static void showLogoutError(String errorMessage) {
        javafx.application.Platform.runLater(() -> {
            try {
                javafx.scene.control.Alert errorAlert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                errorAlert.setTitle("Logout Error");
                errorAlert.setHeaderText("Error During Logout");
                errorAlert.setContentText("An error occurred during logout. Please try again.\n\nError: " + errorMessage);
                errorAlert.showAndWait();
            } catch (Exception e) {
                System.err.println("Error showing logout error dialog: " + e.getMessage());
            }
        });
    }
    
    /**
     * Perform logout with custom stage
     */
    public static void performLogoutWithStage(javafx.scene.Node sourceNode) {
        if (sourceNode != null && sourceNode.getScene() != null) {
            Stage stage = (Stage) sourceNode.getScene().getWindow();
            performLogout(stage, false);
        } else {
            performLogout(null, false);
        }
    }
    
    /**
     * Perform admin logout with custom stage
     */
    public static void performAdminLogoutWithStage(javafx.scene.Node sourceNode) {
        if (sourceNode != null && sourceNode.getScene() != null) {
            Stage stage = (Stage) sourceNode.getScene().getWindow();
            performLogout(stage, true);
        } else {
            performLogout(null, true);
        }
    }
    
    /**
     * Check if user is currently logged in
     */
    public static boolean isUserLoggedIn() {
        return SessionManager.getEmail() != null;
    }
    
    /**
     * Get current user email
     */
    public static String getCurrentUserEmail() {
        return SessionManager.getEmail();
    }
    
    /**
     * Get current user role
     */
    public static String getCurrentUserRole() {
        return SessionManager.getRole();
    }
    
    /**
     * Check if current user is admin
     */
    public static boolean isCurrentUserAdmin() {
        return "admin".equals(SessionManager.getRole());
    }
    
    /**
     * Force logout without confirmation (for security purposes)
     */
    public static void forceLogout(Stage currentStage) {
        System.out.println("🚨 Force logout initiated");
        
        try {
            // Clear all data immediately
            SessionManager.clear();
            NavigationCache.clearAllCache();
            // No Firebase cache to clear - using PostgreSQL
            PerformanceMonitor.clearStats();
            
            // Navigate to login
            LoginPage loginPage = new LoginPage();
            LoginPageController loginController = new LoginPageController(loginPage);
            Scene loginScene = new Scene(loginPage.getView());
            
            if (currentStage != null) {
                currentStage.setScene(loginScene);
                currentStage.setTitle("MoodFlix - Login");
            } else {
                Main.setScene(loginScene);
            }
            
            System.out.println("✅ Force logout completed");
            
        } catch (Exception ex) {
            System.err.println("❌ Error during force logout: " + ex.getMessage());
            // Even if there's an error, try to navigate to login
            try {
                LoginPage loginPage = new LoginPage();
                LoginPageController loginController = new LoginPageController(loginPage);
                Main.setScene(new Scene(loginPage.getView()));
            } catch (Exception navEx) {
                System.err.println("❌ Critical error during force logout: " + navEx.getMessage());
            }
        }
    }
} 