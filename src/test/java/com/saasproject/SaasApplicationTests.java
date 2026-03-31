package com.saasproject;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Basic smoke test to verify application context loads.
 */
@SpringBootTest
@ActiveProfiles("test")
class SaasApplicationTests {

    @Test
    void contextLoads() {
        // Verify Spring context loads successfully
    }
}
