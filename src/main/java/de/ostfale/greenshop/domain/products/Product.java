package de.ostfale.greenshop.domain.products;

import de.ostfale.greenshop.domain.Money;

import java.util.Objects;

/**
 * Something the shop sells, with the price it is sold at today. The id is Stripe's
 * ({@code prod_...}); a product without a price is not for sale and never becomes one of these.
 */
public record Product(String id, String name, Money price) {

    public Product {
        Objects.requireNonNull(price, "price");
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
    }
}
