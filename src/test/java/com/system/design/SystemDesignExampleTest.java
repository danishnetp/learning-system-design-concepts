package com.system.design;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for SystemDesignExample.
 */
public class SystemDesignExampleTest {

    @Test
    public void testGetMessage() {
        SystemDesignExample example = new SystemDesignExample();
        String message = example.getMessage();
        assertNotNull(message);
        assertTrue(message.contains("learning"));
    }
}

