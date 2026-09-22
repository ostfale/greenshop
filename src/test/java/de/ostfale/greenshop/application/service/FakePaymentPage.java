package de.ostfale.greenshop.application.service;

import de.ostfale.greenshop.application.port.out.CheckoutSummary;
import de.ostfale.greenshop.application.port.out.PaymentPage;
import de.ostfale.greenshop.application.port.out.ProductNotForSale;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * A payment page that knows the products a test declares for sale and the checkouts a test
 * puts in. Opening one answers with a made-up address that names the product.
 */
public class FakePaymentPage implements PaymentPage {

    private Set<String> forSale = Set.of();
    private final Map<String, CheckoutSummary> checkouts = new HashMap<>();

    public FakePaymentPage selling(String... productIds) {
        this.forSale = Set.of(productIds);
        return this;
    }

    public FakePaymentPage knowing(String reference, CheckoutSummary checkout) {
        checkouts.put(reference, checkout);
        return this;
    }

    @Override
    public URI open(String productId) {
        if (!forSale.contains(productId)) {
            throw new ProductNotForSale(productId);
        }
        return URI.create("https://pay.example.org/" + productId);
    }

    @Override
    public Optional<CheckoutSummary> find(String reference) {
        return Optional.ofNullable(checkouts.get(reference));
    }
}
