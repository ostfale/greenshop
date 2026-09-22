package de.ostfale.greenshop.adapter.in.web;

import de.ostfale.greenshop.application.port.out.CheckoutSummary;
import de.ostfale.greenshop.domain.Money;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CheckoutController.class)
@Import({FakePurchase.class, Prices.class})
class CheckoutControllerTest {

    private static final CheckoutSummary.Item SCARVES = new CheckoutSummary.Item("Schal", 2);

    @Autowired
    private MockMvc mvc;

    @Autowired
    private FakePurchase purchase;

    @Test
    void sendsTheBuyerToThePaymentPageWithSeeOther() throws Exception {
        purchase.selling("prod_scarf");

        mvc.perform(post("/checkout").param("productId", "prod_scarf"))
                .andExpect(status().isSeeOther())
                .andExpect(header().string("Location", "https://pay.example.org/prod_scarf"));
    }

    @Test
    void refusesAProductThatIsNotForSale() throws Exception {
        purchase.selling();

        var page = page(post("/checkout").param("productId", "prod_gone"), 404);

        assertThat(page.select("#notForSale")).hasSize(1);
    }

    @Test
    void saysSoWhenThePaymentIsDown() throws Exception {
        purchase.goDown();

        var page = page(post("/checkout").param("productId", "prod_scarf"), 503);

        assertThat(page.select("#unavailable")).hasSize(1);
    }

    @Test
    void showsWhatWasBoughtHowManyAndWhetherItIsPaid() throws Exception {
        purchase.knowing("cs_test_1", new CheckoutSummary(List.of(SCARVES), Money.of(4400, "eur"), true));

        var page = page(get("/checkout/success").param("session_id", "cs_test_1"), 200);

        assertThat(page.select("#purchase .item").text()).isEqualTo("Schal");
        assertThat(page.select("#purchase .quantity").text()).isEqualTo("2 ×");
        assertThat(page.select(".total .amount").text().replace(' ', ' ')).isEqualTo("44,00 €");
        assertThat(page.select("#paid")).hasSize(1);
        assertThat(page.select("#open")).isEmpty();
    }

    @Test
    void saysSoWhenThePaymentIsNotInYet() throws Exception {
        purchase.knowing("cs_test_2", new CheckoutSummary(List.of(SCARVES), Money.of(4400, "eur"), false));

        var page = page(get("/checkout/success").param("session_id", "cs_test_2"), 200);

        assertThat(page.select("#open")).hasSize(1);
    }

    @Test
    void knowsNothingOfAnUnknownReference() throws Exception {
        var page = page(get("/checkout/success").param("session_id", "cs_test_unknown"), 404);

        assertThat(page.select("#unknown")).hasSize(1);
    }

    @Test
    void goesBackToTheCatalogWhenTheBuyerCancels() throws Exception {
        mvc.perform(get("/checkout/cancel"))
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attribute("cancelled", true));
    }

    private Document page(MockHttpServletRequestBuilder request, int expectedStatus) throws Exception {
        var html = mvc.perform(request)
                .andExpect(status().is(expectedStatus))
                .andReturn().getResponse().getContentAsString();
        return Jsoup.parse(html);
    }
}
