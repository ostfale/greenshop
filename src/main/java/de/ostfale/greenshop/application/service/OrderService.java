package de.ostfale.greenshop.application.service;

import de.ostfale.greenshop.application.port.in.ConfirmPayment;
import de.ostfale.greenshop.application.port.in.PaymentNotification;
import de.ostfale.greenshop.application.port.in.ShowOrders;
import de.ostfale.greenshop.application.port.out.CheckoutSummary;
import de.ostfale.greenshop.application.port.out.HandledMessages;
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
import java.util.Optional;
import java.util.function.UnaryOperator;

/**
 * Turns the provider's messages into orders. A message only names the checkout; what was
 * bought is looked up fresh, so the order does not depend on how the message was put.
 * <p>
 * Nothing here may happen twice. Three things see to that, and each covers what the others do
 * not: a message already dealt with is dropped by its id, an order is found again by the
 * reference of its checkout, and a status change that has already happened changes nothing.
 * A message is only written down as handled once it is through — one that failed halfway is
 * meant to come again.
 * <p>
 * Messages also arrive out of order: a late payment can be reported before the checkout
 * itself. Whichever comes first places the order, the rest only move its status.
 */
@Service
class OrderService implements ConfirmPayment, ShowOrders {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final PaymentPage paymentPage;
    private final Orders orders;
    private final HandledMessages handledMessages;
    private final Clock clock;

    OrderService(PaymentPage paymentPage, Orders orders, HandledMessages handledMessages, Clock clock) {
        this.paymentPage = paymentPage;
        this.orders = orders;
        this.handledMessages = handledMessages;
        this.clock = clock;
    }

    @Override
    public void checkoutCompleted(PaymentNotification notification) {
        handle(notification, order -> order);
    }

    @Override
    public void paymentSucceeded(PaymentNotification notification) {
        handle(notification, Order::paymentSucceeded);
    }

    @Override
    public void paymentFailed(PaymentNotification notification) {
        handle(notification, Order::paymentFailed);
    }

    @Override
    public List<Order> orders() {
        return orders.all();
    }

    private void handle(PaymentNotification notification, UnaryOperator<Order> verdict) {
        if (handledMessages.alreadyHandled(notification.messageId())) {
            log.debug("OrderService :: message {} was handled before", notification.messageId());
            return;
        }
        var reference = notification.reference();
        var order = orders.find(reference).or(() -> place(reference));
        if (order.isEmpty()) {
            log.debug("OrderService :: checkout {} is not ours, message dropped", reference);
            return;
        }
        var settled = verdict.apply(order.get());
        orders.save(settled);
        handledMessages.handled(notification.messageId());
        log.info("OrderService :: order {} is {}", settled.reference(), settled.status());
    }

    /**
     * A checkout the provider does not know, or one that belongs to another shop, leaves no
     * order behind. The new order is not saved here — the one save is in {@link #handle}.
     */
    private Optional<Order> place(String reference) {
        return paymentPage.find(reference).map(checkout ->
                Order.placed(reference, lines(checkout), checkout.total(), checkout.paid(), Instant.now(clock)));
    }

    private static List<OrderLine> lines(CheckoutSummary checkout) {
        return checkout.items().stream()
                .map(item -> new OrderLine(item.name(), item.quantity()))
                .toList();
    }
}
