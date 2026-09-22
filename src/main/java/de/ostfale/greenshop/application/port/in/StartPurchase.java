package de.ostfale.greenshop.application.port.in;

import de.ostfale.greenshop.application.port.out.PaymentUnavailable;
import de.ostfale.greenshop.application.port.out.ProductNotForSale;

import java.net.URI;

/**
 * A customer wants to buy one product.
 */
public interface StartPurchase {

    /**
     * Where to send the customer to pay for the product; the quantity is chosen there.
     *
     * @throws ProductNotForSale  if the product cannot be bought
     * @throws PaymentUnavailable if the payment cannot be prepared
     */
    URI start(String productId);
}
