package com.moodflix.util;

import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.animation.*;
import javafx.scene.Node;
import javafx.util.Duration;

/**
 * Centralized theme and animation utilities for MoodFlix.
 * Ensures consistent styling, dynamic light/dark modes, and smooth transitions across all views.
 */
public class ThemeManager {
    
    private static final String THEME_CSS = "/moodflix-theme.css";
    private static String currentTheme = "dark";
    private static boolean animationsEnabled = true;
    
    private static String cachedCssUrl = null;

    /**
     * Apply the MoodFlix theme to a scene.
     */
    public static void applyTheme(Scene scene) {
        if (scene != null) {
            if (cachedCssUrl == null) {
                cachedCssUrl = ThemeManager.class.getResource(THEME_CSS).toExternalForm();
            }
            if (!scene.getStylesheets().contains(cachedCssUrl)) {
                scene.getStylesheets().add(cachedCssUrl);
            }
            if (scene.getRoot() != null) {
                scene.getRoot().getStyleClass().removeAll("theme-dark", "theme-light");
                if ("light".equalsIgnoreCase(currentTheme)) {
                    scene.getRoot().getStyleClass().add("theme-light");
                } else {
                    scene.getRoot().getStyleClass().add("theme-dark");
                }
            }
        }
    }
    
    /**
     * Set the current active theme.
     */
    public static void setTheme(Scene scene, String theme) {
        currentTheme = theme;
        applyTheme(scene);
    }

    /**
     * Get the active theme name.
     */
    public static String getTheme() {
        return currentTheme;
    }

    /**
     * Set whether UI transitions and animations are enabled.
     */
    public static void setAnimationsEnabled(boolean enabled) {
        animationsEnabled = enabled;
    }

    /**
     * Check if UI transitions and animations are enabled.
     */
    public static boolean isAnimationsEnabled() {
        return animationsEnabled;
    }

    /**
     * Create a themed scene from a root node.
     */
    public static Scene createThemedScene(Parent root) {
        Scene scene = new Scene(root);
        applyTheme(scene);
        return scene;
    }
    
    // ---- ANIMATION HELPERS ----
    
    /**
     * Fade-in animation for a node.
     */
    public static void fadeIn(Node node, double durationMs) {
        fadeIn(node, durationMs, 0);
    }
    
    public static void fadeIn(Node node, double durationMs, double delayMs) {
        if (!animationsEnabled) {
            node.setOpacity(1.0);
            return;
        }
        node.setOpacity(0);
        FadeTransition ft = new FadeTransition(Duration.millis(durationMs), node);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.setDelay(Duration.millis(delayMs));
        ft.setInterpolator(Interpolator.EASE_OUT);
        ft.play();
    }
    
    /**
     * Slide-up + fade-in animation.
     */
    public static void slideUp(Node node, double durationMs, double delayMs) {
        if (!animationsEnabled) {
            node.setOpacity(1.0);
            node.setTranslateY(0);
            return;
        }
        node.setOpacity(0);
        node.setTranslateY(30);
        
        FadeTransition ft = new FadeTransition(Duration.millis(durationMs), node);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.setDelay(Duration.millis(delayMs));
        ft.setInterpolator(Interpolator.EASE_OUT);
        
        TranslateTransition tt = new TranslateTransition(Duration.millis(durationMs), node);
        tt.setFromY(30);
        tt.setToY(0);
        tt.setDelay(Duration.millis(delayMs));
        tt.setInterpolator(Interpolator.EASE_OUT);
        
        ParallelTransition pt = new ParallelTransition(ft, tt);
        pt.play();
    }
    
    /**
     * Scale bounce animation (for buttons, cards on click).
     */
    public static void scalePop(Node node) {
        if (!animationsEnabled) {
            return;
        }
        ScaleTransition st = new ScaleTransition(Duration.millis(150), node);
        st.setFromX(1.0);
        st.setFromY(1.0);
        st.setToX(0.95);
        st.setToY(0.95);
        st.setAutoReverse(true);
        st.setCycleCount(2);
        st.setInterpolator(Interpolator.EASE_BOTH);
        st.play();
    }
    
    /**
     * Subtle pulse glow animation (for hero elements).
     */
    public static void pulseGlow(Node node) {
        if (!animationsEnabled) {
            return;
        }
        ScaleTransition st = new ScaleTransition(Duration.seconds(2), node);
        st.setFromX(1.0);
        st.setFromY(1.0);
        st.setToX(1.03);
        st.setToY(1.03);
        st.setAutoReverse(true);
        st.setCycleCount(Animation.INDEFINITE);
        st.setInterpolator(Interpolator.EASE_BOTH);
        st.play();
    }
    
    /**
     * Stagger animate a list of children with slide-up effect.
     */
    public static void staggerChildren(javafx.scene.layout.Pane parent, double staggerMs) {
        for (int i = 0; i < parent.getChildren().size(); i++) {
            slideUp(parent.getChildren().get(i), 400, animationsEnabled ? (i * staggerMs) : 0);
        }
    }
}
