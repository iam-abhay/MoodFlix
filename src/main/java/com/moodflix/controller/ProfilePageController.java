package com.moodflix.controller;

import com.moodflix.service.PostgreSQLAuthService;
import com.moodflix.util.SessionManager;
import com.moodflix.view.ProfilePage;
import com.moodflix.view.UserDashboard;
import com.moodflix.controller.UserDashboardController;
import com.moodflix.Main;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.scene.Scene;
import org.json.JSONObject;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import com.moodflix.util.ImageCache;
import com.moodflix.util.MoodflixDialog;

public class ProfilePageController {
    private final ProfilePage view;
    private String profilePicUrl = null;
    private String userEmail;

    public ProfilePageController(ProfilePage view, String userEmail) {
        this.view = view;
        this.userEmail = userEmail;
        setupEventHandlers();
        loadUserDetails(userEmail);
    }

    private void setupEventHandlers() {
        Button uploadPicBtn = view.getUploadPicBtn();
        ImageView profileImageView = view.getProfileImageView();
        Button saveBtn = view.getSaveBtn();
        Button backBtn = view.getBackBtn();
        TextField displayNameField = view.getDisplayNameField();
        Label statusLabel = view.getStatusLabel();

        uploadPicBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Profile Picture");
            fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
            );
            File selectedFile = fileChooser.showOpenDialog(profileImageView.getScene().getWindow());
            if (selectedFile != null) {
                try {
                    PostgreSQLAuthService service = new PostgreSQLAuthService();
                    String destFileName = "profile_photos/" + userEmail.replace("@", "_at_").replace(".", "_") + ".png";
                    String downloadUrl = service.uploadProfileImage(selectedFile, destFileName);
                    if (downloadUrl != null) {
                        Image newImage = ImageCache.getImage(downloadUrl);
                        profileImageView.setImage(newImage);
                        profilePicUrl = downloadUrl;
                        statusLabel.setText("Photo uploaded successfully!");
                        statusLabel.setStyle("-fx-text-fill: #28a745; -fx-font-weight: bold;");
                    } else {
                        statusLabel.setText("Error uploading image.");
                        statusLabel.setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;");
                    }
                } catch (Exception ex) {
                    ///statusLabel.setText("Error loading image. Please try again.");
                    //statusLabel.setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;");
                }
            }
        });

        saveBtn.setOnAction(e -> {
            String newDisplayName = displayNameField.getText().trim();
            String age = view.getAgeField().getText().trim();
            String gender = view.getGenderField().getValue() != null ? view.getGenderField().getValue() : "";
            if (newDisplayName.isEmpty()) {
                statusLabel.setText("Please enter a display name.");
                statusLabel.setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;");
                return;
            }
            saveBtn.setDisable(true);
            statusLabel.setText("🔄 Saving changes...");
            new Thread(() -> {
                try {
                    PostgreSQLAuthService service = new PostgreSQLAuthService();
                    service.updateUserDetails(userEmail, newDisplayName, age, gender, "user");
                    javafx.application.Platform.runLater(() -> {
                        saveBtn.setDisable(false);
                        statusLabel.setText("Profile updated successfully!");
                        statusLabel.setStyle("-fx-text-fill: #28a745; -fx-font-weight: bold;");
                        view.getUserNameLabel().setText("👋 Welcome, " + newDisplayName + "!");
                        MoodflixDialog.showSuccess("Profile Updated", "Your profile has been updated successfully.\nDisplay Name: " + newDisplayName);
                    });
                } catch (Exception ex) {
                    javafx.application.Platform.runLater(() -> {
                        saveBtn.setDisable(false);
                        statusLabel.setText("Error updating profile. Please try again.");
                        statusLabel.setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;");
                        MoodflixDialog.showError("Update Failed", "Unable to update your profile: " + ex.getMessage());
                    });
                }
            }).start();
        });

        backBtn.setOnAction(e -> {
            System.out.println("[DEBUG] Profile back button clicked");
            String operationId = com.moodflix.util.PerformanceMonitor.startOperation("profile_back_navigation");
            
            // Use optimized back navigation
            String userEmail = com.moodflix.util.SessionManager.getEmail();
            com.moodflix.util.BackNavigationOptimizer.smartBackNavigation(userEmail);
            
            com.moodflix.util.PerformanceMonitor.endOperation(operationId, true);
        });
    }

    public void loadUserDetails(String targetEmail) {
        TextField displayNameField = view.getDisplayNameField();
        ImageView profileImageView = view.getProfileImageView();
        Label statusLabel = view.getStatusLabel();
        Label userNameLabel = view.getUserNameLabel();
        Label userRoleLabel = view.getUserRoleLabel();
        Label joinDateLabel = view.getJoinDateLabel();
        Label lastLoginLabel = view.getLastLoginLabel();
        TextField ageField = view.getAgeField();
        ComboBox<String> genderField = view.getGenderField();
        try {
            PostgreSQLAuthService service = new PostgreSQLAuthService();
            JSONObject userObj = service.getUserDetails(targetEmail);
            if (userObj != null && !userObj.toString().equals("null")) {
                // Prefer displayName, fallback to fullName, fallback to email prefix
                String displayName = null;
                if (userObj.has("displayName") && !userObj.getString("displayName").isEmpty()) {
                    displayName = userObj.getString("displayName");
                } else if (userObj.has("fullName") && !userObj.getString("fullName").isEmpty()) {
                    displayName = userObj.getString("fullName");
                } else {
                    displayName = targetEmail.split("@")[0];
                }
                displayNameField.setText(displayName);
                userNameLabel.setText("👋 Welcome, " + displayName + "!");

                if (userObj.has("role")) {
                    String role = userObj.getString("role");
                    view.updateRoleDisplay(role);
                }
                if (userObj.has("profilePicUrl") && !userObj.getString("profilePicUrl").isEmpty()) {
                    String photoUrl = userObj.getString("profilePicUrl");
                    try {
                        Image profileImage = ImageCache.getImage(photoUrl);
                        if (profileImage != null && !profileImage.isError()) {
                            profileImageView.setImage(profileImage);
                            profilePicUrl = photoUrl;
                        }
                    } catch (Exception ex) {
                        // ignore
                    }
                }
                if (userObj.has("age")) {
                    String age = userObj.getString("age");
                    ageField.setText(age);
                }
                if (userObj.has("gender")) {
                    String gender = userObj.getString("gender");
                    genderField.setValue(gender);
                }
                if (userObj.has("phone")) {
                    String phone = userObj.getString("phone");
                    view.getPhoneValueLabel().setText(phone);
                }
                if (userObj.has("country")) {
                    String country = userObj.getString("country");
                    view.getCountryValueLabel().setText(country);
                }
                if (userObj.has("language")) {
                    String language = userObj.getString("language");
                    view.getLanguageValueLabel().setText(language);
                }
                if (userObj.has("birthDate")) {
                    String birthDate = userObj.getString("birthDate");
                    view.getBirthDateValueLabel().setText(birthDate);
                }
                // Support both newsletter and newsletterSubscribed keys
                if (userObj.has("newsletter")) {
                    boolean newsletter = userObj.getBoolean("newsletter");
                    view.getNewsletterValueLabel().setText(newsletter ? "Yes" : "No");
                } else if (userObj.has("newsletterSubscribed")) {
                    boolean newsletter = userObj.getBoolean("newsletterSubscribed");
                    view.getNewsletterValueLabel().setText(newsletter ? "Yes" : "No");
                }
                if (userObj.has("joinDate")) {
                    String joinDate = userObj.getString("joinDate");
                    joinDateLabel.setText("📅 Joined: " + joinDate);
                }
                if (userObj.has("lastLogin")) {
                    String lastLogin = userObj.getString("lastLogin");
                    lastLoginLabel.setText("🕒 Last Login: " + lastLogin);
                }
                statusLabel.setText("User details loaded successfully");
                statusLabel.setStyle("-fx-text-fill: #28a745; -fx-font-weight: bold;");
            } else {
                statusLabel.setText("User not found");
                statusLabel.setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;");
            }
        } catch (Exception ex) {
            statusLabel.setText("Error loading user details");
            statusLabel.setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;");
        }
    }
} 