package com.moodflix.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import com.moodflix.util.ThemeManager;

public class FeedbackPage {
    private VBox view;
    private TextArea feedbackArea;
    private HBox starRatingBox;
    private Button[] starButtons;
    private Label ratingLabel;
    private Button submitBtn;
    private Button backBtn;
    private Label statusLabel;
    private VBox mainContent;
    private HBox headerSection;
    private VBox feedbackSection;
    private HBox actionButtons;
    private ScrollPane scrollPane;
    private int currentRating = 0;

    public FeedbackPage() {
        createView();
    }

    private void createView() {
        view = new VBox(28);
        view.setAlignment(Pos.TOP_CENTER);
        view.setPadding(new Insets(40));
        view.getStyleClass().add("auth-container");

        createHeaderSection();
        createMainContentSection();
        createActionButtons();

        view.getChildren().addAll(headerSection, mainContent);
        scrollPane = new ScrollPane(view);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        ThemeManager.fadeIn(view, 400);
    }

    private void createHeaderSection() {
        headerSection = new HBox(20);
        headerSection.setAlignment(Pos.CENTER);
        headerSection.setPadding(new Insets(24));
        headerSection.getStyleClass().add("glass-card");

        Label headerIcon = new Label("💬");
        headerIcon.setStyle("-fx-font-size: 48px;");

        VBox headerText = new VBox(6);
        Label title = new Label("Share Your Thoughts");
        title.getStyleClass().add("hero-title");
        title.setStyle("-fx-font-size: 28px;");

        Label subtitle = new Label("We'd love to hear your feedback about Moodflix!");
        subtitle.getStyleClass().add("label-secondary");

        headerText.getChildren().addAll(title, subtitle);
        headerSection.getChildren().addAll(headerIcon, headerText);

        ThemeManager.slideUp(headerSection, 500, 100);
    }

    private void createMainContentSection() {
        mainContent = new VBox(28);
        mainContent.setAlignment(Pos.CENTER);
        mainContent.setPadding(new Insets(32));
        mainContent.getStyleClass().add("glass-card");

        createRatingSection();
        createFeedbackSection();

        statusLabel = new Label();
        statusLabel.getStyleClass().add("label-secondary");
        statusLabel.setAlignment(Pos.CENTER);
        statusLabel.setMaxWidth(Double.MAX_VALUE);
        mainContent.getChildren().add(statusLabel);

        ThemeManager.slideUp(mainContent, 500, 200);
    }

    private void createRatingSection() {
        VBox ratingSection = new VBox(14);
        ratingSection.setAlignment(Pos.CENTER);
        ratingSection.setPadding(new Insets(20));
        ratingSection.getStyleClass().add("card");

        Label ratingTitle = new Label("⭐ Rate Your Experience");
        ratingTitle.getStyleClass().add("section-title");

        starRatingBox = new HBox(10);
        starRatingBox.setAlignment(Pos.CENTER);
        starButtons = new Button[5];

        for (int i = 0; i < 5; i++) {
            final int starIndex = i;
            starButtons[i] = new Button("⭐");
            starButtons[i].getStyleClass().add("star-btn");
            starButtons[i].setPrefSize(70, 70);

            starButtons[i].setOnMouseEntered(e -> {
                for (int j = 0; j <= starIndex; j++) {
                    starButtons[j].getStyleClass().removeAll("star-btn");
                    if (!starButtons[j].getStyleClass().contains("star-btn-active"))
                        starButtons[j].getStyleClass().add("star-btn-active");
                }
                for (int j = starIndex + 1; j < 5; j++) {
                    starButtons[j].getStyleClass().removeAll("star-btn-active");
                    if (!starButtons[j].getStyleClass().contains("star-btn"))
                        starButtons[j].getStyleClass().add("star-btn");
                }
            });

            starButtons[i].setOnMouseExited(e -> updateStarDisplay(currentRating));

            starButtons[i].setOnAction(e -> {
                setRating(starIndex + 1);
            });

            starRatingBox.getChildren().add(starButtons[i]);
        }

        ratingLabel = new Label("Rate your experience (1-5 stars)");
        ratingLabel.getStyleClass().add("label-muted");

        ratingSection.getChildren().addAll(ratingTitle, starRatingBox, ratingLabel);
        mainContent.getChildren().add(ratingSection);
    }

