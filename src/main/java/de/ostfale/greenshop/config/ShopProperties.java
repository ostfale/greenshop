package de.ostfale.greenshop.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;
import java.util.Objects;

/**
 * Where the shop can be reached from outside. Stripe sends the customer back to addresses
 * below it, so it is written down once and not pieced together from the port.
 */
@ConfigurationProperties("greenshop")
public record ShopProperties(URI baseUrl) {

    public ShopProperties {
        Objects.requireNonNull(baseUrl, "greenshop.base-url");
    }

    /**
     * An address below the base, for a path that starts with a slash.
     */
    public String address(String path) {
        return baseUrl.toString().replaceAll("/+$", "") + path;
    }
}
