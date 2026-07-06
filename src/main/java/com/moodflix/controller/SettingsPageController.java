package com.moodflix.controller;

import com.moodflix.util.ThemeManager;
import com.moodflix.util.SessionManager;
import com.moodflix.util.BackNavigationOptimizer;
import com.moodflix.database.DatabaseConfig;
import com.moodflix.config.AppConfig;
import com.moodflix.view.SettingsPage;
import com.moodflix.view.MoodflixToast;
import javafx.application.Platform;
import javafx.scene.Scene;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Controller for SettingsPage.
 * Synchronizes choices with ThemeManager and performs diagnostics query on background threads.
 */
public class SettingsPageController {
    private final SettingsPage view;

    public SettingsPageController(SettingsPage view) {
        this.view = view;
        initSettingsState();
        setupActions();
        refreshDiagnostics();
    }

    private void initSettingsState() {
        // Theme State
        String currentTheme = ThemeManager.getTheme();
        if ("light".equalsIgnoreCase(currentTheme)) {
            view.getThemeCombo().setValue("Light Theme");
        } else {
            view.getThemeCombo().setValue("Dark Theme");
        }

        // Animations State
        view.getAnimCheckBox().setSelected(ThemeManager.isAnimationsEnabled());

        // Language default
        view.getLangCombo().setValue("English");
    }

    private void setupActions() {
        // Theme selection action
        view.getThemeCombo().valueProperty().addListener((obs, oldVal, newVal) -> {
            Scene scene = view.getView().getScene();
            if (scene != null) {
                if ("Light Theme".equals(newVal)) {
                    ThemeManager.setTheme(scene, "light");
                    MoodflixToast.show(scene, "Switched to Light Theme", MoodflixToast.ToastType.SUCCESS);
                } else {
                    ThemeManager.setTheme(scene, "dark");
                    MoodflixToast.show(scene, "Switched to Dark Theme (Cinematic)", MoodflixToast.ToastType.SUCCESS);
                }
            }
        });

        // Animations enabled action
        view.getAnimCheckBox().selectedProperty().addListener((obs, oldVal, newVal) -> {
            ThemeManager.setAnimationsEnabled(newVal);
            Scene scene = view.getView().getScene();
            if (scene != null) {
                if (newVal) {
                    MoodflixToast.show(scene, "Animations Enabled", MoodflixToast.ToastType.SUCCESS);
                } else {
                    MoodflixToast.show(scene, "Animations Disabled (Performance Mode)", MoodflixToast.ToastType.SUCCESS);
                }
            }
        });

        // Language change action
        view.getLangCombo().valueProperty().addListener((obs, oldVal, newVal) -> {
            Scene scene = view.getView().getScene();
            if (scene != null) {
                MoodflixToast.show(scene, "Language set to " + newVal, MoodflixToast.ToastType.INFO);
            }
        });

        // DB check button action
        view.getCheckDbBtn().setOnAction(e -> {
            view.getDbStatusVal().setText("Checking...");
            view.getDbStatusVal().getStyleClass().setAll("badge-info");
            refreshDbStatus();
        });

        // API check button action
        view.getCheckApiBtn().setOnAction(e -> {
            view.getApiStatusVal().setText("Testing...");
            view.getApiStatusVal().getStyleClass().setAll("badge-info");
            refreshApiStatus();
        });

        // Navigate back
        view.getBackBtn().setOnAction(e -> {
            String email = SessionManager.getEmail();
            if (email == null || email.isEmpty()) {
                email = "user@moodflix.com";
            }
            BackNavigationOptimizer.smartBackNavigation(email);
        });
    }

    private void refreshDiagnostics() {
        refreshDbStatus();
        refreshApiStatus();
        
        // Memory calculation
        long freeMem = Runtime.getRuntime().freeMemory();
        long totalMem = Runtime.getRuntime().totalMemory();
        long maxMem = Runtime.getRuntime().maxMemory();
        long usedMem = totalMem - freeMem;
        String stats = String.format("Used: %d MB / Total: %d MB (Max: %d MB)", 
            usedMem / (1024 * 1024), totalMem / (1024 * 1024), maxMem / (1024 * 1024));
        view.getMemoryVal().setText(stats);
    }

    private void refreshDbStatus() {
        new Thread(() -> {
            boolean connected = false;
            try {
                connected = DatabaseConfig.isConfigured();
            } catch (Exception ex) {
                connected = false;
            }
            final boolean isConnected = connected;
            Platform.runLater(() -> {
                if (isConnected) {
                    view.getDbStatusVal().setText("Connected");
                    view.getDbStatusVal().getStyleClass().setAll("badge-success");
                    view.getDbPoolVal().setText("Pool Size: 20 | Min Idle: 5");
                } else {
                    view.getDbStatusVal().setText("Disconnected");
                    view.getDbStatusVal().getStyleClass().setAll("badge-danger");
                    view.getDbPoolVal().setText("Connections: 0");
                }
            });
        }).start();
    }

    private void refreshApiStatus() {
        new Thread(() -> {
            boolean ok = false;
            try {
                String apiKey = AppConfig.getOmdbApiKey();
                String testUrl = "https://www.omdbapi.com/";
                if (apiKey != null && !apiKey.isEmpty()) {
                    testUrl += "?apikey=" + apiKey + "&t=inception";
                }
                URL url = new URL(testUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(3000);
                int code = conn.getResponseCode();
                ok = (code == 200 || code == 401 || code == 403);
            } catch (Exception ex) {
                ok = false;
            }
            final boolean isOk = ok;
            Platform.runLater(() -> {
                if (isOk) {
                    view.getApiStatusVal().setText("Active");
                    view.getApiStatusVal().getStyleClass().setAll("badge-success");
                } else {
                    view.getApiStatusVal().setText("Inactive / No Key");
                    view.getApiStatusVal().getStyleClass().setAll("badge-danger");
                }
            });
        }).start();
    }
}
