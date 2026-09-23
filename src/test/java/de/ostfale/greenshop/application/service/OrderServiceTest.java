package de.ostfale.greenshop.application.service;

import de.ostfale.greenshop.application.port.out.CheckoutSummary;
import de.ostfale.greenshop.domain.Money;
import de.ostfale.greenshop.domain.orders.OrderLine;
import de.ostfale.greenshop.domain.orders.OrderStatus;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

class OrderServiceTest {

    private static final Instant NOW = Instant.parse("2026-09-22T12:00:00Z");
    private static final List<CheckoutSummary.Item> TWO_SCARVES = List.of(new CheckoutSummary.Item("Schal", 2));

    private final FakePaymentPage paymentPage = new FakePaymentPage();
    private final FakeOrders orders = new FakeOrders();
    private final OrderService service =
            new OrderService(paymentPage, orders, Clock.fixed(NOW, ZoneId.of("Europe/Berlin")));

    @Test
    void placesAPaidOrderWithWhatWasBought() {
        paymentPage.knowing("cs_test_1", new CheckoutSummary(TWO_SCARVES, Money.of(4400, "eur"), true));

        service.checkoutCompleted("cs_test_1");

        var order = orders.find("cs_test_1").orElseThrow();
        assertThat(order.status()).isEqualTo(OrderStatus.PAID);
        assertThat(order.lines()).containsExactly(new OrderLine("Schal", 2));
        assertThat(order.total()).isEqualTo(Money.of(4400, "eur"));
        assertThat(order.placedAt()).isEqualTo(NOW);
    }

    @Test
    void placesAWaitingOrderWhenTheMoneyComesLater() {
        paymentPage.knowing("cs_test_1", new CheckoutSummary(TWO_SCARVES, Money.of(4400, "eur"), false));

        service.checkoutCompleted("cs_test_1");

        assertThat(orders.find("cs_test_1").orElseThrow().status()).isEqualTo(OrderStatus.AWAITING_PAYMENT);
    }

    @Test
    void aMessageThatArrivesTwicePlacesOneOrder() {
        paymentPage.knowing("cs_test_1", new CheckoutSummary(TWO_SCARVES, Money.of(4400, "eur"), true));

        service.checkoutCompleted("cs_test_1");
        service.checkoutCompleted("cs_test_1");

        assertThat(service.orders()).hasSize(1);
        assertThat(orders.saves()).isEqualTo(1);
    }

    @Test
    void aLatePaymentMovesTheWaitingOrder() {
        paymentPage.knowing("cs_test_1", new CheckoutSummary(TWO_SCARVES, Money.of(4400, "eur"), false));
        service.checkoutCompleted("cs_test_1");

        service.paymentSucceeded("cs_test_1");

        assertThat(orders.find("cs_test_1").orElseThrow().status()).isEqualTo(OrderStatus.PAID);
    }

    @Test
    void aPaymentReportedBeforeItsCheckoutStillPlacesTheOrder() {
        paymentPage.knowing("cs_test_1", new CheckoutSummary(TWO_SCARVES, Money.of(4400, "eur"), false));

        service.paymentFailed("cs_test_1");
        service.checkoutCompleted("cs_test_1");

        assertThat(service.orders()).hasSize(1);
        assertThat(orders.find("cs_test_1").orElseThrow().status()).isEqualTo(OrderStatus.FAILED);
    }

    @Test
    void refusesACheckoutTheProviderDoesNotKnow() {
        assertThatIllegalStateException().isThrownBy(() -> service.checkoutCompleted("cs_test_unknown"));
        assertThat(service.orders()).isEmpty();
    }
}
