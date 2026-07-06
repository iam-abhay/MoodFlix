package com.moodflix.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ImageCacheTest {

    @BeforeEach
    public void setUp() {
        ImageCache.clear();
    }

    @Test
    public void testEmptyUrlBounds() {
        assertNull(ImageCache.getImage(null));
        assertNull(ImageCache.getImage(""));
        assertNull(ImageCache.getImage("   "));
        
        assertNull(ImageCache.getImage(null, 100, 100, true, true));
        assertNull(ImageCache.getImage("", 100, 100, true, true));
    }

    @Test
    public void testClearCache() {
        // Assert clear runs fine
        ImageCache.clear();
        assertNull(ImageCache.getImage("nonexistent-resource.png"));
    }
}
