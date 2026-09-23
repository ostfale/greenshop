package de.ostfale.greenshop.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

/**
 * One clock for the application, in the shop's own zone, so a test can hold time still.
 */
@Configuration
class ClockConfiguration {

    static final ZoneId SHOP_ZONE = ZoneId.of("Europe/Berlin");

    @Bean
    Clock clock() {
        return Clock.system(SHOP_ZONE);
    }
}
