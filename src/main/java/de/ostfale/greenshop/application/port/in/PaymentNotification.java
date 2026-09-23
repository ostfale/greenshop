package de.ostfale.greenshop.application.port.in;

import java.util.Objects;

/**
 * One message of the payment provider: which message it is, and which checkout it is about.
 * The id identifies the message, not the checkout — the same checkout is reported on several
 * times, and every one of those messages may arrive twice.
 */
public record PaymentNotification(String messageId, String reference) {

    public PaymentNotification {
        Objects.requireNonNull(messageId, "messageId");
        Objects.requireNonNull(reference, "reference");
    }
}
