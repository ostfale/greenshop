package de.ostfale.greenshop.adapter.out.memory;

import de.ostfale.greenshop.application.port.out.Orders;
import de.ostfale.greenshop.domain.orders.Order;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The orders in a map, keyed by reference. Concurrent, because Stripe may deliver two
 * messages about the same checkout at the same moment.
 */
@Component
class InMemoryOrders implements Orders {

    private final Map<String, Order> byReference = new ConcurrentHashMap<>();

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
        byReference.put(order.reference(), order);
    }

    @Override
    public List<Order> all() {
        return byReference.values().stream()
                .sorted(Comparator.comparing(Order::placedAt).reversed())
                .toList();
    }
}
