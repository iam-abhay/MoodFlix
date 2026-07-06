package com.moodflix.controller;

import com.moodflix.Main;
import com.moodflix.service.PostgreSQLAuthService;
import com.moodflix.util.SessionManager;
import com.moodflix.view.AdminUserManagementPage;
import com.moodflix.view.AdminDashboard;
import com.moodflix.view.ProfilePage;
import com.moodflix.view.WatchlistPage;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.application.Platform;
import org.json.JSONObject;
import org.json.JSONArray;
import java.util.List;
import java.util.ArrayList;
import javafx.scene.layout.VBox;

public class AdminUserManagementController {
    private final AdminUserManagementPage view;
    private final PostgreSQLAuthService authService = new PostgreSQLAuthService();
    private ObservableList<AdminUserManagementPage.UserData> userDataList;

    public AdminUserManagementController(AdminUserManagementPage view) {
        this.view = view;
        this.userDataList = FXCollections.observableArrayList();
        setupEventHandlers();
        setupTableButtonHandlers();
        loadAllUsers();
    }

    private void setupEventHandlers() {
        // Refresh button
        view.getRefreshBtn().setOnAction(e -> loadAllUsers());

        // Search functionality
        view.getSearchField().textProperty().addListener((observable, oldValue, newValue) -> {
            filterUsers();
        });

        // Filter functionality
        view.getFilterBox().setOnAction(e -> {
            filterUsers();
        });

        // View user details button
        view.getViewUserDetailsBtn().setOnAction(e -> {
            System.out.println("👤 View User Details button clicked!");
            AdminUserManagementPage.UserData selectedUser = view.getUsersTable().getSelectionModel().getSelectedItem();
            if (selectedUser != null) {
                System.out.println("👤 Selected user: " + selectedUser.getEmail());
                viewUserDetails(selectedUser);
            } else {
                System.out.println("⚠️ No user selected!");
                showAlert("Please select a user to view details.", Alert.AlertType.WARNING);
            }
        });

        // View user watchlist button
        view.getViewUserWatchlistBtn().setOnAction(e -> {
            System.out.println("📺 View User Watchlist button clicked!");
            AdminUserManagementPage.UserData selectedUser = view.getUsersTable().getSelectionModel().getSelectedItem();
            if (selectedUser != null) {
                System.out.println("📺 Selected user: " + selectedUser.getEmail());
                viewUserWatchlist(selectedUser);
            } else {
                System.out.println("⚠️ No user selected!");
                showAlert("Please select a user to view watchlist.", Alert.AlertType.WARNING);
            }
        });

        // View user stats button
        view.getViewUserStatsBtn().setOnAction(e -> {
            AdminUserManagementPage.UserData selectedUser = view.getUsersTable().getSelectionModel().getSelectedItem();
            if (selectedUser != null) {
                viewUserStats(selectedUser);
            } else {
                showAlert("Please select a user to view stats.", Alert.AlertType.WARNING);
            }
        });

        // View user activity button
        view.getViewUserActivityBtn().setOnAction(e -> {
            System.out.println("📊 View User Activity button clicked!");
            AdminUserManagementPage.UserData selectedUser = view.getUsersTable().getSelectionModel().getSelectedItem();
            if (selectedUser != null) {
                System.out.println("📊 Selected user: " + selectedUser.getEmail());
                viewUserActivity(selectedUser);
            } else {
                System.out.println("⚠️ No user selected!");
                showAlert("Please select a user to view activity history.", Alert.AlertType.WARNING);
            }
        });

        // Back button - optimized navigation to admin dashboard
        view.getBackBtn().setOnAction(e -> {
            String operationId = com.moodflix.util.PerformanceMonitor.startOperation("admin_user_management_back_navigation");
            
            // Use optimized back navigation
            String adminEmail = com.moodflix.util.SessionManager.getEmail();
            if (adminEmail != null) {
                com.moodflix.util.SessionManager.setSession(adminEmail, "admin");
                com.moodflix.util.BackNavigationOptimizer.navigateBackToDashboard(adminEmail, "admin");
            }
            
            com.moodflix.util.PerformanceMonitor.endOperation(operationId, true);
        });
    }

    private void setupTableButtonHandlers() {
        // Role change
        TableColumn<AdminUserManagementPage.UserData, String> roleCol = view.roleCol;
        if (roleCol != null) {
            roleCol.setOnEditCommit(event -> {
                AdminUserManagementPage.UserData user = event.getRowValue();
                String newRole = event.getNewValue();
                user.setRole(newRole);
                new Thread(() -> {
                    try {
                        authService.updateUserRole(user.getEmail(), newRole);
                    } catch (Exception ex) {
                        showAlert("Failed to update user role.", Alert.AlertType.ERROR);
                    }
                }).start();
            });
        }
        
        // Set up table button callbacks
        view.setOnEditUser(user -> showEditUserDialog(user));
        view.setOnDeleteUser(user -> showDeleteUserDialog(user));
    }

