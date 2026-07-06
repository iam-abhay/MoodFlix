package com.moodflix.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.Node;
import javafx.util.Callback;
import org.json.JSONObject;

import com.moodflix.util.ThemeManager;

public class WatchlistPage {
    private VBox view;
    private ListView<JSONObject> watchlistView;
    private Button refreshBtn;
    private Button removeBtn;
    private Button clearAllBtn;
    private Label statusLabel;
    private Button backBtn;
    private Label headerLabel;
    private Label countLabel;
    private VBox mainContent;
    private HBox headerSection;
    private VBox watchlistSection;
    private HBox actionButtons;
    private ScrollPane scrollPane;

    public WatchlistPage() {
        createView();
    }

    private void createView() {
        view = new VBox(30);
        view.setAlignment(Pos.TOP_CENTER);
        view.setPadding(new Insets(40));
        view.getStyleClass().add("auth-container");

        createHeaderSection();
        createMainContentSection();
        createActionButtons();

        view.getChildren().addAll(headerSection, mainContent);

        ThemeManager.fadeIn(view, 400);
    }

    private void createHeaderSection() {
        headerSection = new HBox(20);
        headerSection.setAlignment(Pos.CENTER);
        headerSection.setPadding(new Insets(25));
        headerSection.getStyleClass().add("glass-card");

        Label headerIcon = new Label("📺");
        headerIcon.setStyle("-fx-font-size: 52px;");
        headerIcon.getStyleClass().add("label-accent");

        VBox headerText = new VBox(8);

        headerLabel = new Label("🎬 Your Watchlist");
        headerLabel.getStyleClass().add("hero-title");
        headerLabel.setStyle("-fx-font-size: 32px;");

        countLabel = new Label("0 items in your watchlist");
        countLabel.getStyleClass().add("label-secondary");

        headerText.getChildren().addAll(headerLabel, countLabel);
        headerSection.getChildren().addAll(headerIcon, headerText);

        ThemeManager.slideUp(headerSection, 500, 100);
    }

    private void createMainContentSection() {
        mainContent = new VBox(30);
        mainContent.setAlignment(Pos.CENTER);
        mainContent.setPadding(new Insets(35));
        mainContent.getStyleClass().add("glass-card");

        createWatchlistSection();

        statusLabel = new Label();
        statusLabel.getStyleClass().add("label-secondary");
        statusLabel.setAlignment(Pos.CENTER);
        statusLabel.setMaxWidth(Double.MAX_VALUE);

        mainContent.getChildren().add(statusLabel);

        ThemeManager.slideUp(mainContent, 500, 200);
    }

    private void createWatchlistSection() {
        watchlistSection = new VBox(15);
        watchlistSection.setAlignment(Pos.CENTER);

        Label watchlistTitle = new Label("🎬 Your Saved Content");
        watchlistTitle.getStyleClass().add("section-title");

        watchlistView = new ListView<>();
        watchlistView.setPrefHeight(400);

        watchlistView.setCellFactory(new Callback<ListView<JSONObject>, ListCell<JSONObject>>() {
            @Override
            public ListCell<JSONObject> call(ListView<JSONObject> param) {
                return new ListCell<JSONObject>() {
                    @Override
                    protected void updateItem(JSONObject item, boolean empty) {
                        super.updateItem(item, empty);

                        if (empty || item == null) {
                            setGraphic(null);
                            setText(null);
                        } else {
                            VBox card = createWatchlistCard(item);
                            setGraphic(card);
                            setText(null);
                        }
                    }
                };
            }
        });

        scrollPane = new ScrollPane(watchlistView);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        watchlistSection.getChildren().addAll(watchlistTitle, scrollPane);
        mainContent.getChildren().add(watchlistSection);
    }

    private VBox createWatchlistCard(JSONObject item) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(20));
        card.getStyleClass().add("card");
        card.setMaxWidth(Double.MAX_VALUE);
        card.setStyle("-fx-min-height: 100;");
        card.setCursor(javafx.scene.Cursor.HAND);

        HBox headerRow = new HBox(15);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        Label contentIcon = new Label("🎬");
        contentIcon.setStyle("-fx-font-size: 28px;");
        contentIcon.getStyleClass().add("label-accent");

        String type = item.optString("type", "movie");
        Label typeLabel = new Label(type.equals("movie") ? "🎬 Movie" : "📺 TV Show");
        typeLabel.getStyleClass().addAll("btn", "badge-accent");
        typeLabel.setStyle("-fx-font-size: 12px; -fx-padding: 4 12;");

        headerRow.getChildren().addAll(contentIcon, typeLabel);

        String title = item.optString("title", "Unknown Title");
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("section-title");
        titleLabel.setStyle("-fx-font-size: 18px;");
        titleLabel.setWrapText(true);

        String addedDate = item.optString("addedDate", "");
        if (!addedDate.isEmpty()) {
            Label dateLabel = new Label("📅 Added: " + addedDate);
            dateLabel.getStyleClass().add("label-muted");
            card.getChildren().addAll(headerRow, titleLabel, dateLabel);
        } else {
            card.getChildren().addAll(headerRow, titleLabel);
        }

        ThemeManager.fadeIn(card, 300, 50);

        return card;
    }

    private void createActionButtons() {
        actionButtons = new HBox(20);
        actionButtons.setAlignment(Pos.CENTER);

        refreshBtn = new Button("🔄 Refresh Watchlist");
        refreshBtn.getStyleClass().addAll("btn", "btn-success");
        refreshBtn.setStyle("-fx-min-width: 160;");

        removeBtn = new Button("🗑️ Remove Selected");
        removeBtn.getStyleClass().addAll("btn", "btn-warning");
        removeBtn.setStyle("-fx-min-width: 160;");

        clearAllBtn = new Button("🗑️ Clear All");
        clearAllBtn.getStyleClass().addAll("btn", "btn-danger");
        clearAllBtn.setStyle("-fx-min-width: 160;");

        backBtn = new Button("← Back to Dashboard");
        backBtn.getStyleClass().addAll("btn", "btn-outline");
        backBtn.setStyle("-fx-min-width: 160;");

        actionButtons.getChildren().addAll(refreshBtn, removeBtn, clearAllBtn, backBtn);
        mainContent.getChildren().add(actionButtons);

        ThemeManager.slideUp(actionButtons, 400, 300);
    }

    // Getters
    public VBox getView() { return view; }
    public ListView<JSONObject> getWatchlistView() { return watchlistView; }
    public Button getRefreshBtn() { return refreshBtn; }
    public Button getRemoveBtn() { return removeBtn; }
    public Button getClearAllBtn() { return clearAllBtn; }
    public Button getBackBtn() { return backBtn; }
    public Label getStatusLabel() { return statusLabel; }
    public Label getHeaderLabel() { return headerLabel; }
    public Label getCountLabel() { return countLabel; }

    // Method to update header for admin viewing user's watchlist
    public void setAdminMode(String userEmail) {
        headerLabel.setText("👑 Admin View: " + userEmail + "'s Watchlist");
        headerLabel.getStyleClass().add("badge-danger");
    }
}