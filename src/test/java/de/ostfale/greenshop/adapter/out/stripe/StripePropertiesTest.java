package de.ostfale.greenshop.adapter.out.stripe;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNoException;

class StripePropertiesTest {

    @Test
    void acceptsATestKey() {
        assertThatNoException().isThrownBy(() -> new StripeProperties("sk_test_dummy"));
    }

    @Test
    void refusesAPlaceholderNobodyFilledIn() {
        assertThatIllegalArgumentException().isThrownBy(() -> new StripeProperties("${STRIPE_SECRET_KEY}"));
    }

    @Test
    void refusesALiveKey() {
        assertThatIllegalArgumentException().isThrownBy(() -> new StripeProperties("sk_live_dummy"));
    }

    @Test
    void refusesAMissingKey() {
        assertThatIllegalArgumentException().isThrownBy(() -> new StripeProperties(null));
    }
}
