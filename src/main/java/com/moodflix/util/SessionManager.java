package com.moodflix.util;

public class SessionManager {
    private static String email;
    private static String role; // "admin" or "user"
    private static String userType; // "admin", "user", "premium", etc.
    private static long loginTime;

    public static void setSession(String emailValue, String roleValue) {
        email = emailValue;
        role = roleValue;
        userType = roleValue; // Store user type based on role
        loginTime = System.currentTimeMillis();
        
        System.out.println("🔐 Session created:");
        System.out.println("   📧 Email: " + email);
        System.out.println("   👤 Role: " + role);
        System.out.println("   🏷️ User Type: " + userType);
        System.out.println("   ⏰ Login Time: " + new java.util.Date(loginTime));
    }

    public static String getEmail() {
        return email;
    }

    public static String getRole() {
        return role;
    }

    public static String getUserType() {
        return userType;
    }

    public static boolean isAdmin() {
        return "admin".equals(role);
    }

    public static boolean isUser() {
        return "user".equals(role);
    }

    public static boolean isPremium() {
        return "premium".equals(role);
    }

    public static long getLoginTime() {
        return loginTime;
    }

    public static long getSessionDuration() {
        if (loginTime == 0) return 0;
        return System.currentTimeMillis() - loginTime;
    }

    public static void clear() {
        System.out.println("🔓 Session cleared for: " + email);
        email = null;
        role = null;
        userType = null;
        loginTime = 0;
    }

    public static String getSessionInfo() {
        if (email == null) return "No active session";
        
        long duration = getSessionDuration();
        long minutes = duration / 60000;
        long seconds = (duration % 60000) / 1000;
        
        return String.format("User: %s | Role: %s | Type: %s | Duration: %dm %ds", 
                           email, role, userType, minutes, seconds);
    }
}
