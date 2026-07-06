package com.moodflix.util;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Enterprise Reusable Component Factory for MoodFlix.
 * Prevents code duplication by centralizing UI control instantiation and styling definitions,
 * guaranteeing adherence to SOLID design principles and unified themes.
 */
public final class ComponentFactory {

    private ComponentFactory() {
        // Factory class
    }

    /**
     * Create standard styled button
     */
    public static Button createButton(String text, String extraStyleClass, double width) {
        Button btn = new Button(text);
        btn.getStyleClass().add("btn");
        if (extraStyleClass != null && !extraStyleClass.isEmpty()) {
            btn.getStyleClass().add(extraStyleClass);
        }
        if (width > 0) {
            btn.setPrefWidth(width);
            btn.setMinWidth(width);
        }
        btn.setCursor(javafx.scene.Cursor.HAND);
        return btn;
    }

    /**
     * Create a standard glassmorphic card container
     */
    public static VBox createGlassCard(double spacing, double paddingVal) {
        VBox card = new VBox(spacing);
        card.getStyleClass().add("glass-card");
        card.setPadding(new Insets(paddingVal));
        return card;
    }

    /**
     * Create standard input text field
     */
    public static TextField createTextField(String promptText, double width) {
        TextField tf = new TextField();
        tf.setPromptText(promptText);
        tf.getStyleClass().add("text-field");
        if (width > 0) {
            tf.setMaxWidth(width);
            tf.setPrefWidth(width);
        }
        return tf;
    }

    /**
     * Create standard password input field
     */
    public static PasswordField createPasswordField(String promptText, double width) {
        PasswordField pf = new PasswordField();
        pf.setPromptText(promptText);
        pf.getStyleClass().add("password-field");
        if (width > 0) {
            pf.setMaxWidth(width);
            pf.setPrefWidth(width);
        }
        return pf;
    }

    /**
     * Create standard ComboBox
     */
    public static <T> ComboBox<T> createComboBox(double width) {
        ComboBox<T> cb = new ComboBox<>();
        cb.getStyleClass().add("combo-box");
        if (width > 0) {
            cb.setPrefWidth(width);
            cb.setMinWidth(width);
        }
        return cb;
    }

    /**
     * Create standard loading indicator spinner
     */
    public static VBox createLoadingSpinner(String loadingMessage) {
        VBox box = new VBox(14);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(30));

        ProgressIndicator spinner = new ProgressIndicator();
        spinner.setMaxSize(48, 48);
        spinner.setStyle("-fx-progress-color: -mf-accent;");

        Label label = new Label(loadingMessage != null ? loadingMessage : "Loading...");
        label.getStyleClass().add("label-secondary");
        label.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        box.getChildren().addAll(spinner, label);
        return box;
    }
}
