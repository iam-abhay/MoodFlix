package com.moodflix.controller;

import com.moodflix.service.PostgreSQLAuthService;
import com.moodflix.util.SessionManager;
import com.moodflix.view.FeedbackPage;
import com.moodflix.view.AdminDashboard;
import com.moodflix.view.UserDashboard;
import com.moodflix.controller.AdminDashboardController;
import com.moodflix.controller.UserDashboardController;
import com.moodflix.Main;
import javafx.scene.control.*;
import javafx.scene.Scene;
import javafx.scene.paint.Color;

public class FeedbackPageController {
    private final FeedbackPage view;

    public FeedbackPageController(FeedbackPage view) {
        this.view = view;
        setupEventHandlers();
    }

    private void setupEventHandlers() {
        TextArea feedbackArea = view.getFeedbackArea();
        Button submitBtn = view.getSubmitBtn();
        Button backBtn = view.getBackBtn();
        Label statusLabel = view.getStatusLabel();

        submitBtn.setOnAction(e -> {
            System.out.println("💾 Submit feedback button clicked");
            
            String feedback = feedbackArea.getText().trim();
            int rating = getCurrentRating();
            
            // Validation
            if (rating == 0) {
                statusLabel.setText("Please select a rating (1-5 stars)");
                statusLabel.setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;");
                
                com.moodflix.util.MoodflixDialog.showWarning("Rating Required", "Please select a rating from 1 to 5 stars before submitting.");
                return;
            }
            
            if (feedback.isEmpty()) {
                statusLabel.setText(" Please enter your feedback");
                statusLabel.setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;");
                
                com.moodflix.util.MoodflixDialog.showWarning("Feedback Required", "Please enter your feedback in the text area before submitting.");
                return;
            }
            
            if (feedback.length() < 10) {
                statusLabel.setText(" Please provide more detailed feedback (at least 10 characters)");
                statusLabel.setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;");
                
                com.moodflix.util.MoodflixDialog.showWarning("More Details Needed", "Please provide more detailed feedback (at least 10 characters).");
                return;
            }
            
            // Disable button during submission
            submitBtn.setDisable(true);
            statusLabel.setText("🔄 Submitting feedback...");
            
            // Perform submission in background thread
            new Thread(() -> {
                try {
                    System.out.println("🚀 Submitting feedback to database...");
                    System.out.println("📊 Rating: " + rating + " stars");
                    System.out.println("📝 Feedback length: " + feedback.length() + " characters");
                    
                    PostgreSQLAuthService service = new PostgreSQLAuthService();
                    service.saveFeedback(SessionManager.getEmail(), feedback, rating);
                    
                    javafx.application.Platform.runLater(() -> {
                        submitBtn.setDisable(false);
                        statusLabel.setText("Feedback submitted successfully!");
                        statusLabel.setStyle("-fx-text-fill: #28a745; -fx-font-weight: bold;");
                        
                        // Show success dialog
                        com.moodflix.util.MoodflixDialog.showSuccess("Thank You!", "Feedback submitted successfully! We appreciate your rating of " + rating + " stars.");
                        
                        // Clear form
                        clearForm();
                    });
                } catch (Exception ex) {
                    System.err.println("Error submitting feedback: " + ex.getMessage());
                    ex.printStackTrace();
                    
                    javafx.application.Platform.runLater(() -> {
                        submitBtn.setDisable(false);
                        statusLabel.setText("Error submitting feedback. Please try again.");
                        statusLabel.setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;");
                        
                        // Show error dialog
                        com.moodflix.util.MoodflixDialog.showError("Submission Failed", "Unable to submit your feedback: " + ex.getMessage());
                    });
                }
            }).start();
        });
        
        backBtn.setOnAction(e -> {
            System.out.println(" Back button clicked");
            String operationId = com.moodflix.util.PerformanceMonitor.startOperation("feedback_back_navigation");
            
            // Use optimized back navigation
            String userEmail = com.moodflix.util.SessionManager.getEmail();
            com.moodflix.util.BackNavigationOptimizer.smartBackNavigation(userEmail);
            
            com.moodflix.util.PerformanceMonitor.endOperation(operationId, true);
        });
    }
    
    private int getCurrentRating() {
        Button[] starButtons = view.getStarButtons();
        for (int i = 0; i < 5; i++) {
            if (starButtons[i].getStyle().contains("#ffd700")) {
                return i + 1;
            }
        }
        return 0;
    }
    
    private void clearForm() {
        // Clear feedback text
        view.getFeedbackArea().clear();
        
        // Reset rating
        Button[] starButtons = view.getStarButtons();
        for (Button star : starButtons) {
            star.setStyle("-fx-background-color: transparent; -fx-text-fill: #e0e0e0; -fx-border-color: transparent;");
        }
        
        // Reset rating label
        view.getRatingLabel().setText("Rate your experience (1-5 stars)");
        view.getRatingLabel().setStyle("-fx-text-fill: #666666;");
        
        System.out.println("Form cleared for next feedback");
    }
} 