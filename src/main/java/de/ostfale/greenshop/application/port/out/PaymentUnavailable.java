package de.ostfale.greenshop.application.port.out;

/**
 * The payment could not be prepared — network, key, or the provider itself. The cause keeps
 * the details.
 */
public class PaymentUnavailable extends RuntimeException {

    public PaymentUnavailable(String message, Throwable cause) {
        super(message, cause);
    }
}
