package com.moodflix.view;

import com.moodflix.util.ThemeManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class LandingPage {
    private final StackPane view;
    private final Button loginButton;
    private final Button signupButton;

    public LandingPage() {
        view = new StackPane();
        view.getStyleClass().add("auth-container");

        VBox heroCard = new VBox(18);
        heroCard.setAlignment(Pos.CENTER_LEFT);
        heroCard.setPadding(new Insets(40));
        heroCard.getStyleClass().add("glass-card");
        heroCard.setMaxWidth(760);

        Label logo = new Label("MoodFlix");
        logo.getStyleClass().add("auth-logo");

        Label title = new Label("Discover Movies, Series, And Music\nThat Match How You Feel");
        title.getStyleClass().add("hero-title");

        Label subtitle = new Label(
            "Start with a quick mood check, get dynamic recommendations, and manage your watchlist like a real streaming app."
        );
        subtitle.getStyleClass().add("hero-subtitle");
        subtitle.setWrapText(true);

        HBox ctaRow = new HBox(14);
        ctaRow.setAlignment(Pos.CENTER_LEFT);
        loginButton = new Button("Login");
        loginButton.getStyleClass().addAll("btn", "btn-success");
        signupButton = new Button("Create Account");
        signupButton.getStyleClass().addAll("btn", "btn-outline");
        ctaRow.getChildren().addAll(loginButton, signupButton);

        HBox statRow = new HBox(12);
        statRow.setAlignment(Pos.CENTER_LEFT);
        statRow.getChildren().addAll(
            createStat("10K+", "Titles"),
            createStat("95%", "Match Accuracy"),
            createStat("24/7", "Mood Support")
        );

        heroCard.getChildren().addAll(logo, title, subtitle, ctaRow, statRow);

        view.getChildren().add(heroCard);
        StackPane.setAlignment(heroCard, Pos.CENTER);
        StackPane.setMargin(heroCard, new Insets(32));

        view.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                ThemeManager.slideUp(heroCard, 550, 80);
            }
        });
    }

    private VBox createStat(String value, String label) {
        VBox stat = new VBox(2);
        stat.getStyleClass().add("stat-card");
        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("stat-value");
        Label textLabel = new Label(label);
        textLabel.getStyleClass().add("stat-label");
        stat.getChildren().addAll(valueLabel, textLabel);
        return stat;
    }

    public StackPane getView() {
        return view;
    }

    public Button getLoginButton() {
        return loginButton;
    }

    public Button getSignupButton() {
        return signupButton;
    }
}

