package com.moodflix.util;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Premium custom dialog replacements for JavaFX Alert.
 * Supports theme variables, animations, and confirmation boxes.
 */
public class MoodflixDialog {
    
    private static boolean confirmationResult = false;

    public static void showInfo(String title, String message) {
        showDialog(title, message, "label-accent", "🎬");
    }
    
    public static void showSuccess(String title, String message) {
        showDialog(title, message, "badge-success", "✅");
    }
    
    public static void showError(String title, String message) {
        showDialog(title, message, "badge-danger", "❌");
    }

    public static void showWarning(String title, String message) {
        showDialog(title, message, "badge-info", "⚠️");
    }
    
    private static void showDialog(String title, String message, String styleClass, String icon) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.TRANSPARENT);
        dialog.setTitle(title);

        VBox root = new VBox(20);
        root.setPadding(new Insets(28, 32, 28, 32));
        root.setAlignment(Pos.CENTER);
        root.getStyleClass().addAll("card", "glass-card");
        root.setStyle("-fx-max-width: 420; -fx-border-width: 1.5;");

        HBox iconBar = new HBox(12);
        iconBar.setAlignment(Pos.CENTER);
        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 38px;");
        iconLabel.getStyleClass().add(styleClass);
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        iconBar.getChildren().addAll(iconLabel, titleLabel);

        Label msgLabel = new Label(message);
        msgLabel.setStyle("-fx-font-size: 15px;");
        msgLabel.getStyleClass().add("label-secondary");
        msgLabel.setWrapText(true);
        msgLabel.setAlignment(Pos.CENTER);

        Button closeBtn = new Button("OK");
        closeBtn.getStyleClass().addAll("btn", "btn-outline");
        closeBtn.setOnAction(e -> dialog.close());
        closeBtn.setDefaultButton(true);

        root.getChildren().addAll(iconBar, msgLabel, closeBtn);
        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        ThemeManager.applyTheme(scene);
        
        dialog.setScene(scene);
        dialog.sizeToScene();
        
        if (ThemeManager.isAnimationsEnabled()) {
            ThemeManager.fadeIn(root, 300);
        }
        
        dialog.showAndWait();
    }

    /**
     * Show a custom animated confirmation dialog with Confirm / Cancel options.
     * Returns true if user clicks Confirm, false otherwise.
     */
    public static boolean showConfirmation(String title, String message) {
        confirmationResult = false;
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.TRANSPARENT);
        dialog.setTitle(title);

        VBox root = new VBox(20);
        root.setPadding(new Insets(28, 32, 28, 32));
        root.setAlignment(Pos.CENTER);
        root.getStyleClass().addAll("card", "glass-card");
        root.setStyle("-fx-max-width: 420; -fx-border-width: 1.5;");

        HBox iconBar = new HBox(12);
        iconBar.setAlignment(Pos.CENTER);
        Label iconLabel = new Label("❓");
        iconLabel.setStyle("-fx-font-size: 38px;");
        iconLabel.getStyleClass().add("label-accent");
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        iconBar.getChildren().addAll(iconLabel, titleLabel);

        Label msgLabel = new Label(message);
        msgLabel.setStyle("-fx-font-size: 15px;");
        msgLabel.getStyleClass().add("label-secondary");
        msgLabel.setWrapText(true);
        msgLabel.setAlignment(Pos.CENTER);

        Button yesBtn = new Button("Confirm");
        yesBtn.getStyleClass().addAll("btn");
        yesBtn.setOnAction(e -> {
            confirmationResult = true;
            dialog.close();
        });

        Button noBtn = new Button("Cancel");
        noBtn.getStyleClass().addAll("btn", "btn-outline");
        noBtn.setOnAction(e -> {
            confirmationResult = false;
            dialog.close();
        });

        HBox btnBox = new HBox(16, yesBtn, noBtn);
        btnBox.setAlignment(Pos.CENTER);

        root.getChildren().addAll(iconBar, msgLabel, btnBox);
        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        ThemeManager.applyTheme(scene);
        
        dialog.setScene(scene);
        dialog.sizeToScene();
        
        if (ThemeManager.isAnimationsEnabled()) {
            ThemeManager.fadeIn(root, 300);
        }
        
        dialog.showAndWait();
        return confirmationResult;
    }
}