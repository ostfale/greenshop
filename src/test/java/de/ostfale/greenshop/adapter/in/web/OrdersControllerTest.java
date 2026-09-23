package de.ostfale.greenshop.adapter.in.web;

import de.ostfale.greenshop.application.port.in.ShowOrders;
import de.ostfale.greenshop.domain.Money;
import de.ostfale.greenshop.domain.orders.Order;
import de.ostfale.greenshop.domain.orders.OrderLine;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrdersController.class)
@Import({Prices.class, Dates.class, OrdersControllerTest.Fixtures.class})
class OrdersControllerTest {

    private static final Instant NOON_UTC = Instant.parse("2026-09-22T12:05:00Z");
    private static final List<OrderLine> ONE_SCARF = List.of(new OrderLine("Schal", 1));
    private static final Money TWENTY_TWO_EURO = Money.of(2200, "eur");

    @TestConfiguration
    static class Fixtures {

        @Bean
        List<Order> orders() {
            return new ArrayList<>();
        }

        @Bean
        ShowOrders showOrders(List<Order> orders) {
            return () -> orders;
        }

        @Bean
        FakeRefundPurchase refundPurchase() {
            return new FakeRefundPurchase();
        }

        @Bean
        Clock clock() {
            return Clock.fixed(NOON_UTC, ZoneId.of("Europe/Berlin"));
        }
    }

    @Autowired
    private MockMvc mvc;

    @Autowired
    private List<Order> orders;

    @Autowired
    private FakeRefundPurchase refundPurchase;

    @BeforeEach
    void startClean() {
        orders.clear();
        refundPurchase.reset();
    }

    @Test
    void listsTheOrdersWithLinesTotalAndStatus() throws Exception {
        orders.add(Order.placed("cs_test_1", "pi_test_1",
                List.of(new OrderLine("Schal", 2), new OrderLine("Kaffeebecher", 1)),
                Money.of(5900, "eur"), true, NOON_UTC));
        orders.add(Order.placed("cs_test_2", "pi_test_2", List.of(new OrderLine("Schal", 1)),
                Money.of(2200, "eur"), false, NOON_UTC));

        var rows = page().select("#orders tbody tr");

        assertThat(rows).hasSize(2);
        var first = rows.first();
        assertThat(first.attr("data-reference")).isEqualTo("cs_test_1");
        assertThat(first.select(".placed").text()).isEqualTo("22.09.2026, 14:05");
        assertThat(first.select(".lines").text()).isEqualTo("2 × Schal, 1 × Kaffeebecher");
        assertThat(first.select(".total").text().replace(' ', ' ')).isEqualTo("59,00 €");
        assertThat(first.select(".status").text()).isEqualTo("bezahlt");
        assertThat(rows.last().select(".status").text()).isEqualTo("wartet auf Zahlung");
    }

    @Test
    void offersTheRefundOnlyWhereThereIsMoneyToGiveBack() throws Exception {
        orders.add(paid("cs_test_1", "pi_test_1"));
        orders.add(Order.placed("cs_test_2", "pi_test_2", ONE_SCARF, TWENTY_TWO_EURO, false, NOON_UTC));
        orders.add(paid("cs_test_3", "pi_test_3").refunded());

        var rows = page().select("#orders tbody tr");

        assertThat(rows.get(0).select("form").attr("action")).isEqualTo("/orders/cs_test_1/refund");
        assertThat(rows.get(1).select("form")).isEmpty();
        assertThat(rows.get(2).select("form")).isEmpty();
        assertThat(rows.get(2).select(".status").text()).isEqualTo("erstattet");
    }

    @Test
    void saysWhatWentBackAfterARefund() throws Exception {
        orders.add(paid("cs_test_1", "pi_test_1"));

        var html = mvc.perform(post("/orders/cs_test_1/refund"))
                .andExpect(redirectedUrl("/orders"))
                .andExpect(flash().attributeExists("refunded"))
                .andReturn();

        assertThat(refundPurchase.refunded()).containsExactly("cs_test_1");
        assertThat(html.getResponse().getStatus()).isEqualTo(302);
    }

    @Test
    void saysSoWhenTheRefundIsRefused() throws Exception {
        orders.add(paid("cs_test_1", "pi_test_1"));
        refundPurchase.refuse();

        mvc.perform(post("/orders/cs_test_1/refund"))
                .andExpect(redirectedUrl("/orders"))
                .andExpect(flash().attribute("refundRefused", true));
    }

    private static Order paid(String reference, String payment) {
        return Order.placed(reference, payment, ONE_SCARF, TWENTY_TWO_EURO, true, NOON_UTC);
    }

    @Test
    void saysSoWhenNothingWasSoldYet() throws Exception {
        orders.clear();

        assertThat(page().select("#empty")).hasSize(1);
    }

    private Document page() throws Exception {
        var html = mvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return Jsoup.parse(html);
    }
}
