package de.ostfale.greenshop.adapter.out.stripe;

import com.stripe.StripeClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class StripeConfiguration {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Bean
    StripeClient stripeClient(StripeProperties properties) {
        String key = properties.secretKey();
        log.info("StripeConfiguration :: Stripe client created with test key ...{}", key.substring(key.length() - 4));
        return new StripeClient(properties.secretKey());
    }
}
