package de.ostfale.greenshop.adapter.in.web;

import de.ostfale.greenshop.application.port.in.ShowPurchase;
import de.ostfale.greenshop.application.port.in.StartPurchase;
import de.ostfale.greenshop.application.port.out.CheckoutSummary;
import de.ostfale.greenshop.application.port.out.PaymentUnavailable;
import de.ostfale.greenshop.application.port.out.ProductNotForSale;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Both purchase use cases as a web test sees them.
 */
class FakePurchase implements StartPurchase, ShowPurchase {

    private Set<String> forSale = Set.of();
    private final Map<String, CheckoutSummary> purchases = new HashMap<>();
    private boolean unavailable;
    private UUID lastAttempt;

    UUID lastAttempt() {
        return lastAttempt;
    }

    void selling(String... productIds) {
        this.forSale = Set.of(productIds);
        this.unavailable = false;
    }

    void knowing(String reference, CheckoutSummary purchase) {
        purchases.put(reference, purchase);
    }

    void goDown() {
        this.unavailable = true;
    }

    @Override
    public URI start(String productId, UUID attempt) {
        lastAttempt = attempt;
        if (unavailable) {
            throw new PaymentUnavailable("fake payment is down", null);
        }
        if (!forSale.contains(productId)) {
            throw new ProductNotForSale(productId);
        }
        return URI.create("https://pay.example.org/" + productId);
    }

    @Override
    public Optional<CheckoutSummary> purchase(String reference) {
        return Optional.ofNullable(purchases.get(reference));
    }
}
