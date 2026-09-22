package de.ostfale.greenshop.application.port.in;

import de.ostfale.greenshop.application.port.out.CheckoutSummary;
import de.ostfale.greenshop.application.port.out.PaymentUnavailable;

import java.util.Optional;

/**
 * A customer is back from paying and wants to see what they bought.
 */
public interface ShowPurchase {

    /**
     * The checkout behind the reference the customer came back with, or empty if there is
     * none.
     *
     * @throws PaymentUnavailable if the payment provider cannot be reached
     */
    Optional<CheckoutSummary> purchase(String reference);
}
