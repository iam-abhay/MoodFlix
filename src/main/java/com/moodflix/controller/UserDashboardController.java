package com.moodflix.controller;

import com.moodflix.model.Content;
import com.moodflix.service.PostgreSQLContentService;
import com.moodflix.service.PostgreSQLWatchlistService;
import com.moodflix.service.PostgreSQLAuthService;
import com.moodflix.util.SessionManager;
import com.moodflix.view.UserDashboard;
import com.moodflix.view.MoodflixToast;
import com.moodflix.util.MoodflixDialog;
import com.moodflix.Main;

import javafx.application.Platform;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Hyperlink;
import javafx.scene.layout.HBox;
import javafx.util.Duration;

import java.util.List;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Controller for UserDashboard.
 * Manages live debounced search, sidebar navigation routes, dynamic recommendation engines,
 * and toast alert notifications.
 */
public class UserDashboardController {
    private final UserDashboard view;
    private Timeline searchDebounceTimeline;
    private final Set<String> recentSearches = new LinkedHashSet<>();

    public UserDashboardController(UserDashboard view) {
        this.view = view;
        loadUserProfilePhoto();
        setupEventHandlers();
        setupSearchHandler();
        setupKeyboardShortcuts();
    }

    private void loadUserProfilePhoto() {
        String userEmail = SessionManager.getEmail();
        if (userEmail == null) return;
        javafx.scene.image.ImageView profilePhotoView = view.getProfilePhotoView();
        
        String operationId = com.moodflix.util.PerformanceMonitor.startOperation("profile_photo_load");
        
        PostgreSQLAuthService service = new PostgreSQLAuthService();
        service.getUserDetailsAsync(userEmail)
            .thenAcceptAsync(userObj -> {
                com.moodflix.util.PerformanceMonitor.endOperation(operationId, userObj != null);
                
                if (userObj != null && userObj.has("profilePicUrl") && !userObj.getString("profilePicUrl").isEmpty()) {
                    String photoUrl = userObj.getString("profilePicUrl");
                    javafx.scene.image.Image profileImage = new javafx.scene.image.Image(photoUrl);
                    Platform.runLater(() -> {
                        if (profilePhotoView != null && !profileImage.isError()) {
                            profilePhotoView.setImage(profileImage);
                        }
                    });
                }
            }).exceptionally(throwable -> {
                System.err.println("Error loading profile photo: " + throwable.getMessage());
                return null;
            });
    }

