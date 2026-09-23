package de.ostfale.greenshop.domain.orders;

import java.util.Objects;

/**
 * One line of an order, copied at the moment of purchase: the name the product was sold under
 * then, and how many. Renaming the product later does not rewrite what was bought.
 */
public record OrderLine(String name, long quantity) {

    public OrderLine {
        Objects.requireNonNull(name, "name");
        if (quantity < 1) {
            throw new IllegalArgumentException("quantity must be at least 1: " + quantity);
        }
    }
}
