package de.ostfale.greenshop.adapter.in.web;

import de.ostfale.greenshop.application.port.in.RefundPurchase;
import de.ostfale.greenshop.application.port.out.RefundRefused;
import de.ostfale.greenshop.domain.Money;
import de.ostfale.greenshop.domain.orders.Order;
import de.ostfale.greenshop.domain.orders.OrderLine;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Writes down which orders a web test asked back, or refuses when it says so.
 */
class FakeRefundPurchase implements RefundPurchase {

    private final List<String> refunded = new ArrayList<>();
    private boolean refusing;

    List<String> refunded() {
        return refunded;
    }

    void refuse() {
        refusing = true;
    }

    void reset() {
        refunded.clear();
        refusing = false;
    }

    @Override
    public Order refund(String reference) {
        if (refusing) {
            throw new RefundRefused("fake provider refuses to refund " + reference, null);
        }
        refunded.add(reference);
        return Order.placed(reference, "pi_test", List.of(new OrderLine("Schal", 1)),
                Money.of(2200, "eur"), true, Instant.parse("2026-09-22T12:05:00Z")).refunded();
    }
}
