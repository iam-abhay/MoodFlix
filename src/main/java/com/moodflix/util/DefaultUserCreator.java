package com.moodflix.util;

import com.moodflix.service.PostgreSQLAuthService;

/**
 * Utility class to create a default test user
 * Email: default@email.com
 * Password: 123456
 */
public class DefaultUserCreator {
    
    public static void main(String[] args) {
        System.out.println("🔧 Creating Default Test User");
        System.out.println("=============================");
        System.out.println("Email: default@email.com");
        System.out.println("Password: 123456");
        System.out.println();
        
        try {
            PostgreSQLAuthService authService = new PostgreSQLAuthService();
            
            // Create the default user
            System.out.println("🚀 Creating default test user...");
            authService.signup("default@email.com", "123456", "user");
            
            System.out.println();
            System.out.println("🎉 SUCCESS! Default test user created successfully!");
            System.out.println("================================================");
            System.out.println("You can now login with:");
            System.out.println("   Email: default@email.com");
            System.out.println("   Password: 123456");
            
        } catch (Exception e) {
            System.err.println();
            System.err.println("💥 ERROR! Exception occurred:");
            System.err.println("=============================");
            e.printStackTrace();
            System.err.println();
            System.err.println("Please check your PostgreSQL configuration and try again.");
        }
    }
} 