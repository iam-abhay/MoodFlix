package com.moodflix.service;

import com.moodflix.database.DatabaseConfig;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class PostgreSQLAuthServiceTest {

    private Connection mockConnection;
    private PreparedStatement mockStatement;
    private ResultSet mockResultSet;
    private PostgreSQLAuthService authService;

    @BeforeEach
    public void setUp() throws Exception {
        mockConnection = mock(Connection.class);
        mockStatement = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockStatement.executeUpdate()).thenReturn(1);

        DatabaseConfig.setMockConnection(mockConnection);
        authService = new PostgreSQLAuthService();
    }

    @AfterEach
    public void tearDown() {
        DatabaseConfig.setMockConnection(null);
    }

    @Test
    public void testLoginSuccess() throws Exception {
        // Hash password
        String hashed = BCrypt.hashpw("password123", BCrypt.gensalt());

        // Mock database result for user validation
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getString("password_hash")).thenReturn(hashed);
        when(mockResultSet.getString("role")).thenReturn("user");
        when(mockResultSet.getInt("id")).thenReturn(10);
        when(mockResultSet.getString("email")).thenReturn("user@test.com");

        JSONObject result = authService.login("user@test.com", "password123");
        assertNotNull(result);
        assertEquals("user", result.getString("role"));
        assertEquals("user@test.com", result.getString("email"));
    }

    @Test
    public void testLoginFailureWrongPassword() throws Exception {
        String hashed = BCrypt.hashpw("password123", BCrypt.gensalt());

        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getString("password_hash")).thenReturn(hashed);

        Exception exception = assertThrows(Exception.class, () -> {
            authService.login("user@test.com", "wrongpassword");
        });
        assertTrue(exception.getMessage().contains("Invalid password"));
    }

    @Test
    public void testRegisterUser() throws Exception {
        // Mock email availability query and RETURNING id
        when(mockResultSet.next()).thenReturn(false, true); // First check email, then return id
        when(mockResultSet.getInt("id")).thenReturn(15);

        JSONObject signupResult = authService.signup("newuser@test.com", "securePass123");
        assertNotNull(signupResult);
        assertEquals("newuser@test.com", signupResult.getString("email"));
    }
}
