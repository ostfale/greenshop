package de.ostfale.greenshop.adapter.in.web;

import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

/**
 * Formats a moment for the page, in German and in the shop's zone: "22.09.2026, 14:05".
 * Called from the templates as {@code @dates.format(...)}.
 */
@Component("dates")
class Dates {

    private final DateTimeFormatter format;

    Dates(Clock clock) {
        this.format = DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm").withZone(clock.getZone());
    }

    public String format(Instant moment) {
        return format.format(moment);
    }
}
