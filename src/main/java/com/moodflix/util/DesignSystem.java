package com.moodflix.util;

/**
 * Centralized Design System Constants for MoodFlix.
 * Standardizes visual styling tokens (colors, typography, spacing, border radii, and effects)
 * to maintain consistent design across custom programmatic JavaFX nodes.
 */
public final class DesignSystem {

    private DesignSystem() {
        // Utility class
    }

    // ---- COLORS ----
    public static final String COLOR_ACCENT = "#e50914";       // Netflix Red
    public static final String COLOR_ACCENT_LIGHT = "#f54b50"; // Red highlight
    public static final String COLOR_ACCENT_DARK = "#b80710";  // Deep Red
    
    public static final String COLOR_BG_PRIMARY = "#141414";
    public static final String COLOR_BG_SECONDARY = "#181818";
    public static final String COLOR_BG_CARD = "#202020";
    public static final String COLOR_BG_INPUT = "#232323";
    public static final String COLOR_BG_GLASS = "rgba(24, 24, 24, 0.72)";
    
    public static final String COLOR_TEXT_PRIMARY = "#ffffff";
    public static final String COLOR_TEXT_SECONDARY = "#a3a3a3";
    public static final String COLOR_TEXT_MUTED = "#737373";

    public static final String COLOR_SUCCESS = "#22c55e";
    public static final String COLOR_WARNING = "#eab308";
    public static final String COLOR_DANGER = "#ef4444";
    public static final String COLOR_INFO = "#3b82f6";

    // ---- TYPOGRAPHY ----
    public static final String FONT_FAMILY = "Segoe UI";
    public static final double FONT_SIZE_XS = 11.0;
    public static final double FONT_SIZE_SM = 13.0;
    public static final double FONT_SIZE_MD = 14.0;
    public static final double FONT_SIZE_LG = 18.0;
    public static final double FONT_SIZE_XL = 24.0;
    public static final double FONT_SIZE_XXL = 32.0;

    // ---- SPACING & PADDING ----
    public static final double SPACING_XS = 6.0;
    public static final double SPACING_SM = 12.0;
    public static final double SPACING_MD = 18.0;
    public static final double SPACING_LG = 28.0;
    public static final double SPACING_XL = 40.0;

    // ---- BORDER RADII ----
    public static final double RADIUS_SM = 8.0;
    public static final double RADIUS_MD = 10.0;
    public static final double RADIUS_LG = 16.0;
    public static final double RADIUS_XL = 20.0;

    // ---- TRANSITIONS & ANIMATIONS ----
    public static final double TRANSITION_FAST_MS = 150.0;
    public static final double TRANSITION_NORMAL_MS = 300.0;
    public static final double TRANSITION_SLOW_MS = 500.0;
}
