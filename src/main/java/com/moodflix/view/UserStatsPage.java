package com.moodflix.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import com.moodflix.util.ThemeManager;

public class UserStatsPage {
    private VBox mainLayout;
    private Label totalActivitiesLabel;
    private Label totalWatchTimeLabel;
    private Label favoriteMoodLabel;
    private Label favoriteTypeLabel;
    private Label mostWatchedLabel;
    private Button backBtn;

    public UserStatsPage() {
        initializeUI();
    }

    private void initializeUI() {
        mainLayout = new VBox(28);
        mainLayout.setPadding(new Insets(40));
        mainLayout.setAlignment(Pos.TOP_CENTER);
        mainLayout.getStyleClass().add("auth-container");

        // Header
        VBox headerBox = new VBox(6);
        headerBox.setAlignment(Pos.CENTER);
        headerBox.setPadding(new Insets(20));
        headerBox.getStyleClass().add("glass-card");

        Label title = new Label("📈 Analytics & Insights");
        title.getStyleClass().add("hero-title");
        title.setStyle("-fx-font-size: 28px;");

        Label subtitle = new Label("Your cinematic journey and watching habits at a glance");
        subtitle.getStyleClass().add("label-secondary");

        headerBox.getChildren().addAll(title, subtitle);

        // Stats Grid
        TilePane grid = new TilePane();
        grid.setPrefColumns(3);
        grid.setHgap(20);
        grid.setVgap(20);
        grid.setAlignment(Pos.CENTER);

        totalActivitiesLabel = new Label("...");
        totalWatchTimeLabel = new Label("...");
        favoriteMoodLabel = new Label("...");
        favoriteTypeLabel = new Label("...");
        mostWatchedLabel = new Label("...");

        VBox card1 = createStatCard("📊", "Total Activities", totalActivitiesLabel);
        VBox card2 = createStatCard("⏱️", "Total Watch Time", totalWatchTimeLabel);
        VBox card3 = createStatCard("🎭", "Favorite Mood", favoriteMoodLabel);
        VBox card4 = createStatCard("📺", "Favorite Type", favoriteTypeLabel);
        VBox card5 = createStatCard("🎬", "Most Watched Title", mostWatchedLabel);

        grid.getChildren().addAll(card1, card2, card3, card4, card5);

        // Back Button
        backBtn = new Button("⬅️ Back to Dashboard");
        backBtn.getStyleClass().addAll("btn", "btn-outline");
        backBtn.setStyle("-fx-min-width: 180;");

        mainLayout.getChildren().addAll(headerBox, grid, backBtn);

        ThemeManager.fadeIn(mainLayout, 400);
        ThemeManager.slideUp(grid, 500, 200);
    }

    private VBox createStatCard(String icon, String labelText, Label valueLabel) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.getStyleClass().add("stat-card");
        card.setPrefWidth(220);
        card.setMinWidth(200);
        card.setPadding(new Insets(20));
        
        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 32px;");
        
        valueLabel.getStyleClass().add("stat-value");
        valueLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-wrap-text: true; -fx-text-alignment: center;");
        
        Label descLabel = new Label(labelText);
        descLabel.getStyleClass().add("stat-label");
        
        card.getChildren().addAll(iconLabel, valueLabel, descLabel);
        return card;
    }

    public VBox getView() { return mainLayout; }
    public Label getTotalActivitiesLabel() { return totalActivitiesLabel; }
    public Label getTotalWatchTimeLabel() { return totalWatchTimeLabel; }
    public Label getFavoriteMoodLabel() { return favoriteMoodLabel; }
    public Label getFavoriteTypeLabel() { return favoriteTypeLabel; }
    public Label getMostWatchedLabel() { return mostWatchedLabel; }
    public Button getBackBtn() { return backBtn; }
} 