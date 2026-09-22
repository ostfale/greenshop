package de.ostfale.greenshop.application.port.out;

import java.net.URI;

/**
 * A page outside the shop where the customer pays. The price is looked up there and then,
 * never taken from what the browser sent.
 */
public interface PaymentPage {

    /**
     * Prepares the payment of one piece of the product and says where to send the customer.
     *
     * @throws ProductNotForSale  if the product does not exist, is archived, or has no price
     *                            that can be paid once
     * @throws PaymentUnavailable if the payment provider cannot be reached
     */
    URI open(String productId);
}
