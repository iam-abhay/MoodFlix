package com.moodflix.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import com.moodflix.util.ThemeManager;

/**
 * Modern settings configuration page.
 * Allows theme toggle, animation bypass, language selections, and diagnostics monitoring.
 */
public class SettingsPage {
    private ScrollPane view;
    private VBox mainContainer;
    private Button backBtn;
    
    private ComboBox<String> themeCombo;
    private CheckBox animCheckBox;
    private ComboBox<String> langCombo;
    
    private Label dbStatusVal;
    private Label dbPoolVal;
    private Label apiStatusVal;
    private Label memoryVal;
    
    private Button checkDbBtn;
    private Button checkApiBtn;

    public SettingsPage() {
        createView();
    }

    private void createView() {
        mainContainer = new VBox(24);
        mainContainer.setAlignment(Pos.TOP_CENTER);
        mainContainer.setPadding(new Insets(40));
        mainContainer.getStyleClass().add("auth-container");

        // Header Section
        HBox headerSection = new HBox(20);
        headerSection.setAlignment(Pos.CENTER_LEFT);
        headerSection.setPadding(new Insets(24));
        headerSection.getStyleClass().add("glass-card");

        Label headerIcon = new Label("⚙️");
        headerIcon.setStyle("-fx-font-size: 48px;");
        headerIcon.getStyleClass().add("label-accent");

        VBox headerText = new VBox(6);
        Label title = new Label("Application Settings");
        title.getStyleClass().add("hero-title");
        title.setStyle("-fx-font-size: 28px;");

        Label subtitle = new Label("Configure your theme preferences, animations, and monitor system diagnostics.");
        subtitle.getStyleClass().add("label-secondary");
        headerText.getChildren().addAll(title, subtitle);
        headerSection.getChildren().addAll(headerIcon, headerText);

        // General settings card
        VBox genCard = new VBox(20);
        genCard.setPadding(new Insets(24));
        genCard.getStyleClass().add("glass-card");
        
        Label genTitle = new Label("⚙️ General Settings");
        genTitle.getStyleClass().add("section-title");

        GridPane genGrid = new GridPane();
        genGrid.setHgap(20);
        genGrid.setVgap(16);
        
        Label themeLabel = new Label("Preferred Theme");
        themeLabel.getStyleClass().add("label-secondary");
        themeCombo = new ComboBox<>();
        themeCombo.getItems().addAll("Dark Theme", "Light Theme");
        themeCombo.setPrefWidth(200);
        genGrid.add(themeLabel, 0, 0);
        genGrid.add(themeCombo, 1, 0);

        Label animLabel = new Label("Enable Smooth Animations");
        animLabel.getStyleClass().add("label-secondary");
        animCheckBox = new CheckBox();
        genGrid.add(animLabel, 0, 1);
        genGrid.add(animCheckBox, 1, 1);

        Label langLabel = new Label("Interface Language");
        langLabel.getStyleClass().add("label-secondary");
        langCombo = new ComboBox<>();
        langCombo.getItems().addAll("English", "Hindi", "Spanish", "French");
        langCombo.setPrefWidth(200);
        genGrid.add(langLabel, 0, 2);
        genGrid.add(langCombo, 1, 2);

        genCard.getChildren().addAll(genTitle, genGrid);

        // Diagnostics Card
        VBox diagCard = new VBox(20);
        diagCard.setPadding(new Insets(24));
        diagCard.getStyleClass().add("glass-card");

        Label diagTitle = new Label("📈 System Diagnostics");
        diagTitle.getStyleClass().add("section-title");

        GridPane diagGrid = new GridPane();
        diagGrid.setHgap(20);
        diagGrid.setVgap(16);

        Label dbLabel = new Label("Database Status");
        dbLabel.getStyleClass().add("label-secondary");
        dbStatusVal = new Label("Checking...");
        dbStatusVal.getStyleClass().add("badge-info");
        diagGrid.add(dbLabel, 0, 0);
        diagGrid.add(dbStatusVal, 1, 0);

        Label dbPoolLabel = new Label("Connection Pool");
        dbPoolLabel.getStyleClass().add("label-secondary");
        dbPoolVal = new Label("Connections: -");
        diagGrid.add(dbPoolLabel, 0, 1);
        diagGrid.add(dbPoolVal, 1, 1);

        Label apiLabel = new Label("OMDB API Status");
        apiLabel.getStyleClass().add("label-secondary");
        apiStatusVal = new Label("Checking...");
        apiStatusVal.getStyleClass().add("badge-info");
        diagGrid.add(apiLabel, 0, 2);
        diagGrid.add(apiStatusVal, 1, 2);

        Label memLabel = new Label("JVM Memory Usage");
        memLabel.getStyleClass().add("label-secondary");
        memoryVal = new Label("Calculating...");
        diagGrid.add(memLabel, 0, 3);
        diagGrid.add(memoryVal, 1, 3);

        HBox diagActions = new HBox(12);
        checkDbBtn = new Button("Refresh DB Status");
        checkDbBtn.getStyleClass().addAll("btn", "btn-ghost");
        checkApiBtn = new Button("Test OMDB API");
        checkApiBtn.getStyleClass().addAll("btn", "btn-ghost");
        diagActions.getChildren().addAll(checkDbBtn, checkApiBtn);

        diagCard.getChildren().addAll(diagTitle, diagGrid, diagActions);

        // About Card
        VBox aboutCard = new VBox(14);
        aboutCard.setPadding(new Insets(24));
        aboutCard.getStyleClass().add("glass-card");

        Label aboutTitle = new Label("ℹ️ About MoodFlix");
        aboutTitle.getStyleClass().add("section-title");

        Label appName = new Label("MoodFlix Desktop App");
        appName.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        Label version = new Label("Version: 1.0.0 (Production Ready)");
        version.getStyleClass().add("label-secondary");
        Label dev = new Label("Developed by: Advanced FX/UX Product Team");
        dev.getStyleClass().add("label-muted");

        aboutCard.getChildren().addAll(aboutTitle, appName, version, dev);

        // Back button row
        HBox actionRow = new HBox(20);
        actionRow.setAlignment(Pos.CENTER);
        backBtn = new Button("Back to Dashboard");
        backBtn.getStyleClass().addAll("btn", "btn-outline");
        actionRow.getChildren().add(backBtn);

        mainContainer.getChildren().addAll(headerSection, genCard, diagCard, aboutCard, actionRow);

        view = new ScrollPane(mainContainer);
        view.setFitToWidth(true);
        view.setFitToHeight(true);

        ThemeManager.fadeIn(mainContainer, 500);
    }

    public ScrollPane getView() { return view; }
    public Button getBackBtn() { return backBtn; }
    public ComboBox<String> getThemeCombo() { return themeCombo; }
    public CheckBox getAnimCheckBox() { return animCheckBox; }
    public ComboBox<String> getLangCombo() { return langCombo; }
    
    public Label getDbStatusVal() { return dbStatusVal; }
    public Label getDbPoolVal() { return dbPoolVal; }
    public Label getApiStatusVal() { return apiStatusVal; }
    public Label getMemoryVal() { return memoryVal; }
    
    public Button getCheckDbBtn() { return checkDbBtn; }
    public Button getCheckApiBtn() { return checkApiBtn; }
}
