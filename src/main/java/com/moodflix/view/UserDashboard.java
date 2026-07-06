package com.moodflix.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.Node;
import javafx.scene.text.Text;
import java.util.List;
import com.moodflix.model.Content;
import javafx.scene.Scene;
import javafx.animation.*;
import javafx.util.Duration;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Stop;
import javafx.scene.input.KeyCode;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.json.JSONObject;
import javafx.scene.text.TextFlow;
import javafx.scene.text.TextAlignment;
import javafx.scene.Cursor;
import javafx.application.HostServices;
import com.moodflix.util.ThemeManager;
import com.moodflix.util.SessionManager;
import com.moodflix.util.ImageCache;
import com.moodflix.util.MoodflixDialog;

/**
 * Modern Premium Dashboard view for MoodFlix.
 * Incorporates Netflix-inspired glassmorphism design, collapsible sidebar, live search, and premium movie cards.
 */
public class UserDashboard {
    private final HostServices hostServices;
    private BorderPane view;
    private ComboBox<String> moodCombo;
    private ComboBox<String> typeCombo;
    private Button moodRecBtn;
    private Button generalRecBtn;
    private Button recommendedBtn;
    private VBox recommendationsContainer;

    private VBox sidebar;
    private ImageView profilePhotoView;
    private Button profileBtn;
    private Button watchlistBtn;
    private Button feedbackBtn;
    private Button settingsBtn;
    private Button logoutBtn;
    private Button activityBtn;
    private Button collapseBtn;
    
    private VBox mainContent;
    private ScrollPane scrollPane;
    private VBox headerSection;
    private Label welcomeLabel;
    private Label subtitleLabel;
    private VBox illustrationSection;

    // Search and filters
    private TextField searchField;
    private HBox recentSearchesBox;

    // Collapsible sidebar state
    private boolean isCollapsed = false;

    public UserDashboard() {
        this(null);
    }
    
    public UserDashboard(HostServices hostServices) {
        this.hostServices = hostServices;
        createModernDashboard();
    }
  
