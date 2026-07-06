package com.moodflix.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.TableView;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ScrollPane;
import javafx.util.Callback;
import javafx.scene.control.cell.ComboBoxTableCell;
import com.moodflix.util.ThemeManager;

public class AdminUserManagementPage {
    private VBox view;
    private TableView<UserData> usersTable;
    private TextField searchField;
    private ComboBox<String> filterBox;
    private Button refreshBtn;
    private Button viewUserDetailsBtn;
    private Button viewUserWatchlistBtn;
    private Button viewUserActivityBtn;
    private Button backBtn;
    private Button viewUserStatsBtn;
    private Label statusLabel;
    private VBox mainContainer;
    private VBox headerSection;
    private VBox contentSection;
    private HBox searchSection;
    private VBox tableSection;
    private VBox actionSection;
    public TableColumn<UserData, String> roleCol;
    private ScrollPane scrollPaneRoot;
    
    // Callbacks for table button actions
    private java.util.function.Consumer<UserData> onEditUser;
    private java.util.function.Consumer<UserData> onDeleteUser;

    public static class UserData {
        private String email;
        private String fullName;
        private String age;
        private String gender;
        private String role;
        private String joinDate;
        private String lastLogin;
        private String watchlistCount;

        public UserData(String email, String fullName, String age, String gender, String role, String joinDate, String lastLogin, String watchlistCount) {
            this.email = email;
            this.fullName = fullName;
            this.age = age;
            this.gender = gender;
            this.role = role;
            this.joinDate = joinDate;
            this.lastLogin = lastLogin;
            this.watchlistCount = watchlistCount;
        }

        // Getters
        public String getEmail() { return email; }
        public String getFullName() { return fullName; }
        public String getAge() { return age; }
        public String getGender() { return gender; }
        public String getRole() { return role; }
        public String getJoinDate() { return joinDate; }
        public String getLastLogin() { return lastLogin; }
        public String getWatchlistCount() { return watchlistCount; }

        // Setters
        public void setEmail(String email) { this.email = email; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public void setAge(String age) { this.age = age; }
        public void setGender(String gender) { this.gender = gender; }
        public void setRole(String role) { this.role = role; }
        public void setJoinDate(String joinDate) { this.joinDate = joinDate; }
        public void setLastLogin(String lastLogin) { this.lastLogin = lastLogin; }
        public void setWatchlistCount(String watchlistCount) { this.watchlistCount = watchlistCount; }
    }

    public AdminUserManagementPage() {
        createView();
    }

    private void createView() {
        // Main container with admin theme
        mainContainer = new VBox(30);
        mainContainer.setAlignment(Pos.TOP_CENTER);
        mainContainer.setPadding(new Insets(40));
        mainContainer.getStyleClass().add("auth-container");

        // Header Section
        createHeaderSection();
        
        // Content Section
        createContentSection();

        mainContainer.getChildren().addAll(headerSection, contentSection);

        // Wrap the entire mainContainer in a ScrollPane
        scrollPaneRoot = new ScrollPane(mainContainer);
        scrollPaneRoot.setFitToWidth(true);
        scrollPaneRoot.setFitToHeight(true);
        view = null; // no longer used as root

        // ThemeManager animations
        ThemeManager.fadeIn(mainContainer, 400);
        ThemeManager.slideUp(headerSection, 500, 100);
        ThemeManager.slideUp(contentSection, 500, 200);
    }

    private void createHeaderSection() {
        headerSection = new VBox(15);
        headerSection.setAlignment(Pos.CENTER);
        headerSection.setPadding(new Insets(30));
        headerSection.getStyleClass().add("glass-card");

        // Admin icon
        Label adminIcon = new Label("👑");
        adminIcon.getStyleClass().add("hero-title");

        // Title
        Label titleLabel = new Label("Admin User Management");
        titleLabel.getStyleClass().add("section-title");

        Label subtitleLabel = new Label("View and manage all user accounts and watchlists");
        subtitleLabel.getStyleClass().add("label-secondary");

        headerSection.getChildren().addAll(adminIcon, titleLabel, subtitleLabel);
    }

