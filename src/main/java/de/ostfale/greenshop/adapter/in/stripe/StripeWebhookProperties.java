package de.ostfale.greenshop.adapter.in.stripe;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * The secret Stripe signs its messages with. Locally it is the one `stripe listen` prints —
 * not the one of an endpoint in the Dashboard, which is a different secret. Anything that is
 * not a {@code whsec_...} stops the start, like the API key.
 */
@ConfigurationProperties("stripe")
public record StripeWebhookProperties(String webhookSecret) {

    public StripeWebhookProperties {
        if (webhookSecret == null || !webhookSecret.startsWith("whsec_")) {
            throw new IllegalArgumentException(
                    "StripeWebhookProperties :: stripe.webhook-secret must be a Stripe signing secret (whsec_...), set STRIPE_WEBHOOK_SECRET");
        }
    }
}
