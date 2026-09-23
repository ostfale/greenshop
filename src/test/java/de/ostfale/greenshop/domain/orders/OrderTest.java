package de.ostfale.greenshop.domain.orders;

import de.ostfale.greenshop.domain.Money;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

class OrderTest {

    private static final List<OrderLine> TWO_SCARVES = List.of(new OrderLine("Schal", 2));
    private static final Money TOTAL = Money.of(4400, "eur");
    private static final Instant NOW = Instant.parse("2026-09-22T12:00:00Z");

    @Test
    void isPaidAtOnceWhenTheMoneyIsIn() {
        assertThat(placed(true).status()).isEqualTo(OrderStatus.PAID);
    }

    @Test
    void waitsWhenTheMoneyComesLater() {
        assertThat(placed(false).status()).isEqualTo(OrderStatus.AWAITING_PAYMENT);
    }

    @Test
    void aWaitingOrderEndsPaidOrFailed() {
        assertThat(placed(false).paymentSucceeded().status()).isEqualTo(OrderStatus.PAID);
        assertThat(placed(false).paymentFailed().status()).isEqualTo(OrderStatus.FAILED);
    }

    @Test
    void sayingItTwiceChangesNothing() {
        var paid = placed(true);
        var failed = placed(false).paymentFailed();

        assertThat(paid.paymentSucceeded()).isEqualTo(paid);
        assertThat(failed.paymentFailed()).isEqualTo(failed);
    }

    @Test
    void aPaidOrderDoesNotFailAndAFailedOneIsNotPaid() {
        assertThatIllegalStateException().isThrownBy(() -> placed(true).paymentFailed());
        assertThatIllegalStateException().isThrownBy(() -> placed(false).paymentFailed().paymentSucceeded());
    }

    @Test
    void hasAtLeastOneLine() {
        assertThatIllegalArgumentException().isThrownBy(() -> Order.placed("cs_test_1", List.of(), TOTAL, true, NOW));
    }

    private static Order placed(boolean paid) {
        return Order.placed("cs_test_1", TWO_SCARVES, TOTAL, paid, NOW);
    }
}
