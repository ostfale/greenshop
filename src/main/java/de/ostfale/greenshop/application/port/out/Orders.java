package de.ostfale.greenshop.application.port.out;

import de.ostfale.greenshop.domain.orders.Order;

import java.util.List;
import java.util.Optional;

/**
 * Where the orders are kept. One order per reference: saving again replaces it.
 */
public interface Orders {

    Optional<Order> find(String reference);

    void save(Order order);

    /**
     * Every order, the newest first.
     */
    List<Order> all();
}
