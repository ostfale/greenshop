package de.ostfale.greenshop.application.port.out;

import de.ostfale.greenshop.domain.orders.Order;

import java.util.List;
import java.util.Optional;

/**
 * Where the orders are kept. One order per reference: saving again replaces it.
 */
public interface Orders {

    Optional<Order> find(String reference);

    /**
     * The order that went through this payment. The provider reports a refund on the payment,
     * not on the checkout, so this is the way back from one to the other.
     */
    Optional<Order> findByPayment(String payment);

    void save(Order order);

    /**
     * Every order, the newest first.
     */
    List<Order> all();
}
