package de.ostfale.greenshop.adapter.in.web;

import de.ostfale.greenshop.domain.Money;
import de.ostfale.greenshop.domain.products.Product;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CatalogController.class)
@Import({FakeShowCatalog.class, Prices.class})
class CatalogControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private FakeShowCatalog catalog;

    @Test
    void listsTheProductsWithGermanPrices() throws Exception {
        catalog.offer(
                new Product("prod_shirt", "T-Shirt weiß", Money.of(3500, "eur")),
                new Product("prod_mug", "Kaffeebecher", Money.of(1500, "eur")));

        var page = page(200);

        var rows = page.select("#products tr");
        assertThat(rows).hasSize(2);
        assertThat(rows.first().attr("data-id")).isEqualTo("prod_shirt");
        assertThat(rows.first().select(".name").text()).isEqualTo("T-Shirt weiß");
        assertThat(normalized(rows.first().select(".price").text())).isEqualTo("35,00 €");
        assertThat(normalized(rows.last().select(".price").text())).isEqualTo("15,00 €");
    }

    @Test
    void saysSoWhenThereIsNothingToBuy() throws Exception {
        catalog.offer();

        var page = page(200);

        assertThat(page.select("#empty")).hasSize(1);
        assertThat(page.select("#products")).isEmpty();
    }

    @Test
    void keepsThePageWhenTheCatalogIsDown() throws Exception {
        catalog.goDown();

        var page = page(503);

        assertThat(page.select("#unavailable")).hasSize(1);
        assertThat(page.select("#empty")).isEmpty();
    }

    private Document page(int expectedStatus) throws Exception {
        var html = mvc.perform(get("/"))
                .andExpect(status().is(expectedStatus))
                .andReturn().getResponse().getContentAsString();
        return Jsoup.parse(html);
    }

    /**
     * The German format puts a non-breaking space before the currency sign.
     */
    private static String normalized(String text) {
        return text.replace(' ', ' ');
    }
}
