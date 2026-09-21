package de.ostfale.greenshop.application.service;

import de.ostfale.greenshop.application.port.in.ShowCatalog;
import de.ostfale.greenshop.application.port.out.ProductCatalog;
import de.ostfale.greenshop.domain.products.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Hands the catalog through for now. It gets something to decide once a product becomes
 * an order.
 */
@Service
class CatalogService implements ShowCatalog {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final ProductCatalog catalog;

    CatalogService(ProductCatalog catalog) {
        this.catalog = catalog;
    }

    @Override
    public List<Product> productsForSale() {
        var result = catalog.productsForSale();
        log.info("CatalogService :: Products for sale: {}", result);
        return result;
    }
}
