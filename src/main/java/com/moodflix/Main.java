package com.moodflix;

import javafx.application.*;
import javafx.scene.*;
import javafx.scene.image.Image;
import javafx.stage.*;
import com.moodflix.view.*;
import com.moodflix.controller.*;
import com.moodflix.database.DatabaseInitializer;
import com.moodflix.util.SampleDataInitializer;
import com.moodflix.util.ThemeManager;
import java.util.concurrent.CompletableFuture;

public class Main extends Application {

    private static Stage primaryStage;
    private static HostServices hostServices;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        hostServices = getHostServices();
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/moodflix icon_final.jpeg")));
        stage.setResizable(true); // Allow resizing
        stage.setMinWidth(800);   // Set a reasonable minimum width
        stage.setMinHeight(600);  // Set a reasonable minimum height
        stage.setWidth(1200);
        stage.setHeight(800);
        stage.setMaximized(true); // Start maximized for real app feel
        // The default window decorations (minimize, maximize, close) are enabled by default
        
        // Check if admin mode is requested
        Parameters params = getParameters();
        Scene scene;
        if (params.getRaw().contains("admin")) {
            // Direct admin access for testing
            System.out.println("Opening Admin Dashboard directly ");
            com.moodflix.view.AdminDashboard adminView = new com.moodflix.view.AdminDashboard();
            com.moodflix.controller.AdminDashboardController adminController = new com.moodflix.controller.AdminDashboardController(adminView);
            scene = new Scene(adminView.getView());
            stage.setTitle("MOODFLIX - Admin Dashboard");
        } else {
            // Landing-first flow
            LandingPage landingPage = new LandingPage();
            LandingPageController landingController = new LandingPageController(landingPage);
            scene = new Scene(landingPage.getView());
            stage.setTitle("MOODFLIX");
        }
        ThemeManager.applyTheme(scene);
        stage.setScene(scene);
        stage.show();
    }

    public static void setScene(Scene scene) {
        ThemeManager.applyTheme(scene);
        primaryStage.setScene(scene);
    }

    public static HostServices getAppHostServices() {
        return hostServices;
    }

    public static void main(String[] args) {
        System.out.println("🚀 Starting MoodFlix Application...");
        
        // Asynchronous database and sample data initialization
        CompletableFuture.runAsync(() -> {
            try {
                long start = System.currentTimeMillis();
                DatabaseInitializer.initializeDatabase();
                System.out.println("✅ Database initialized successfully!");
                SampleDataInitializer.initializeSampleData();
                System.out.println("⚡ Database setup completed in " + (System.currentTimeMillis() - start) + "ms");
            } catch (Exception e) {
                System.err.println("❌ Failed to initialize database: " + e.getMessage());
                System.err.println("Please check your database connection settings.");
            }
        });
        
        launch(args);
    }
}