    private void setupEventHandlers() {
        Button moodRecBtn = view.getMoodRecBtn();
        Button generalRecBtn = view.getGeneralRecBtn();
        ComboBox<String> moodCombo = view.getMoodCombo();
        ComboBox<String> typeCombo = view.getTypeCombo();
        
        Button profileBtn = view.getProfileBtn();
        Button watchlistBtn = view.getWatchlistBtn();
        Button feedbackBtn = view.getFeedbackBtn();
        Button activityBtn = view.getActivityBtn();
        Button settingsBtn = view.getSettingsBtn();
        Button logoutBtn = view.getLogoutBtn();

        Button recommendedBtn = view.getRecommendedBtn();
        if (recommendedBtn != null) {
            recommendedBtn.setOnAction(e -> {
                String mood = moodCombo.getValue();
                String type = typeCombo.getValue();
                if (mood != null && type != null) {
                    view.showRecommendationsLoading();
                    String opId = com.moodflix.util.PerformanceMonitor.startOperation("smart_recommendations");
                    
                    com.moodflix.util.PerformanceOptimizer.runAsync(() -> {
                        // Advanced scored recommendation engine
                        return com.moodflix.service.RecommendationService.getScoredRecommendations(SessionManager.getEmail(), mood, type);
                    }).thenAcceptAsync(results -> {
                        com.moodflix.util.PerformanceMonitor.endOperation(opId, true);
                        Platform.runLater(() -> view.showRecommendationsGrid(results));
                    }).exceptionally(throwable -> {
                        com.moodflix.util.PerformanceMonitor.endOperation(opId, false);
                        Platform.runLater(() -> view.showRecommendationsError(throwable.getMessage()));
                        return null;
                    });
                }
            });
        }

        if (moodRecBtn != null) {
            moodRecBtn.setOnAction(e -> {
                String mood = moodCombo.getValue();
                if (mood == null || mood.isEmpty()) {
                    view.showRecommendationsError("Please select an emotion.");
                    return;
                }
                view.showRecommendationsLoading();
                
                String operationId = com.moodflix.util.PerformanceMonitor.startOperation("mood_recommendations");
                
                com.moodflix.util.PerformanceOptimizer.runAsync(() -> {
                    try {
                        PostgreSQLContentService service = new PostgreSQLContentService();
                        return service.getFilteredContentList(mood, null);
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                }).thenAcceptAsync(results -> {
                    com.moodflix.util.PerformanceMonitor.endOperation(operationId, true);
                    Platform.runLater(() -> view.showRecommendationsGrid(results));
                }).exceptionally(throwable -> {
                    com.moodflix.util.PerformanceMonitor.endOperation(operationId, false);
                    Platform.runLater(() -> view.showRecommendationsError(throwable.getMessage()));
                    return null;
                });
            });
        }

        if (generalRecBtn != null) {
            generalRecBtn.setOnAction(e -> {
                String type = typeCombo.getValue();
                if (type == null || type.isEmpty()) {
                    view.showRecommendationsError("Please select a content type.");
                    return;
                }
                view.showRecommendationsLoading();
                
                String operationId = com.moodflix.util.PerformanceMonitor.startOperation("type_recommendations");
                
                com.moodflix.util.PerformanceOptimizer.runAsync(() -> {
                    try {
                        PostgreSQLContentService service = new PostgreSQLContentService();
                        return service.getFilteredContentList(null, type);
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                }).thenAcceptAsync(results -> {
                    com.moodflix.util.PerformanceMonitor.endOperation(operationId, true);
                    Platform.runLater(() -> view.showRecommendationsGrid(results));
                }).exceptionally(throwable -> {
                    com.moodflix.util.PerformanceMonitor.endOperation(operationId, false);
                    Platform.runLater(() -> view.showRecommendationsError(throwable.getMessage()));
                    return null;
                });
            });
        }

        // Sidebar click actions
        if (profileBtn != null) {
            com.moodflix.util.ClickOptimizer.setNavigationClickHandler(profileBtn, "ProfilePage", () -> {
                String userEmail = SessionManager.getEmail();
                com.moodflix.view.ProfilePage profileView = new com.moodflix.view.ProfilePage(userEmail);
                new com.moodflix.controller.ProfilePageController(profileView, userEmail);
                return new Scene(profileView.getView());
            });
        }

        if (watchlistBtn != null) {
            com.moodflix.util.ClickOptimizer.setNavigationClickHandler(watchlistBtn, "WatchlistPage", () -> {
                com.moodflix.view.WatchlistPage watchlistView = new com.moodflix.view.WatchlistPage();
                new com.moodflix.controller.WatchlistPageController(watchlistView);
                return new Scene(watchlistView.getView());
            });
        }

        if (feedbackBtn != null) {
            com.moodflix.util.ClickOptimizer.setNavigationClickHandler(feedbackBtn, "FeedbackPage", () -> {
                com.moodflix.view.FeedbackPage feedbackView = new com.moodflix.view.FeedbackPage();
                new com.moodflix.controller.FeedbackPageController(feedbackView);
                return new Scene(feedbackView.getView());
            });
        }

        if (activityBtn != null) {
            com.moodflix.util.ClickOptimizer.setNavigationClickHandler(activityBtn, "ActivityHistoryPage", () -> {
                com.moodflix.view.ActivityHistoryPage activityView = new com.moodflix.view.ActivityHistoryPage();
                new com.moodflix.controller.ActivityHistoryController(activityView);
                return new Scene(activityView.getView());
            });
        }

        if (settingsBtn != null) {
            com.moodflix.util.ClickOptimizer.setNavigationClickHandler(settingsBtn, "SettingsPage", () -> {
                com.moodflix.view.SettingsPage settingsView = new com.moodflix.view.SettingsPage();
                new com.moodflix.controller.SettingsPageController(settingsView);
                return new Scene(settingsView.getView());
            });
        }

        if (logoutBtn != null) {
            logoutBtn.setOnAction(e -> {
                boolean confirm = MoodflixDialog.showConfirmation("Logout Confirmation", "Are you sure you want to logout of MoodFlix?");
                if (confirm) {
                    com.moodflix.util.LogoutManager.performLogoutWithStage(logoutBtn);
                }
            });
        }
    }

    private void setupSearchHandler() {
        TextField searchField = view.getSearchField();
        if (searchField == null) return;
        
        searchField.textProperty().addListener((obs, oldText, newText) -> {
            if (searchDebounceTimeline != null) {
                searchDebounceTimeline.stop();
            }
            
            searchDebounceTimeline = new Timeline(new KeyFrame(Duration.millis(300), event -> {
                performSearch(newText);
            }));
            searchDebounceTimeline.play();
        });
    }

    private void performSearch(String query) {
        if (query == null || query.trim().isEmpty()) {
            Platform.runLater(() -> view.showRecommendationsGrid(null));
            return;
        }
        
        view.showRecommendationsLoading();
        
        new Thread(() -> {
            try {
                PostgreSQLContentService service = new PostgreSQLContentService();
                List<Content> searchResults = service.searchContent(query.trim());
                Platform.runLater(() -> {
                    view.showRecommendationsGrid(searchResults);
                    addRecentSearch(query.trim());
                });
            } catch (Exception ex) {
                Platform.runLater(() -> view.showRecommendationsError("Search error: " + ex.getMessage()));
            }
        }).start();
    }

    private void addRecentSearch(String query) {
        if (query == null || query.trim().isEmpty() || query.length() < 3) return;
        recentSearches.remove(query);
        recentSearches.add(query);
        
        if (recentSearches.size() > 5) {
            java.util.Iterator<String> it = recentSearches.iterator();
            if (it.hasNext()) {
                it.next();
                it.remove();
            }
        }
        
        updateRecentSearchesUI();
    }

    private void updateRecentSearchesUI() {
        HBox box = view.getRecentSearchesBox();
        if (box == null) return;
        
        Platform.runLater(() -> {
            while (box.getChildren().size() > 1) {
                box.getChildren().remove(1);
            }
            
            for (String search : recentSearches) {
                Hyperlink searchLink = new Hyperlink(search);
                searchLink.getStyleClass().add("badge-accent");
                searchLink.setStyle("-fx-font-size: 11px; -fx-padding: 2 8; -fx-cursor: hand;");
                searchLink.setOnAction(e -> {
                    view.getSearchField().setText(search);
                    performSearch(search);
                });
                box.getChildren().add(searchLink);
            }
        });
    }

    private void setupKeyboardShortcuts() {
        javafx.scene.Parent root = view.getView();
        if (root == null) return;
        
        root.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.setOnKeyPressed(event -> {
                    if (event.isControlDown()) {
                        switch (event.getCode()) {
                            case S:
                                if (view.getSearchField() != null) {
                                    view.getSearchField().requestFocus();
                                    event.consume();
                                }
                                break;
                            case P:
                                if (view.getProfileBtn() != null) {
                                    view.getProfileBtn().fire();
                                    event.consume();
                                }
                                break;
                            case W:
                                if (view.getWatchlistBtn() != null) {
                                    view.getWatchlistBtn().fire();
                                    event.consume();
                                }
                                break;
                            case L:
                                if (view.getLogoutBtn() != null) {
                                    view.getLogoutBtn().fire();
                                    event.consume();
                                }
                                break;
                            case H:
                                com.moodflix.util.BackNavigationOptimizer.smartBackNavigation(SessionManager.getEmail());
                                event.consume();
                                break;
                            default:
                                break;
                        }
                    }
                });
            }
        });
    }
}