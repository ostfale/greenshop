package de.ostfale.greenshop.application.service;

import de.ostfale.greenshop.application.port.in.ShowPurchase;
import de.ostfale.greenshop.application.port.in.StartPurchase;
import de.ostfale.greenshop.application.port.out.CheckoutSummary;
import de.ostfale.greenshop.application.port.out.PaymentPage;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.Optional;

/**
 * Hands the purchase through to the payment page. Once the payment is confirmed by webhook
 * (step 5) this is where an order comes into being.
 */
@Service
class PurchaseService implements StartPurchase, ShowPurchase {

    private final PaymentPage paymentPage;

    PurchaseService(PaymentPage paymentPage) {
        this.paymentPage = paymentPage;
    }

    @Override
    public URI start(String productId) {
        return paymentPage.open(productId);
    }

    @Override
    public Optional<CheckoutSummary> purchase(String reference) {
        return paymentPage.find(reference);
    }
}
