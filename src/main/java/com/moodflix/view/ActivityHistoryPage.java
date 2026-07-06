package com.moodflix.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.control.cell.PropertyValueFactory;
import com.moodflix.model.Activity;
import com.moodflix.util.ThemeManager;

public class ActivityHistoryPage {
    private VBox mainLayout;
    private TableView<Activity> activityTable;
    private Label statusLabel;
    private Button refreshBtn;
    private Button backBtn;
    private ComboBox<String> filterMoodBox;
    private ComboBox<String> filterTypeBox;
    private DatePicker startDatePicker;
    private DatePicker endDatePicker;
    private Button filterBtn;
    private Button clearFilterBtn;
    private Label totalActivitiesLabel;
    private Label totalWatchTimeLabel;
    private Label titleLabel;

    public ActivityHistoryPage() {
        initializeUI();
    }

    private void initializeUI() {
        mainLayout = new VBox(24);
        mainLayout.setPadding(new Insets(30));
        mainLayout.getStyleClass().add("auth-container");

        HBox headerBox = createHeader();
        HBox filterBox = createFilterSection();
        HBox statsBox = createStatisticsSection();
        VBox tableBox = createActivityTable();
        HBox actionBox = createActionButtons();

        mainLayout.getChildren().addAll(headerBox, filterBox, statsBox, tableBox, actionBox);

        ThemeManager.fadeIn(mainLayout, 400);
    }

    private HBox createHeader() {
        HBox headerBox = new HBox(20);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setPadding(new Insets(20));
        headerBox.getStyleClass().add("glass-card");

        Label icon = new Label("📊");
        icon.setStyle("-fx-font-size: 36px;");

        VBox titleBox = new VBox(4);
        titleLabel = new Label("📊 Activity History");
        titleLabel.getStyleClass().add("hero-title");
        titleLabel.setStyle("-fx-font-size: 26px;");

        Label subtitleLabel = new Label("Track your viewing history and preferences");
        subtitleLabel.getStyleClass().add("label-secondary");

        titleBox.getChildren().addAll(titleLabel, subtitleLabel);
        headerBox.getChildren().addAll(icon, titleBox);

        ThemeManager.slideUp(headerBox, 500, 100);
        return headerBox;
    }

    private HBox createFilterSection() {
        HBox filterBox = new HBox(14);
        filterBox.setAlignment(Pos.CENTER_LEFT);
        filterBox.setPadding(new Insets(16));
        filterBox.getStyleClass().add("card");

        Label filterLabel = new Label("🔍 Filter:");
        filterLabel.getStyleClass().add("section-title");
        filterLabel.setStyle("-fx-font-size: 14px;");

        Label moodLabel = new Label("Mood:");
        moodLabel.getStyleClass().add("label-secondary");
        filterMoodBox = new ComboBox<>();
        filterMoodBox.getItems().addAll("All", "Happy", "Sad", "Excited", "Relaxed", "Romantic", "Thriller");
        filterMoodBox.setValue("All");
        filterMoodBox.setPrefWidth(120);

        Label typeLabel = new Label("Type:");
        typeLabel.getStyleClass().add("label-secondary");
        filterTypeBox = new ComboBox<>();
        filterTypeBox.getItems().addAll("All", "Movie", "Series", "Anime", "Documentary");
        filterTypeBox.setValue("All");
        filterTypeBox.setPrefWidth(120);

        Label startDateLabel = new Label("From:");
        startDateLabel.getStyleClass().add("label-secondary");
        startDatePicker = new DatePicker();
        startDatePicker.setPrefWidth(120);

        Label endDateLabel = new Label("To:");
        endDateLabel.getStyleClass().add("label-secondary");
        endDatePicker = new DatePicker();
        endDatePicker.setPrefWidth(120);

        filterBtn = new Button("🔍 Apply");
        filterBtn.getStyleClass().addAll("btn", "btn-success");

        clearFilterBtn = new Button("🗑️ Clear");
        clearFilterBtn.getStyleClass().addAll("btn", "btn-danger");

        filterBox.getChildren().addAll(
            filterLabel,
            new Separator(javafx.geometry.Orientation.VERTICAL),
            moodLabel, filterMoodBox,
            typeLabel, filterTypeBox,
            startDateLabel, startDatePicker,
            endDateLabel, endDatePicker,
            filterBtn, clearFilterBtn
        );

        ThemeManager.slideUp(filterBox, 400, 200);
        return filterBox;
    }

