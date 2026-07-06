package com.moodflix.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class DesignSystemTest {

    @Test
    public void testTokens() {
        assertEquals("#e50914", DesignSystem.COLOR_ACCENT);
        assertEquals("#141414", DesignSystem.COLOR_BG_PRIMARY);
        assertEquals("#ffffff", DesignSystem.COLOR_TEXT_PRIMARY);
        assertEquals(8.0, DesignSystem.RADIUS_SM);
        assertEquals(300.0, DesignSystem.TRANSITION_NORMAL_MS);
    }
}
