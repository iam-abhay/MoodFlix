package com.moodflix.service;

import com.moodflix.model.Content;
import java.util.List;

public class FixContentImages {
    public static void main(String[] args) throws Exception {
        PostgreSQLContentService service = new PostgreSQLContentService();
        List<Content> allContent = service.getAllContent();
        int fixed = 0;
        for (Content c : allContent) {
            String imageUrl = c.getImageUrl();
            boolean needsFix = (imageUrl == null || imageUrl.trim().isEmpty() ||
                (!imageUrl.matches("https?://.*\\.(jpg|jpeg|png|gif|webp)(\\?.*)?$")));
            if (needsFix) {
                String placeholder = "https://via.placeholder.com/160x220?text=Movie+Poster";
                c.setImageUrl(placeholder);
                service.updateContent(c.getTitle(), c);
                System.out.println("[FIXED] " + c.getTitle());
                fixed++;
            }
        }
        System.out.println("Fixed " + fixed + " content items with missing/invalid imageUrl.");
    }
} 