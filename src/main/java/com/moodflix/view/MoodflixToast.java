package com.moodflix.view;

import com.moodflix.util.ThemeManager;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.stage.Popup;
import javafx.stage.Window;
import javafx.util.Duration;

/**
 * Modern non-blocking toast notification system.
 * Positions itself at the bottom right of the window and overlays other UI components.
 */
public class MoodflixToast {

    public enum ToastType {
        SUCCESS, ERROR, WARNING, INFO
    }

    /**
     * Show a toast message on the given scene.
     */
    public static void show(Scene scene, String message, ToastType type) {
        if (scene == null) return;
        Window window = scene.getWindow();
        if (window == null) return;

        Popup popup = new Popup();
        
        HBox container = new HBox(10);
        container.setAlignment(Pos.CENTER_LEFT);
        container.getStyleClass().add("toast-container");
        
        String icon = "ℹ️";
        switch (type) {
            case SUCCESS:
                container.getStyleClass().add("toast-success");
                icon = "✅";
                break;
            case ERROR:
                container.getStyleClass().add("toast-error");
                icon = "❌";
                break;
            case WARNING:
                container.getStyleClass().add("toast-warning");
                icon = "⚠️";
                break;
            case INFO:
                container.getStyleClass().add("toast-info");
                icon = "ℹ️";
                break;
        }

        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 16px;");
        Label textLabel = new Label(message);
        textLabel.getStyleClass().add("toast-text");
        textLabel.setWrapText(true);
        textLabel.setMaxWidth(300);

        container.getChildren().addAll(iconLabel, textLabel);
        
        // Inherit scene stylesheets to resolve CSS theme variables
        container.getStylesheets().addAll(scene.getStylesheets());
        
        // Inherit theme-specific styling
        if (scene.getRoot() != null) {
            if (scene.getRoot().getStyleClass().contains("theme-light")) {
                container.getStyleClass().add("theme-light");
            } else {
                container.getStyleClass().add("theme-dark");
            }
        }
        
        popup.getContent().add(container);
        
        // Position toast at bottom right of the parent stage/window
        popup.setOnShown(e -> {
            popup.setX(window.getX() + window.getWidth() - container.getWidth() - 40);
            popup.setY(window.getY() + window.getHeight() - container.getHeight() - 60);
        });

        popup.show(window);

        // Handle animation fade sequence
        if (ThemeManager.isAnimationsEnabled()) {
            container.setOpacity(0.0);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), container);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);

            PauseTransition pause = new PauseTransition(Duration.seconds(3.0));

            FadeTransition fadeOut = new FadeTransition(Duration.millis(400), container);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(event -> popup.hide());

            SequentialTransition seq = new SequentialTransition(fadeIn, pause, fadeOut);
            seq.play();
        } else {
            container.setOpacity(1.0);
            PauseTransition pause = new PauseTransition(Duration.seconds(3.0));
            pause.setOnFinished(event -> popup.hide());
            pause.play();
        }
    }
}
