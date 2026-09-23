package de.ostfale.greenshop.application.port.out;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;

/**
 * A page outside the shop where the customer pays. The price is looked up there and then,
 * never taken from what the browser sent.
 */
public interface PaymentPage {

    /**
     * Prepares the payment of the product and says where to send the customer. How many they
     * take is chosen on the payment page, not here.
     * <p>
     * The attempt names this one press of the buy button. Pressing it twice must not start two
     * payments, so the same attempt leads back to the same payment page.
     *
     * @throws ProductNotForSale  if the product does not exist, is archived, or has no price
     *                            that can be paid once
     * @throws PaymentUnavailable if the payment provider cannot be reached
     */
    URI open(String productId, UUID attempt);

    /**
     * The checkout the customer came back from, by the reference the provider handed back.
     * Empty for a reference it does not know — and for a checkout that did not start in this
     * shop, which the provider may well know but which is none of our business.
     *
     * @throws PaymentUnavailable if the payment provider cannot be reached
     */
    Optional<CheckoutSummary> find(String reference);
}
