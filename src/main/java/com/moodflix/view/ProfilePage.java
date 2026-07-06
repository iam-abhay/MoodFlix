package com.moodflix.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import com.moodflix.util.ThemeManager;

public class ProfilePage {
    private VBox view;
    private Label emailLabel;
    private Label displayNameLabel;
    private TextField displayNameField;
    private ImageView profileImageView;
    private Button uploadPicBtn;
    private Button saveBtn;
    private Label statusLabel;
    private Button backBtn;
    private Label userNameLabel;
    private Label userRoleLabel;
    private Label joinDateLabel;
    private Label lastLoginLabel;
    private VBox userDetailsBox;
    private VBox profileSection;
    private VBox detailsSection;
    private ScrollPane scrollPane;
    private TextField ageField;
    private ComboBox<String> genderField;
    private Label phoneValueLabel;
    private Label countryValueLabel;
    private Label languageValueLabel;
    private Label birthDateValueLabel;
    private Label newsletterValueLabel;

    public ProfilePage(String userEmail) {
        createView(userEmail);
    }

    private void createView(String userEmail) {
        view = new VBox(28);
        view.setAlignment(Pos.TOP_CENTER);
        view.setPadding(new Insets(40));
        view.getStyleClass().add("auth-container");

        Label title = new Label("👤 User Profile");
        title.getStyleClass().add("hero-title");
        title.setStyle("-fx-font-size: 32px;");

        createProfileSection();
        createUserDetailsSection(userEmail);
        createActionButtons();

        view.getChildren().addAll(title, profileSection, detailsSection);
        scrollPane = new ScrollPane(view);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        // Entrance animations
        view.sceneProperty().addListener((obs, o, n) -> {
            if (n != null) {
                ThemeManager.fadeIn(title, 400, 0);
                ThemeManager.slideUp(profileSection, 500, 150);
                ThemeManager.slideUp(detailsSection, 500, 300);
            }
        });
    }

    private void createProfileSection() {
        profileSection = new VBox(18);
        profileSection.setAlignment(Pos.CENTER);
        profileSection.setPadding(new Insets(24));
        profileSection.getStyleClass().add("glass-card");
        profileSection.setStyle("-fx-max-width: 600;");

        profileImageView = new ImageView();
        profileImageView.setFitWidth(150);
        profileImageView.setFitHeight(150);
        profileImageView.setPreserveRatio(true);
        Circle clip = new Circle(75, 75, 75);
        profileImageView.setClip(clip);

        try {
            javafx.scene.image.Image defaultImage = new javafx.scene.image.Image(
                    getClass().getResourceAsStream("/images/default-profile.png"));
            if (!defaultImage.isError()) {
                profileImageView.setImage(defaultImage);
            }
        } catch (Exception ignored) {}

        uploadPicBtn = new Button("📷 Upload Photo");
        uploadPicBtn.getStyleClass().addAll("btn", "btn-outline");
        profileSection.getChildren().addAll(profileImageView, uploadPicBtn);
    }

    private void createUserDetailsSection(String userEmail) {
        detailsSection = new VBox(22);
        detailsSection.setAlignment(Pos.CENTER);
        detailsSection.setPadding(new Insets(28));
        detailsSection.getStyleClass().add("glass-card");
        detailsSection.setStyle("-fx-max-width: 600;");

        userDetailsBox = new VBox(14);
        userDetailsBox.setAlignment(Pos.CENTER);

        userNameLabel = new Label("👋 Welcome!");
        userNameLabel.getStyleClass().add("section-title");
        userNameLabel.setStyle("-fx-font-size: 20px;");

        emailLabel = new Label("📧 " + userEmail);
        emailLabel.getStyleClass().add("label-accent");

        VBox personalInfoSection = createPersonalInfoSection();
        VBox accountInfoSection = createAccountInfoSection();

        userDetailsBox.getChildren().addAll(
            userNameLabel, emailLabel, new Separator(),
            personalInfoSection, new Separator(), accountInfoSection
        );

        detailsSection.getChildren().add(userDetailsBox);
    }

