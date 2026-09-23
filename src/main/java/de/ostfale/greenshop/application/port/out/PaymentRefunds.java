package de.ostfale.greenshop.application.port.out;

/**
 * Giving money back through the payment provider.
 */
public interface PaymentRefunds {

    /**
     * Gives back everything that was paid on this payment.
     *
     * @throws RefundRefused      if the provider will not give this payment back, for instance
     *                            because it already did
     * @throws PaymentUnavailable if the provider cannot be reached
     */
    void refund(String payment);
}
