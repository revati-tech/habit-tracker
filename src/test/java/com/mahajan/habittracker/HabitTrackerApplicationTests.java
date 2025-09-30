package com.mahajan.habittracker;

import com.mahajan.habittracker.config.SecurityTestConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Import(SecurityTestConfig.class) // injects our relaxed test security
@DisplayName("Application context loads successfully")
class HabitTrackerApplicationTests {

    @Test
    void contextLoads() {
        // If this runs without exceptions, context is fine.
    }
}
