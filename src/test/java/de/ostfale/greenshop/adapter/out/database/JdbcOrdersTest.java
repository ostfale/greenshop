package de.ostfale.greenshop.adapter.out.database;

import de.ostfale.greenshop.domain.Money;
import de.ostfale.greenshop.domain.orders.Order;
import de.ostfale.greenshop.domain.orders.OrderLine;
import de.ostfale.greenshop.domain.orders.OrderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.context.annotation.Import;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Against a real H2, with the schema Flyway builds — the same migration the application runs.
 */
@JdbcTest(properties = "spring.datasource.url=jdbc:h2:mem:orders;DB_CLOSE_DELAY=-1")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ImportAutoConfiguration(FlywayAutoConfiguration.class)
@Import(JdbcOrders.class)
class JdbcOrdersTest {

    private static final Instant NOON = Instant.parse("2026-09-23T12:00:00Z");

    @Autowired
    private JdbcOrders orders;

    @Test
    void keepsAnOrderWithItsLinesInTheirOrder() {
        orders.save(order("cs_test_1", "pi_test_1", OrderStatus.PAID, NOON));

        var found = orders.find("cs_test_1").orElseThrow();

        assertThat(found.payment()).isEqualTo("pi_test_1");
        assertThat(found.total()).isEqualTo(Money.of(5900, "eur"));
        assertThat(found.status()).isEqualTo(OrderStatus.PAID);
        assertThat(found.placedAt()).isEqualTo(NOON);
        assertThat(found.lines())
                .containsExactly(new OrderLine("Schal", 2), new OrderLine("Kaffeebecher", 1));
    }

    @Test
    void savingAgainReplacesTheOrderInsteadOfAddingOne() {
        var order = order("cs_test_2", "pi_test_2", OrderStatus.PAID, NOON);
        orders.save(order);

        orders.save(order.refunded());

        assertThat(orders.find("cs_test_2").orElseThrow().status()).isEqualTo(OrderStatus.REFUNDED);
        assertThat(orders.find("cs_test_2").orElseThrow().lines()).hasSize(2);
    }

    @Test
    void findsAnOrderByThePaymentItWentThrough() {
        orders.save(order("cs_test_3", "pi_test_3", OrderStatus.PAID, NOON));

        assertThat(orders.findByPayment("pi_test_3")).map(Order::reference).contains("cs_test_3");
        assertThat(orders.findByPayment("pi_nobody")).isEmpty();
    }

    @Test
    void listsTheNewestFirst() {
        orders.save(order("cs_test_4", "pi_test_4", OrderStatus.PAID, NOON));
        orders.save(order("cs_test_5", "pi_test_5", OrderStatus.PAID, NOON.plusSeconds(60)));

        assertThat(orders.all()).map(Order::reference).startsWith("cs_test_5");
    }

    @Test
    void knowsNothingOfAReferenceItNeverSaw() {
        assertThat(orders.find("cs_test_nothing")).isEmpty();
    }

    private static Order order(String reference, String payment, OrderStatus status, Instant placedAt) {
        var lines = List.of(new OrderLine("Schal", 2), new OrderLine("Kaffeebecher", 1));
        return new Order(reference, payment, lines, Money.of(5900, "eur"), status, placedAt);
    }
}
