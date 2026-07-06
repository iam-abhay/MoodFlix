package com.moodflix.controller;

import com.moodflix.Main;
import com.moodflix.view.AdminDashboard;
import com.moodflix.view.UserDashboard;
import com.moodflix.controller.AdminDashboardController;
import com.moodflix.controller.UserDashboardController;
import com.moodflix.controller.SignUpPageController;
import com.moodflix.service.PostgreSQLAuthService;
import com.moodflix.util.SessionManager;
import com.moodflix.view.LoginPage;
import com.moodflix.view.SignUpPage;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import org.json.JSONObject;
import javafx.scene.layout.VBox;
import com.moodflix.util.MoodflixDialog;

public class LoginPageController {
    private final LoginPage view;

    public LoginPageController(LoginPage view) {
        this.view = view;
        setupEventHandlers();
    }

    private void setupEventHandlers() {
        TextField emailField = view.getEmailField();
        PasswordField passwordField = view.getPasswordField();
        Button loginBtn = view.getLoginBtn();
        Text statusText = view.getStatusText();
        Hyperlink signUpLink = view.getSignUpLink();
        PostgreSQLAuthService authService = new PostgreSQLAuthService();

        loginBtn.setOnAction(e -> {
            String email = emailField.getText().trim();
            String password = passwordField.getText();
            
            if (email.isEmpty() || password.isEmpty()) {
                statusText.setFill(javafx.scene.paint.Color.RED);
                statusText.setText("Please enter both email and password.");
                return;
            }
            
            System.out.println("🔐 Login button clicked!");
            System.out.println("🔐 Attempting login for email: " + email);
            
            loginBtn.setDisable(true);
            statusText.setFill(javafx.scene.paint.Color.BLUE);
            statusText.setText("Logging in...");
            
            // Try optimized login first, with fallback to direct login
            try {
                com.moodflix.util.LoginOptimizer.loginAsync(email, password)
                    .thenAcceptAsync(loginResult -> {
                        javafx.application.Platform.runLater(() -> {
                            loginBtn.setDisable(false);
                            
                            if (loginResult.isSuccess()) {
                                System.out.println("✅ Optimized login successful!");
                                handleSuccessfulLogin(loginResult, email);
                            } else {
                                System.out.println("⚠️ Optimized login failed, trying direct login...");
                                // Fallback to direct login
                                tryDirectLogin(email, password);
                            }
                        });
                    }).exceptionally(throwable -> {
                        System.err.println("❌ Error in optimized login: " + throwable.getMessage());
                        javafx.application.Platform.runLater(() -> {
                            System.out.println("⚠️ Optimized login failed, trying direct login...");
                            tryDirectLogin(email, password);
                        });
                        return null;
                    });
            } catch (Exception ex) {
                System.err.println("❌ Exception in login optimization: " + ex.getMessage());
                javafx.application.Platform.runLater(() -> {
                    tryDirectLogin(email, password);
                });
            }
        });

        signUpLink.setOnAction(e -> {
            System.out.println("📝 Sign up link clicked!");
            SignUpPage signUpPage = new SignUpPage();
            SignUpPageController signUpController = new SignUpPageController(signUpPage);
            Main.setScene(new Scene(signUpPage.getView()));
            Stage stage = (Stage) view.getLoginBtn().getScene().getWindow();
            stage.setTitle("MoodFlix - Sign Up");
        });
    }
    
    /**
     * Handle successful login result
     */
    private void handleSuccessfulLogin(com.moodflix.util.LoginOptimizer.LoginResult loginResult, String email) {
        view.getStatusText().setFill(javafx.scene.paint.Color.GREEN);
        view.getStatusText().setText("Login Successful! Redirecting...");
        
        // Get the prepared dashboard and user details
        Scene dashboard = loginResult.getDashboard();
        String role = loginResult.getRole();
        
        if (dashboard != null) {
            Stage stage = (Stage) view.getLoginBtn().getScene().getWindow();
            
            // Set the appropriate title based on role
            if ("admin".equals(role)) {
                System.out.println("👑 ADMIN DETECTED - Redirecting to Admin Dashboard...");
                stage.setTitle("MoodFlix - Admin Dashboard");
                
                MoodflixDialog.showSuccess(
                    "Admin Access Granted",
                    "🎉 Welcome Admin!\n\nYou have access to all administrative features:\n• Content Management\n• User Activity History\n• System Administration\n• Analytics Dashboard"
                );
            } else {
                System.out.println("👤 REGULAR USER DETECTED - Redirecting to User Dashboard...");
                stage.setTitle("MoodFlix - User Dashboard");
                
                MoodflixDialog.showSuccess(
                    "Welcome Back!",
                    "🎉 Welcome to MoodFlix!\n\nYour personalized experience includes:\n• Personalized recommendations\n• Watchlist management\n• Activity history tracking\n• Favorite content"
                );
            }
            
            // Set the scene (this should be instant since it's already prepared)
            Main.setScene(dashboard);
            
            // Preload dashboards for future back navigation
            System.out.println("🚀 Preloading dashboards for future back navigation...");
            com.moodflix.util.BackNavigationOptimizer.preloadAllDashboards(email);
            
        } else {
            // Fallback if dashboard preparation failed
            System.err.println("❌ Dashboard preparation failed, using fallback");
            handleLoginFallback(email, role);
        }
    }
    
