package de.ostfale.greenshop.application.service;

import de.ostfale.greenshop.application.port.out.CatalogUnavailable;
import de.ostfale.greenshop.application.port.out.ProductCatalog;
import de.ostfale.greenshop.domain.products.Product;

import java.util.List;

/**
 * A catalog that holds what a test puts in, or fails when a test says so.
 */
public class FakeProductCatalog implements ProductCatalog {

    private List<Product> products = List.of();
    private boolean unavailable;

    public FakeProductCatalog with(Product... products) {
        this.products = List.of(products);
        return this;
    }

    public FakeProductCatalog unavailable() {
        this.unavailable = true;
        return this;
    }

    @Override
    public List<Product> productsForSale() {
        if (unavailable) {
            throw new CatalogUnavailable("fake catalog is down", null);
        }
        return products;
    }
}
