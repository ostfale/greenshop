package de.ostfale.greenshop.application.service;

import de.ostfale.greenshop.application.port.out.CatalogUnavailable;
import de.ostfale.greenshop.domain.Money;
import de.ostfale.greenshop.domain.products.Product;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class CatalogServiceTest {

    private static final Product SCARF = new Product("prod_scarf", "Schal", Money.of(2200, "eur"));
    private static final Product MUG = new Product("prod_mug", "Kaffeebecher", Money.of(1500, "eur"));

    @Test
    void offersWhatTheCatalogHasInItsOrder() {
        var service = new CatalogService(new FakeProductCatalog().with(SCARF, MUG));

        assertThat(service.productsForSale()).containsExactly(SCARF, MUG);
    }

    @Test
    void letsAnUnavailableCatalogThrough() {
        var service = new CatalogService(new FakeProductCatalog().unavailable());

        assertThatExceptionOfType(CatalogUnavailable.class).isThrownBy(service::productsForSale);
    }
}