    private HBox createStatisticsSection() {
        HBox statsBox = new HBox(24);
        statsBox.setAlignment(Pos.CENTER_LEFT);
        statsBox.setPadding(new Insets(16));
        statsBox.getStyleClass().add("card");

        VBox totalActivitiesBox = new VBox(4);
        totalActivitiesBox.getStyleClass().add("stat-card");
        Label totalActivitiesTitle = new Label("📺 Total Activities");
        totalActivitiesTitle.getStyleClass().add("stat-label");
        totalActivitiesLabel = new Label("0");
        totalActivitiesLabel.getStyleClass().add("stat-value");
        totalActivitiesBox.getChildren().addAll(totalActivitiesTitle, totalActivitiesLabel);

        VBox totalWatchTimeBox = new VBox(4);
        totalWatchTimeBox.getStyleClass().add("stat-card");
        Label totalWatchTimeTitle = new Label("⏱️ Watch Time");
        totalWatchTimeTitle.getStyleClass().add("stat-label");
        totalWatchTimeLabel = new Label("0 hours");
        totalWatchTimeLabel.getStyleClass().add("stat-value");
        totalWatchTimeBox.getChildren().addAll(totalWatchTimeTitle, totalWatchTimeLabel);

        VBox favoriteMoodBox = new VBox(4);
        favoriteMoodBox.getStyleClass().add("stat-card");
        Label favoriteMoodTitle = new Label("😊 Favorite Mood");
        favoriteMoodTitle.getStyleClass().add("stat-label");
        Label favoriteMoodLabel = new Label("Happy");
        favoriteMoodLabel.getStyleClass().add("stat-value");
        favoriteMoodBox.getChildren().addAll(favoriteMoodTitle, favoriteMoodLabel);

        VBox favoriteTypeBox = new VBox(4);
        favoriteTypeBox.getStyleClass().add("stat-card");
        Label favoriteTypeTitle = new Label("🎬 Favorite Type");
        favoriteTypeTitle.getStyleClass().add("stat-label");
        Label favoriteTypeLabel = new Label("Movie");
        favoriteTypeLabel.getStyleClass().add("stat-value");
        favoriteTypeBox.getChildren().addAll(favoriteTypeTitle, favoriteTypeLabel);

        statsBox.getChildren().addAll(totalActivitiesBox, totalWatchTimeBox, favoriteMoodBox, favoriteTypeBox);

        ThemeManager.slideUp(statsBox, 400, 300);
        return statsBox;
    }

    private VBox createActivityTable() {
        VBox tableBox = new VBox(12);
        tableBox.setPadding(new Insets(16));
        tableBox.getStyleClass().add("glass-card");

        Label tableTitle = new Label("📋 Recent Activities");
        tableTitle.getStyleClass().add("section-title");

        activityTable = new TableView<>();
        activityTable.setPlaceholder(new Label("No activities found"));

        TableColumn<Activity, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        titleCol.setPrefWidth(200);

        TableColumn<Activity, String> moodCol = new TableColumn<>("Mood");
        moodCol.setCellValueFactory(new PropertyValueFactory<>("mood"));
        moodCol.setPrefWidth(100);

        TableColumn<Activity, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        typeCol.setPrefWidth(100);

        TableColumn<Activity, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("formattedDate"));
        dateCol.setPrefWidth(150);

        TableColumn<Activity, String> durationCol = new TableColumn<>("Duration");
        durationCol.setCellValueFactory(new PropertyValueFactory<>("formattedDuration"));
        durationCol.setPrefWidth(100);

        TableColumn<Activity, String> ratingCol = new TableColumn<>("Rating");
        ratingCol.setCellValueFactory(new PropertyValueFactory<>("formattedRating"));
        ratingCol.setPrefWidth(80);

        activityTable.getColumns().addAll(titleCol, moodCol, typeCol, dateCol, durationCol, ratingCol);
        activityTable.setPrefHeight(400);

        statusLabel = new Label("Ready to load activities");
        statusLabel.getStyleClass().add("label-muted");

        tableBox.getChildren().addAll(tableTitle, activityTable, statusLabel);

        ThemeManager.slideUp(tableBox, 500, 400);
        return tableBox;
    }

    private HBox createActionButtons() {
        HBox actionBox = new HBox(16);
        actionBox.setAlignment(Pos.CENTER_RIGHT);

        refreshBtn = new Button("🔄 Refresh");
        refreshBtn.getStyleClass().addAll("btn", "btn-success");

        backBtn = new Button("⬅️ Back");
        backBtn.getStyleClass().addAll("btn", "btn-outline");

        actionBox.getChildren().addAll(refreshBtn, backBtn);
        return actionBox;
    }

    // Getters
    public VBox getView() { return mainLayout; }
    public TableView<Activity> getActivityTable() { return activityTable; }
    public Label getStatusLabel() { return statusLabel; }
    public Button getRefreshBtn() { return refreshBtn; }
    public Button getBackBtn() { return backBtn; }
    public ComboBox<String> getFilterMoodBox() { return filterMoodBox; }
    public ComboBox<String> getFilterTypeBox() { return filterTypeBox; }
    public DatePicker getStartDatePicker() { return startDatePicker; }
    public DatePicker getEndDatePicker() { return endDatePicker; }
    public Button getFilterBtn() { return filterBtn; }
    public Button getClearFilterBtn() { return clearFilterBtn; }
    public Label getTotalActivitiesLabel() { return totalActivitiesLabel; }
    public Label getTotalWatchTimeLabel() { return totalWatchTimeLabel; }

    public void setAdminMode(String userEmail) {
        if (titleLabel != null) {
            titleLabel.setText("📊 Activity History - " + userEmail);
        }
    }
} 