    private void createContentSection() {
        contentSection = new VBox(25);
        contentSection.setAlignment(Pos.CENTER);
        contentSection.setPadding(new Insets(30));
        contentSection.getStyleClass().add("glass-card");

        // Search and Filter Section
        createSearchSection();
        
        // Table Section
        createTableSection();
        
        // Action Section
        createActionSection();

        contentSection.getChildren().addAll(searchSection, tableSection, actionSection);

        // Staggered animations for sub-sections
        ThemeManager.slideUp(searchSection, 500, 300);
        ThemeManager.slideUp(tableSection, 500, 400);
        ThemeManager.slideUp(actionSection, 500, 500);
    }

    private void createSearchSection() {
        searchSection = new HBox(20);
        searchSection.setAlignment(Pos.CENTER);
        searchSection.setPadding(new Insets(20));
        searchSection.getStyleClass().add("card");

        // Search field
        VBox searchFieldSection = new VBox(5);
        Label searchLabel = new Label("🔍 Search Users");
        searchLabel.getStyleClass().add("label-secondary");
        
        searchField = new TextField();
        searchField.setPromptText("Search by email, name, or role...");
        searchField.setPrefWidth(300);
        searchFieldSection.getChildren().addAll(searchLabel, searchField);

        // Filter dropdown
        VBox filterSection = new VBox(5);
        Label filterLabel = new Label("🎭 Filter by Role");
        filterLabel.getStyleClass().add("label-secondary");
        
        filterBox = new ComboBox<>();
        filterBox.getItems().addAll("All Users", "👤 Users Only", "👑 Admins Only");
        filterBox.setValue("All Users");
        filterBox.setPrefWidth(200);
        filterSection.getChildren().addAll(filterLabel, filterBox);

        // Refresh button
        refreshBtn = new Button("🔄 Refresh");
        refreshBtn.getStyleClass().addAll("btn", "btn-outline");

        searchSection.getChildren().addAll(searchFieldSection, filterSection, refreshBtn);
    }

    private void createTableSection() {
        tableSection = new VBox(15);
        tableSection.setAlignment(Pos.CENTER);
        tableSection.setPadding(new Insets(20));
        tableSection.getStyleClass().add("card");

        // Table title
        Label tableTitle = new Label("📊 User Accounts");
        tableTitle.getStyleClass().add("section-title");

        // Users table
        createUsersTable();

        tableSection.getChildren().addAll(tableTitle, usersTable);
    }

