package com.moodflix.util;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.paint.Color;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

/**
 * Performance monitoring dashboard for the Moodflix application
 */
public class PerformanceDashboard {
    private VBox view;
    private Label cacheStatsLabel;
    private Label httpStatsLabel;
    private Label memoryStatsLabel;
    private Label responseTimeLabel;
    private Label navigationStatsLabel;
    private Label clickStatsLabel;
    private Label backNavigationStatsLabel;
    private ProgressBar cacheHitRateBar;
    private ProgressBar httpSuccessRateBar;
    private ProgressBar navigationSuccessRateBar;
    private ProgressBar backNavigationSuccessRateBar;
    private Button refreshBtn;
    private Button clearCacheBtn;
    private Button clearConnectionsBtn;
    private Timeline updateTimer;
    
    public PerformanceDashboard() {
        createView();
        startMonitoring();
    }
    
    private void createView() {
        view = new VBox(20);
        view.setAlignment(Pos.TOP_CENTER);
        view.setPadding(new Insets(30));
        view.setStyle("-fx-background-color: linear-gradient(to bottom right, #181c24 0%, #23272f 100%);");
        
        // Header
        Label headerLabel = new Label("📊 Performance Dashboard");
        headerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        headerLabel.setTextFill(Color.WHITE);
        headerLabel.setAlignment(Pos.CENTER);
        
        // Stats Grid
        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(20);
        statsGrid.setVgap(15);
        statsGrid.setAlignment(Pos.CENTER);
        
        // Cache Statistics
        VBox cacheBox = createStatBox("Cache Performance", "🎯");
        cacheStatsLabel = new Label("Loading cache stats...");
        cacheStatsLabel.setFont(Font.font("Arial", 12));
        cacheStatsLabel.setTextFill(Color.LIGHTGRAY);
        cacheStatsLabel.setWrapText(true);
        
        cacheHitRateBar = new ProgressBar(0.0);
        cacheHitRateBar.setPrefWidth(200);
        cacheHitRateBar.setStyle("-fx-accent: #28a745;");
        
        cacheBox.getChildren().addAll(cacheStatsLabel, cacheHitRateBar);
        
        // HTTP Statistics
        VBox httpBox = createStatBox("HTTP Performance", "🌐");
        httpStatsLabel = new Label("Loading HTTP stats...");
        httpStatsLabel.setFont(Font.font("Arial", 12));
        httpStatsLabel.setTextFill(Color.LIGHTGRAY);
        httpStatsLabel.setWrapText(true);
        
        httpSuccessRateBar = new ProgressBar(0.0);
        httpSuccessRateBar.setPrefWidth(200);
        httpSuccessRateBar.setStyle("-fx-accent: #17a2b8;");
        
        httpBox.getChildren().addAll(httpStatsLabel, httpSuccessRateBar);
        
        // Memory Statistics
        VBox memoryBox = createStatBox("Memory Usage", "💾");
        memoryStatsLabel = new Label("Loading memory stats...");
        memoryStatsLabel.setFont(Font.font("Arial", 12));
        memoryStatsLabel.setTextFill(Color.LIGHTGRAY);
        memoryStatsLabel.setWrapText(true);
        
        memoryBox.getChildren().add(memoryStatsLabel);
        
        // Response Time
        VBox responseBox = createStatBox("Response Times", "⚡");
        responseTimeLabel = new Label("Loading response times...");
        responseTimeLabel.setFont(Font.font("Arial", 12));
        responseTimeLabel.setTextFill(Color.LIGHTGRAY);
        responseTimeLabel.setWrapText(true);
        
        responseBox.getChildren().add(responseTimeLabel);
        
        // Navigation Statistics
        VBox navigationBox = createStatBox("Navigation Performance", "🚀");
        navigationStatsLabel = new Label("Loading navigation stats...");
        navigationStatsLabel.setFont(Font.font("Arial", 12));
        navigationStatsLabel.setTextFill(Color.LIGHTGRAY);
        navigationStatsLabel.setWrapText(true);
        
        navigationSuccessRateBar = new ProgressBar(0.0);
        navigationSuccessRateBar.setPrefWidth(200);
        navigationSuccessRateBar.setStyle("-fx-accent: #ffc107;");
        
        navigationBox.getChildren().addAll(navigationStatsLabel, navigationSuccessRateBar);
        
        // Click Statistics
        VBox clickBox = createStatBox("Click Performance", "🖱️");
        clickStatsLabel = new Label("Loading click stats...");
        clickStatsLabel.setFont(Font.font("Arial", 12));
        clickStatsLabel.setTextFill(Color.LIGHTGRAY);
        clickStatsLabel.setWrapText(true);
        
        clickBox.getChildren().add(clickStatsLabel);
        
        // Back Navigation Statistics
        VBox backNavigationBox = createStatBox("Back Navigation", "⬅️");
        backNavigationStatsLabel = new Label("Loading back navigation stats...");
        backNavigationStatsLabel.setFont(Font.font("Arial", 12));
        backNavigationStatsLabel.setTextFill(Color.LIGHTGRAY);
        backNavigationStatsLabel.setWrapText(true);
        
        backNavigationSuccessRateBar = new ProgressBar(0.0);
        backNavigationSuccessRateBar.setPrefWidth(200);
        backNavigationSuccessRateBar.setStyle("-fx-accent: #6f42c1;");
        
        backNavigationBox.getChildren().addAll(backNavigationStatsLabel, backNavigationSuccessRateBar);
        
        // Add to grid
        statsGrid.add(cacheBox, 0, 0);
        statsGrid.add(httpBox, 1, 0);
        statsGrid.add(memoryBox, 0, 1);
        statsGrid.add(responseBox, 1, 1);
        statsGrid.add(navigationBox, 0, 2);
        statsGrid.add(clickBox, 1, 2);
        statsGrid.add(backNavigationBox, 0, 3);
        
        // Control buttons
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        
        refreshBtn = new Button("🔄 Refresh Stats");
        refreshBtn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20;");
        refreshBtn.setOnAction(e -> updateStats());
        
        clearCacheBtn = new Button("🗑️ Clear Cache");
        clearCacheBtn.setStyle("-fx-background-color: #ffc107; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20;");
        clearCacheBtn.setOnAction(e -> {
            PerformanceOptimizer.clearAllCaches();
            updateStats();
        });
        
        clearConnectionsBtn = new Button("🔌 Clear Connections");
        clearConnectionsBtn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20;");
        clearConnectionsBtn.setOnAction(e -> {
            HttpConnectionManager.clearConnectionPool();
            updateStats();
        });
        
        buttonBox.getChildren().addAll(refreshBtn, clearCacheBtn, clearConnectionsBtn);
        
        // Add all components
        view.getChildren().addAll(headerLabel, statsGrid, buttonBox);
    }
    
