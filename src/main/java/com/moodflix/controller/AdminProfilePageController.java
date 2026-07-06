package com.moodflix.controller;

import com.moodflix.service.PostgreSQLAuthService;
import com.moodflix.util.SessionManager;
import com.moodflix.view.AdminProfilePage;
import com.moodflix.view.AdminDashboard;
import com.moodflix.Main;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.Scene;
import org.json.JSONObject;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.stage.FileChooser;
import java.io.File;
import com.moodflix.util.ImageCache;
import javafx.scene.layout.HBox;

public class AdminProfilePageController {
    private final AdminProfilePage view;
    private String profilePicUrl = null;
    private String adminEmail;

    public AdminProfilePageController(AdminProfilePage view, String adminEmail) {
        this.view = view;
        this.adminEmail = adminEmail;
        setupEventHandlers();
        loadAdminDetails();
        setFieldsEditable(false);
        addEditButton();
    }

    private void setupEventHandlers() {
        Button uploadPicBtn = view.getUploadPicBtn();
        ImageView profileImageView = view.getProfileImageView();
        Button saveBtn = view.getSaveBtn();
        Button backBtn = view.getBackBtn();
        TextField displayNameField = view.getDisplayNameField();
        Label statusLabel = view.getStatusLabel();

        // Back button: optimized navigation to admin dashboard
        backBtn.setOnAction(e -> {
            String operationId = com.moodflix.util.PerformanceMonitor.startOperation("admin_profile_back_navigation");
            
            // Use optimized back navigation
            if (adminEmail != null) {
                com.moodflix.util.SessionManager.setSession(adminEmail, "admin");
                com.moodflix.util.BackNavigationOptimizer.navigateBackToDashboard(adminEmail, "admin");
            }
            
            com.moodflix.util.PerformanceMonitor.endOperation(operationId, true);
        });

        uploadPicBtn.setOnAction(e -> {
            System.out.println("📷 Upload photo button clicked (admin)");
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Profile Picture");
            fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
            );
            File selectedFile = fileChooser.showOpenDialog(profileImageView.getScene().getWindow());
            if (selectedFile != null) {
                try {
                    PostgreSQLAuthService service = new PostgreSQLAuthService();
                    String destFileName = "profile_photos/" + adminEmail.replace("@", "_at_").replace(".", "_") + ".png";
                    String downloadUrl = service.uploadProfileImage(selectedFile, destFileName);
                    if (downloadUrl != null) {
                        Image newImage = ImageCache.getImage(downloadUrl);
                        profileImageView.setImage(newImage);
                        profilePicUrl = downloadUrl;
                        System.out.println(" Admin profile photo saved: " + downloadUrl);
                        statusLabel.setText(" Photo uploaded successfully!");
                        statusLabel.setStyle("-fx-text-fill: #28a745; -fx-font-weight: bold;");
                    } else {
                        statusLabel.setText(" Error uploading image.");
                        statusLabel.setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;");
                    }
                } catch (Exception ex) {
                    System.err.println("Error loading image: " + ex.getMessage());
                    statusLabel.setText(" Error loading image. Please try again.");
                    statusLabel.setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;");
                }
            }
        });