    private void createUsersTable() {
        usersTable = new TableView<>();
        // Wrap the usersTable in a ScrollPane for scrollability
        ScrollPane tableScrollPane = new ScrollPane(usersTable);
        tableScrollPane.setFitToWidth(true);
        tableScrollPane.setFitToHeight(true);
        // Set preferred height to show 4 rows (plus header)
        double rowHeight = 44; // Increased row height for better visibility
        double headerHeight = 36; // Increased header height
        usersTable.setFixedCellSize(rowHeight);
        usersTable.setPrefHeight(headerHeight + rowHeight * 4 + 48); // 4 rows + header + extra padding
        usersTable.setMinHeight(headerHeight + rowHeight * 4 + 48); // Ensure minimum height for 4 rows
        usersTable.setEditable(true);

        // Configure table columns
        TableColumn<UserData, String> emailCol = new TableColumn<>("📧 Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        emailCol.setPrefWidth(200);

        TableColumn<UserData, String> nameCol = new TableColumn<>("👤 Full Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        nameCol.setPrefWidth(150);

        TableColumn<UserData, String> ageCol = new TableColumn<>("🎂 Age");
        ageCol.setCellValueFactory(new PropertyValueFactory<>("age"));
        ageCol.setPrefWidth(80);

        TableColumn<UserData, String> genderCol = new TableColumn<>("⚧ Gender");
        genderCol.setCellValueFactory(new PropertyValueFactory<>("gender"));
        genderCol.setPrefWidth(100);

        roleCol = new TableColumn<>("🎭 Role");
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));
        // Role dropdown for editing
        roleCol.setCellFactory(ComboBoxTableCell.forTableColumn("user", "admin"));
        TableColumn<UserData, Void> editCol = new TableColumn<>("Edit");
        editCol.setCellFactory(new Callback<TableColumn<UserData, Void>, TableCell<UserData, Void>>() {
            @Override
            public TableCell<UserData, Void> call(final TableColumn<UserData, Void> param) {
                final TableCell<UserData, Void> cell = new TableCell<UserData, Void>() {
                    private final Button editBtn = new Button("Edit");
                    {
                        editBtn.getStyleClass().addAll("btn", "btn-warning");
                        editBtn.setOnAction((e) -> {
                            System.out.println("🔧 Edit button clicked!");
                            UserData user = getTableView().getItems().get(getIndex());
                            System.out.println("🔧 Editing user: " + user.getEmail());
                            if (onEditUser != null) {
                                onEditUser.accept(user);
                            } else {
                                System.out.println("❌ Edit callback is null!");
                            }
                        });
                    }
                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(editBtn);
                        }
                    }
                };
                return cell;
            }
        });
        TableColumn<UserData, Void> deleteCol = new TableColumn<>("Delete");
        deleteCol.setCellFactory(new Callback<TableColumn<UserData, Void>, TableCell<UserData, Void>>() {
            @Override
            public TableCell<UserData, Void> call(final TableColumn<UserData, Void> param) {
                final TableCell<UserData, Void> cell = new TableCell<UserData, Void>() {
                    private final Button deleteBtn = new Button("Delete");
                    {
                        deleteBtn.getStyleClass().addAll("btn", "btn-danger");
                        deleteBtn.setOnAction((e) -> {
                            System.out.println("🗑️ Delete button clicked!");
                            UserData user = getTableView().getItems().get(getIndex());
                            System.out.println("🗑️ Deleting user: " + user.getEmail());
                            if (onDeleteUser != null) {
                                onDeleteUser.accept(user);
                            } else {
                                System.out.println("❌ Delete callback is null!");
                            }
                        });
                    }
                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(deleteBtn);
                        }
                    }
                };
                return cell;
            }
        });
        usersTable.getColumns().addAll(emailCol, nameCol, ageCol, genderCol, roleCol, editCol, deleteCol);
    }

    private void createActionSection() {
        actionSection = new VBox(15);
        actionSection.setAlignment(Pos.CENTER);
        actionSection.setPadding(new Insets(20));
        actionSection.getStyleClass().add("card");

        // Action buttons
        HBox actionButtons = new HBox(20);
        actionButtons.setAlignment(Pos.CENTER);

        viewUserDetailsBtn = new Button("👤 View User Details");
        viewUserDetailsBtn.getStyleClass().addAll("btn", "btn-success");

        viewUserStatsBtn = new Button("📈 View User Stats");
        viewUserStatsBtn.getStyleClass().addAll("btn", "btn-success");

        viewUserWatchlistBtn = new Button("📺 View User Watchlist");
        viewUserWatchlistBtn.getStyleClass().addAll("btn", "btn-success");

        viewUserActivityBtn = new Button("📊 View User Activity");
        viewUserActivityBtn.getStyleClass().addAll("btn", "btn-success");

        backBtn = new Button("← Back to Admin Dashboard");
        backBtn.getStyleClass().addAll("btn", "btn-outline");

        actionButtons.getChildren().addAll(viewUserDetailsBtn, viewUserStatsBtn, viewUserWatchlistBtn, viewUserActivityBtn, backBtn);

        // Status label
        statusLabel = new Label();
        statusLabel.getStyleClass().add("label-muted");
        statusLabel.setAlignment(Pos.CENTER);
        statusLabel.setMaxWidth(Double.MAX_VALUE);

        actionSection.getChildren().addAll(actionButtons, statusLabel);
    }

    // Getters
    public ScrollPane getView() { return scrollPaneRoot; }
    public TableView<UserData> getUsersTable() { return usersTable; }
    public TextField getSearchField() { return searchField; }
    public ComboBox<String> getFilterBox() { return filterBox; }
    public Button getRefreshBtn() { return refreshBtn; }
    public Button getViewUserDetailsBtn() { return viewUserDetailsBtn; }
    public Button getViewUserWatchlistBtn() { return viewUserWatchlistBtn; }
    public Button getViewUserActivityBtn() { return viewUserActivityBtn; }
    public Button getBackBtn() { return backBtn; }
    public Label getStatusLabel() { return statusLabel; }
    public Button getViewUserStatsBtn() { return viewUserStatsBtn; }
    
    // Setter methods for callbacks
    public void setOnEditUser(java.util.function.Consumer<UserData> callback) {
        this.onEditUser = callback;
    }
    
    public void setOnDeleteUser(java.util.function.Consumer<UserData> callback) {
        this.onDeleteUser = callback;
    }
} 