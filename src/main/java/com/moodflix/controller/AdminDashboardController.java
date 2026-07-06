package com.moodflix.controller;

import com.moodflix.model.Content;
import com.moodflix.service.PostgreSQLContentService;
import com.moodflix.view.AdminDashboard;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.json.JSONObject;
import java.util.Iterator;
import java.util.HashMap;
import com.moodflix.util.SessionManager;
import com.moodflix.Main;
import com.moodflix.util.ImageCache;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class AdminDashboardController {
    private final AdminDashboard view;
    private final ObservableList<Content> contentList = FXCollections.observableArrayList();
    private final HashMap<Content, String> contentKeyMap = new HashMap<>();

    public AdminDashboardController(AdminDashboard view) {
        this.view = view;
        setupEventHandlers();
        loadContentTable();
        // Load admin profile photo in sidebar
        loadAdminProfilePhoto();
        // Add manual refresh button
        addRefreshButton();
    }

    private void setupEventHandlers() {
        Button uploadBtn = view.getUploadBtn();
        TextField titleField = view.getTitleField();
        ComboBox<String> moodBox = view.getMoodBox();
        ComboBox<String> typeBox = view.getTypeBox();
        TextField linkField = view.getLinkField();
        TextField descriptionField = view.getDescriptionField(); // Add this line if not present
        TextField imageUrlField = view.getImageUrlField();
        Label statusLabel = view.getStatusLabel();
        TableView<Content> contentTable = (TableView<Content>) view.getContentTable();
        Button editBtn = view.getEditBtn();
        Button deleteBtn = view.getDeleteBtn();
        Label manageStatus = view.getManageStatus();

        uploadBtn.setOnAction(e -> {
            System.out.println("Upload button clicked!");
            String title = titleField.getText();
            String mood = moodBox.getValue();
            String type = typeBox.getValue();
            String link = linkField.getText();
            String description = descriptionField.getText();
            String imageUrl = imageUrlField.getText();
            
            System.out.println("Uploading content: " + title + " | " + mood + " | " + type + " | " + link);
            
            if (title.isEmpty() || mood == null || type == null || link.isEmpty()) {
                statusLabel.setText(" Please fill all fields.");
                System.out.println("Validation failed: Empty fields detected");
                return;
            }
            
            // Disable button during upload
            uploadBtn.setDisable(true);
            statusLabel.setText("Uploading content... Please wait.");
            
            try {
                Content content = new Content(title, mood, type, link, description, imageUrl);
                PostgreSQLContentService service = new PostgreSQLContentService();
                System.out.println("Attempting to upload content to database...");
                service.uploadContent(content);
                System.out.println("Content uploaded successfully!");
                statusLabel.setText(" Content uploaded successfully!");
                titleField.clear();
                linkField.clear();
                moodBox.setValue(null);
                typeBox.setValue(null);
                loadContentTable();
                // Show success popup
                com.moodflix.util.MoodflixDialog.showSuccess("Success", "Content added successfully!");
            } catch (Exception ex) {
                System.err.println("Upload failed: " + ex.getMessage());
                ex.printStackTrace();
                statusLabel.setText(" Upload failed: " + ex.getMessage());
            } finally {
                uploadBtn.setDisable(false);
            }
        });

        editBtn.setOnAction(e -> {
            Content selected = contentTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                Dialog<Content> dialog = new Dialog<>();
                dialog.setTitle("Edit Content");
                dialog.setHeaderText("Edit all content details");
                ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
                dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

                GridPane editGrid = new GridPane();
                editGrid.setHgap(10);
                editGrid.setVgap(10);
                editGrid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

                TextField editTitleField = new TextField(selected.getTitle());
                ComboBox<String> editMoodBox = new ComboBox<>();
                editMoodBox.getItems().addAll("Happy", "Sad", "Thriller", "Feel Good", "Comedy", "Romantic");
                editMoodBox.setValue(selected.getMood());
                ComboBox<String> editTypeBox = new ComboBox<>();
                editTypeBox.getItems().addAll("Movie", "Series", "Song", "Trailer", "Shorts");
                editTypeBox.setValue(selected.getType());
                TextField editLinkField = new TextField(selected.getLink());
                TextField editDescriptionField = new TextField(selected.getDescription());
                TextField editImageUrlField = new TextField(selected.getImageUrl());

                editGrid.add(new Label("Title:"), 0, 0); editGrid.add(editTitleField, 1, 0);
                editGrid.add(new Label("Mood:"), 0, 1); editGrid.add(editMoodBox, 1, 1);
                editGrid.add(new Label("Type:"), 0, 2); editGrid.add(editTypeBox, 1, 2);
                editGrid.add(new Label("Link:"), 0, 3); editGrid.add(editLinkField, 1, 3);
                editGrid.add(new Label("Description:"), 0, 4); editGrid.add(editDescriptionField, 1, 4);
                editGrid.add(new Label("Image URL:"), 0, 5); editGrid.add(editImageUrlField, 1, 5);

                dialog.getDialogPane().setContent(editGrid);
                dialog.setResultConverter(dialogButton -> {
                    if (dialogButton == saveButtonType) {
                        selected.setTitle(editTitleField.getText());
                        selected.setMood(editMoodBox.getValue());
                        selected.setType(editTypeBox.getValue());
                        selected.setLink(editLinkField.getText());
                        selected.setDescription(editDescriptionField.getText());
                        selected.setImageUrl(editImageUrlField.getText());
                        return selected;
                    }
                    return null;
                });
                dialog.showAndWait().ifPresent(updatedContent -> {
                    try {
                        String key = contentKeyMap.get(selected);
                        PostgreSQLContentService service = new PostgreSQLContentService();
                        service.updateContent(key, updatedContent);
                        contentTable.refresh();
                        manageStatus.setText("Content updated successfully!");
                        // Show popup alert for success
                        com.moodflix.util.MoodflixDialog.showSuccess("Success", "Data edited successfully!");
                    } catch (Exception ex) {
                        manageStatus.setText("Error updating content.");
                        ex.printStackTrace();
                    }
                });
            }
        });

        deleteBtn.setOnAction(e -> {
            Content selected = contentTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                try {
                    String key = contentKeyMap.get(selected);
                    PostgreSQLContentService service = new PostgreSQLContentService();
                    service.deleteContentByKey(key);
                    contentList.remove(selected);
                    manageStatus.setText("Content deleted.");
                    // Show popup alert for success
                    com.moodflix.util.MoodflixDialog.showSuccess("Success", "Content deleted successfully!");
                } catch (Exception ex) {
                    manageStatus.setText("Error deleting content.");
                    ex.printStackTrace();
                }
            }
        });

        // Sidebar navigation wiring
        Button profileBtn = view.getProfileBtn();
        Button feedbackBtn = view.getFeedbackBtn();
        Button activityHistoryBtn = view.getActivityHistoryBtn();
        Button userManagementBtn = view.getUserManagementBtn();
        Button logoutBtn = view.getLogoutBtn();

        profileBtn.setOnAction(e -> {
            String adminEmail = SessionManager.getEmail();
            com.moodflix.view.AdminProfilePage adminProfileView = new com.moodflix.view.AdminProfilePage(adminEmail);
            com.moodflix.controller.AdminProfilePageController adminProfileController = new com.moodflix.controller.AdminProfilePageController(adminProfileView, adminEmail);
            Main.setScene(new javafx.scene.Scene(adminProfileView.getView()));
        });

        feedbackBtn.setOnAction(e -> {
            com.moodflix.view.FeedbackPage feedbackView = new com.moodflix.view.FeedbackPage();
            com.moodflix.controller.FeedbackPageController feedbackController = new com.moodflix.controller.FeedbackPageController(feedbackView);
            Main.setScene(new javafx.scene.Scene(feedbackView.getView()));
        });

        activityHistoryBtn.setOnAction(e -> {
            System.out.println("📊 Opening Activity History page...");
            try {
                com.moodflix.view.ActivityHistoryPage activityView = new com.moodflix.view.ActivityHistoryPage();
                com.moodflix.controller.ActivityHistoryController activityController = new com.moodflix.controller.ActivityHistoryController(activityView);
                // Optionally, pass real analytics data to the controller here
                Main.setScene(new javafx.scene.Scene(activityView.getView()));
            } catch (Exception ex) {
                System.err.println("Error opening Activity History: " + ex.getMessage());
                ex.printStackTrace();
                com.moodflix.util.MoodflixDialog.showError("Navigation Error", "Unable to open Activity History page: " + ex.getMessage());
            }
        });

        userManagementBtn.setOnAction(e -> {
            System.out.println("👥 Opening User Management page...");
            try {
                com.moodflix.view.AdminUserManagementPage userManagementView = new com.moodflix.view.AdminUserManagementPage();
                com.moodflix.controller.AdminUserManagementController userManagementController = new com.moodflix.controller.AdminUserManagementController(userManagementView);
                Main.setScene(new javafx.scene.Scene(userManagementView.getView()));
            } catch (Exception ex) {
                System.err.println("Error opening User Management: " + ex.getMessage());
                ex.printStackTrace();
                com.moodflix.util.MoodflixDialog.showError("Navigation Error", "Unable to open User Management page: " + ex.getMessage());
            }
        });

        logoutBtn.setOnAction(e -> {
            System.out.println("🚪 Admin logout button clicked");
            // Use centralized logout manager
            com.moodflix.util.LogoutManager.performAdminLogoutWithStage(logoutBtn);
        });
    }

    private void loadContentTable() {
        System.out.println("Loading content table...");
        TableView<Content> contentTable = (TableView<Content>) view.getContentTable();
        contentList.clear();
        contentKeyMap.clear();
        try {
            PostgreSQLContentService service = new PostgreSQLContentService();
            System.out.println("Fetching content from database...");
            String json = service.getAllContentJson();
            System.out.println("Raw JSON response: " + json);
            
            if (json != null && !json.equals("null") && !json.isEmpty()) {
                JSONObject obj = new JSONObject(json);
                Iterator<String> keys = obj.keys();
                int count = 0;
                while (keys.hasNext()) {
                    String key = keys.next();
                    JSONObject item = obj.getJSONObject(key);
                    Content c = new Content(
                        item.optString("title", ""),
                        item.optString("mood", ""),
                        item.optString("type", ""),
                        item.optString("link", ""),
                        item.optString("description", ""),
                        item.optString("imageUrl", "")
                    );
                    contentList.add(c);
                    contentKeyMap.put(c, key);
                    count++;
                }
                System.out.println("Loaded " + count + " content items");
            } else {
                System.out.println("No content found or empty response");
            }
        } catch (Exception ex) {
            System.err.println("Error loading content table: " + ex.getMessage());
            ex.printStackTrace();
        }
        contentTable.setItems(contentList);
        System.out.println("Content table updated with " + contentList.size() + " items");
    }

    private void loadAdminProfilePhoto() {
        String adminEmail = SessionManager.getEmail();
        if (adminEmail == null) return;
        ImageView profilePhotoView = view.getProfilePhotoView();
        try {
            com.moodflix.service.PostgreSQLAuthService authService = new com.moodflix.service.PostgreSQLAuthService();
            org.json.JSONObject userObj = authService.getUserDetails(adminEmail);
            if (userObj != null && userObj.has("profilePicUrl") && !userObj.getString("profilePicUrl").isEmpty()) {
                String photoUrl = userObj.getString("profilePicUrl");
                Image profileImage = ImageCache.getImage(photoUrl);
                if (profileImage != null && !profileImage.isError()) {
                    profilePhotoView.setImage(profileImage);
                }
            }
        } catch (Exception ex) {
            System.err.println("Error loading admin profile photo: " + ex.getMessage());
        }
    }

    private void addRefreshButton() {
        Button refreshBtn = new Button("Refresh Content");
        refreshBtn.setStyle("-fx-background-color: #17a2b8; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 6 14; -fx-font-size: 13;");
        refreshBtn.setOnAction(e -> {
            com.moodflix.service.PostgreSQLContentService.invalidateCache();
            loadContentTable();
            com.moodflix.view.MoodflixToast.show(view.getView().getScene(), "Content cache refreshed!", com.moodflix.view.MoodflixToast.ToastType.SUCCESS);
        });
        // Add to the view (e.g., top of main content)
        if (view.getContentTable() != null && view.getContentTable().getParent() instanceof VBox) {
            VBox parent = (VBox) view.getContentTable().getParent();
            parent.getChildren().add(0, refreshBtn);
        }
    }
} 