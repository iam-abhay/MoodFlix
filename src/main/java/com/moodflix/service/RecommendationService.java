package com.moodflix.service;

import com.moodflix.model.Content;
import com.moodflix.model.Activity;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Intelligent Rule-Based Recommendation Engine for MoodFlix.
 * Computes content scoring and relevance matching based on:
 * - Current requested mood & type
 * - User Watchlist content (prioritizes saved items)
 * - User Activity history (suppresses recently watched items to avoid repetitive recommendations)
 */
public final class RecommendationService {

    private RecommendationService() {
        // Utility class
    }

    /**
     * Get scored and prioritized recommendations for a user.
     */
    public static List<Content> getScoredRecommendations(String userEmail, String mood, String type) {
        System.out.println("[REC-ENGINE] Calculating recommendations for: " + userEmail);
        
        List<Content> candidates = new ArrayList<>();
        Set<String> watchedTitles = new HashSet<>();
        Set<String> watchlistTitles = new HashSet<>();

        try {
            // 1. Fetch filtered candidates
            PostgreSQLContentService contentService = new PostgreSQLContentService();
            candidates = contentService.getFilteredContentList(mood, type);

            if (userEmail != null && !userEmail.isEmpty()) {
                // 2. Fetch watch history to identify repeat watches
                PostgreSQLDatabaseService dbService = new PostgreSQLDatabaseService();
                List<Activity> activities = dbService.getActivitiesByUser(userEmail);
                for (Activity act : activities) {
                    watchedTitles.add(act.getTitle().toLowerCase().trim());
                }

                // 3. Fetch watchlist to identify user preferences
                PostgreSQLWatchlistService watchlistService = new PostgreSQLWatchlistService();
                List<Content> watchlist = watchlistService.getWatchlist(userEmail);
                for (Content w : watchlist) {
                    watchlistTitles.add(w.getTitle().toLowerCase().trim());
                }
            }
        } catch (Exception e) {
            System.err.println("[REC-ENGINE] Error reading preferences from database: " + e.getMessage());
        }

        // 4. Score candidates
        class ScoredContent implements Comparable<ScoredContent> {
            final Content content;
            double score;

            ScoredContent(Content content, double score) {
                this.content = content;
                this.score = score;
            }

            @Override
            public int compareTo(ScoredContent other) {
                return Double.compare(other.score, this.score); // Descending order
            }
        }

        List<ScoredContent> scoredList = new ArrayList<>();

        for (Content candidate : candidates) {
            double score = 0;
            String normalizedTitle = candidate.getTitle().toLowerCase().trim();

            // Match mood weight
            if (mood != null && mood.equalsIgnoreCase(candidate.getMood())) {
                score += 50.0;
            }

            // Match type weight
            if (type != null && type.equalsIgnoreCase(candidate.getType())) {
                score += 30.0;
            }

            // Watchlist affinity
            if (watchlistTitles.contains(normalizedTitle)) {
                score += 15.0; // Boost items user has explicitly saved
            }

            // Freshness / Repetition suppression
            if (watchedTitles.contains(normalizedTitle)) {
                score -= 20.0; // Penalty for items already watched
            } else {
                score += 10.0; // Small bonus for unwatched items
            }

            // Add small randomization to avoid static recommendations on every load
            score += Math.random() * 5.0;

            scoredList.add(new ScoredContent(candidate, score));
        }

        // Sort by relevance score
        scoredList.sort(null);

        // Map back to Content objects
        return scoredList.stream()
                .map(sc -> sc.content)
                .collect(Collectors.toList());
    }
}
