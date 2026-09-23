package de.ostfale.greenshop.application.service;

import de.ostfale.greenshop.application.port.out.Orders;
import de.ostfale.greenshop.domain.orders.Order;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Orders in a map, with a count of how often something was saved.
 */
public class FakeOrders implements Orders {

    private final Map<String, Order> byReference = new LinkedHashMap<>();
    private int saves;

    @Override
    public Optional<Order> find(String reference) {
        return Optional.ofNullable(byReference.get(reference));
    }

    @Override
    public Optional<Order> findByPayment(String payment) {
        return byReference.values().stream()
                .filter(order -> payment.equals(order.payment()))
                .findFirst();
    }

    @Override
    public void save(Order order) {
        saves++;
        byReference.put(order.reference(), order);
    }

    @Override
    public List<Order> all() {
        var all = new ArrayList<>(byReference.values());
        all.sort(Comparator.comparing(Order::placedAt).reversed());
        return all;
    }

    public int saves() {
        return saves;
    }
}
