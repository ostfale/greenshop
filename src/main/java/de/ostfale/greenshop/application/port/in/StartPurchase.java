package de.ostfale.greenshop.application.port.in;

import de.ostfale.greenshop.application.port.out.PaymentUnavailable;
import de.ostfale.greenshop.application.port.out.ProductNotForSale;

import java.net.URI;
import java.util.UUID;

/**
 * A customer wants to buy one product.
 */
public interface StartPurchase {

    /**
     * Where to send the customer to pay for the product; the quantity is chosen there. The
     * attempt names this press of the buy button, so that pressing twice pays once.
     *
     * @throws ProductNotForSale  if the product cannot be bought
     * @throws PaymentUnavailable if the payment cannot be prepared
     */
    URI start(String productId, UUID attempt);
}
