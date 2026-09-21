package de.ostfale.greenshop.application.port.out;

import de.ostfale.greenshop.domain.products.Product;

import java.util.List;

/**
 * What the shop has for sale. Where the catalog is kept is the adapter's business.
 */
public interface ProductCatalog {

    /**
     * Every product that is active and has a price, in the order the catalog keeps them.
     *
     * @throws CatalogUnavailable if the catalog cannot be read
     */
    List<Product> productsForSale();
}
