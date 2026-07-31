package com.drakalabs.schoolmngsys.shared.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** The one seam for "now" — lets time-relative business rules (e.g. BR-AT-004) be tested deterministically. */
@Configuration
public class ClockConfig {

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
