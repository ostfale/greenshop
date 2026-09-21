package de.ostfale.greenshop.adapter.in.web;

import de.ostfale.greenshop.domain.Money;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PricesTest {

    private final Prices prices = new Prices();

    @Test
    void formatsEuroTheGermanWay() {
        assertThat(normalized(prices.format(Money.of(3500, "eur")))).isEqualTo("35,00 €");
    }

    @Test
    void takesTheThousandsSeparatorFromTheLocale() {
        assertThat(normalized(prices.format(Money.of(123456, "eur")))).isEqualTo("1.234,56 €");
    }

    @Test
    void showsACurrencyWithoutCentsWithoutThem() {
        assertThat(normalized(prices.format(Money.of(1500, "jpy")))).startsWith("1.500 ");
    }

    /**
     * The German format puts a non-breaking space before the currency sign.
     */
    private static String normalized(String text) {
        return text.replace(' ', ' ');
    }
}