    private void showEditUserDialog(AdminUserManagementPage.UserData user) {
        Platform.runLater(() -> {
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Edit User");
            dialog.setHeaderText("Edit details for: " + user.getEmail());
            VBox box = new VBox(10);
            box.setPadding(new javafx.geometry.Insets(20));
            TextField nameField = new TextField(user.getFullName());
            TextField ageField = new TextField(user.getAge());
            TextField genderField = new TextField(user.getGender());
            ComboBox<String> roleBox = new ComboBox<>(FXCollections.observableArrayList("user", "admin"));
            roleBox.setValue(user.getRole());
            box.getChildren().addAll(new Label("Full Name:"), nameField, new Label("Age:"), ageField, new Label("Gender:"), genderField, new Label("Role:"), roleBox);
            dialog.getDialogPane().setContent(box);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            dialog.showAndWait().ifPresent(result -> {
                if (result == ButtonType.OK) {
                    user.setFullName(nameField.getText());
                    user.setAge(ageField.getText());
                    user.setGender(genderField.getText());
                    user.setRole(roleBox.getValue());
                    new Thread(() -> {
                        try {
                            authService.updateUserDetails(user.getEmail(), user.getFullName(), user.getAge(), user.getGender(), user.getRole());
                            Platform.runLater(() -> {
                                this.loadAllUsers();
                                // Show popup for successful update
                                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                alert.setTitle("Success");
                                alert.setHeaderText(null);
                                alert.setContentText("User details updated successfully!");
                                alert.showAndWait();
                            });
                        } catch (Exception ex) {
                            showAlert("Failed to update user details.", Alert.AlertType.ERROR);
                        }
                    }).start();
                }
            });
        });
    }

