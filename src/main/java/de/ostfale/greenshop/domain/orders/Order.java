package de.ostfale.greenshop.domain.orders;

import de.ostfale.greenshop.domain.Money;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * A purchase the payment provider has confirmed. It comes into being only through the
 * provider's own message, never through the page the customer returns to. The reference is the
 * provider's id for the checkout; it is also what makes a message that arrives twice harmless.
 * <p>
 * The payment is what the provider charges and gives back on. It is the provider's id as well,
 * and empty as long as a waiting order has none.
 * <p>
 * Lines and total are copies of what was bought, not references to the catalog.
 */
public record Order(String reference, String payment, List<OrderLine> lines, Money total,
                    OrderStatus status, Instant placedAt) {

    public Order {
        if (reference == null || reference.isBlank()) {
            throw new IllegalArgumentException("reference must not be blank");
        }
        lines = List.copyOf(lines);
        if (lines.isEmpty()) {
            throw new IllegalArgumentException("an order has at least one line");
        }
        Objects.requireNonNull(total, "total");
        Objects.requireNonNull(status, "status");
        Objects.requireNonNull(placedAt, "placedAt");
    }

    /**
     * A new order, paid at once or still waiting for its money.
     */
    public static Order placed(String reference, String payment, List<OrderLine> lines, Money total,
                               boolean paid, Instant placedAt) {
        var status = paid ? OrderStatus.PAID : OrderStatus.AWAITING_PAYMENT;
        return new Order(reference, payment, lines, total, status, placedAt);
    }

    /**
     * The money is in. Saying so twice changes nothing; a failed order does not come back, and
     * one that was given back is not paid again.
     */
    public Order paymentSucceeded() {
        return switch (status) {
            case PAID -> this;
            case AWAITING_PAYMENT -> withStatus(OrderStatus.PAID);
            case FAILED -> throw new IllegalStateException("order " + reference + " has failed, it cannot be paid");
            case REFUNDED -> throw new IllegalStateException("order " + reference + " was refunded, it cannot be paid");
        };
    }

    /**
     * The money will not come. Saying so twice changes nothing; a paid order stays paid —
     * taking money back is a refund, not a failure.
     */
    public Order paymentFailed() {
        return switch (status) {
            case FAILED -> this;
            case AWAITING_PAYMENT -> withStatus(OrderStatus.FAILED);
            case PAID, REFUNDED -> throw new IllegalStateException("order " + reference + " is paid, it cannot fail");
        };
    }

    /**
     * The money went back. Only a paid order can be given back, and giving it back twice is the
     * same as once — the second message from the provider says nothing new.
     */
    public Order refunded() {
        return switch (status) {
            case REFUNDED -> this;
            case PAID -> withStatus(OrderStatus.REFUNDED);
            case AWAITING_PAYMENT, FAILED ->
                    throw new IllegalStateException("order " + reference + " is not paid, there is nothing to give back");
        };
    }

    private Order withStatus(OrderStatus newStatus) {
        return new Order(reference, payment, lines, total, newStatus, placedAt);
    }
}
