package de.ostfale.greenshop.adapter.in.stripe;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNoException;

class StripeWebhookPropertiesTest {

    @Test
    void acceptsASigningSecret() {
        assertThatNoException().isThrownBy(() -> new StripeWebhookProperties("whsec_dummy"));
    }

    @Test
    void refusesAPlaceholderNobodyFilledIn() {
        assertThatIllegalArgumentException().isThrownBy(() -> new StripeWebhookProperties("${STRIPE_WEBHOOK_SECRET}"));
    }

    @Test
    void refusesTheApiKeyInItsPlace() {
        assertThatIllegalArgumentException().isThrownBy(() -> new StripeWebhookProperties("sk_test_dummy"));
    }
}
