package de.ostfale.greenshop.application.service;

import de.ostfale.greenshop.application.port.out.CheckoutSummary;
import de.ostfale.greenshop.application.port.out.ProductNotForSale;
import de.ostfale.greenshop.domain.Money;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class PurchaseServiceTest {

    private static final UUID ATTEMPT = UUID.fromString("6f1b2a3c-0000-4000-8000-000000000001");

    private final FakePaymentPage paymentPage = new FakePaymentPage().selling("prod_scarf");
    private final PurchaseService service = new PurchaseService(paymentPage);

    @Test
    void sendsTheCustomerToThePaymentPage() {
        assertThat(service.start("prod_scarf", ATTEMPT))
                .isEqualTo(URI.create("https://pay.example.org/prod_scarf"));
        assertThat(paymentPage.attempts()).containsExactly(ATTEMPT);
    }

    @Test
    void letsARefusalThrough() {
        assertThatExceptionOfType(ProductNotForSale.class)
                .isThrownBy(() -> service.start("prod_gone", ATTEMPT));
    }

    @Test
    void findsTheCheckoutTheCustomerCameBackFrom() {
        var checkout = new CheckoutSummary(List.of(new CheckoutSummary.Item("Schal", 1)), Money.of(2200, "eur"), true);
        paymentPage.knowing("cs_test_1", checkout);

        assertThat(service.purchase("cs_test_1")).contains(checkout);
        assertThat(service.purchase("cs_test_unknown")).isEmpty();
    }
}