        // Save Changes button logic
        saveBtn.setOnAction(e -> {
            try {
                String displayName = view.getDisplayNameField().getText();
                String age = view.getAgeField().getText();
                String gender = view.getGenderField().getValue() != null ? view.getGenderField().getValue() : "";
                String picUrl = this.profilePicUrl != null ? this.profilePicUrl : "";
                com.moodflix.service.PostgreSQLAuthService service = new com.moodflix.service.PostgreSQLAuthService();
                service.updateAdminProfileWithPut(adminEmail, displayName, picUrl, age, gender);
                setFieldsEditable(false);
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText(null);
                alert.setContentText("Profile updated successfully!");
                alert.showAndWait();
            } catch (Exception ex) {
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Failed to update profile");
                alert.setContentText(ex.getMessage());
                alert.showAndWait();
            }
        });
    }

    private void loadAdminDetails() {
        System.out.println("🔍 Loading admin details for: " + adminEmail);
        TextField displayNameField = view.getDisplayNameField();
        ImageView profileImageView = view.getProfileImageView();
        Label statusLabel = view.getStatusLabel();
        Label userNameLabel = view.getUserNameLabel();
        Label userRoleLabel = view.getUserRoleLabel();
        Label joinDateLabel = view.getJoinDateLabel();
        Label lastLoginLabel = view.getLastLoginLabel();
        
        // Show loading state
        statusLabel.setText("🔄 Loading admin details...");
        statusLabel.setStyle("-fx-text-fill: #17a2b8; -fx-font-weight: bold;");
        
        // Use async operation for better performance
        PostgreSQLAuthService service = new PostgreSQLAuthService();
        service.getUserDetailsAsync(adminEmail).thenAcceptAsync(userObj -> {
            javafx.application.Platform.runLater(() -> {
                try {
                    if (userObj != null && !userObj.toString().equals("null")) {
                        System.out.println("📋 Admin details found: " + userObj.toString());
                        // Display name
                        if (userObj.has("displayName") && !userObj.getString("displayName").isEmpty()) {
                            String displayName = userObj.getString("displayName");
                            displayNameField.setText(displayName);
                            userNameLabel.setText("👋 Welcome, " + displayName + "!");
                        } else {
                            String defaultName = adminEmail.split("@")[0];
                            displayNameField.setText(defaultName);
                            userNameLabel.setText("👋 Welcome, " + defaultName + "!");
                        }
                        // Age
                        if (userObj.has("age")) {
                            String age = userObj.getString("age");
                            view.getAgeField().setText(age);
                        }
                        // Gender
                        if (userObj.has("gender")) {
                            String gender = userObj.getString("gender");
                            view.getGenderField().setValue(gender);
                        }
                        // Role
                        if (userObj.has("role")) {
                            String role = userObj.getString("role");
                            userRoleLabel.setText("👑 Role: " + (role.equals("admin") ? "Admin" : role));
                        }
                        // Profile photo
                        if (userObj.has("profilePicUrl") && !userObj.getString("profilePicUrl").isEmpty()) {
                            String photoUrl = userObj.getString("profilePicUrl");
                            try {
                                 Image profileImage = ImageCache.getImage(photoUrl);
                                 if (profileImage != null && !profileImage.isError()) {
                                     profileImageView.setImage(profileImage);
                                     profilePicUrl = photoUrl;
                                 }
                            } catch (Exception photoEx) {
                                System.err.println(" Could not load profile photo: " + photoEx.getMessage());
                            }
                        }
                        // Join date
                        if (userObj.has("createdAt")) {
                            try {
                                long createdAt = userObj.getLong("createdAt");
                                LocalDateTime joinDate = LocalDateTime.ofEpochSecond(createdAt / 1000, 0, java.time.ZoneOffset.UTC);
                                String formattedDate = joinDate.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"));
                                joinDateLabel.setText("📅 Joined: " + formattedDate);
                            } catch (Exception dateEx) {
                                System.err.println("⚠️ Could not parse join date: " + dateEx.getMessage());
                            }
                        }
                        // Last login
                        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' HH:mm"));
                        lastLoginLabel.setText("🕒 Last Login: " + currentTime);
                        statusLabel.setText("Admin details loaded successfully");
                        statusLabel.setStyle("-fx-text-fill: #28a745; -fx-font-weight: bold;");
                    } else {
                        String defaultName = adminEmail.split("@")[0];
                        displayNameField.setText(defaultName);
                        userNameLabel.setText("👋 Welcome, " + defaultName + "!");
                        userRoleLabel.setText("👑 Role: Admin");
                        statusLabel.setText("ℹ️ Using default admin profile settings");
                        statusLabel.setStyle("-fx-text-fill: #17a2b8; -fx-font-weight: bold;");
                    }
                } catch (Exception ex) {
                    System.err.println(" Error loading admin details: " + ex.getMessage());
                    ex.printStackTrace();
                    String defaultName = adminEmail.split("@")[0];
                    displayNameField.setText(defaultName);
                    userNameLabel.setText("👋 Welcome, " + defaultName + "!");
                    userRoleLabel.setText("👑 Role: Admin");
                    statusLabel.setText("⚠️ Error loading admin profile, using defaults");
                    statusLabel.setStyle("-fx-text-fill: #ffc107; -fx-font-weight: bold;");
                }
            });
        }).exceptionally(throwable -> {
            javafx.application.Platform.runLater(() -> {
                System.err.println(" Error loading admin details: " + throwable.getMessage());
                String defaultName = adminEmail.split("@")[0];
                displayNameField.setText(defaultName);
                userNameLabel.setText("👋 Welcome, " + defaultName + "!");
                userRoleLabel.setText("👑 Role: Admin");
                statusLabel.setText("⚠️ Error loading admin profile, using defaults");
                statusLabel.setStyle("-fx-text-fill: #ffc107; -fx-font-weight: bold;");
            });
            return null;
        });
    }

    private void addEditButton() {
        // Add Edit button next to Save Changes button
        Button saveBtn = view.getSaveBtn();
        HBox buttonBox = (HBox) saveBtn.getParent();
        Button editBtn = new Button("✏️ Edit");
        editBtn.setStyle("-fx-background-color: #ffc107; -fx-text-fill: #333; -fx-font-weight: bold; -fx-background-radius: 25; -fx-padding: 12 30; -fx-font-size: 14;");
        editBtn.setOnMouseEntered(e -> editBtn.setStyle("-fx-background-color: #ffb300; -fx-text-fill: #333; -fx-font-weight: bold; -fx-background-radius: 25; -fx-padding: 12 30; -fx-font-size: 14;"));
        editBtn.setOnMouseExited(e -> editBtn.setStyle("-fx-background-color: #ffc107; -fx-text-fill: #333; -fx-font-weight: bold; -fx-background-radius: 25; -fx-padding: 12 30; -fx-font-size: 14;"));
        buttonBox.getChildren().add(0, editBtn);
        editBtn.setOnAction(e -> setFieldsEditable(true));
    }

    private void setFieldsEditable(boolean editable) {
        view.getDisplayNameField().setEditable(editable);
        // Enable/disable age and gender fields if they are editable
        // If ageValueLabel and genderValueLabel are Labels, convert to TextField/ComboBox in the view for editing
        try {
            java.lang.reflect.Method getAgeField = view.getClass().getMethod("getAgeField");
            TextField ageField = (TextField) getAgeField.invoke(view);
            ageField.setEditable(editable);
        } catch (Exception ignored) {}
        try {
            java.lang.reflect.Method getGenderField = view.getClass().getMethod("getGenderField");
            ComboBox<String> genderField = (ComboBox<String>) getGenderField.invoke(view);
            genderField.setDisable(!editable);
        } catch (Exception ignored) {}
    }
} 