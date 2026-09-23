package de.ostfale.greenshop.application.service;

import de.ostfale.greenshop.application.port.in.ShowPurchase;
import de.ostfale.greenshop.application.port.in.StartPurchase;
import de.ostfale.greenshop.application.port.out.CheckoutSummary;
import de.ostfale.greenshop.application.port.out.PaymentPage;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;

/**
 * Hands the purchase through to the payment page. The order comes into being elsewhere, when
 * the provider confirms the payment.
 */
@Service
class PurchaseService implements StartPurchase, ShowPurchase {

    private final PaymentPage paymentPage;

    PurchaseService(PaymentPage paymentPage) {
        this.paymentPage = paymentPage;
    }

    @Override
    public URI start(String productId, UUID attempt) {
        return paymentPage.open(productId, attempt);
    }

    @Override
    public Optional<CheckoutSummary> purchase(String reference) {
        return paymentPage.find(reference);
    }
}
