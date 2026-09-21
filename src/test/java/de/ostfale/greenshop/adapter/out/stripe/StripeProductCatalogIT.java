package de.ostfale.greenshop.adapter.out.stripe;

import com.stripe.StripeClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Talks to the real Stripe sandbox. Runs only where STRIPE_SECRET_KEY is set — in IntelliJ,
 * with the key in the run configuration of this test; the Maven build skips it.
 * <p>
 * The client is built only after the key has been checked, so a missing key skips the test
 * with a reason instead of failing it.
 */
class StripeProductCatalogIT {

    private StripeProductCatalog catalog;

    @BeforeEach
    void connectToTheSandbox() {
        var key = System.getenv("STRIPE_SECRET_KEY");
        assumeTrue(key != null && key.startsWith("sk_test_"), "STRIPE_SECRET_KEY is not set in this run configuration");
        catalog = new StripeProductCatalog(new StripeClient(key));
    }

    @Test
    void readsTheProductsOfTheSandbox() {
        var products = catalog.productsForSale();

        assertThat(products).isNotEmpty();
        assertThat(products).allSatisfy(product -> {
            assertThat(product.id()).startsWith("prod_");
            assertThat(product.price().amount()).isPositive();
        });
    }
}
