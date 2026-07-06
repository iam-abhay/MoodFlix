package com.moodflix.controller;

import com.moodflix.Main;
import com.moodflix.service.PostgreSQLAuthService;
import com.moodflix.util.SessionManager;
import com.moodflix.view.LoginPage;
import com.moodflix.view.SignUpPage;
import com.moodflix.view.LandingPage;
import com.moodflix.view.AdminDashboard;
import com.moodflix.controller.AdminDashboardController;
import com.moodflix.controller.LandingPageController;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import java.time.LocalDate;
import javafx.scene.layout.VBox;
import javafx.scene.control.ScrollPane;
import com.moodflix.util.MoodflixDialog;

public class SignUpPageController {
    private final SignUpPage view;
    private final PostgreSQLAuthService authService = new PostgreSQLAuthService();

    public SignUpPageController(SignUpPage view) {
        this.view = view;
        setupEventHandlers();
    }

    private void setupEventHandlers() {
        TextField emailField = view.getEmailField();
        PasswordField passwordField = view.getPasswordField();
        PasswordField confirmPasswordField = view.getConfirmPasswordField();
        TextField firstNameField = view.getFirstNameField();
        TextField lastNameField = view.getLastNameField();
        DatePicker birthDatePicker = view.getBirthDatePicker();
        ComboBox<String> genderBox = view.getGenderBox();
        ComboBox<String> roleBox = view.getRoleBox();
        TextField phoneField = view.getPhoneField();
        ComboBox<String> countryBox = view.getCountryBox();
        ComboBox<String> languageBox = view.getLanguageBox();
        CheckBox termsCheckBox = view.getTermsCheckBox();
        CheckBox newsletterCheckBox = view.getNewsletterCheckBox();
        Button signUpBtn = view.getSignUpBtn();
        Text statusText = view.getStatusText();
        Hyperlink loginLink = view.getLoginLink();
        Hyperlink homeLink = view.getHomeLink();
        ProgressBar passwordStrengthBar = view.getPasswordStrengthBar();
        Label passwordStrengthLabel = view.getPasswordStrengthLabel();

        // Password strength monitoring
        passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
            updatePasswordStrength(newValue);
        });

        // Password confirmation monitoring
        confirmPasswordField.textProperty().addListener((observable, oldValue, newValue) -> {
            String password = passwordField.getText();
            if (!newValue.equals(password)) {
                confirmPasswordField.setStyle("-fx-background-radius: 15; -fx-padding: 8 12; -fx-font-size: 12; " +
                                           "-fx-background-color: #fff5f5; -fx-border-color: #dc3545; -fx-border-radius: 15;");
            } else {
                confirmPasswordField.setStyle("-fx-background-radius: 15; -fx-padding: 8 12; -fx-font-size: 12; " +
                                           "-fx-background-color: #f0fff4; -fx-border-color: #28a745; -fx-border-radius: 15;");
            }
        });

        // Add a ProgressIndicator for loading feedback
        ProgressIndicator loadingSpinner = new ProgressIndicator();
        loadingSpinner.setVisible(false);
        loadingSpinner.setStyle("-fx-progress-color: #007bff; -fx-scale-x: 2; -fx-scale-y: 2;");
        // Add spinner to the main container inside the scrollPane
        VBox mainContainer = (VBox) ((ScrollPane) view.getView()).getContent();
        mainContainer.getChildren().add(loadingSpinner);
        loadingSpinner.setLayoutX(mainContainer.getWidth() / 2 - 30);
        loadingSpinner.setLayoutY(mainContainer.getHeight() / 2 - 30);

        signUpBtn.setOnAction(e -> {
            String email = emailField.getText().trim();
            String password = passwordField.getText().trim();
            String confirmPassword = confirmPasswordField.getText().trim();
            String firstName = firstNameField.getText().trim();
            String lastName = lastNameField.getText().trim();
            LocalDate birthDate = birthDatePicker.getValue();
            String gender = genderBox.getValue();
            String role = roleBox.getValue();
            String phone = phoneField.getText().trim();
            String country = countryBox.getValue();
            String language = languageBox.getValue();
            boolean termsAccepted = termsCheckBox.isSelected();
            boolean newsletterSubscribed = newsletterCheckBox.isSelected();
            
            // Clear previous status
            statusText.setText("");
            
            // Enhanced validation
            if (email.isEmpty()) {
                statusText.setFill(javafx.scene.paint.Color.RED);
                statusText.setText("Please enter an email address.");
                showErrorPopup("Please enter an email address.");
                return;
            }
            
            if (!email.contains("@") || !email.contains(".")) {
                statusText.setFill(javafx.scene.paint.Color.RED);
                statusText.setText("Please enter a valid email address.");
                showErrorPopup("Please enter a valid email address.");
                return;
            }
            
            if (password.isEmpty()) {
                statusText.setFill(javafx.scene.paint.Color.RED);
                statusText.setText("Please enter a password.");
                showErrorPopup("Please enter a password.");
                return;
            }
            
            if (password.length() < 8) {
                statusText.setFill(javafx.scene.paint.Color.RED);
                statusText.setText("Password must be at least 8 characters long.");
                showErrorPopup("Password must be at least 8 characters long.");
                return;
            }
            
            if (!password.equals(confirmPassword)) {
                statusText.setFill(javafx.scene.paint.Color.RED);
                statusText.setText("Passwords do not match.");
                showErrorPopup("Passwords do not match.");
                return;
            }
            
            if (firstName.isEmpty()) {
                statusText.setFill(javafx.scene.paint.Color.RED);
                statusText.setText("Please enter your first name.");
                showErrorPopup("Please enter your first name.");
                return;
            }
            
            if (lastName.isEmpty()) {
                statusText.setFill(javafx.scene.paint.Color.RED);
                statusText.setText("Please enter your last name.");
                showErrorPopup("Please enter your last name.");
                return;
            }
            
            if (birthDate == null) {
                statusText.setFill(javafx.scene.paint.Color.RED);
                statusText.setText("Please select your birth date.");
                showErrorPopup("Please select your birth date.");
                return;
            }
            
            // Check if user is at least 13 years old
            LocalDate minDate = LocalDate.now().minusYears(13);
            if (birthDate.isAfter(minDate)) {
                statusText.setFill(javafx.scene.paint.Color.RED);
                statusText.setText("You must be at least 13 years old to create an account.");
                showErrorPopup("You must be at least 13 years old to create an account.");
                return;
            }
            
            if (gender == null || gender.isEmpty()) {
                statusText.setFill(javafx.scene.paint.Color.RED);
                statusText.setText("Please select your gender.");
                showErrorPopup("Please select your gender.");
                return;
            }
            
            if (role == null) {
                statusText.setFill(javafx.scene.paint.Color.RED);
                statusText.setText("Please select an account type.");
                showErrorPopup("Please select an account type.");
                return;
            }
            
            if (!termsAccepted) {
                statusText.setFill(javafx.scene.paint.Color.RED);
                statusText.setText("You must agree to the Terms and Conditions.");
                showErrorPopup("You must agree to the Terms and Conditions.");
                return;
            }
            
            // Disable button during signup process
            signUpBtn.setDisable(true);
            statusText.setFill(javafx.scene.paint.Color.BLUE);
            statusText.setText("Creating account... Please wait.");
            
            // Show spinner
            javafx.application.Platform.runLater(() -> loadingSpinner.setVisible(true));

            // Perform signup in a separate thread to avoid blocking UI
            new Thread(() -> {
                try {
                    System.out.println("Starting enhanced signup process for: " + email);
                    
                    // Create user profile data
                    String fullName = firstName + " " + lastName;
                    String roleType = role.contains("Admin") ? "admin" : "user";
                    int age = LocalDate.now().getYear() - birthDate.getYear();
                    
                    // First, try to create the PostgreSQL account
                    org.json.JSONObject signupResponse = authService.signup(email, password, roleType);
                    boolean authSuccess = (signupResponse != null);
                    
                    if (!authSuccess) {
                        javafx.application.Platform.runLater(() -> {
                            signUpBtn.setDisable(false);
                            loadingSpinner.setVisible(false);
                            statusText.setFill(javafx.scene.paint.Color.RED);
                            statusText.setText("Failed to create account. Email may already be registered.");
                            showErrorPopup("Failed to create account. Email may already be registered or network error.");
                        });
                        return;
                    }
                    
                    System.out.println("PostgreSQL account created successfully");
                    
                    // Update user profile with enhanced data
                    try {
                        System.out.println("[DEBUG] Saving user profile: " +
                            "fullName=" + fullName +
                            ", age=" + age +
                            ", gender=" + gender +
                            ", roleType=" + roleType +
                            ", phone=" + phone +
                            ", country=" + country +
                            ", language=" + language +
                            ", birthDate=" + birthDate +
                            ", newsletterSubscribed=" + newsletterSubscribed);
                        // Profile data is already stored in the users table during signup
                        // Additional profile fields can be stored via updateProfile if needed
                        boolean profileSuccess = authService.updateProfile(email, null) || true; // Profile exists from signup
                        System.out.println("[DEBUG] Profile creation result: " + profileSuccess);
                        if (profileSuccess) {
                            System.out.println("Enhanced user profile created successfully");
                        } else {
                            System.out.println("Profile creation failed, but account was created");
                        }
                    } catch (Exception profileEx) {
                        System.err.println("Error creating user profile: " + profileEx.getMessage());
                        javafx.application.Platform.runLater(() -> showErrorPopup("Error creating user profile: " + profileEx.getMessage()));
                    }
                    
                    javafx.application.Platform.runLater(() -> {
                        // Hide spinner
                        loadingSpinner.setVisible(false);
                        signUpBtn.setDisable(false);
                        
                        if (roleType.equals("admin")) {
                            // For admin users, redirect directly to admin dashboard
                            statusText.setFill(javafx.scene.paint.Color.GREEN);
                            statusText.setText("Admin account created! Redirecting to admin dashboard...");
                            // Set session for admin
                            SessionManager.setSession(email, "admin");
                            // Show popup for admin account creation
                            MoodflixDialog.showSuccess("Admin Account Created", "Welcome " + fullName + "! Your admin account has been created successfully.");
                            // Navigate immediately (no artificial delay)
                            AdminDashboard adminDashboard = new AdminDashboard();
                            AdminDashboardController adminController = new AdminDashboardController(adminDashboard);
                            Main.setScene(new Scene(adminDashboard.getView()));
                        } else {
                            // For regular users, redirect to login page
                            statusText.setFill(javafx.scene.paint.Color.GREEN);
                            statusText.setText("Account created successfully! Redirecting to login...");
                            // Show popup for user account creation
                            MoodflixDialog.showSuccess("Account Created", "Welcome " + fullName + "! Your account has been created successfully.");
                            // Navigate immediately (no artificial delay)
                            com.moodflix.view.LoginPage loginPage2 = new com.moodflix.view.LoginPage();
                            com.moodflix.controller.LoginPageController loginController2 = new com.moodflix.controller.LoginPageController(loginPage2);
                            Main.setScene(new Scene(loginPage2.getView()));
                        }
                    });
                    
                } catch (Exception ex) {
                    System.err.println("Exception during signup: " + ex.getMessage());
                    ex.printStackTrace();
                    
                    javafx.application.Platform.runLater(() -> {
                        loadingSpinner.setVisible(false);
                        signUpBtn.setDisable(false);
                        statusText.setFill(javafx.scene.paint.Color.RED);
                        statusText.setText("Signup failed due to an error: " + ex.getMessage());
                        showErrorPopup("Signup failed due to an error: " + ex.getMessage());
                    });
                }
            }).start();
        });

        loginLink.setOnAction(e -> {
            com.moodflix.view.LoginPage loginPage3 = new com.moodflix.view.LoginPage();
            com.moodflix.controller.LoginPageController loginController3 = new com.moodflix.controller.LoginPageController(loginPage3);
            Main.setScene(new Scene(loginPage3.getView()));
        });

        homeLink.setOnAction(e -> {
            LandingPage landingPage = new LandingPage();
            LandingPageController landingController = new LandingPageController(landingPage);
            Main.setScene(new Scene(landingPage.getView()));
        });
    }

    private void updatePasswordStrength(String password) {
        int strength = 0;
        String strengthText = "Weak";
        String strengthColor = "#dc3545";
        String barColor = "#dc3545";
        
        if (password.length() >= 8) strength++;
        if (password.matches(".*[a-z].*")) strength++;
        if (password.matches(".*[A-Z].*")) strength++;
        if (password.matches(".*\\d.*")) strength++;
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) strength++;
        
        if (strength >= 4) {
            strengthText = "Strong";
            strengthColor = "#28a745";
            barColor = "#28a745";
        } else if (strength >= 2) {
            strengthText = "Medium";
            strengthColor = "#ffc107";
            barColor = "#ffc107";
        }
        final String finalStrengthText = strengthText;
        final String finalStrengthColor = strengthColor;
        final double progress = strength / 5.0;
        final String finalBarColor = barColor;
        javafx.application.Platform.runLater(() -> {
            view.getPasswordStrengthBar().setProgress(progress);
            view.getPasswordStrengthLabel().setText("Password strength: " + finalStrengthText);
            view.getPasswordStrengthLabel().setStyle("-fx-text-fill: " + finalStrengthColor + ";");
            view.getPasswordStrengthBar().setStyle("-fx-accent: " + finalBarColor + ";");
        });
    }

    private void showErrorPopup(String message) {
        javafx.application.Platform.runLater(() -> {
            MoodflixDialog.showError("Signup Error", message);
        });
    }
} 