    private void createFeedbackSection() {
        feedbackSection = new VBox(14);
        feedbackSection.setAlignment(Pos.CENTER);
        feedbackSection.setPadding(new Insets(20));
        feedbackSection.getStyleClass().add("card");

        Label feedbackTitle = new Label("📝 Tell Us More");
        feedbackTitle.getStyleClass().add("section-title");

        feedbackArea = new TextArea();
        feedbackArea.setPromptText("Share your thoughts, suggestions, or any issues you've encountered...\n\n" +
                                 "• What did you like about Moodflix?\n" +
                                 "• What could be improved?\n" +
                                 "• Any features you'd like to see?\n" +
                                 "• Any bugs or issues you found?");
        feedbackArea.setPrefRowCount(8);
        feedbackArea.setPrefWidth(500);

        Label charCounter = new Label("0 characters");
        charCounter.getStyleClass().add("label-muted");

        feedbackArea.textProperty().addListener((obs, oldVal, newVal) -> {
            charCounter.setText(newVal.length() + " characters");
            charCounter.getStyleClass().removeAll("label-muted", "label-accent", "badge-danger");
            if (newVal.length() > 1000) {
                charCounter.getStyleClass().add("badge-danger");
            } else if (newVal.length() > 500) {
                charCounter.getStyleClass().add("label-accent");
            } else {
                charCounter.getStyleClass().add("label-muted");
            }
        });

        feedbackSection.getChildren().addAll(feedbackTitle, feedbackArea, charCounter);
        mainContent.getChildren().add(feedbackSection);
    }

    private void createActionButtons() {
        actionButtons = new HBox(16);
        actionButtons.setAlignment(Pos.CENTER);

        submitBtn = new Button("💾 Submit Feedback");
        submitBtn.getStyleClass().addAll("btn", "btn-success");
        submitBtn.setStyle("-fx-min-width: 160;");

        backBtn = new Button("← Back to Dashboard");
        backBtn.getStyleClass().addAll("btn", "btn-outline");
        backBtn.setStyle("-fx-min-width: 160;");

        actionButtons.getChildren().addAll(submitBtn, backBtn);
        mainContent.getChildren().add(actionButtons);
    }

    private void setRating(int rating) {
        currentRating = rating;
        updateStarDisplay(rating);
        String[] ratingTexts = {
            "Poor - We need to improve",
            "Fair - Room for improvement",
            "Good - Satisfactory experience",
            "Very Good - Great experience",
            "Excellent - Outstanding experience"
        };
        ratingLabel.setText(ratingTexts[rating - 1]);
        ratingLabel.getStyleClass().removeAll("label-muted", "label-accent", "badge-success");
        ratingLabel.getStyleClass().add("badge-success");
    }

    private void updateStarDisplay(int rating) {
        for (int i = 0; i < 5; i++) {
            starButtons[i].getStyleClass().removeAll("star-btn", "star-btn-active");
            if (i < rating) {
                starButtons[i].getStyleClass().add("star-btn-active");
            } else {
                starButtons[i].getStyleClass().add("star-btn");
            }
        }
    }

    private int getCurrentRating() {
        return currentRating;
    }

    // Getters
    public ScrollPane getView() { return scrollPane; }
    public TextArea getFeedbackArea() { return feedbackArea; }
    public Button getSubmitBtn() { return submitBtn; }
    public Button getBackBtn() { return backBtn; }
    public Label getStatusLabel() { return statusLabel; }
    public Button[] getStarButtons() { return starButtons; }
    public Label getRatingLabel() { return ratingLabel; }
} 