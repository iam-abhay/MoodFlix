package com.moodflix.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import com.moodflix.util.ThemeManager;
import com.moodflix.Main;
import javafx.scene.Scene;

public class LoginPage {
    private VBox view;
    private TextField emailField;
    private PasswordField passwordField;
    private Button loginBtn;
    private Button resetBtn;
    private Button createBtn;
    private Text statusText;
    private Hyperlink signUpLink;
    private Hyperlink homeLink;
    private VBox loginForm;
    private VBox headerSection;
    private ScrollPane scrollPane;

    public LoginPage() {
        createView();
    }

    private void createView() {
        // ── Root container — dark gradient background ──
        view = new VBox();
        view.setAlignment(Pos.CENTER);
        view.getStyleClass().add("auth-container");

        // ── Left decorative panel ──
        VBox leftPanel = new VBox(20);
        leftPanel.setAlignment(Pos.CENTER);
        leftPanel.setPrefWidth(360);
        leftPanel.setMaxWidth(420);
        leftPanel.getStyleClass().add("glass-card");

        Label expressLabel = new Label("Feel\nFree\nTo\nExpress!");
        expressLabel.setFont(Font.font("Georgia", javafx.scene.text.FontPosture.ITALIC, 54));
        expressLabel.getStyleClass().add("hero-title");
        expressLabel.setStyle("-fx-opacity: 0.25; -fx-line-spacing: 10;");

        Label movieEmoji = new Label("🎬");
        movieEmoji.setFont(Font.font(60));
        movieEmoji.setStyle("-fx-opacity: 0.5;");

        leftPanel.getChildren().addAll(movieEmoji, expressLabel);

        // ── Right panel — contains header + form ──
        createHeaderSection();
        createLoginFormSection();

        VBox rightPanel = new VBox(20, headerSection, loginForm);
        rightPanel.setAlignment(Pos.CENTER);
        rightPanel.setPadding(new Insets(10));
        rightPanel.setPrefWidth(440);
        rightPanel.setMaxWidth(480);

        // ── Page layout ──
        HBox pageLayout = new HBox(40, leftPanel, rightPanel);
        pageLayout.setAlignment(Pos.CENTER);
        pageLayout.setPadding(new Insets(40));
        pageLayout.setFillHeight(false);
        HBox.setHgrow(leftPanel, Priority.ALWAYS);
        HBox.setHgrow(rightPanel, Priority.ALWAYS);

        // Responsive binding
        view.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                pageLayout.prefWidthProperty().bind(newScene.widthProperty());
                pageLayout.prefHeightProperty().bind(newScene.heightProperty());
            }
        });

        view.getChildren().add(pageLayout);

        scrollPane = new ScrollPane(view);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        // ── Play entrance animations after scene is attached ──
        view.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                ThemeManager.fadeIn(leftPanel, 600, 100);
                ThemeManager.slideUp(rightPanel, 500, 250);
            }
        });
    }

    private void createHeaderSection() {
        headerSection = new VBox(10);
        headerSection.setAlignment(Pos.CENTER);
        headerSection.setPadding(new Insets(24));
        headerSection.getStyleClass().add("auth-card");
        headerSection.setMaxWidth(440);

        Label logoLabel = new Label("🎬 MoodFlix");
        logoLabel.getStyleClass().add("auth-logo");

        Label subtitle = new Label("✨ Feel The Story ✨");
        subtitle.getStyleClass().add("auth-subtitle");

        headerSection.getChildren().addAll(logoLabel, subtitle);
    }

    private void createLoginFormSection() {
        loginForm = new VBox(18);
        loginForm.setAlignment(Pos.CENTER);
        loginForm.setPadding(new Insets(32));
        loginForm.setPrefWidth(440);
        loginForm.getStyleClass().add("auth-card");

        Label formTitle = new Label("🔐 LOGIN");
        formTitle.getStyleClass().add("auth-title");

        // Email field
        VBox emailBox = new VBox(6);
        Label emailLabel = new Label("👤 Username");
        emailLabel.getStyleClass().add("label-secondary");
        emailLabel.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 13));
        emailField = new TextField();
        emailField.setPromptText("Enter username");
        emailField.setMaxWidth(360);
        emailBox.getChildren().addAll(emailLabel, emailField);

        // Password field
        VBox passBox = new VBox(6);
        Label passLabel = new Label("🔒 Password");
        passLabel.getStyleClass().add("label-secondary");
        passLabel.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 13));
        passwordField = new PasswordField();
        passwordField.setPromptText("Enter password");
        passwordField.setMaxWidth(360);
        passBox.getChildren().addAll(passLabel, passwordField);

        // Buttons
        loginBtn = new Button("Login");
        loginBtn.getStyleClass().add("btn");
        loginBtn.setPrefWidth(140);

        resetBtn = new Button("Reset");
        resetBtn.getStyleClass().addAll("btn", "btn-danger");
        resetBtn.setPrefWidth(140);
        resetBtn.setOnAction(e -> {
            emailField.clear();
            passwordField.clear();
            statusText.setText("");
        });

        HBox buttonBox = new HBox(16, loginBtn, resetBtn);
        buttonBox.setAlignment(Pos.CENTER);

        // Status text
        statusText = new Text();
        statusText.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        statusText.setStyle("-fx-fill: #f87171;");

        // Sign-up section
        signUpLink = new Hyperlink("Don't have an account?");
        homeLink = new Hyperlink("Back to Home");
        homeLink.setOnAction(e -> {
            LandingPage landingPage = new LandingPage();
            com.moodflix.controller.LandingPageController landingController =
                new com.moodflix.controller.LandingPageController(landingPage);
            Main.setScene(new Scene(landingPage.getView()));
        });

        createBtn = new Button("Create New Account");
        createBtn.getStyleClass().addAll("btn", "btn-success");
        createBtn.setPrefWidth(220);
        createBtn.setOnAction(e -> {
            SignUpPage signUpPage = new SignUpPage();
            com.moodflix.controller.SignUpPageController signUpController =
                new com.moodflix.controller.SignUpPageController(signUpPage);
            Main.setScene(new Scene(signUpPage.getView()));
        });

        VBox signupBox = new VBox(6, signUpLink, createBtn, homeLink);
        signupBox.setAlignment(Pos.CENTER);

        // Separator
        Separator sep = new Separator();
        sep.setMaxWidth(300);

        loginForm.getChildren().addAll(formTitle, emailBox, passBox, buttonBox, statusText, sep, signupBox);
    }

    // Getters
    public VBox getView() { return view; }
    public TextField getEmailField() { return emailField; }
    public PasswordField getPasswordField() { return passwordField; }
    public Button getLoginBtn() { return loginBtn; }
    public Button getResetBtn() { return resetBtn; }
    public Button getCreateBtn() { return createBtn; }
    public Text getStatusText() { return statusText; }
    public Hyperlink getSignUpLink() { return signUpLink; }
    public ScrollPane getScrollPane() { return scrollPane; }
}