    private void createModernDashboard() {
        BorderPane mainView = new BorderPane();
        mainView.getStyleClass().add("auth-container");

        // CREATE COLLAPSIBLE SIDEBAR
        createSidebar();
        mainView.setLeft(sidebar);

        VBox mainContainer = new VBox(28);
        mainContainer.setPadding(new Insets(24, 32, 32, 32));
        mainContainer.setAlignment(Pos.TOP_CENTER);
        mainContainer.setStyle("-fx-background-color: transparent;");

        // DASHBOARD HEADER & LIVE SEARCH PANEL
        createDashboardHeaderSection();
        mainContainer.getChildren().add(headerSection);

        // MOOD SELECTION SECTION
        VBox moodSection = new VBox(14);
        moodSection.setAlignment(Pos.CENTER);
        Label moodTitle = new Label("How Are You Feeling Today?");
        moodTitle.getStyleClass().add("section-title");
        
        HBox moodCards = new HBox(18);
        moodCards.setAlignment(Pos.CENTER);
        moodCards.setPadding(new Insets(6, 0, 10, 0));
        
        String[][] moods = {
            {"😊", "Happy", "Feel good vibes"},
            {"😢", "Sad", "Emotional stories"},
            {"😱", "Thriller", "Heart-pounding"},
            {"😂", "Comedy", "Comedies galore"},
            {"😍", "Romantic", "Love stories"}
        };
        
        for (int i = 0; i < moods.length; i++) {
            String[] mood = moods[i];
            VBox card = new VBox(6);
            card.setAlignment(Pos.CENTER);
            card.setPrefWidth(125);
            card.setPrefHeight(100);
            card.getStyleClass().add("mood-card");
            
            Label emoji = new Label(mood[0]);
            emoji.setFont(Font.font("Segoe UI Emoji", FontWeight.BOLD, 30));
            
            Label label = new Label(mood[1]);
            label.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            
            Label desc = new Label(mood[2]);
            desc.getStyleClass().add("label-muted");
            desc.setStyle("-fx-font-size: 11px;");
            
            card.getChildren().addAll(emoji, label, desc);
            
            card.setOnMouseClicked(e -> {
                for (Node n : moodCards.getChildren()) {
                    n.getStyleClass().remove("btn-success");
                    n.setStyle("");
                }
                card.getStyleClass().add("btn-success");
                moodCombo.setValue(mood[1]);
            });
            moodCards.getChildren().add(card);
        }
        moodSection.getChildren().addAll(moodTitle, moodCards);
        mainContainer.getChildren().add(moodSection);

        // RECOMMENDATIONS DROPDOWNS & DYNAMIC CONTAINER
        VBox recSection = new VBox(18);
        recSection.setAlignment(Pos.CENTER);
        recSection.setPadding(new Insets(24));
        recSection.getStyleClass().add("glass-card");
        
        Label recTitle = new Label("Personalized Suggestion Engine");
        recTitle.getStyleClass().add("section-title");
        
        HBox recBtnBox = new HBox(16);
        recBtnBox.setAlignment(Pos.CENTER);
        moodRecBtn = new Button("Mood Suggest");
        moodRecBtn.getStyleClass().addAll("btn", "btn-outline");
        generalRecBtn = new Button("Type Suggest");
        generalRecBtn.getStyleClass().addAll("btn", "btn-outline");
        recBtnBox.getChildren().addAll(moodRecBtn, generalRecBtn);

        HBox recDropdownBox = new HBox(12);
        recDropdownBox.setAlignment(Pos.CENTER);
        
        moodCombo = new ComboBox<>();
        moodCombo.getItems().addAll("Happy", "Sad", "Thriller", "Feel Good", "Comedy", "Romantic");
        moodCombo.setPromptText("Select Emotion");
        moodCombo.setStyle("-fx-background-radius: 10; -fx-font-size: 13;");
        
        typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("Movie", "Series", "Song", "Trailer", "Shorts");
        typeCombo.setPromptText("Select Type");
        typeCombo.setStyle("-fx-background-radius: 10; -fx-font-size: 13;");
        
        recDropdownBox.getChildren().addAll(moodCombo, typeCombo);

        this.recommendedBtn = new Button("Recommend Matches");
        recommendedBtn.getStyleClass().addAll("btn", "btn-warning");
        recommendedBtn.setDisable(true);
        
        moodCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            recommendedBtn.setDisable(moodCombo.getValue() == null || typeCombo.getValue() == null);
        });
        typeCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            recommendedBtn.setDisable(moodCombo.getValue() == null || typeCombo.getValue() == null);
        });

        VBox recActionBox = new VBox(12, recBtnBox, recDropdownBox, recommendedBtn);
        recActionBox.setAlignment(Pos.CENTER);

        recommendationsContainer = new VBox(14);
        recommendationsContainer.setAlignment(Pos.CENTER);
        recommendationsContainer.setPadding(new Insets(10, 0, 10, 0));
        
        recSection.getChildren().addAll(recTitle, recActionBox, recommendationsContainer);
        mainContainer.getChildren().add(recSection);

        // TRENDING CAROUSEL SECTION
        VBox trendingSection = new VBox(10);
        trendingSection.setAlignment(Pos.CENTER_LEFT);
        Label trendingTitle = new Label("Trending Now");
        trendingTitle.getStyleClass().add("section-title");
        
        HBox trendingRow = new HBox(18);
        trendingRow.setAlignment(Pos.CENTER_LEFT);
        trendingRow.setPadding(new Insets(6, 0, 6, 0));
        
        String[][] trending = {
            {"https://m.media-amazon.com/images/I/71niXI3lxlL._AC_SY679_.jpg", "Action Thriller"},
            {"https://m.media-amazon.com/images/I/81p+xe8cbnL._AC_SY679_.jpg", "Love Story"},
            {"https://m.media-amazon.com/images/I/91G8kOe7tLL._AC_SY679_.jpg", "Space Series"},
            {"https://m.media-amazon.com/images/I/81Q1bJz4GLL._AC_SY679_.jpg", "Dark Secrets"},
            {"https://m.media-amazon.com/images/I/81Zt42ioCgL._AC_SY679_.jpg", "Laugh Out Loud"},
            {"https://m.media-amazon.com/images/I/81VwqH9hQbL._AC_SY679_.jpg", "Wild Nature"}
        };
        
        for (String[] t : trending) {
            VBox card = new VBox(8);
            card.setAlignment(Pos.TOP_CENTER);
            card.setPrefWidth(130);
            card.getStyleClass().add("poster-card");
            
            ImageView poster = new ImageView();
            poster.setFitWidth(120);
            poster.setFitHeight(160);
            poster.setPreserveRatio(false);
            poster.setSmooth(true);
            
            // Lazy load cached images with background loading
            Image image = ImageCache.getImage(t[0], 120, 160, false, true);
            poster.setImage(image);
            
            // Clip rounded corners on image
            javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(120, 160);
            clip.setArcWidth(16); clip.setArcHeight(16);
            poster.setClip(clip);

            Label label = new Label(t[1]);
            label.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
            card.getChildren().addAll(poster, label);
            
            card.setOnMouseClicked(e -> {
                MoodflixDialog.showInfo("Trending Title", "You selected: " + t[1]);
            });
            
            // Zoom effect
            card.setOnMouseEntered(e -> {
                if (ThemeManager.isAnimationsEnabled()) {
                    ScaleTransition st = new ScaleTransition(Duration.millis(180), card);
                    st.setToX(1.06); st.setToY(1.06); st.play();
                }
            });
            card.setOnMouseExited(e -> {
                if (ThemeManager.isAnimationsEnabled()) {
                    ScaleTransition st = new ScaleTransition(Duration.millis(180), card);
                    st.setToX(1.0); st.setToY(1.0); st.play();
                }
            });
            
            trendingRow.getChildren().add(card);
        }
        
        ScrollPane trendingScroll = new ScrollPane(trendingRow);
        trendingScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        trendingScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        trendingScroll.setFitToHeight(true);
        trendingScroll.setStyle("-fx-background: transparent; -fx-border-color: transparent;");
        
        trendingSection.getChildren().addAll(trendingTitle, trendingScroll);
        mainContainer.getChildren().add(trendingSection);

        // DIAGNOSTIC STATS BAR
        HBox statsBar = new HBox(40);
        statsBar.setAlignment(Pos.CENTER);
        statsBar.setPadding(new Insets(24));
        statsBar.getStyleClass().add("glass-card");
        
        String[][] stats = {
            {"10K+", "Movies & Shows"},
            {"50K+", "Happy Users"},
            {"95%", "Match Accuracy"},
            {"24/7", "Support"}
        };
        for (String[] stat : stats) {
            VBox statBox = new VBox(4);
            statBox.setAlignment(Pos.CENTER);
            Label statNum = new Label(stat[0]);
            statNum.getStyleClass().add("stat-value");
            Label statLabel = new Label(stat[1]);
            statLabel.getStyleClass().add("stat-label");
            statBox.getChildren().addAll(statNum, statLabel);
            statsBar.getChildren().add(statBox);
        }
        mainContainer.getChildren().add(statsBar);

        // Scrollable content area
        ScrollPane dashboardScrollPane = new ScrollPane(mainContainer);
        dashboardScrollPane.setFitToWidth(true);
        dashboardScrollPane.setFitToHeight(false);
        dashboardScrollPane.setStyle("-fx-background: transparent; -fx-border-color: transparent;");

        // FLOATING CHAT SYSTEM
        Button chatBtn = new Button("🎬");
        chatBtn.getStyleClass().addAll("btn");
        chatBtn.setStyle("-fx-font-size: 20px; -fx-background-radius: 30; -fx-pref-width: 60; -fx-pref-height: 60;");

        StackPane chatWindow = new StackPane();
        chatWindow.setMaxWidth(340);
        chatWindow.setPrefWidth(340);
        chatWindow.getStyleClass().add("glass-card");
        chatWindow.setStyle("-fx-border-color: -mf-accent; -fx-border-radius: 18; -fx-effect: dropshadow(gaussian, -mf-accent-glow, 12, 0, 0, 2);");
        chatWindow.setVisible(false);

        VBox chatOverlay = new VBox(12);
        chatOverlay.setPadding(new Insets(16));
        chatOverlay.setAlignment(Pos.TOP_CENTER);
        
        Label chatTitle = new Label("MoodFlix Assistant 🤖");
        chatTitle.getStyleClass().add("section-title");
        
        VBox chatHistory = new VBox(10);
        chatHistory.setAlignment(Pos.TOP_LEFT);
        
        ScrollPane chatScroll = new ScrollPane(chatHistory);
        chatScroll.setFitToWidth(true);
        chatScroll.setPrefHeight(300);
        chatScroll.setStyle("-fx-background: transparent; -fx-border-color: transparent;");
        
        TextField chatInput = new TextField();
        chatInput.setPromptText("Ask me about movies...");
        chatInput.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                handleRichChat(chatInput, chatHistory, chatScroll);
            }
        });
        
        chatOverlay.getChildren().addAll(chatTitle, chatScroll, chatInput);
        chatWindow.getChildren().add(chatOverlay);

        chatBtn.setOnAction(e -> {
            boolean visible = !chatWindow.isVisible();
            chatWindow.setVisible(visible);
            if (visible && ThemeManager.isAnimationsEnabled()) {
                ThemeManager.fadeIn(chatWindow, 300);
            }
        });

        StackPane overlay = new StackPane();
        overlay.setPickOnBounds(false);
        overlay.getChildren().addAll(dashboardScrollPane, chatBtn, chatWindow);
        StackPane.setAlignment(chatBtn, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(chatBtn, new Insets(0, 40, 40, 0));
        StackPane.setAlignment(chatWindow, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(chatWindow, new Insets(0, 40, 110, 0));

        mainView.setCenter(overlay);
        this.view = mainView;
    }

    private void createSidebar() {
        sidebar = new VBox(24);
        sidebar.setAlignment(Pos.TOP_CENTER);
        sidebar.setPadding(new Insets(30, 12, 30, 12));
        sidebar.setPrefWidth(180);
        sidebar.getStyleClass().add("sidebar");

        // Top Sidebar Controls
        HBox topBox = new HBox(8);
        topBox.setAlignment(Pos.CENTER);
        
        Label brandLabel = new Label("🎬 MoodFlix");
        brandLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 18px;");
        brandLabel.getStyleClass().add("label-accent");
        
        collapseBtn = new Button("◀");
        collapseBtn.getStyleClass().addAll("btn-ghost");
        collapseBtn.setStyle("-fx-font-size: 14px; -fx-padding: 4 8;");
        collapseBtn.setOnAction(e -> toggleSidebar());
        
        topBox.getChildren().addAll(brandLabel, collapseBtn);
        sidebar.getChildren().add(topBox);

        // User profile picture card
        VBox avatarSection = new VBox(8);
        avatarSection.setAlignment(Pos.CENTER);
        profilePhotoView = new ImageView();
        profilePhotoView.setFitWidth(60);
        profilePhotoView.setFitHeight(60);
        profilePhotoView.setPreserveRatio(true);
        javafx.scene.shape.Circle clip = new javafx.scene.shape.Circle(30, 30, 30);
        profilePhotoView.setClip(clip);
        
        try {
            Image defaultImage = ImageCache.getImage("/moodflix icon_final.jpeg");
            profilePhotoView.setImage(defaultImage);
        } catch (Exception e) {
            profilePhotoView.setStyle("-fx-background-color: #e50914; -fx-background-radius: 30;");
        }
        
        avatarSection.getChildren().addAll(profilePhotoView);
        sidebar.getChildren().add(avatarSection);

        // Navigation controls
        profileBtn = createNavButton("👤  Profile");
        watchlistBtn = createNavButton("⭐  Watchlist");
        feedbackBtn = createNavButton("💬  Feedback");
        activityBtn = createNavButton("📈  Activity");
        settingsBtn = createNavButton("⚙️  Settings");
        logoutBtn = createNavButton("🚪  Logout");

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        sidebar.getChildren().addAll(profileBtn, watchlistBtn, feedbackBtn, activityBtn, settingsBtn, spacer, logoutBtn);
    }

    private Button createNavButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("sidebar-btn");
        button.setMaxWidth(Double.MAX_VALUE);
        return button;
    }

    private void toggleSidebar() {
        isCollapsed = !isCollapsed;
        double targetWidth = isCollapsed ? 68 : 180;
        
        if (ThemeManager.isAnimationsEnabled()) {
            Timeline timeline = new Timeline();
            KeyValue widthVal = new KeyValue(sidebar.prefWidthProperty(), targetWidth);
            KeyFrame keyFrame = new KeyFrame(Duration.millis(250), widthVal);
            timeline.getKeyFrames().add(keyFrame);
            timeline.setOnFinished(e -> updateSidebarUI());
            timeline.play();
        } else {
            sidebar.setPrefWidth(targetWidth);
            updateSidebarUI();
        }
    }
    
    private void updateSidebarUI() {
        if (isCollapsed) {
            profileBtn.setText("👤");
            watchlistBtn.setText("⭐");
            feedbackBtn.setText("💬");
            activityBtn.setText("📈");
            settingsBtn.setText("⚙️");
            logoutBtn.setText("🚪");
            collapseBtn.setText("▶");
            
            Tooltip.install(profileBtn, new Tooltip("Profile"));
            Tooltip.install(watchlistBtn, new Tooltip("Watchlist"));
            Tooltip.install(feedbackBtn, new Tooltip("Feedback"));
            Tooltip.install(activityBtn, new Tooltip("Activity"));
            Tooltip.install(settingsBtn, new Tooltip("Settings"));
            Tooltip.install(logoutBtn, new Tooltip("Logout"));
        } else {
            profileBtn.setText("👤  Profile");
            watchlistBtn.setText("⭐  Watchlist");
            feedbackBtn.setText("💬  Feedback");
            activityBtn.setText("📈  Activity");
            settingsBtn.setText("⚙️  Settings");
            logoutBtn.setText("🚪  Logout");
            collapseBtn.setText("◀");
            
            Tooltip.uninstall(profileBtn, null);
            Tooltip.uninstall(watchlistBtn, null);
            Tooltip.uninstall(feedbackBtn, null);
            Tooltip.uninstall(activityBtn, null);
            Tooltip.uninstall(settingsBtn, null);
            Tooltip.uninstall(logoutBtn, null);
        }
    }

    private void createDashboardHeaderSection() {
        headerSection = new VBox(12);
        headerSection.setAlignment(Pos.CENTER_LEFT);
        headerSection.setPadding(new Insets(24));
        headerSection.getStyleClass().add("glass-card");

        HBox welcomeBox = new HBox(12);
        welcomeBox.setAlignment(Pos.CENTER_LEFT);
        welcomeLabel = new Label("Welcome back to MoodFlix!");
        welcomeLabel.getStyleClass().add("hero-title");
        welcomeLabel.setStyle("-fx-font-size: 26px;");
        
        subtitleLabel = new Label("Choose how you are feeling or search items directly.");
        subtitleLabel.getStyleClass().add("label-secondary");
        welcomeBox.getChildren().addAll(welcomeLabel);

        // Sleek Live Search Bar
        HBox searchRow = new HBox(12);
        searchRow.setAlignment(Pos.CENTER_LEFT);
        
        searchField = new TextField();
        searchField.setPromptText("🔍 Search movies, series, trailers, or soundtracks...");
        searchField.setPrefWidth(420);
        searchField.getStyleClass().add("text-field");

        searchRow.getChildren().addAll(searchField);

        // Recent searches row
        recentSearchesBox = new HBox(8);
        recentSearchesBox.setAlignment(Pos.CENTER_LEFT);
        Label recentLbl = new Label("Recent Searches:");
        recentLbl.getStyleClass().add("label-muted");
        recentLbl.setStyle("-fx-font-size: 12px;");
        recentSearchesBox.getChildren().add(recentLbl);

        headerSection.getChildren().addAll(welcomeBox, subtitleLabel, searchRow, recentSearchesBox);
    }

    public static Scene createRecommendationGridScene(List<Content> contentList) {
        VBox root = new VBox(24);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.TOP_CENTER);
        root.getStyleClass().add("auth-container");

        // Back button
        Button backBtn = new Button("← Back to Dashboard");
        backBtn.getStyleClass().addAll("btn", "btn-outline");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(24);
        grid.setVgap(24);
        grid.setAlignment(Pos.CENTER);

        int cols = 4;
        int row = 0, col = 0;
        for (Content c : contentList) {
            VBox card = new VBox(0);
            card.setMinWidth(180);
            card.setMaxWidth(180);
            card.setPrefWidth(180);
            card.setMinHeight(280);
            card.setMaxHeight(280);
            card.setPrefHeight(280);
            card.getStyleClass().add("poster-card");

            StackPane imagePane = new StackPane();
            imagePane.setPrefHeight(180);

            ImageView imgView = new ImageView();
            imgView.setFitWidth(180);
            imgView.setFitHeight(180);
            imgView.setPreserveRatio(false);
            imgView.setSmooth(true);
            
            if (c.getImageUrl() != null && !c.getImageUrl().isEmpty()) {
                imgView.setImage(ImageCache.getImage(c.getImageUrl(), 180, 180, false, true));
            } else {
                imgView.setImage(ImageCache.getImage("https://images.unsplash.com/photo-1517602302552-471fe67acf66?auto=format&fit=crop&w=180&q=80", 180, 180, false, true));
            }

            javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(180, 180);
            clip.setArcWidth(20); clip.setArcHeight(20);
            imgView.setClip(clip);
            imagePane.getChildren().add(imgView);

            VBox detailsBox = new VBox(4);
            detailsBox.setPadding(new Insets(10));
            
            Label titleLabel = new Label(c.getTitle());
            titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
            
            Label typeLabel = new Label(c.getType().toUpperCase());
            typeLabel.getStyleClass().add("label-accent");
            typeLabel.setStyle("-fx-font-size: 10px;");
            
            detailsBox.getChildren().addAll(titleLabel, typeLabel);
            card.getChildren().addAll(imagePane, detailsBox);

            // Context Menu
            ContextMenu ctxMenu = new ContextMenu();
            MenuItem playItem = new MenuItem("▶ Play Content");
            playItem.setOnAction(ev -> {
                if (c.getLink() != null && !c.getLink().isEmpty()) {
                    try { java.awt.Desktop.getDesktop().browse(new java.net.URI(c.getLink())); } catch(Exception ex) {}
                }
            });
            MenuItem copyLinkItem = new MenuItem("📋 Copy Content Link");
            copyLinkItem.setOnAction(ev -> {
                javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
                javafx.scene.input.ClipboardContent cc = new javafx.scene.input.ClipboardContent();
                cc.putString(c.getLink());
                clipboard.setContent(cc);
            });
            ctxMenu.getItems().addAll(playItem, copyLinkItem);
            card.setOnContextMenuRequested(ev -> {
                ctxMenu.show(card, ev.getScreenX(), ev.getScreenY());
            });

            card.setOnMouseClicked(event -> {
                try {
                    java.awt.Desktop.getDesktop().browse(new java.net.URI(c.getLink()));
                } catch (Exception ex) {
                    MoodflixDialog.showError("Error", "Failed to open link: " + ex.getMessage());
                }
            });

            grid.add(card, col, row);
            col++;
            if (col == cols) { col = 0; row++; }
        }

        ScrollPane scrollPane = new ScrollPane(grid);
        scrollPane.setFitToWidth(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background: transparent; -fx-border-color: transparent;");

        root.getChildren().addAll(backBtn, scrollPane);
        Scene scene = new Scene(root, 1200, 800);
        scene.getProperties().put("backBtn", backBtn);
        ThemeManager.applyTheme(scene);
        return scene;
    }

    private VBox createMovieCard(Content c) {
        VBox card = new VBox(0);
        card.setMinWidth(180);
        card.setMaxWidth(180);
        card.setPrefWidth(180);
        card.setMinHeight(270);
        card.setMaxHeight(270);
        card.setPrefHeight(270);
        card.getStyleClass().add("poster-card");

        StackPane imagePane = new StackPane();
        imagePane.setPrefHeight(180);

        ImageView imgView = new ImageView();
        imgView.setFitWidth(180);
        imgView.setFitHeight(180);
        imgView.setPreserveRatio(false);
        imgView.setSmooth(true);
        
        ProgressIndicator shimmer = new ProgressIndicator();
        shimmer.setMaxSize(24, 24);
        shimmer.setStyle("-fx-progress-color: -mf-accent;");
        imagePane.getChildren().add(shimmer);

        if (c.getImageUrl() != null && !c.getImageUrl().isEmpty()) {
            Image img = ImageCache.getImage(c.getImageUrl(), 180, 180, false, true);
            if (img == null) {
                // fallback to placeholder if image loading failed
                imagePane.getChildren().remove(shimmer);
                img = ImageCache.getImage("https://images.unsplash.com/photo-1517602302552-471fe67acf66?auto=format&fit=crop&w=180&q=80", 180, 180, false, true);
                imgView.setImage(img);
            } else {
                imgView.setImage(img);
                if (img.getProgress() >= 1.0) {
                    imagePane.getChildren().remove(shimmer);
                } else {
                    img.progressProperty().addListener((obs, oldVal, newVal) -> {
                        if (newVal.doubleValue() >= 1.0) {
                            javafx.application.Platform.runLater(() -> imagePane.getChildren().remove(shimmer));
                        }
                    });
                }
            }
        } else {
            imagePane.getChildren().remove(shimmer);
            imgView.setImage(ImageCache.getImage("https://images.unsplash.com/photo-1517602302552-471fe67acf66?auto=format&fit=crop&w=180&q=80", 180, 180, false, true));
        }

        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(180, 180);
        clip.setArcWidth(20); clip.setArcHeight(20);
        imgView.setClip(clip);
        imagePane.getChildren().add(imgView);

        // Badges
        double score = 4.0 + (Math.abs(c.getTitle().hashCode()) % 10) * 0.1;
        Label ratingBadge = new Label(String.format("⭐ %.1f", score));
        ratingBadge.getStyleClass().add("badge-warning");
        ratingBadge.setStyle("-fx-font-size: 11px; -fx-padding: 3 8;");
        StackPane.setAlignment(ratingBadge, Pos.TOP_LEFT);
        StackPane.setMargin(ratingBadge, new Insets(10));
        imagePane.getChildren().add(ratingBadge);

        Label typeBadge = new Label(c.getType().toUpperCase());
        typeBadge.getStyleClass().add("badge-accent");
        typeBadge.setStyle("-fx-font-size: 10px; -fx-padding: 3 8;");
        StackPane.setAlignment(typeBadge, Pos.TOP_RIGHT);
        StackPane.setMargin(typeBadge, new Insets(10));
        imagePane.getChildren().add(typeBadge);

        VBox detailsBox = new VBox(6);
        detailsBox.setPadding(new Insets(12));
        detailsBox.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label(c.getTitle());
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        String desc = c.getDescription();
        if (desc == null || desc.isEmpty()) desc = "No description available.";
        if (desc.length() > 40) desc = desc.substring(0, 37) + "...";
        Label descLabel = new Label(desc);
        descLabel.getStyleClass().add("label-secondary");
        descLabel.setStyle("-fx-font-size: 12px;");

        HBox actionsRow = new HBox(8);
        actionsRow.setAlignment(Pos.CENTER_RIGHT);

        Button favBtn = new Button("❤️");
        favBtn.getStyleClass().add("btn-ghost");
        favBtn.setStyle("-fx-padding: 2; -fx-font-size: 13px;");
        favBtn.setOnAction(e -> {
            e.consume();
            MoodflixToast.show(card.getScene(), "Added to favorites: " + c.getTitle(), MoodflixToast.ToastType.SUCCESS);
        });

        Button watchlistBtn = new Button("➕");
        watchlistBtn.getStyleClass().add("btn-ghost");
        watchlistBtn.setStyle("-fx-padding: 2; -fx-font-size: 13px;");
        watchlistBtn.setOnAction(e -> {
            e.consume();
            String userEmail = SessionManager.getEmail();
            if (userEmail == null || userEmail.isEmpty()) {
                MoodflixToast.show(card.getScene(), "Please log in first", MoodflixToast.ToastType.ERROR);
                return;
            }
            new Thread(() -> {
                try {
                    com.moodflix.service.PostgreSQLWatchlistService wService = new com.moodflix.service.PostgreSQLWatchlistService();
                    wService.addToWatchlist(userEmail, c.getTitle());
                    javafx.application.Platform.runLater(() -> {
                        MoodflixToast.show(card.getScene(), "Added " + c.getTitle() + " to watchlist", MoodflixToast.ToastType.SUCCESS);
                    });
                } catch(Exception ex) {
                    javafx.application.Platform.runLater(() -> {
                        MoodflixToast.show(card.getScene(), "Failed: " + ex.getMessage(), MoodflixToast.ToastType.ERROR);
                    });
                }
            }).start();
        });

        actionsRow.getChildren().addAll(favBtn, watchlistBtn);

        HBox titleActions = new HBox(8, titleLabel, actionsRow);
        HBox.setHgrow(titleLabel, Priority.ALWAYS);
        titleActions.setAlignment(Pos.CENTER_LEFT);

        detailsBox.getChildren().addAll(titleActions, descLabel);
        card.getChildren().addAll(imagePane, detailsBox);

        // Hover
        card.setOnMouseEntered(ev -> {
            if (ThemeManager.isAnimationsEnabled()) {
                ScaleTransition st = new ScaleTransition(Duration.millis(180), card);
                st.setToX(1.05); st.setToY(1.05); st.play();
            }
        });
        card.setOnMouseExited(ev -> {
            if (ThemeManager.isAnimationsEnabled()) {
                ScaleTransition st = new ScaleTransition(Duration.millis(180), card);
                st.setToX(1.0); st.setToY(1.0); st.play();
            }
        });

        // Context Menu
        ContextMenu ctx = new ContextMenu();
        MenuItem play = new MenuItem("▶ Play Content");
        play.setOnAction(ev -> {
            if (c.getLink() != null && !c.getLink().isEmpty()) {
                try { java.awt.Desktop.getDesktop().browse(new java.net.URI(c.getLink())); } catch(Exception ex){}
            }
        });
        MenuItem watchItem = new MenuItem("➕ Watchlist");
        watchItem.setOnAction(ev -> watchlistBtn.fire());
        ctx.getItems().addAll(play, watchItem);
        card.setOnContextMenuRequested(ev -> ctx.show(card, ev.getScreenX(), ev.getScreenY()));

        card.setOnMouseClicked(ev -> {
            if (c.getLink() != null && !c.getLink().isEmpty()) {
                try { java.awt.Desktop.getDesktop().browse(new java.net.URI(c.getLink())); } catch(Exception ex){}
            }
        });

        return card;
    }

    // Getters
    public Pane getView() { return view; }
    public ComboBox<String> getMoodBox() { return moodCombo; }
    public ComboBox<String> getTypeBox() { return typeCombo; }
    public Button getProfileBtn() { return profileBtn; }
    public Button getWatchlistBtn() { return watchlistBtn; }
    public Button getFeedbackBtn() { return feedbackBtn; }
    public Button getSettingsBtn() { return settingsBtn; }
    public Button getLogoutBtn() { return logoutBtn; }
    public Button getActivityBtn() { return activityBtn; }
    public ImageView getProfilePhotoView() { return profilePhotoView; }
    public Button getMoodRecBtn() { return moodRecBtn; }
    public Button getGeneralRecBtn() { return generalRecBtn; }
    public ComboBox<String> getMoodCombo() { return moodCombo; }
    public ComboBox<String> getTypeCombo() { return typeCombo; }
    
    public TextField getSearchField() { return searchField; }
    public HBox getRecentSearchesBox() { return recentSearchesBox; }
    public Button getRecommendedBtn() { return recommendedBtn; }

    public void showRecommendationsLoading() {
        recommendationsContainer.getChildren().clear();
        ProgressIndicator spinner = new ProgressIndicator();
        spinner.setPrefSize(40, 40);
        spinner.setStyle("-fx-progress-color: -mf-accent;");
        Label loadingLabel = new Label("Curating best matches...");
        loadingLabel.getStyleClass().add("label-secondary");
        VBox box = new VBox(12, spinner, loadingLabel);
        box.setAlignment(Pos.CENTER);
        recommendationsContainer.getChildren().add(box);
    }

    public void showRecommendationsGrid(java.util.List<com.moodflix.model.Content> results) {
        recommendationsContainer.getChildren().clear();
        if (results == null || results.isEmpty()) {
            showRecommendationsError("No recommendations found matching your selection.");
            return;
        }
        
        ScrollPane scroll = new ScrollPane();
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setFitToHeight(true);
        scroll.setStyle("-fx-background: transparent; -fx-border-color: transparent;");
        
        HBox grid = new HBox(18);
        grid.setAlignment(Pos.CENTER_LEFT);
        grid.setPadding(new Insets(10, 0, 10, 0));
        
        for (com.moodflix.model.Content c : results) {
            VBox card = createMovieCard(c);
            grid.getChildren().add(card);
        }
        scroll.setContent(grid);
        recommendationsContainer.getChildren().add(scroll);
    }

    public void showRecommendationsError(String message) {
        recommendationsContainer.getChildren().clear();
        Label errorLabel = new Label(message);
        errorLabel.getStyleClass().add("badge-danger");
        errorLabel.setStyle("-fx-font-size: 14px; -fx-padding: 8 16;");
        recommendationsContainer.getChildren().add(errorLabel);
    }

    // Rich chat handler: styled bubbles, images, links
    private void handleRichChat(TextField chatInput, VBox chatHistory, ScrollPane chatScroll) {
        String userMsg = chatInput.getText();
        if (userMsg == null || userMsg.trim().isEmpty()) return;
        addChatBubble(chatHistory, userMsg, true, null, null);
        String lower = userMsg.toLowerCase();
        if (lower.contains("tell me about moodflix") || lower.contains("information about moodflix")) {
            addChatBubble(chatHistory,
                "MoodFlix is a mood-based entertainment application that recommends content based on users' emotions. " +
                "It leverages intuitive UI and smart algorithms to deliver personalized movie and music suggestions.",
                false, null, null);
        } else
        if (lower.contains("hello") || lower.contains("hi")) {
            addChatBubble(chatHistory, "Hello! How can I help you with MoodFlix today?", false, null, null);
        } else if (lower.contains("problem") || lower.contains("help") || lower.contains("issue")) {
            addChatBubble(chatHistory, "I'm here to help! You can ask about login issues, watchlist, or recommendations.", false, null, null);
        } else if (lower.contains("watchlist")) {
            addChatBubble(chatHistory, "You can add movies or series to your watchlist from the recommendations page.", false, null, null);
        } else if (lower.contains("admin")) {
            addChatBubble(chatHistory, "Admins can manage content and users from the Admin Dashboard.", false, null, null);
        } else if (lower.contains("bye")) {
            addChatBubble(chatHistory, "Goodbye! Enjoy MoodFlix!", false, null, null);
        } else {
            addChatBubble(chatHistory, "Searching for info...", false, null, null);
            new Thread(() -> {
                String searchResults = fetchOmdbListRich(userMsg, chatHistory, chatScroll);
                if (searchResults != null) {
                    javafx.application.Platform.runLater(() -> replaceLastBotBubble(chatHistory, searchResults, null, null));
                } else {
                    String title = extractTitle(userMsg);
                    OmdbResult result = (title != null && !title.isEmpty()) ? fetchOmdbInfoWithPosterRich(title) : null;
                    javafx.application.Platform.runLater(() -> {
                        if (result != null && result.text != null) {
                            replaceLastBotBubble(chatHistory, result.text, result.posterUrl, result.imdbUrl);
                        } else {
                            replaceLastBotBubble(chatHistory, "Sorry, I couldn't extract a movie or series title from your message.", null, null);
                        }
                    });
                }
                javafx.application.Platform.runLater(() -> chatScroll.setVvalue(1.0));
            }).start();
        }
        chatInput.clear();
        chatScroll.setVvalue(1.0);
    }

    private void addChatBubble(VBox chatHistory, String text, boolean isUser, String posterUrl, String imdbUrl) {
        HBox bubbleRow = new HBox();
        bubbleRow.setAlignment(isUser ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        bubbleRow.setPadding(new Insets(2, 0, 2, 0));
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        VBox bubble = new VBox(4);
        bubble.setPadding(new Insets(8, 12, 8, 12));
        bubble.setMaxWidth(260);
        bubble.getStyleClass().add(isUser ? "chat-bubble-user" : "chat-bubble-bot");
        
        Text msgText = new Text(text);
        msgText.setFill(Color.WHITE);
        TextFlow textFlow = new TextFlow(msgText);
        textFlow.setTextAlignment(isUser ? TextAlignment.RIGHT : TextAlignment.LEFT);
        bubble.getChildren().add(textFlow);
        
        if (posterUrl != null && !posterUrl.equals("N/A")) {
            try {
                ImageView poster = new ImageView(ImageCache.getImage(posterUrl, 80, 120, true, true));
                poster.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: -mf-accent; -fx-border-width: 1.5;");
                bubble.getChildren().add(poster);
            } catch(Exception ignored){}
        }
        if (imdbUrl != null && !imdbUrl.isEmpty() && hostServices != null) {
            Hyperlink imdbLink = new Hyperlink("View on IMDb");
            imdbLink.setStyle("-fx-text-fill: -mf-warning; -fx-font-weight: bold; -fx-font-size: 12px;");
            imdbLink.setOnAction(e -> hostServices.showDocument(imdbUrl));
            bubble.getChildren().add(imdbLink);
        }
        if (isUser) {
            bubbleRow.getChildren().addAll(spacer, bubble);
        } else {
            bubbleRow.getChildren().addAll(bubble, spacer);
        }
        chatHistory.getChildren().add(bubbleRow);
    }

    private void replaceLastBotBubble(VBox chatHistory, String text, String posterUrl, String imdbUrl) {
        for (int i = chatHistory.getChildren().size() - 1; i >= 0; i--) {
            HBox row = (HBox) chatHistory.getChildren().get(i);
            if (row.getAlignment() == Pos.CENTER_LEFT) {
                chatHistory.getChildren().remove(i);
                addChatBubble(chatHistory, text, false, posterUrl, imdbUrl);
                break;
            }
        }
    }

    private String fetchOmdbListRich(String query, VBox chatHistory, ScrollPane chatScroll) {
        try {
            String apiKey = "76dfa4c6";
            String urlStr = "https://www.omdbapi.com/?apikey=" + apiKey + "&s=" + URLEncoder.encode(query, "UTF-8");
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            JSONObject obj = new JSONObject(sb.toString());
            if (obj.has("Response") && obj.getString("Response").equals("True") && obj.has("Search")) {
                org.json.JSONArray arr = obj.getJSONArray("Search");
                if (arr.length() == 1) return null;
                javafx.application.Platform.runLater(() -> showOmdbListResultsRich(arr, chatHistory, chatScroll));
                return "Here are some results. Click a title for details.";
            }
        } catch (Exception e) { }
        return null;
    }

    private void showOmdbListResultsRich(org.json.JSONArray arr, VBox chatHistory, ScrollPane chatScroll) {
        for (int i = 0; i < arr.length(); i++) {
            JSONObject item = arr.getJSONObject(i);
            String title = item.optString("Title", "N/A");
            String year = item.optString("Year", "N/A");
            String imdbID = item.optString("imdbID", "");
            String poster = item.optString("Poster", "");
            HBox row = new HBox(8);
            row.setAlignment(Pos.CENTER_LEFT);
            if (poster != null && !poster.equals("N/A")) {
                try {
                    ImageView img = new ImageView(ImageCache.getImage(poster, 40, 60, true, true));
                    row.getChildren().add(img);
                } catch(Exception ignored){}
            }
            Hyperlink link = new Hyperlink(title + " (" + year + ")");
            link.getStyleClass().add("hyperlink");
            link.setCursor(Cursor.HAND);
            link.setOnAction(e -> {
                new Thread(() -> {
                    OmdbResult details = fetchOmdbInfoWithPosterByIdRich(imdbID);
                    javafx.application.Platform.runLater(() -> addChatBubble(chatHistory, details.text, false, details.posterUrl, details.imdbUrl));
                    javafx.application.Platform.runLater(() -> chatScroll.setVvalue(1.0));
                }).start();
            });
            row.getChildren().add(link);
            chatHistory.getChildren().add(row);
        }
        chatScroll.setVvalue(1.0);
    }

    private OmdbResult fetchOmdbInfoWithPosterByIdRich(String imdbID) {
        try {
            String apiKey = "76dfa4c6";
            String urlStr = "https://www.omdbapi.com/?apikey=" + apiKey + "&i=" + imdbID;
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            JSONObject obj = new JSONObject(sb.toString());
            if (obj.has("Response") && obj.getString("Response").equals("True")) {
                return formatOmdbDetailsRich(obj);
            }
        } catch (Exception e) { }
        return new OmdbResult("Sorry, I couldn't fetch details for that title.", null, null);
    }

    private OmdbResult fetchOmdbInfoWithPosterRich(String title) {
        try {
            String apiKey = "76dfa4c6";
            String typeParam = "";
            String seasonParam = "";
            String episodeParam = "";
            String lowerTitle = title.toLowerCase();
            int season = extractNumberAfterKeyword(lowerTitle, "season");
            int episode = extractNumberAfterKeyword(lowerTitle, "episode");
            if (lowerTitle.contains("series") || lowerTitle.contains("show")) {
                typeParam = "&type=series";
                title = title.replaceAll("(?i)series|show", "").trim();
            }
            if (season > 0) seasonParam = "&Season=" + season;
            if (episode > 0) episodeParam = "&Episode=" + episode;
            title = title.replaceAll("(?i)season \\d+", "").replaceAll("(?i)episode \\d+", "").trim();
            String urlStr = "https://www.omdbapi.com/?apikey=" + apiKey + "&t=" + URLEncoder.encode(title, "UTF-8") + typeParam + seasonParam + episodeParam;
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            JSONObject obj = new JSONObject(sb.toString());
            if (obj.has("Response") && obj.getString("Response").equals("True")) {
                return formatOmdbDetailsRich(obj);
            }
        } catch (Exception e) { }
        return new OmdbResult("Sorry, I couldn't find info on that title.", null, null);
    }

    private OmdbResult formatOmdbDetailsRich(JSONObject obj) {
        StringBuilder info = new StringBuilder();
        info.append("Title: ").append(obj.optString("Title", "N/A")).append("\n");
        info.append("Year: ").append(obj.optString("Year", "N/A")).append("\n");
        info.append("Type: ").append(obj.optString("Type", "N/A")).append("\n");
        info.append("Genre: ").append(obj.optString("Genre", "N/A")).append("\n");
        info.append("IMDb: ").append(obj.optString("imdbRating", "N/A")).append("\n");
        info.append("Plot: ").append(obj.optString("Plot", "N/A")).append("\n");
        String poster = obj.optString("Poster", "");
        String imdbID = obj.optString("imdbID", "");
        String imdbUrl = imdbID.isEmpty() ? null : ("https://www.imdb.com/title/" + imdbID);
        return new OmdbResult(info.toString(), poster, imdbUrl);
    }

    private int extractNumberAfterKeyword(String text, String keyword) {
        java.util.regex.Matcher m = java.util.regex.Pattern.compile(keyword + " (\\d+)").matcher(text);
        if (m.find()) {
            try { return Integer.parseInt(m.group(1)); } catch (Exception e) { return -1; }
        }
        return -1;
    }

    private String extractTitle(String msg) {
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("[\"']([^\"']+)[\"']").matcher(msg);
        if (m.find()) return m.group(1);
        String cleaned = msg.replaceAll("(?i)movie|series|about|info|recommend|please|show|me|tell|find|watch|of|the|a|an|on|for|give|suggest|\\?", "").trim();
        if (cleaned.length() >= 2) return cleaned;
        return msg.trim();
    }

    private static class OmdbResult {
        String text;
        String posterUrl;
        String imdbUrl;
        OmdbResult(String text, String posterUrl, String imdbUrl) {
            this.text = text;
            this.posterUrl = posterUrl;
            this.imdbUrl = imdbUrl;
        }
    }
}
