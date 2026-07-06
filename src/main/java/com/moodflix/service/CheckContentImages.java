package com.moodflix.service;

import com.moodflix.model.Content;
import java.util.List;

public class CheckContentImages {
    public static void main(String[] args) throws Exception {
        PostgreSQLContentService service = new PostgreSQLContentService();
        List<Content> allContent = service.getAllContent();
        int total = 0, missing = 0;
        for (Content c : allContent) {
            total++;
            String url = c.getImageUrl();
            if (url == null || url.trim().isEmpty()) {
                System.out.println("[MISSING] " + c.getTitle() + " has no imageUrl");
                missing++;
            }
        }
        System.out.println("Checked " + total + " items. " + missing + " missing image URLs.");
    }
} 