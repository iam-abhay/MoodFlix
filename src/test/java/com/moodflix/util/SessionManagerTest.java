package com.moodflix.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SessionManagerTest {

    @BeforeEach
    public void setUp() {
        SessionManager.clear();
    }

    @Test
    public void testSessionLifecycle() {
        assertNull(SessionManager.getEmail());
        assertNull(SessionManager.getRole());
        assertFalse(SessionManager.isAdmin());

        SessionManager.setSession("test@moodflix.com", "user");
        assertEquals("test@moodflix.com", SessionManager.getEmail());
        assertEquals("user", SessionManager.getRole());
        assertFalse(SessionManager.isAdmin());

        SessionManager.clear();
        assertNull(SessionManager.getEmail());
        assertNull(SessionManager.getRole());
    }

    @Test
    public void testAdminPrivileges() {
        SessionManager.setSession("admin@moodflix.com", "admin");
        assertEquals("admin@moodflix.com", SessionManager.getEmail());
        assertEquals("admin", SessionManager.getRole());
        assertTrue(SessionManager.isAdmin());
    }
}
