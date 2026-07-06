package com.moodflix.util;

import com.moodflix.service.PostgreSQLContentService;

/**
 * Utility to check content in PostgreSQL database
 */
public class ContentChecker {
    
    public static void main(String[] args) {
        System.out.println("🔍 Checking Content in PostgreSQL Database");
        System.out.println("=======================================");
        System.out.println();
        
        try {
            // List all content
            PostgreSQLContentService service = new PostgreSQLContentService();
            java.util.List<com.moodflix.model.Content> allContent = service.getAllContent();
            System.out.println("Total content items: " + allContent.size());
            for (com.moodflix.model.Content c : allContent) {
                System.out.println(" - " + c.getTitle() + " (" + c.getMood() + "/" + c.getType() + ")");
            }
            
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