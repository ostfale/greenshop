package de.ostfale.greenshop.application.service;

import de.ostfale.greenshop.application.port.out.CheckoutSummary;
import de.ostfale.greenshop.application.port.out.ProductNotForSale;
import de.ostfale.greenshop.domain.Money;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class PurchaseServiceTest {

    private final FakePaymentPage paymentPage = new FakePaymentPage().selling("prod_scarf");
    private final PurchaseService service = new PurchaseService(paymentPage);

    @Test
    void sendsTheCustomerToThePaymentPage() {
        assertThat(service.start("prod_scarf")).isEqualTo(URI.create("https://pay.example.org/prod_scarf"));
    }

    @Test
    void letsARefusalThrough() {
        assertThatExceptionOfType(ProductNotForSale.class).isThrownBy(() -> service.start("prod_gone"));
    }

    @Test
    void findsTheCheckoutTheCustomerCameBackFrom() {
        var checkout = new CheckoutSummary(List.of(new CheckoutSummary.Item("Schal", 1)), Money.of(2200, "eur"), true);
        paymentPage.knowing("cs_test_1", checkout);

        assertThat(service.purchase("cs_test_1")).contains(checkout);
        assertThat(service.purchase("cs_test_unknown")).isEmpty();
    }
}
