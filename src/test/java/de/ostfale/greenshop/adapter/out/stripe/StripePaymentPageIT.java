package de.ostfale.greenshop.adapter.out.stripe;

import com.stripe.StripeClient;
import de.ostfale.greenshop.application.port.out.ProductNotForSale;
import de.ostfale.greenshop.config.ShopProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Talks to the real Stripe sandbox, like {@link StripeProductCatalogIT}. A session it opens
 * is never paid and expires by itself after a day.
 */
class StripePaymentPageIT {

    private StripeProductCatalog catalog;
    private StripePaymentPage paymentPage;

    @BeforeEach
    void connectToTheSandbox() {
        var key = System.getenv("STRIPE_SECRET_KEY");
        assumeTrue(key != null && key.startsWith("sk_test_"), "STRIPE_SECRET_KEY is not set in this run configuration");
        var stripe = new StripeClient(key);
        catalog = new StripeProductCatalog(stripe);
        paymentPage = new StripePaymentPage(stripe, new ShopProperties(URI.create("http://localhost:8484")));
    }

    @Test
    void opensStripesCheckoutForAProductOfTheCatalog() {
        var product = catalog.productsForSale().getFirst();

        var address = paymentPage.open(product.id());

        assertThat(address.getHost()).isEqualTo("checkout.stripe.com");
    }

    @Test
    void refusesAProductThatDoesNotExist() {
        assertThatExceptionOfType(ProductNotForSale.class)
                .isThrownBy(() -> paymentPage.open("prod_doesnotexist"));
    }
}
