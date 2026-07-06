package com.moodflix.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import com.moodflix.util.ThemeManager;
import com.moodflix.util.ImageCache;

public class AdminDashboard {
    private BorderPane view;
    private TextField titleField;
    private ComboBox<String> moodBox;
    private ComboBox<String> typeBox;
    private TextField linkField;
    private Button uploadBtn;
    private Label statusLabel;
    private TableView<com.moodflix.model.Content> contentTable;
    private Button editBtn;
    private Button deleteBtn;
    private Label manageStatus;
    private Button logoutBtn;
    private Button profileBtn;
    private Button feedbackBtn;
    private Button activityHistoryBtn;
    private Button userManagementBtn;
    private VBox mainContent;
    private VBox sidebar;
    private VBox headerSection;
    private HBox contentSection;
    private VBox uploadSection;
    private VBox manageSection;
    private Label adminTitle;
    private Label adminSubtitle;
    private ScrollPane scrollPane;
    private TextField descriptionField;
    private TextField imageUrlField;
    private ImageView profilePhotoView;
    private ScrollPane uploadScrollPane;
    private ScrollPane manageScrollPane;

    public AdminDashboard() {
        System.out.println(" AdminDashboard constructor called");
        createView();
    }

    private void createView() {
        System.out.println(" AdminDashboard.createView() called");
        view = new BorderPane();
        view.getStyleClass().add("root");

        createSidebar();
        System.out.println("Sidebar created");
        createMainContent();
        System.out.println("Main content created");

        view.setLeft(sidebar);
        scrollPane = new ScrollPane(mainContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        view.setCenter(scrollPane);

        ThemeManager.fadeIn(mainContent, 400);
        ThemeManager.slideUp(headerSection, 500, 100);
        ThemeManager.slideUp(contentSection, 500, 250);
        System.out.println(" AdminDashboard view fully constructed");
    }

    private void createSidebar() {
        sidebar = new VBox(18);
        sidebar.setAlignment(Pos.TOP_CENTER);
        sidebar.setPadding(new Insets(28, 16, 28, 16));
        sidebar.setPrefWidth(200);
        sidebar.getStyleClass().add("sidebar");

        VBox avatarSection = new VBox(10);
        avatarSection.setAlignment(Pos.CENTER);

        profilePhotoView = new ImageView();
        profilePhotoView.setFitWidth(64);
        profilePhotoView.setFitHeight(64);
        profilePhotoView.setPreserveRatio(true);
        javafx.scene.shape.Circle clip = new javafx.scene.shape.Circle(32, 32, 32);
        profilePhotoView.setClip(clip);
        try {
            Image defaultImage = ImageCache.getImage("/images/default-profile.png");
            if (defaultImage != null && !defaultImage.isError()) {
                profilePhotoView.setImage(defaultImage);
            }
        } catch (Exception e) {
            // No default image
        }

        Label avatarIcon = new Label("🎬 ADMIN");
        avatarIcon.getStyleClass().add("hero-title");
        avatarIcon.setStyle("-fx-font-size: 20px;");

        Label adminLabel = new Label("Admin Panel");
        adminLabel.getStyleClass().add("label-secondary");
        adminLabel.setAlignment(Pos.CENTER);

        avatarSection.getChildren().addAll(profilePhotoView, avatarIcon, adminLabel);
        sidebar.getChildren().add(avatarSection);

        profileBtn = createNavButton("Profile");
        feedbackBtn = createNavButton("Feedback");
        activityHistoryBtn = createNavButton("Analytics");
        userManagementBtn = createNavButton("User Management");
        logoutBtn = createNavButton("Logout");
        logoutBtn.getStyleClass().add("btn-danger");

        sidebar.getChildren().addAll(profileBtn, feedbackBtn, activityHistoryBtn, userManagementBtn, logoutBtn);
    }

    private Button createNavButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().addAll("sidebar-btn");
        button.setMaxWidth(Double.MAX_VALUE);
        return button;
    }

    private void createMainContent() {
        mainContent = new VBox(28);
        mainContent.setAlignment(Pos.TOP_CENTER);
        mainContent.setPadding(new Insets(36));

        createHeaderSection();
        createContentSection();

        mainContent.getChildren().addAll(headerSection, contentSection);
    }

