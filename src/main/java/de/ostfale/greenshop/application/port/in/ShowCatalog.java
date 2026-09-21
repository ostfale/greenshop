package de.ostfale.greenshop.application.port.in;

import de.ostfale.greenshop.application.port.out.CatalogUnavailable;
import de.ostfale.greenshop.domain.products.Product;

import java.util.List;

/**
 * What the shop page offers.
 */
public interface ShowCatalog {

    /**
     * The products for sale, in the order the catalog keeps them.
     *
     * @throws CatalogUnavailable if the catalog cannot be read
     */
    List<Product> productsForSale();
}
