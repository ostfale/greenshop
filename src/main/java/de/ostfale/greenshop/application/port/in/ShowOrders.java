package de.ostfale.greenshop.application.port.in;

import de.ostfale.greenshop.domain.orders.Order;

import java.util.List;

/**
 * What has been sold, for the shop's own eyes.
 */
public interface ShowOrders {

    /**
     * Every order, the newest first.
     */
    List<Order> orders();
}
