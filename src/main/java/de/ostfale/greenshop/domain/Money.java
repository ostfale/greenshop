package de.ostfale.greenshop.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Currency;
import java.util.Objects;

/**
 * An amount in the smallest unit of its currency, the way Stripe counts: 3,00 € is 300.
 * Formatting for the page is the template's business.
 */
public record Money(long amount, Currency currency) {

    private static final Logger log = LoggerFactory.getLogger(Money.class);

    public Money {
        Objects.requireNonNull(currency, "currency");
        if (amount < 0) {
            throw new IllegalArgumentException("amount must not be negative: " + amount);
        }
        log.debug("Money :: Money created with amount: {} and currency: {}", amount, currency);
    }

    public static Money of(long amount, String currencyCode) {
        Objects.requireNonNull(currencyCode, "currencyCode");
        log.debug("Money :: of() called with amount: {} and currencyCode: {}", amount, currencyCode);
        return new Money(amount, Currency.getInstance(currencyCode.toUpperCase()));
    }
}
