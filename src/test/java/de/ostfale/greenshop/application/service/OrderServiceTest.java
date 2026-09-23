package de.ostfale.greenshop.application.service;

import de.ostfale.greenshop.application.port.in.PaymentNotification;
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

class OrderServiceTest {

    private static final Instant NOW = Instant.parse("2026-09-23T12:00:00Z");
    private static final List<CheckoutSummary.Item> TWO_SCARVES = List.of(new CheckoutSummary.Item("Schal", 2));
    private static final Money FORTY_FOUR_EURO = Money.of(4400, "eur");

    private final FakePaymentPage paymentPage = new FakePaymentPage();
    private final FakeOrders orders = new FakeOrders();
    private final FakeHandledMessages handledMessages = new FakeHandledMessages();
    private final OrderService service = new OrderService(paymentPage, orders, handledMessages,
            Clock.fixed(NOW, ZoneId.of("Europe/Berlin")));

    @Test
    void placesAPaidOrderWithWhatWasBought() {
        checkoutIs(true);

        service.checkoutCompleted(new PaymentNotification("evt_1", "cs_test_1"));

        var order = orders.find("cs_test_1").orElseThrow();
        assertThat(order.status()).isEqualTo(OrderStatus.PAID);
        assertThat(order.lines()).containsExactly(new OrderLine("Schal", 2));
        assertThat(order.total()).isEqualTo(FORTY_FOUR_EURO);
        assertThat(order.placedAt()).isEqualTo(NOW);
    }

    @Test
    void placesAWaitingOrderWhenTheMoneyComesLater() {
        checkoutIs(false);

        service.checkoutCompleted(new PaymentNotification("evt_1", "cs_test_1"));

        assertThat(orders.find("cs_test_1").orElseThrow().status()).isEqualTo(OrderStatus.AWAITING_PAYMENT);
    }

    @Test
    void theSameMessageTwiceIsDealtWithOnce() {
        checkoutIs(true);

        service.checkoutCompleted(new PaymentNotification("evt_1", "cs_test_1"));
        service.checkoutCompleted(new PaymentNotification("evt_1", "cs_test_1"));

        assertThat(service.orders()).hasSize(1);
        assertThat(orders.saves()).isEqualTo(1);
    }

    @Test
    void anotherMessageAboutTheSameCheckoutPlacesNoSecondOrder() {
        checkoutIs(true);

        service.checkoutCompleted(new PaymentNotification("evt_1", "cs_test_1"));
        service.checkoutCompleted(new PaymentNotification("evt_2", "cs_test_1"));

        assertThat(service.orders()).hasSize(1);
    }

    @Test
    void aLatePaymentMovesTheWaitingOrder() {
        checkoutIs(false);
        service.checkoutCompleted(new PaymentNotification("evt_1", "cs_test_1"));

        service.paymentSucceeded(new PaymentNotification("evt_2", "cs_test_1"));

        assertThat(orders.find("cs_test_1").orElseThrow().status()).isEqualTo(OrderStatus.PAID);
    }

    @Test
    void aPaymentReportedBeforeItsCheckoutStillPlacesTheOrder() {
        checkoutIs(false);

        service.paymentFailed(new PaymentNotification("evt_1", "cs_test_1"));
        service.checkoutCompleted(new PaymentNotification("evt_2", "cs_test_1"));

        assertThat(service.orders()).hasSize(1);
        assertThat(orders.find("cs_test_1").orElseThrow().status()).isEqualTo(OrderStatus.FAILED);
    }

    @Test
    void dropsAMessageAboutACheckoutOfAnotherShop() {
        service.checkoutCompleted(new PaymentNotification("evt_1", "cs_test_somebody_else"));

        assertThat(service.orders()).isEmpty();
    }

    @Test
    void writesAMessageDownOnlyOnceItIsThrough() {
        service.checkoutCompleted(new PaymentNotification("evt_1", "cs_test_somebody_else"));

        assertThat(handledMessages.ids()).isEmpty();
    }

    private void checkoutIs(boolean paid) {
        paymentPage.knowing("cs_test_1", new CheckoutSummary(TWO_SCARVES, FORTY_FOUR_EURO, paid));
    }
}