    private void showDeleteUserDialog(AdminUserManagementPage.UserData user) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Delete user '" + user.getEmail() + "'?", ButtonType.YES, ButtonType.NO);
            alert.showAndWait().ifPresent(result -> {
                if (result == ButtonType.YES) {
                    new Thread(() -> {
                        try {
                            authService.deleteUser(user.getEmail());
                            Platform.runLater(() -> {
                                this.loadAllUsers();
                                // Show popup for successful deletion
                                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                                successAlert.setTitle("Success");
                                successAlert.setHeaderText(null);
                                successAlert.setContentText("User deleted successfully!");
                                successAlert.showAndWait();
                            });
                        } catch (Exception ex) {
                            showAlert("Failed to delete user.", Alert.AlertType.ERROR);
                        }
                    }).start();
                }
            });
        });
    }

    private void loadAllUsers() {
        view.getStatusLabel().setText("🔄 Loading users...");
        view.getStatusLabel().setStyle("-fx-text-fill: #17a2b8;");

        // Run in background thread
        new Thread(() -> {
            try {
                System.out.println("👥 Loading all users for admin...");
                
                // Get all users from PostgreSQL
                List<AdminUserManagementPage.UserData> users = getAllUsersFromDatabase();
                
                Platform.runLater(() -> {
                    userDataList.clear();
                    userDataList.addAll(users);
                    view.getUsersTable().setItems(userDataList);
                    
                    view.getStatusLabel().setText(" Loaded " + users.size() + " users successfully");
                    view.getStatusLabel().setStyle("-fx-text-fill: #28a745;");
                    
                    System.out.println(" Loaded " + users.size() + " users for admin management");
                });
                
            } catch (Exception ex) {
                System.err.println(" Error loading users: " + ex.getMessage());
                ex.printStackTrace();
                
                Platform.runLater(() -> {
                    view.getStatusLabel().setText("Error loading users: " + ex.getMessage());
                    view.getStatusLabel().setStyle("-fx-text-fill: #dc3545;");
                });
            }
        }).start();
    }

    private List<AdminUserManagementPage.UserData> getAllUsersFromDatabase() throws Exception {
        List<AdminUserManagementPage.UserData> users = new ArrayList<>();
        // Fetch all users from PostgreSQL
        JSONObject usersJson = authService.getAllUsers();
        if (usersJson != null && !usersJson.toString().equals("null")) {
            for (String key : usersJson.keySet()) {
                JSONObject userObj = usersJson.getJSONObject(key);
                
                // Debug: Print the user object to see what fields are available
                System.out.println("🔍 User data for key '" + key + "': " + userObj.toString());
                
                // Try multiple possible email fields
                String email = userObj.optString("email", "");
                System.out.println("📧 Email from 'email' field: '" + email + "'");
                
                if (email.isEmpty()) {
                    email = userObj.optString("userEmail", "");
                    System.out.println("📧 Email from 'userEmail' field: '" + email + "'");
                }
                if (email.isEmpty()) {
                    // If email is still empty, use the key (which might be the sanitized email)
                    email = key.replace("_", ".").replace("_at_", "@");
                    System.out.println("📧 Email from key conversion: '" + email + "'");
                }
                
                String fullName = userObj.optString("fullName", userObj.optString("displayName", ""));
                String age = userObj.optString("age", "");
                String gender = userObj.optString("gender", "");
                String role = userObj.optString("role", "user");
                String joinDate = userObj.optString("joinDate", "");
                String lastLogin = userObj.optString("lastLogin", "");
                String watchlistCount = "" + userObj.optInt("watchlistCount", 0);
                
                System.out.println("📧 Extracted email: " + email);
                
                users.add(new AdminUserManagementPage.UserData(
                    email, fullName, age, gender, role, joinDate, lastLogin, watchlistCount
                ));
            }
        }
        return users;
    }

    private void filterUsers() {
        String searchText = view.getSearchField().getText().toLowerCase();
        String filterValue = view.getFilterBox().getValue();
        
        ObservableList<AdminUserManagementPage.UserData> filteredList = FXCollections.observableArrayList();
        
        for (AdminUserManagementPage.UserData user : userDataList) {
            boolean matchesSearch = searchText.isEmpty() || 
                user.getEmail().toLowerCase().contains(searchText) ||
                user.getFullName().toLowerCase().contains(searchText) ||
                user.getRole().toLowerCase().contains(searchText);
            
            boolean matchesFilter = true;
            if (filterValue.equals("👤 Users Only")) {
                matchesFilter = "user".equals(user.getRole());
            } else if (filterValue.equals("👑 Admins Only")) {
                matchesFilter = "admin".equals(user.getRole());
            }
            
            if (matchesSearch && matchesFilter) {
                filteredList.add(user);
            }
        }
        
        view.getUsersTable().setItems(filteredList);
        view.getStatusLabel().setText("🔍 Showing " + filteredList.size() + " filtered users");
        view.getStatusLabel().setStyle("-fx-text-fill: #17a2b8;");
    }

    private void viewUserDetails(AdminUserManagementPage.UserData user) {
        System.out.println("👤 Viewing details for user: " + user.getEmail());
        
        // Create a profile page for the selected user
        ProfilePage userProfilePage = new ProfilePage(user.getEmail());
        ProfilePageController profileController = new ProfilePageController(userProfilePage, user.getEmail());
        
        // Show the profile page
        Main.setScene(new Scene(userProfilePage.getView()));
        
        view.getStatusLabel().setText("👤 Viewing details for: " + user.getEmail());
        view.getStatusLabel().setStyle("-fx-text-fill: #28a745;");
    }

    private void viewUserWatchlist(AdminUserManagementPage.UserData user) {
        System.out.println("📺 Viewing watchlist for user: " + user.getEmail());
        
        // Create a watchlist page for the selected user with the new constructor
        WatchlistPage userWatchlistPage = new WatchlistPage();
        WatchlistPageController watchlistController = new WatchlistPageController(userWatchlistPage, user.getEmail());
        
        // Show the watchlist page
        Main.setScene(new Scene(userWatchlistPage.getView()));
        
        view.getStatusLabel().setText("📺 Viewing watchlist for: " + user.getEmail());
        view.getStatusLabel().setStyle("-fx-text-fill: #28a745;");
    }

    private void viewUserStats(AdminUserManagementPage.UserData user) {
        System.out.println("📈 Viewing stats for user: " + user.getEmail());
        com.moodflix.view.UserStatsPage statsPage = new com.moodflix.view.UserStatsPage();
        new com.moodflix.controller.UserStatsPageController(statsPage, user.getEmail());
        Main.setScene(new Scene(statsPage.getView()));
        view.getStatusLabel().setText("📈 Viewing stats for: " + user.getEmail());
        view.getStatusLabel().setStyle("-fx-text-fill: #28a745;");
    }

    private void showAlert(String message, Alert.AlertType alertType) {
        Platform.runLater(() -> {
            Alert alert = new Alert(alertType, message, ButtonType.OK);
            alert.showAndWait();
        });
    }

    private void viewUserActivity(AdminUserManagementPage.UserData user) {
        System.out.println("📊 Viewing activity history for user: " + user.getEmail());
        
        // Create an activity history page for the selected user
        com.moodflix.view.ActivityHistoryPage activityPage = new com.moodflix.view.ActivityHistoryPage();
        com.moodflix.controller.ActivityHistoryController activityController = new com.moodflix.controller.ActivityHistoryController(activityPage, user.getEmail());
        
        // Show the activity history page
        Main.setScene(new Scene(activityPage.getView()));
        
        view.getStatusLabel().setText("📊 Viewing activity history for: " + user.getEmail());
        view.getStatusLabel().setStyle("-fx-text-fill: #28a745; -fx-font-weight: bold;");
    }
} 