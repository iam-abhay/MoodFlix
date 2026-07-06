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

public class PostgreSQLContentServiceTest {

    private Connection mockConnection;
    private PreparedStatement mockStatement;
    private ResultSet mockResultSet;
    private PostgreSQLContentService contentService;

    @BeforeEach
    public void setUp() throws Exception {
        mockConnection = mock(Connection.class);
        mockStatement = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockStatement.executeUpdate()).thenReturn(1);

        DatabaseConfig.setMockConnection(mockConnection);
        contentService = new PostgreSQLContentService();
    }

    @AfterEach
    public void tearDown() {
        DatabaseConfig.setMockConnection(null);
    }

    @Test
    public void testAddContent() {
        Content c = new Content("Inception", "Thrilled", "Movie", "http://inception", "Dream thief movie", "http://inception-poster");
        assertDoesNotThrow(() -> contentService.uploadContent(c));
    }

    @Test
    public void testDeleteContent() {
        assertDoesNotThrow(() -> contentService.deleteContent("Inception"));
    }

    @Test
    public void testGetFilteredContentList() throws Exception {
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getString("title")).thenReturn("Inception");
        when(mockResultSet.getString("mood")).thenReturn("Thrilled");
        when(mockResultSet.getString("type")).thenReturn("Movie");
        when(mockResultSet.getString("link")).thenReturn("http://inception");
        when(mockResultSet.getString("description")).thenReturn("Dream thief");
        when(mockResultSet.getString("image_url")).thenReturn("http://inception-poster");

        List<Content> list = contentService.getFilteredContentList("Thrilled", "Movie");
        assertEquals(1, list.size());
        assertEquals("Inception", list.get(0).getTitle());
    }

    @Test
    public void testSearchContent() throws Exception {
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getString("title")).thenReturn("Inception");
        when(mockResultSet.getString("mood")).thenReturn("Thrilled");
        when(mockResultSet.getString("type")).thenReturn("Movie");
        when(mockResultSet.getString("link")).thenReturn("http://inception");
        when(mockResultSet.getString("description")).thenReturn("Dream thief");
        when(mockResultSet.getString("image_url")).thenReturn("http://inception-poster");

        List<Content> list = contentService.searchContent("Incep");
        assertEquals(1, list.size());
        assertEquals("Inception", list.get(0).getTitle());
    }
}
