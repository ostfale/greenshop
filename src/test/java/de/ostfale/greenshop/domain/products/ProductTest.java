package de.ostfale.greenshop.domain.products;

import de.ostfale.greenshop.domain.Money;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class ProductTest {

    private static final Money THREE_EURO = Money.of(300, "eur");

    @Test
    void carriesItsPrice() {
        var sticker = new Product("prod_sticker", "JUG-Hamburg-Sticker", THREE_EURO);

        assertThat(sticker.price().amount()).isEqualTo(300);
        assertThat(sticker.price().currency().getCurrencyCode()).isEqualTo("EUR");
    }

    @Test
    void refusesABlankName() {
        assertThatIllegalArgumentException().isThrownBy(() -> new Product("prod_sticker", " ", THREE_EURO));
    }

    @Test
    void refusesAMissingId() {
        assertThatIllegalArgumentException().isThrownBy(() -> new Product(null, "JUG-Hamburg-Sticker", THREE_EURO));
    }

    @Test
    void refusesAProductWithoutPrice() {
        assertThatNullPointerException().isThrownBy(() -> new Product("prod_sticker", "JUG-Hamburg-Sticker", null));
    }

    @Test
    void refusesANegativeAmount() {
        assertThatIllegalArgumentException().isThrownBy(() -> Money.of(-1, "eur"));
    }
}
