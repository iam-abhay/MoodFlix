package com.moodflix.service;

import com.moodflix.database.DatabaseConfig;
import com.moodflix.model.Content;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class RecommendationServiceTest {

    private Connection mockConnection;
    private PreparedStatement mockStatement;
    private ResultSet mockResultSet;

    @BeforeEach
    public void setUp() throws Exception {
        mockConnection = mock(Connection.class);
        mockStatement = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);

        DatabaseConfig.setMockConnection(mockConnection);
    }

    @AfterEach
    public void tearDown() {
        DatabaseConfig.setMockConnection(null);
    }

    @Test
    public void testScoredRecommendations() throws Exception {
        // Query 1: Candidates selection
        // Mock two movie candidates: Inception and Interstellar
        ResultSet candidatesRS = mock(ResultSet.class);
        when(candidatesRS.next()).thenReturn(true, true, false);
        when(candidatesRS.getString("title")).thenReturn("Inception", "Interstellar");
        when(candidatesRS.getString("mood")).thenReturn("Thrilled", "Thrilled");
        when(candidatesRS.getString("type")).thenReturn("Movie", "Movie");
        when(candidatesRS.getString("link")).thenReturn("http://inception", "http://interstellar");
        when(candidatesRS.getString("description")).thenReturn("Dream thief", "Space travel");
        when(candidatesRS.getString("image_url")).thenReturn("http://inc-poster", "http://int-poster");

        // Query 2: User details ID retrieval
        ResultSet userIdRS = mock(ResultSet.class);
        when(userIdRS.next()).thenReturn(true);
        when(userIdRS.getInt("id")).thenReturn(10); // user id = 10

        // Query 3: Activity History (Interstellar has been watched)
        ResultSet activityRS = mock(ResultSet.class);
        when(activityRS.next()).thenReturn(true, false);
        when(activityRS.getInt("id")).thenReturn(1);
        when(activityRS.getString("title")).thenReturn("Interstellar");
        when(activityRS.getString("mood")).thenReturn("Thrilled");
        when(activityRS.getString("type")).thenReturn("Movie");
        when(activityRS.getTimestamp("activity_date")).thenReturn(Timestamp.valueOf(LocalDateTime.now()));
        when(activityRS.getInt("duration")).thenReturn(120);
        when(activityRS.getInt("rating")).thenReturn(5);

        // Query 4: Watchlist (Inception is on user's watchlist)
        ResultSet watchlistRS = mock(ResultSet.class);
        when(watchlistRS.next()).thenReturn(true, false);
        when(watchlistRS.getString("title")).thenReturn("Inception");
        when(watchlistRS.getString("mood")).thenReturn("Thrilled");
        when(watchlistRS.getString("type")).thenReturn("Movie");
        when(watchlistRS.getString("link")).thenReturn("http://inception");
        when(watchlistRS.getString("description")).thenReturn("Dream thief");
        when(watchlistRS.getString("image_url")).thenReturn("http://inc-poster");

        // Chain the result sets consecutively as they are queried by the services inside RecommendationService
        when(mockStatement.executeQuery()).thenReturn(candidatesRS, userIdRS, activityRS, userIdRS, watchlistRS);

        List<Content> recommendations = RecommendationService.getScoredRecommendations("user@test.com", "Thrilled", "Movie");

        assertNotNull(recommendations);
        assertEquals(2, recommendations.size());
        
        // "Inception" should be first because it is in the watchlist and has not been watched.
        // "Interstellar" should be second because it has been watched and gets penalized.
        assertEquals("Inception", recommendations.get(0).getTitle());
        assertEquals("Interstellar", recommendations.get(1).getTitle());
    }
}
