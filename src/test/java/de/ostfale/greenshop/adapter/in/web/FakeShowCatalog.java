package de.ostfale.greenshop.adapter.in.web;

import de.ostfale.greenshop.application.port.in.ShowCatalog;
import de.ostfale.greenshop.application.port.out.CatalogUnavailable;
import de.ostfale.greenshop.domain.products.Product;

import java.util.List;

/**
 * The use case as a web test sees it: what the test puts in, or a failure when it says so.
 */
class FakeShowCatalog implements ShowCatalog {

    private List<Product> products = List.of();
    private boolean unavailable;

    void offer(Product... products) {
        this.products = List.of(products);
        this.unavailable = false;
    }

    void goDown() {
        this.unavailable = true;
    }

    @Override
    public List<Product> productsForSale() {
        if (unavailable) {
            throw new CatalogUnavailable("fake catalog is down", null);
        }
        return products;
    }
}
