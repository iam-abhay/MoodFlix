package com.moodflix.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import java.time.LocalDate;
import com.moodflix.util.ThemeManager;

public class SignUpPage {
    private VBox view;
    private TextField emailField;
    private PasswordField passwordField;
    private PasswordField confirmPasswordField;
    private TextField firstNameField;
    private TextField lastNameField;
    private TextField ageField;
    private ComboBox<String> genderBox;
    private ComboBox<String> roleBox;
    private DatePicker birthDatePicker;
    private TextField phoneField;
    private ComboBox<String> countryBox;
    private ComboBox<String> languageBox;
    private CheckBox termsCheckBox;
    private CheckBox newsletterCheckBox;
    private Button signUpBtn;
    private Text statusText;
    private Hyperlink loginLink;
    private Hyperlink homeLink;
    private VBox mainContainer;
    private VBox signupForm;
    private VBox headerSection;
    private ScrollPane scrollPane;
    private Label welcomeLabel;
    private Label subtitleLabel;
    private ProgressBar passwordStrengthBar;
    private Label passwordStrengthLabel;

    public SignUpPage() {
        System.out.println("[DEBUG] SignUpPage constructor called");
        createView();
    }

    private void createView() {
        mainContainer = new VBox(28);
        mainContainer.setAlignment(Pos.CENTER);
        mainContainer.setPadding(new Insets(40));
        mainContainer.getStyleClass().add("auth-container");

        createHeaderSection();
        createSignupFormSection();

        mainContainer.getChildren().addAll(headerSection, signupForm);

        scrollPane = new ScrollPane(mainContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        view = mainContainer;

        // Entrance animations
        mainContainer.sceneProperty().addListener((obs, o, n) -> {
            if (n != null) {
                ThemeManager.slideUp(headerSection, 500, 100);
                ThemeManager.slideUp(signupForm, 500, 250);
            }
        });
    }

    private void createHeaderSection() {
        headerSection = new VBox(12);
        headerSection.setAlignment(Pos.CENTER);
        headerSection.setPadding(new Insets(28));
        headerSection.getStyleClass().add("auth-card");
        headerSection.setMaxWidth(620);

        Label entertainmentIcon = new Label("🎭");
        entertainmentIcon.setFont(Font.font(56));

        welcomeLabel = new Label("Join 🎬 MoodFlix Today!");
        welcomeLabel.getStyleClass().add("auth-title");

        subtitleLabel = new Label("Create your account and start your entertainment journey");
        subtitleLabel.getStyleClass().add("auth-subtitle");

        headerSection.getChildren().setAll(entertainmentIcon, welcomeLabel, subtitleLabel);
    }

    private void createSignupFormSection() {
        signupForm = new VBox(22);
        signupForm.setAlignment(Pos.CENTER);
        signupForm.setPadding(new Insets(36));
        signupForm.setPrefWidth(620);
        signupForm.getStyleClass().add("auth-card");
        signupForm.setMaxWidth(620);

        Label formTitle = new Label("🎭 Create Your MoodFlix Account");
        formTitle.getStyleClass().add("section-title");

        VBox personalSection = createPersonalInfoSection();
        VBox accountSection = createAccountInfoSection();
        VBox additionalSection = createAdditionalInfoSection();
        VBox preferencesSection = createPreferencesSection();

        signUpBtn = new Button("🎭 Create MoodFlix Account");
        signUpBtn.getStyleClass().addAll("btn", "btn-success");
        signUpBtn.setPrefWidth(320);
        signUpBtn.setStyle("-fx-font-size: 16px; -fx-padding: 14 40;");

        statusText = new Text();
        statusText.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        statusText.setStyle("-fx-fill: #f87171;");

        loginLink = new Hyperlink("Already have a 🎬 MoodFlix account? Sign in");
        loginLink.setOnAction(e -> {
            LoginPage loginPage = new LoginPage();
            javafx.stage.Stage stage = (javafx.stage.Stage) loginLink.getScene().getWindow();
            javafx.scene.Scene scene = new javafx.scene.Scene(loginPage.getView(), 1200, 700);
            ThemeManager.applyTheme(scene);
            stage.setScene(scene);
        });
        homeLink = new Hyperlink("Back to Home");

        signupForm.getChildren().addAll(formTitle, personalSection, accountSection,
                additionalSection, preferencesSection, signUpBtn, statusText, loginLink, homeLink);
    }

    private VBox createPersonalInfoSection() {
        VBox personalSection = new VBox(14);
        personalSection.setAlignment(Pos.CENTER_LEFT);
        personalSection.setPadding(new Insets(20));
        personalSection.getStyleClass().add("card");

        Label sectionTitle = new Label("👤 Personal Information");
        sectionTitle.getStyleClass().add("section-title");
        sectionTitle.setStyle("-fx-font-size: 16px;");

        // First Name
        VBox firstNameSection = new VBox(5);
        Label firstNameLabel = new Label("👤 First Name *");
        firstNameLabel.getStyleClass().add("label-secondary");
        firstNameField = new TextField();
        firstNameField.setPromptText("Enter your first name");
        firstNameSection.getChildren().addAll(firstNameLabel, firstNameField);

        // Last Name
        VBox lastNameSection = new VBox(5);
        Label lastNameLabel = new Label("👤 Last Name *");
        lastNameLabel.getStyleClass().add("label-secondary");
        lastNameField = new TextField();
        lastNameField.setPromptText("Enter your last name");
        lastNameSection.getChildren().addAll(lastNameLabel, lastNameField);

        // Birth Date
        VBox birthDateSection = new VBox(5);
        Label birthDateLabel = new Label("🎂 Birth Date *");
        birthDateLabel.getStyleClass().add("label-secondary");
        birthDatePicker = new DatePicker();
        birthDatePicker.setPromptText("Select your birth date");
        birthDatePicker.setValue(LocalDate.now().minusYears(18));
        birthDateSection.getChildren().addAll(birthDateLabel, birthDatePicker);

        // Gender
        VBox genderSection = new VBox(5);
        Label genderLabel = new Label("⚧ Gender *");
        genderLabel.getStyleClass().add("label-secondary");
        genderBox = new ComboBox<>();
        genderBox.getItems().addAll("👨 Male", "👩 Female", "⚧ Non-binary", "🤷 Prefer not to say");
        genderBox.setPromptText("Select your gender");
        genderBox.setMaxWidth(Double.MAX_VALUE);
        genderSection.getChildren().addAll(genderLabel, genderBox);

        // Phone Number
        VBox phoneSection = new VBox(5);
        Label phoneLabel = new Label("📱 Phone Number");
        phoneLabel.getStyleClass().add("label-secondary");
        phoneField = new TextField();
        phoneField.setPromptText("Enter your phone number (optional)");
        phoneSection.getChildren().addAll(phoneLabel, phoneField);

        HBox nameRow = new HBox(15, firstNameSection, lastNameSection);
        HBox.setHgrow(firstNameSection, Priority.ALWAYS);
        HBox.setHgrow(lastNameSection, Priority.ALWAYS);

        HBox detailsRow = new HBox(15, birthDateSection, genderSection);
        HBox.setHgrow(birthDateSection, Priority.ALWAYS);
        HBox.setHgrow(genderSection, Priority.ALWAYS);

        personalSection.getChildren().addAll(sectionTitle, nameRow, detailsRow, phoneSection);
        return personalSection;
    }

    private VBox createAccountInfoSection() {
        VBox accountSection = new VBox(14);
        accountSection.setAlignment(Pos.CENTER_LEFT);
        accountSection.setPadding(new Insets(20));
        accountSection.getStyleClass().add("card");

        Label sectionTitle = new Label("🔐 Account Information");
        sectionTitle.getStyleClass().add("section-title");
        sectionTitle.setStyle("-fx-font-size: 16px;");

        // Email
        VBox emailSection = new VBox(5);
        Label emailLabel = new Label("📧 Email Address *");
        emailLabel.getStyleClass().add("label-secondary");
        emailField = new TextField();
        emailField.setPromptText("Enter your email address");
        emailSection.getChildren().addAll(emailLabel, emailField);

        // Password
        VBox passwordSection = new VBox(5);
        Label passwordLabel = new Label("🔒 Password *");
        passwordLabel.getStyleClass().add("label-secondary");
        passwordField = new PasswordField();
        passwordField.setPromptText("Create a strong password");

        passwordStrengthBar = new ProgressBar(0);
        passwordStrengthBar.setPrefWidth(240);

        passwordStrengthLabel = new Label("Password strength: Weak");
        passwordStrengthLabel.getStyleClass().add("label-muted");
        passwordStrengthLabel.setStyle("-fx-text-fill: #f87171;");

        passwordSection.getChildren().addAll(passwordLabel, passwordField, passwordStrengthBar, passwordStrengthLabel);

        // Confirm Password
        VBox confirmPasswordSection = new VBox(5);
        Label confirmPasswordLabel = new Label("🔒 Confirm Password *");
        confirmPasswordLabel.getStyleClass().add("label-secondary");
        confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm your password");
        confirmPasswordSection.getChildren().addAll(confirmPasswordLabel, confirmPasswordField);

        accountSection.getChildren().addAll(sectionTitle, emailSection, passwordSection, confirmPasswordSection);
        return accountSection;
    }

    private VBox createAdditionalInfoSection() {
        VBox additionalSection = new VBox(14);
        additionalSection.setAlignment(Pos.CENTER_LEFT);
        additionalSection.setPadding(new Insets(20));
        additionalSection.getStyleClass().add("card");

        Label sectionTitle = new Label("🌍 Additional Information");
        sectionTitle.getStyleClass().add("section-title");
        sectionTitle.setStyle("-fx-font-size: 16px;");

        // Country
        VBox countrySection = new VBox(5);
        Label countryLabel = new Label("🌍 Country");
        countryLabel.getStyleClass().add("label-secondary");
        countryBox = new ComboBox<>();
        countryBox.getItems().addAll("🇺🇸 United States", "🇨🇦 Canada", "🇬🇧 United Kingdom", "🇦🇺 Australia",
                "🇩🇪 Germany", "🇫🇷 France", "🇯🇵 Japan", "🇮🇳 India", "🇧🇷 Brazil", "🇲🇽 Mexico");
        countryBox.setPromptText("Select your country");
        countryBox.setMaxWidth(Double.MAX_VALUE);
        countrySection.getChildren().addAll(countryLabel, countryBox);

        // Language
        VBox languageSection = new VBox(5);
        Label languageLabel = new Label("🗣️ Preferred Language");
        languageLabel.getStyleClass().add("label-secondary");
        languageBox = new ComboBox<>();
        languageBox.getItems().addAll("🇺🇸 English", "🇪🇸 Spanish", "🇫🇷 French", "🇩🇪 German",
                "🇯🇵 Japanese", "🇨🇳 Chinese", "🇰🇷 Korean", "🇮🇹 Italian");
        languageBox.setValue("🇺🇸 English");
        languageBox.setMaxWidth(Double.MAX_VALUE);
        languageSection.getChildren().addAll(languageLabel, languageBox);

        // Role
        VBox roleSection = new VBox(5);
        Label roleLabel = new Label("🎭 Account Type *");
        roleLabel.getStyleClass().add("label-secondary");
        roleBox = new ComboBox<>();
        roleBox.getItems().addAll("👤 User - Browse and watch content", "👑 Admin - Upload and manage content");
        roleBox.setPromptText("Select your account type");
        roleBox.setValue("👤 User - Browse and watch content");
        roleBox.setMaxWidth(Double.MAX_VALUE);
        roleBox.setTooltip(new Tooltip("User: Browse and watch entertainment content\nAdmin: Upload and manage content for users"));
        roleSection.getChildren().addAll(roleLabel, roleBox);

        HBox locationRow = new HBox(15, countrySection, languageSection);
        HBox.setHgrow(countrySection, Priority.ALWAYS);
        HBox.setHgrow(languageSection, Priority.ALWAYS);

        additionalSection.getChildren().addAll(sectionTitle, locationRow, roleSection);
        return additionalSection;
    }

    private VBox createPreferencesSection() {
        VBox preferencesSection = new VBox(14);
        preferencesSection.setAlignment(Pos.CENTER_LEFT);
        preferencesSection.setPadding(new Insets(20));
        preferencesSection.getStyleClass().add("card");

        Label sectionTitle = new Label("⚙️ Preferences & Terms");
        sectionTitle.getStyleClass().add("section-title");
        sectionTitle.setStyle("-fx-font-size: 16px;");

        termsCheckBox = new CheckBox("I agree to the Terms and Conditions *");
        newsletterCheckBox = new CheckBox("📧 Subscribe to newsletter for updates and recommendations");
        newsletterCheckBox.setSelected(true);

        Label privacyLabel = new Label("🔒 Your privacy is important to us. We will never share your personal information.");
        privacyLabel.getStyleClass().add("label-muted");
        privacyLabel.setWrapText(true);
        privacyLabel.setMaxWidth(500);

        preferencesSection.getChildren().addAll(sectionTitle, termsCheckBox, newsletterCheckBox, privacyLabel);
        return preferencesSection;
    }

    // Getters
    public ScrollPane getView() {
        System.out.println("[DEBUG] getView() called, returning scrollPane");
        return scrollPane;
    }
    public TextField getEmailField() { return emailField; }
    public PasswordField getPasswordField() { return passwordField; }
    public PasswordField getConfirmPasswordField() { return confirmPasswordField; }
    public TextField getFirstNameField() { return firstNameField; }
    public TextField getLastNameField() { return lastNameField; }
    public DatePicker getBirthDatePicker() { return birthDatePicker; }
    public ComboBox<String> getGenderBox() { return genderBox; }
    public ComboBox<String> getRoleBox() { return roleBox; }
    public TextField getPhoneField() { return phoneField; }
    public ComboBox<String> getCountryBox() { return countryBox; }
    public ComboBox<String> getLanguageBox() { return languageBox; }
    public CheckBox getTermsCheckBox() { return termsCheckBox; }
    public CheckBox getNewsletterCheckBox() { return newsletterCheckBox; }
    public Button getSignUpBtn() { return signUpBtn; }
    public Text getStatusText() { return statusText; }
    public Hyperlink getLoginLink() { return loginLink; }
    public Hyperlink getHomeLink() { return homeLink; }
    public ProgressBar getPasswordStrengthBar() { return passwordStrengthBar; }
    public Label getPasswordStrengthLabel() { return passwordStrengthLabel; }
} 
