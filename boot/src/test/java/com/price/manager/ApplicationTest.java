package com.price.manager;

import org.junit.jupiter.api.Test; // Test class added ONLY to cover main() invocation not covered by application tests.

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class ApplicationTest {

    @Test
    void main() {
        try {
            Application.main(new String[]{});
            assertTrue(true, "Application started successfully");
        } catch (final Exception e) {
            fail("Application failed to start: " + e.getMessage());
        }
    }
}
