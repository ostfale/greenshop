package de.ostfale.greenshop.adapter.out.stripe;

import com.stripe.StripeClient;
import de.ostfale.greenshop.application.port.out.ProductNotForSale;
import de.ostfale.greenshop.config.ShopProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.UUID;

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

        var address = paymentPage.open(product.id(), UUID.randomUUID());

        assertThat(address.getHost()).isEqualTo("checkout.stripe.com");
    }

    /**
     * The point of the idempotency key: the second call opens no second checkout, Stripe
     * answers it with the session of the first.
     */
    @Test
    void theSameAttemptTwiceOpensOneCheckout() {
        var product = catalog.productsForSale().getFirst();
        var attempt = UUID.randomUUID();

        var first = paymentPage.open(product.id(), attempt);
        var second = paymentPage.open(product.id(), attempt);

        assertThat(second).isEqualTo(first);
    }

    @Test
    void anotherAttemptOpensAnotherCheckout() {
        var product = catalog.productsForSale().getFirst();

        var first = paymentPage.open(product.id(), UUID.randomUUID());
        var second = paymentPage.open(product.id(), UUID.randomUUID());

        assertThat(second).isNotEqualTo(first);
    }

    @Test
    void knowsNothingOfASessionThatDoesNotExist() {
        assertThat(paymentPage.find("cs_test_doesnotexist")).isEmpty();
    }

    @Test
    void refusesAProductThatDoesNotExist() {
        assertThatExceptionOfType(ProductNotForSale.class)
                .isThrownBy(() -> paymentPage.open("prod_doesnotexist", UUID.randomUUID()));
    }
}
