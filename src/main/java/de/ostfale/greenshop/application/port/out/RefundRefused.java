package de.ostfale.greenshop.application.port.out;

/**
 * The provider will not give this payment back — already refunded, never charged, or too old.
 * Trying again would end the same way.
 */
public class RefundRefused extends RuntimeException {

    public RefundRefused(String message, Throwable cause) {
        super(message, cause);
    }
}
