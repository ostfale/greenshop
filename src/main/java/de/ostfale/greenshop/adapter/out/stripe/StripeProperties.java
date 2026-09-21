package de.ostfale.greenshop.adapter.out.stripe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * The secret key comes from the environment. greenshop is a playground: anything but a
 * test key stops the start, and so does a placeholder nobody filled in.
 */
@ConfigurationProperties("stripe")
public record StripeProperties(String secretKey) {

    private static final Logger log = LoggerFactory.getLogger(StripeProperties.class);

    public StripeProperties {
        if (secretKey == null || !secretKey.startsWith("sk_test_")) {
             log.error("StripeProperties :: stripe.secret-key must be a Stripe test key (sk_test_...), set STRIPE_SECRET_KEY");
            throw new IllegalArgumentException("StripeProperties :: stripe.secret-key must be a Stripe test key (sk_test_...), set STRIPE_SECRET_KEY");
        }
    }
}