    private VBox createStatBox(String title, String icon) {
        VBox box = new VBox(10);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(20));
        box.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-background-radius: 10;");
        box.setPrefWidth(300);
        box.setPrefHeight(200);
        
        Label titleLabel = new Label(icon + " " + title);
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setAlignment(Pos.CENTER);
        
        box.getChildren().add(titleLabel);
        return box;
    }
    
    private void startMonitoring() {
        // Update stats every 2 seconds
        updateTimer = new Timeline(new KeyFrame(Duration.seconds(2), e -> updateStats()));
        updateTimer.setCycleCount(Animation.INDEFINITE);
        updateTimer.play();
        
        // Initial update
        updateStats();
    }
    
    private void updateStats() {
        Platform.runLater(() -> {
            try {
                // Update cache stats
                String cacheStats = PerformanceOptimizer.getPerformanceStats();
                cacheStatsLabel.setText(cacheStats);
                
                // Calculate cache hit rate
                String[] lines = cacheStats.split("\n");
                for (String line : lines) {
                    if (line.contains("Hit Rate:")) {
                        String rateStr = line.split(":")[1].trim().replace("%", "");
                        try {
                            double hitRate = Double.parseDouble(rateStr) / 100.0;
                            cacheHitRateBar.setProgress(hitRate);
                        } catch (NumberFormatException e) {
                            cacheHitRateBar.setProgress(0.0);
                        }
                        break;
                    }
                }
                
                // Update HTTP stats
                String httpStats = HttpConnectionManager.getHttpStats();
                httpStatsLabel.setText(httpStats);
                
                // Calculate HTTP success rate
                String[] httpLines = httpStats.split("\n");
                for (String line : httpLines) {
                    if (line.contains("Success Rate:")) {
                        String rateStr = line.split(":")[1].trim().replace("%", "");
                        try {
                            double successRate = Double.parseDouble(rateStr) / 100.0;
                            httpSuccessRateBar.setProgress(successRate);
                        } catch (NumberFormatException e) {
                            httpSuccessRateBar.setProgress(0.0);
                        }
                        break;
                    }
                }
                
                // Update memory stats
                Runtime runtime = Runtime.getRuntime();
                long totalMemory = runtime.totalMemory();
                long freeMemory = runtime.freeMemory();
                long usedMemory = totalMemory - freeMemory;
                long maxMemory = runtime.maxMemory();
                
                double memoryUsagePercent = (double) usedMemory / maxMemory * 100;
                
                memoryStatsLabel.setText(String.format(
                    "Memory Usage:\n" +
                    "Used: %.1f MB\n" +
                    "Free: %.1f MB\n" +
                    "Total: %.1f MB\n" +
                    "Max: %.1f MB\n" +
                    "Usage: %.1f%%",
                    usedMemory / 1024.0 / 1024.0,
                    freeMemory / 1024.0 / 1024.0,
                    totalMemory / 1024.0 / 1024.0,
                    maxMemory / 1024.0 / 1024.0,
                    memoryUsagePercent
                ));
                
                // Update response time stats
                String perfStats = PerformanceOptimizer.getPerformanceStats();
                String[] perfLines = perfStats.split("\n");
                for (String line : perfLines) {
                    if (line.contains("Avg Response Time:")) {
                        responseTimeLabel.setText("Response Times:\n" + line);
                        break;
                    }
                }
                
                // Update navigation stats
                String navStats = NavigationOptimizer.getNavigationStats();
                navigationStatsLabel.setText(navStats);
                
                // Calculate navigation success rate
                String[] navLines = navStats.split("\n");
                for (String line : navLines) {
                    if (line.contains("Instant Navigations:")) {
                        String rateStr = line.split("\\(")[1].split("%")[0];
                        try {
                            double successRate = Double.parseDouble(rateStr) / 100.0;
                            navigationSuccessRateBar.setProgress(successRate);
                        } catch (NumberFormatException e) {
                            navigationSuccessRateBar.setProgress(0.0);
                        }
                        break;
                    }
                }
                
                // Update click stats
                String clickStats = ClickOptimizer.getClickStats();
                clickStatsLabel.setText(clickStats);
                
                // Update back navigation stats
                String backNavStats = BackNavigationOptimizer.getBackNavigationStats();
                backNavigationStatsLabel.setText(backNavStats);
                
                // Calculate back navigation success rate
                String[] backNavLines = backNavStats.split("\n");
                for (String line : backNavLines) {
                    if (line.contains("Instant Back Navigations:")) {
                        String rateStr = line.split("\\(")[1].split("%")[0];
                        try {
                            double successRate = Double.parseDouble(rateStr) / 100.0;
                            backNavigationSuccessRateBar.setProgress(successRate);
                        } catch (NumberFormatException e) {
                            backNavigationSuccessRateBar.setProgress(0.0);
                        }
                        break;
                    }
                }
                
            } catch (Exception e) {
                System.err.println("Error updating performance stats: " + e.getMessage());
            }
        });
    }
    
    public VBox getView() {
        return view;
    }
    
    public void stopMonitoring() {
        if (updateTimer != null) {
            updateTimer.stop();
        }
    }
} 