    private void createHeaderSection() {
        headerSection = new VBox(8);
        headerSection.setAlignment(Pos.CENTER);
        headerSection.setPadding(new Insets(20));
        headerSection.getStyleClass().add("glass-card");

        adminTitle = new Label("🎬 Moodflix Admin Dashboard");
        adminTitle.getStyleClass().add("hero-title");

        adminSubtitle = new Label("Upload and manage entertainment content for users");
        adminSubtitle.getStyleClass().add("label-secondary");

        headerSection.getChildren().addAll(adminTitle, adminSubtitle);
    }

    private void createContentSection() {
        contentSection = new HBox(24);
        contentSection.setAlignment(Pos.CENTER);

        createUploadSection();
        uploadScrollPane = new ScrollPane(uploadSection);
        uploadScrollPane.setFitToWidth(true);
        uploadScrollPane.setPrefHeight(500);

        createManageSection();
        manageScrollPane = new ScrollPane(manageSection);
        manageScrollPane.setFitToWidth(true);
        manageScrollPane.setPrefHeight(500);

        contentSection.getChildren().addAll(uploadScrollPane, manageScrollPane);
    }

    private void createUploadSection() {
        uploadSection = new VBox(16);
        uploadSection.setAlignment(Pos.TOP_CENTER);
        uploadSection.setPadding(new Insets(24));
        uploadSection.setPrefWidth(400);
        uploadSection.getStyleClass().add("glass-card");

        Label uploadTitle = new Label("Upload New Content");
        uploadTitle.getStyleClass().add("section-title");

        titleField = new TextField();
        titleField.setPromptText("Enter content title (e.g., The Matrix)");

        descriptionField = new TextField();
        descriptionField.setPromptText("Enter a short description");

        imageUrlField = new TextField();
        imageUrlField.setPromptText("Enter direct image URL (e.g., https://...jpg)");
        imageUrlField.setTooltip(new Tooltip("Direct image URL for content poster"));

        moodBox = new ComboBox<>();
        moodBox.getItems().addAll("Happy", "Sad", "Thriller", "Feel Good", "Comedy", "Romantic");
        moodBox.setPromptText("Select target mood");

        typeBox = new ComboBox<>();
        typeBox.getItems().addAll("Movie", "Series", "Song", "Trailer", "Shorts");
        typeBox.setPromptText("Select content type");

        linkField = new TextField();
        linkField.setPromptText("Enter media URL (YouTube/Drive/URL)");

        uploadBtn = new Button("Upload Content");
        uploadBtn.getStyleClass().addAll("btn", "btn-success");

        statusLabel = new Label();
        statusLabel.getStyleClass().add("label-accent");
        statusLabel.setAlignment(Pos.CENTER);
        statusLabel.setMaxWidth(Double.MAX_VALUE);

        Label titleLbl = new Label("Title:");
        titleLbl.getStyleClass().add("label-secondary");
        Label moodLbl = new Label("Mood:");
        moodLbl.getStyleClass().add("label-secondary");
        Label typeLbl = new Label("Type:");
        typeLbl.getStyleClass().add("label-secondary");
        Label linkLbl = new Label("Link:");
        linkLbl.getStyleClass().add("label-secondary");
        Label descLbl = new Label("Description:");
        descLbl.getStyleClass().add("label-secondary");
        Label imgLbl = new Label("Image URL:");
        imgLbl.getStyleClass().add("label-secondary");

        uploadSection.getChildren().addAll(titleLbl, titleField, moodLbl, moodBox, typeLbl, typeBox, linkLbl, linkField, descLbl, descriptionField, imgLbl, imageUrlField, uploadBtn, statusLabel);
    }

