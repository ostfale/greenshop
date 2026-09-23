package de.ostfale.greenshop.application.service;

import de.ostfale.greenshop.application.port.in.ConfirmPayment;
import de.ostfale.greenshop.application.port.in.ShowOrders;
import de.ostfale.greenshop.application.port.out.CheckoutSummary;
import de.ostfale.greenshop.application.port.out.Orders;
import de.ostfale.greenshop.application.port.out.PaymentPage;
import de.ostfale.greenshop.domain.orders.Order;
import de.ostfale.greenshop.domain.orders.OrderLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

/**
 * Turns the provider's messages into orders. The message only names the checkout; what was
 * bought is looked up fresh, so the order does not depend on how the message was put.
 * <p>
 * Messages arrive more than once and not necessarily in order: a late payment can be reported
 * before the checkout itself. Whichever comes first places the order, the rest only move its
 * status.
 */
@Service
class OrderService implements ConfirmPayment, ShowOrders {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final PaymentPage paymentPage;
    private final Orders orders;
    private final Clock clock;

    OrderService(PaymentPage paymentPage, Orders orders, Clock clock) {
        this.paymentPage = paymentPage;
        this.orders = orders;
        this.clock = clock;
    }

    @Override
    public void checkoutCompleted(String reference) {
        if (orders.find(reference).isPresent()) {
            log.debug("OrderService :: order {} already placed, message ignored", reference);
            return;
        }
        place(reference);
    }

    @Override
    public void paymentSucceeded(String reference) {
        var order = orders.find(reference).orElseGet(() -> place(reference));
        save(order.paymentSucceeded());
    }

    @Override
    public void paymentFailed(String reference) {
        var order = orders.find(reference).orElseGet(() -> place(reference));
        save(order.paymentFailed());
    }

    @Override
    public List<Order> orders() {
        return orders.all();
    }

    private Order place(String reference) {
        var checkout = paymentPage.find(reference)
                .orElseThrow(() -> new IllegalStateException("checkout " + reference + " is unknown to the provider"));
        var order = Order.placed(reference, lines(checkout), checkout.total(), checkout.paid(), Instant.now(clock));
        save(order);
        return order;
    }

    private void save(Order order) {
        orders.save(order);
        log.info("OrderService :: order {} is {}", order.reference(), order.status());
    }

    private static List<OrderLine> lines(CheckoutSummary checkout) {
        return checkout.items().stream()
                .map(item -> new OrderLine(item.name(), item.quantity()))
                .toList();
    }
}