    private VBox createPersonalInfoSection() {
        VBox personalSection = new VBox(12);
        personalSection.setAlignment(Pos.CENTER_LEFT);
        personalSection.setPadding(new Insets(18));
        personalSection.getStyleClass().add("card");

        Label sectionTitle = new Label("👤 Personal Information");
        sectionTitle.getStyleClass().add("section-title");
        sectionTitle.setStyle("-fx-font-size: 16px;");

        Label fullNameLabel = new Label("👤 Full Name:");
        fullNameLabel.getStyleClass().add("label-secondary");
        displayNameField = new TextField();
        displayNameField.setPromptText("Enter your full name");
        displayNameField.setPrefWidth(300);

        Label ageLabel = new Label("🎂 Age:");
        ageLabel.getStyleClass().add("label-secondary");
        ageField = new TextField();
        ageField.setPromptText("Enter your age");
        ageField.setPrefWidth(300);

        Label genderLabel = new Label("⚧ Gender:");
        genderLabel.getStyleClass().add("label-secondary");
        genderField = new ComboBox<>();
        genderField.getItems().addAll("Male", "Female", "Other");
        genderField.setPromptText("Select your gender");
        genderField.setPrefWidth(300);

        Label phoneLabel = new Label("📱 Phone:");
        phoneLabel.getStyleClass().add("label-secondary");
        phoneValueLabel = new Label();
        phoneValueLabel.getStyleClass().add("label-muted");

        Label countryLabel = new Label("🌍 Country:");
        countryLabel.getStyleClass().add("label-secondary");
        countryValueLabel = new Label();
        countryValueLabel.getStyleClass().add("label-muted");

        Label languageLabel = new Label("🗣️ Language:");
        languageLabel.getStyleClass().add("label-secondary");
        languageValueLabel = new Label();
        languageValueLabel.getStyleClass().add("label-muted");

        Label birthDateLabel = new Label("🎂 Birth Date:");
        birthDateLabel.getStyleClass().add("label-secondary");
        birthDateValueLabel = new Label();
        birthDateValueLabel.getStyleClass().add("label-muted");

        userRoleLabel = new Label();

        personalSection.getChildren().addAll(
            sectionTitle,
            fullNameLabel, displayNameField,
            ageLabel, ageField,
            genderLabel, genderField,
            phoneLabel, phoneValueLabel,
            countryLabel, countryValueLabel,
            languageLabel, languageValueLabel,
            birthDateLabel, birthDateValueLabel,
            userRoleLabel
        );
        return personalSection;
    }

    private VBox createAccountInfoSection() {
        VBox accountSection = new VBox(10);
        accountSection.setAlignment(Pos.CENTER_LEFT);
        accountSection.setPadding(new Insets(18));
        accountSection.getStyleClass().add("card");

        Label sectionTitle = new Label("📊 Account Information");
        sectionTitle.getStyleClass().add("section-title");
        sectionTitle.setStyle("-fx-font-size: 16px;");

        joinDateLabel = new Label("📅 Joined: " +
                java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("MMMM dd, yyyy")));
        joinDateLabel.getStyleClass().add("label-muted");

        lastLoginLabel = new Label("🕒 Last Login: " +
                java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' HH:mm")));
        lastLoginLabel.getStyleClass().add("label-muted");

        Label accountStatusLabel = new Label("Account Status: Active");
        accountStatusLabel.getStyleClass().add("badge-success");

        Label memberSinceLabel = new Label("🎭 Member Since: December 2024");
        memberSinceLabel.getStyleClass().add("label-muted");

        accountSection.getChildren().addAll(sectionTitle, joinDateLabel, lastLoginLabel, accountStatusLabel, memberSinceLabel);
        return accountSection;
    }

    public void updateRoleDisplay(String role) {
        if ("admin".equals(role)) {
            userRoleLabel.setText("👑 Role: Admin");
            userRoleLabel.getStyleClass().setAll("badge-danger");
        } else {
            userRoleLabel.setText("🎭 Role: User");
            userRoleLabel.getStyleClass().setAll("badge-success");
        }
    }

    private void createActionButtons() {
        HBox buttonBox = new HBox(16);
        buttonBox.setAlignment(Pos.CENTER);

        saveBtn = new Button("💾 Save Changes");
        saveBtn.getStyleClass().addAll("btn", "btn-success");

        backBtn = new Button("← Back to Dashboard");
        backBtn.getStyleClass().addAll("btn", "btn-outline");

        buttonBox.getChildren().addAll(saveBtn, backBtn);
        detailsSection.getChildren().add(buttonBox);

        statusLabel = new Label();
        statusLabel.getStyleClass().add("label-secondary");
        statusLabel.setAlignment(Pos.CENTER);
        statusLabel.setMaxWidth(Double.MAX_VALUE);
        detailsSection.getChildren().add(statusLabel);
    }

    // Getters
    public ScrollPane getView() { return scrollPane; }
    public Label getEmailLabel() { return emailLabel; }
    public TextField getDisplayNameField() { return displayNameField; }
    public ImageView getProfileImageView() { return profileImageView; }
    public Button getUploadPicBtn() { return uploadPicBtn; }
    public Button getSaveBtn() { return saveBtn; }
    public Label getStatusLabel() { return statusLabel; }
    public Button getBackBtn() { return backBtn; }
    public Label getUserNameLabel() { return userNameLabel; }
    public Label getUserRoleLabel() { return userRoleLabel; }
    public Label getJoinDateLabel() { return joinDateLabel; }
    public Label getLastLoginLabel() { return lastLoginLabel; }
    public TextField getAgeField() { return ageField; }
    public ComboBox<String> getGenderField() { return genderField; }
    public Label getPhoneValueLabel() { return phoneValueLabel; }
    public Label getCountryValueLabel() { return countryValueLabel; }
    public Label getLanguageValueLabel() { return languageValueLabel; }
    public Label getBirthDateValueLabel() { return birthDateValueLabel; }
    public Label getNewsletterValueLabel() { return newsletterValueLabel; }
} 
