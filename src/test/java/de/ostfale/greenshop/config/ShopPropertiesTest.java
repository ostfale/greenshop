package de.ostfale.greenshop.config;

import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

class ShopPropertiesTest {

    @Test
    void putsThePathBelowTheBase() {
        var shop = new ShopProperties(URI.create("http://localhost:8484"));

        assertThat(shop.address("/checkout/cancel")).isEqualTo("http://localhost:8484/checkout/cancel");
    }

    @Test
    void doesNotDoubleTheSlashOfABaseThatEndsWithOne() {
        var shop = new ShopProperties(URI.create("https://shop.example.org/"));

        assertThat(shop.address("/checkout/cancel")).isEqualTo("https://shop.example.org/checkout/cancel");
    }
}
