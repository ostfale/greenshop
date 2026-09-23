package de.ostfale.greenshop.application.service;

import de.ostfale.greenshop.application.port.out.CheckoutSummary;
import de.ostfale.greenshop.application.port.out.PaymentPage;
import de.ostfale.greenshop.application.port.out.ProductNotForSale;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * A payment page that knows the products a test declares for sale and the checkouts a test
 * puts in. Opening one answers with a made-up address and writes the attempt down, so a test
 * can see how often it was really opened.
 */
public class FakePaymentPage implements PaymentPage {

    private Set<String> forSale = Set.of();
    private final Map<String, CheckoutSummary> checkouts = new HashMap<>();
    private final List<UUID> attempts = new ArrayList<>();

    public FakePaymentPage selling(String... productIds) {
        this.forSale = Set.of(productIds);
        return this;
    }

    public FakePaymentPage knowing(String reference, CheckoutSummary checkout) {
        checkouts.put(reference, checkout);
        return this;
    }

    public List<UUID> attempts() {
        return attempts;
    }

    @Override
    public URI open(String productId, UUID attempt) {
        if (!forSale.contains(productId)) {
            throw new ProductNotForSale(productId);
        }
        attempts.add(attempt);
        return URI.create("https://pay.example.org/" + productId);
    }

    @Override
    public Optional<CheckoutSummary> find(String reference) {
        return Optional.ofNullable(checkouts.get(reference));
    }
}