    private void createManageSection() {
        manageSection = new VBox(16);
        manageSection.setAlignment(Pos.TOP_CENTER);
        manageSection.setPadding(new Insets(24));
        manageSection.setPrefWidth(500);
        manageSection.getStyleClass().add("glass-card");

        Label manageTitle = new Label("Manage Content");
        manageTitle.getStyleClass().add("section-title");

        contentTable = new TableView<>();
        contentTable.setPrefHeight(300);

        TableColumn<com.moodflix.model.Content, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        titleCol.setPrefWidth(150);

        TableColumn<com.moodflix.model.Content, String> moodCol = new TableColumn<>("Mood");
        moodCol.setCellValueFactory(new PropertyValueFactory<>("mood"));
        moodCol.setPrefWidth(100);

        TableColumn<com.moodflix.model.Content, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        typeCol.setPrefWidth(100);

        TableColumn<com.moodflix.model.Content, String> linkCol = new TableColumn<>("🔗 Link");
        linkCol.setCellValueFactory(new PropertyValueFactory<>("link"));
        linkCol.setPrefWidth(200);

        TableColumn<com.moodflix.model.Content, String> imageCol = new TableColumn<>("Image");
        imageCol.setCellValueFactory(new PropertyValueFactory<>("imageUrl"));
        imageCol.setPrefWidth(80);
        imageCol.setCellFactory(col -> new TableCell<com.moodflix.model.Content, String>() {
            private final ImageView imageView = new ImageView();
            {
                imageView.setFitWidth(60);
                imageView.setFitHeight(40);
                imageView.setPreserveRatio(true);
            }
            @Override
            protected void updateItem(String imageUrl, boolean empty) {
                super.updateItem(imageUrl, empty);
                if (empty || imageUrl == null || imageUrl.isEmpty()) {
                    setGraphic(null);
                } else {
                    try {
                        imageView.setImage(ImageCache.getImage(imageUrl, 60, 40, true, true));
                        setGraphic(imageView);
                    } catch (Exception e) {
                        setGraphic(null);
                    }
                }
            }
        });
        contentTable.getColumns().add(0, imageCol);

        TableColumn<com.moodflix.model.Content, String> imageUrlCol = new TableColumn<>("Image URL");
        imageUrlCol.setCellValueFactory(new PropertyValueFactory<>("imageUrl"));
        imageUrlCol.setPrefWidth(200);
        contentTable.getColumns().add(1, imageUrlCol);

        contentTable.getColumns().addAll(titleCol, moodCol, typeCol, linkCol);

        HBox actionButtons = new HBox(16);
        actionButtons.setAlignment(Pos.CENTER);

        editBtn = new Button("✏️ Edit Selected");
        editBtn.getStyleClass().addAll("btn", "btn-warning");

        deleteBtn = new Button("🗑️ Delete Selected");
        deleteBtn.getStyleClass().addAll("btn", "btn-danger");

        actionButtons.getChildren().addAll(editBtn, deleteBtn);

        manageStatus = new Label();
        manageStatus.getStyleClass().add("label-accent");
        manageStatus.setAlignment(Pos.CENTER);
        manageStatus.setMaxWidth(Double.MAX_VALUE);

        manageSection.getChildren().addAll(manageTitle, contentTable, actionButtons, manageStatus);
    }

    // Getters
    public BorderPane getView() { return view; }
    public TextField getTitleField() { return titleField; }
    public ComboBox<String> getMoodBox() { return moodBox; }
    public ComboBox<String> getTypeBox() { return typeBox; }
    public TextField getLinkField() { return linkField; }
    public Button getUploadBtn() { return uploadBtn; }
    public Label getStatusLabel() { return statusLabel; }
    public TableView<com.moodflix.model.Content> getContentTable() { return contentTable; }
    public Button getEditBtn() { return editBtn; }
    public Button getDeleteBtn() { return deleteBtn; }
    public Label getManageStatus() { return manageStatus; }
    public Button getLogoutBtn() { return logoutBtn; }
    public Button getProfileBtn() { return profileBtn; }
    public Button getFeedbackBtn() { return feedbackBtn; }
    public Button getActivityHistoryBtn() { return activityHistoryBtn; }
    public Button getUserManagementBtn() { return userManagementBtn; }
    public TextField getDescriptionField() { return descriptionField; }
    public TextField getImageUrlField() { return imageUrlField; }
    public ImageView getProfilePhotoView() { return profilePhotoView; }
} 