    /**
     * Try direct login as fallback when optimized login fails
     */
    private void tryDirectLogin(String email, String password) {
        System.out.println("🔄 Attempting direct login for: " + email);
        
        try {
            // Use PostgreSQL authentication
            PostgreSQLAuthService postgresAuthService = new PostgreSQLAuthService();
            org.json.JSONObject authResponse = postgresAuthService.login(email, password);
            
            if (authResponse != null) {
                System.out.println("✅ Direct authentication successful!");
                
                // Extract role from response
                String role = authResponse.optString("role", "user");
                String userId = authResponse.getString("localId");
                
                // Store session
                com.moodflix.util.SessionManager.setSession(email, role);
                
                // Create dashboard directly
                Stage stage = (Stage) view.getLoginBtn().getScene().getWindow();
                
                if ("admin".equals(role)) {
                    System.out.println("👑 ADMIN DETECTED - Creating Admin Dashboard...");
                    com.moodflix.view.AdminDashboard adminDashboard = new com.moodflix.view.AdminDashboard();
                    com.moodflix.controller.AdminDashboardController adminController = new com.moodflix.controller.AdminDashboardController(adminDashboard);
                    Scene scene = new javafx.scene.Scene(adminDashboard.getView(), 1200, 800);
                    Main.setScene(scene);
                    stage.setTitle("MoodFlix - Admin Dashboard");
                    
                    MoodflixDialog.showSuccess(
                        "Admin Access Granted",
                        "🎉 Welcome Admin!\n\nYou have access to all administrative features:\n• Content Management\n• User Activity History\n• System Administration\n• Analytics Dashboard"
                    );
                } else {
                    System.out.println("👤 REGULAR USER DETECTED - Creating User Dashboard...");
                    com.moodflix.view.UserDashboard userDashboard = new com.moodflix.view.UserDashboard();
                    com.moodflix.controller.UserDashboardController userController = new com.moodflix.controller.UserDashboardController(userDashboard);
                    Scene scene = new javafx.scene.Scene(userDashboard.getView(), 1200, 800);
                    Main.setScene(scene);
                    stage.setTitle("MoodFlix - User Dashboard");
                    
                    MoodflixDialog.showSuccess(
                        "Welcome Back!",
                        "🎉 Welcome to MoodFlix!\n\nYour personalized experience includes:\n• Personalized recommendations\n• Watchlist management\n• Activity history tracking\n• Favorite content"
                    );
                }
                
                view.getLoginBtn().setDisable(false);
                view.getStatusText().setFill(javafx.scene.paint.Color.GREEN);
                view.getStatusText().setText("Login Successful!");
                
                // Preload dashboards for future back navigation
                System.out.println("🚀 Preloading dashboards for future back navigation...");
                com.moodflix.util.BackNavigationOptimizer.preloadAllDashboards(email);
                
            } else {
                System.out.println("❌ Direct authentication failed for: " + email);
                view.getLoginBtn().setDisable(false);
                view.getStatusText().setFill(javafx.scene.paint.Color.RED);
                view.getStatusText().setText("Login Failed. Please check your credentials.");
                
                MoodflixDialog.showError("Login Failed", "Please verify your credentials and try again.");
            }
            
        } catch (Exception ex) {
            System.err.println("❌ Exception in direct login: " + ex.getMessage());
            ex.printStackTrace();
             
             view.getLoginBtn().setDisable(false);
             view.getStatusText().setFill(javafx.scene.paint.Color.RED);
             view.getStatusText().setText("Login error. Please try again.");
            
            MoodflixDialog.showError("Login Error", "An error occurred during login:\n" + ex.getMessage());
        }
    }
    
    /**
     * Determine user role from user details
     */
    private String determineUserRole(org.json.JSONObject userDetails) {
        if (userDetails == null) {
            System.out.println("No user details found, using default 'user' role");
            return "user";
        }
        
        if (userDetails.has("role")) {
            String role = userDetails.getString("role");
            System.out.println("Found role: " + role);
            
            if ("admin".equalsIgnoreCase(role)) {
                return "admin";
            }
        }
        
        return "user";
    }
    
    /**
     * Handle login fallback when dashboard preparation fails
     */
    private void handleLoginFallback(String email, String role) {
        try {
            Stage stage = (Stage) view.getLoginBtn().getScene().getWindow();
            
            if ("admin".equals(role)) {
                // Create admin dashboard as fallback
                com.moodflix.view.AdminDashboard adminDashboard = new com.moodflix.view.AdminDashboard();
                com.moodflix.controller.AdminDashboardController adminController = new com.moodflix.controller.AdminDashboardController(adminDashboard);
                Main.setScene(new javafx.scene.Scene(adminDashboard.getView()));
                stage.setTitle("MoodFlix - Admin Dashboard");
            } else {
                // Create user dashboard as fallback
                com.moodflix.view.UserDashboard userDashboard = new com.moodflix.view.UserDashboard(com.moodflix.Main.getAppHostServices());
                com.moodflix.controller.UserDashboardController userController = new com.moodflix.controller.UserDashboardController(userDashboard);
                Main.setScene(new javafx.scene.Scene(userDashboard.getView()));
                stage.setTitle("MoodFlix - User Dashboard");
            }
            
            System.out.println("✅ Login fallback completed successfully");
            
        } catch (Exception ex) {
            System.err.println("❌ Login fallback failed: " + ex.getMessage());
            MoodflixDialog.showError("Login Error", "An error occurred while creating the dashboard:\n" + ex.getMessage());
        }
    }
} 
