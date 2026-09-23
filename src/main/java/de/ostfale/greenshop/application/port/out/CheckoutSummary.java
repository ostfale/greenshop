package de.ostfale.greenshop.application.port.out;

import de.ostfale.greenshop.domain.Money;

import java.util.List;
import java.util.Objects;

/**
 * What a checkout holds, as the payment provider reports it right now: what was bought and how
 * many, the total, whether the money is in, and which payment it went through. For showing, not
 * for deciding — whether an order is paid is settled by the provider's own message, not by a
 * page somebody reloads.
 */
public record CheckoutSummary(List<Item> items, Money total, boolean paid, String payment) {

    public CheckoutSummary {
        items = List.copyOf(items);
        Objects.requireNonNull(total, "total");
    }

    /**
     * One line of the checkout: the name it was sold under and how many were taken.
     */
    public record Item(String name, long quantity) {

        public Item {
            Objects.requireNonNull(name, "name");
            if (quantity < 1) {
                throw new IllegalArgumentException("quantity must be at least 1: " + quantity);
            }
        }
    }
}
