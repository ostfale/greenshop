package de.ostfale.greenshop.application.port.in;

import de.ostfale.greenshop.application.port.out.PaymentUnavailable;
import de.ostfale.greenshop.application.port.out.RefundRefused;
import de.ostfale.greenshop.domain.orders.Order;

/**
 * The shop gives an order back.
 */
public interface RefundPurchase {

    /**
     * Gives the whole order back and answers with it in its new state.
     *
     * @throws IllegalArgumentException if no order is kept under this reference
     * @throws IllegalStateException    if the order is not in a state that can be given back
     * @throws RefundRefused            if the provider will not give the payment back
     * @throws PaymentUnavailable       if the provider cannot be reached
     */
    Order refund(String reference);
}
