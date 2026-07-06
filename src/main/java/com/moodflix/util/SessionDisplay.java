package com.moodflix.util;

import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class SessionDisplay {
    
    public static Label createSessionInfoLabel() {
        Label sessionLabel = new Label();
        sessionLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        sessionLabel.setTextFill(Color.LIGHTGRAY);
        
        // Update session info
        updateSessionInfo(sessionLabel);
        
        return sessionLabel;
    }
    
    public static void updateSessionInfo(Label sessionLabel) {
        String sessionInfo = SessionManager.getSessionInfo();
        sessionLabel.setText(sessionInfo);
        
        // Set color based on user type
        if (SessionManager.isAdmin()) {
            sessionLabel.setTextFill(Color.GOLD);
        } else if (SessionManager.isUser()) {
            sessionLabel.setTextFill(Color.LIGHTBLUE);
        } else {
            sessionLabel.setTextFill(Color.LIGHTGRAY);
        }
        
        // Create tooltip with detailed session info
        String tooltipText = createDetailedSessionInfo();
        Tooltip tooltip = new Tooltip(tooltipText);
        sessionLabel.setTooltip(tooltip);
    }
    
    private static String createDetailedSessionInfo() {
        if (SessionManager.getEmail() == null) {
            return "No active session";
        }
        
        long duration = SessionManager.getSessionDuration();
        long minutes = duration / 60000;
        long seconds = (duration % 60000) / 1000;
        long hours = minutes / 60;
        minutes = minutes % 60;
        
        StringBuilder info = new StringBuilder();
        info.append("Session Information:\n");
        info.append("Email: ").append(SessionManager.getEmail()).append("\n");
        info.append("Role: ").append(SessionManager.getRole()).append("\n");
        info.append("User Type: ").append(SessionManager.getUserType()).append("\n");
        info.append("Login Time: ").append(new java.util.Date(SessionManager.getLoginTime())).append("\n");
        info.append("Session Duration: ");
        
        if (hours > 0) {
            info.append(hours).append("h ").append(minutes).append("m ").append(seconds).append("s");
        } else if (minutes > 0) {
            info.append(minutes).append("m ").append(seconds).append("s");
        } else {
            info.append(seconds).append("s");
        }
        
        return info.toString();
    }
    
    public static String getUserTypeDisplay() {
        if (SessionManager.isAdmin()) {
            return "👑 Admin";
        } else if (SessionManager.isUser()) {
            return "👤 User";
        } else if (SessionManager.isPremium()) {
            return "⭐ Premium";
        } else {
            return "👤 " + SessionManager.getUserType();
        }
    }
    
    public static String getRoleDisplay() {
        String role = SessionManager.getRole();
        if ("admin".equals(role)) {
            return "👑 Administrator";
        } else if ("user".equals(role)) {
            return "👤 Regular User";
        } else if ("premium".equals(role)) {
            return "⭐ Premium User";
        } else {
            return "👤 " + role;
        }
    }
} 