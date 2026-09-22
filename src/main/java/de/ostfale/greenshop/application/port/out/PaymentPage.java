package de.ostfale.greenshop.application.port.out;

import java.net.URI;
import java.util.Optional;

/**
 * A page outside the shop where the customer pays. The price is looked up there and then,
 * never taken from what the browser sent.
 */
public interface PaymentPage {

    /**
     * Prepares the payment of the product and says where to send the customer. How many they
     * take is chosen on the payment page, not here.
     *
     * @throws ProductNotForSale  if the product does not exist, is archived, or has no price
     *                            that can be paid once
     * @throws PaymentUnavailable if the payment provider cannot be reached
     */
    URI open(String productId);

    /**
     * The checkout the customer came back from, by the reference the provider handed back.
     * Empty for a reference it does not know.
     *
     * @throws PaymentUnavailable if the payment provider cannot be reached
     */
    Optional<CheckoutSummary> find(String reference);
}
