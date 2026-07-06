package com.moodflix.service;

import com.moodflix.database.DatabaseConfig;
import com.moodflix.model.Content;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class PostgreSQLWatchlistServiceTest {

    private Connection mockConnection;
    private PreparedStatement mockStatement;
    private PostgreSQLWatchlistService watchlistService;

    @BeforeEach
    public void setUp() throws Exception {
        mockConnection = mock(Connection.class);
        mockStatement = mock(PreparedStatement.class);

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeUpdate()).thenReturn(1);

        DatabaseConfig.setMockConnection(mockConnection);
        watchlistService = new PostgreSQLWatchlistService();
    }

    @AfterEach
    public void tearDown() {
        DatabaseConfig.setMockConnection(null);
    }

    @Test
    public void testAddToWatchlist() throws Exception {
        ResultSet mockUserRS = mock(ResultSet.class);
        when(mockUserRS.next()).thenReturn(true);
        when(mockUserRS.getInt("id")).thenReturn(10);

        ResultSet mockContentRS = mock(ResultSet.class);
        when(mockContentRS.next()).thenReturn(true);
        when(mockContentRS.getInt("id")).thenReturn(20);

        when(mockStatement.executeQuery()).thenReturn(mockUserRS, mockContentRS);

        assertDoesNotThrow(() -> watchlistService.addToWatchlist("user@test.com", "Inception"));
    }

    @Test
    public void testGetWatchlist() throws Exception {
        ResultSet mockUserRS = mock(ResultSet.class);
        when(mockUserRS.next()).thenReturn(true);
        when(mockUserRS.getInt("id")).thenReturn(10);

        ResultSet mockWatchlistRS = mock(ResultSet.class);
        when(mockWatchlistRS.next()).thenReturn(true, false);
        when(mockWatchlistRS.getString("title")).thenReturn("Inception");
        when(mockWatchlistRS.getString("mood")).thenReturn("Thriller");
        when(mockWatchlistRS.getString("type")).thenReturn("Movie");
        when(mockWatchlistRS.getString("link")).thenReturn("http://link");
        when(mockWatchlistRS.getString("description")).thenReturn("Dream thief");
        when(mockWatchlistRS.getString("image_url")).thenReturn("http://image");

        when(mockStatement.executeQuery()).thenReturn(mockUserRS, mockWatchlistRS);

        List<Content> list = watchlistService.getWatchlist("user@test.com");
        assertEquals(1, list.size());
        assertEquals("Inception", list.get(0).getTitle());
    }

    @Test
    public void testRemoveFromWatchlist() throws Exception {
        ResultSet mockUserRS = mock(ResultSet.class);
        when(mockUserRS.next()).thenReturn(true);
        when(mockUserRS.getInt("id")).thenReturn(10);

        ResultSet mockContentRS = mock(ResultSet.class);
        when(mockContentRS.next()).thenReturn(true);
        when(mockContentRS.getInt("id")).thenReturn(20);

        when(mockStatement.executeQuery()).thenReturn(mockUserRS, mockContentRS);

        assertDoesNotThrow(() -> watchlistService.removeFromWatchlist("user@test.com", "Inception"));
    }

    @Test
    public void testIsInWatchlist() throws Exception {
        ResultSet mockUserRS = mock(ResultSet.class);
        when(mockUserRS.next()).thenReturn(true);
        when(mockUserRS.getInt("id")).thenReturn(10);

        ResultSet mockContentRS = mock(ResultSet.class);
        when(mockContentRS.next()).thenReturn(true);
        when(mockContentRS.getInt("id")).thenReturn(20);

        ResultSet mockCountRS = mock(ResultSet.class);
        when(mockCountRS.next()).thenReturn(true);
        when(mockCountRS.getInt("count")).thenReturn(1);

        when(mockStatement.executeQuery()).thenReturn(mockUserRS, mockContentRS, mockCountRS);

        assertTrue(watchlistService.isInWatchlist("user@test.com", "Inception"));
    }
}
