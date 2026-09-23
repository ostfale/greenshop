package de.ostfale.greenshop.adapter.in.stripe;

import com.stripe.net.Webhook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Signs its messages the way Stripe does — HMAC-SHA256 over "timestamp.payload" with the
 * signing secret — so the real verification runs, without Stripe.
 */
@WebMvcTest(value = StripeWebhookController.class, properties = "stripe.webhook-secret=" + StripeWebhookControllerTest.SECRET)
@Import({FakeConfirmPayment.class, StripeWebhookControllerTest.Properties.class})
class StripeWebhookControllerTest {

    static final String SECRET = "whsec_test_secret";

    @TestConfiguration
    @EnableConfigurationProperties(StripeWebhookProperties.class)
    static class Properties {
    }

    @Autowired
    private MockMvc mvc;

    @Autowired
    private FakeConfirmPayment confirmPayment;

    @BeforeEach
    void startClean() {
        confirmPayment.reset();
    }

    @Test
    void placesTheOrderWhenTheCheckoutIsCompleted() throws Exception {
        send(event("checkout.session.completed", "cs_test_1"), SECRET).andExpect(status().isOk());

        assertThat(confirmPayment.calls()).containsExactly("completed cs_test_1");
    }

    @Test
    void passesOnTheLateVerdict() throws Exception {
        send(event("checkout.session.async_payment_succeeded", "cs_test_1"), SECRET).andExpect(status().isOk());
        send(event("checkout.session.async_payment_failed", "cs_test_2"), SECRET).andExpect(status().isOk());

        assertThat(confirmPayment.calls()).containsExactly("succeeded cs_test_1", "failed cs_test_2");
    }

    @Test
    void refusesAMessageSignedWithAnotherSecret() throws Exception {
        send(event("checkout.session.completed", "cs_test_1"), "whsec_somebody_else").andExpect(status().isBadRequest());

        assertThat(confirmPayment.calls()).isEmpty();
    }

    @Test
    void refusesAMessageThatWasChangedAfterSigning() throws Exception {
        var signed = event("checkout.session.completed", "cs_test_1");
        var header = signature(signed, SECRET);

        mvc.perform(post("/stripe/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Stripe-Signature", header)
                        .content(signed.replace("cs_test_1", "cs_test_2")))
                .andExpect(status().isBadRequest());

        assertThat(confirmPayment.calls()).isEmpty();
    }

    @Test
    void acknowledgesATypeNobodyHereCaresAbout() throws Exception {
        send(event("customer.created", "cus_1"), SECRET).andExpect(status().isOk());

        assertThat(confirmPayment.calls()).isEmpty();
    }

    @Test
    void asksStripeToRetryWhenTheCheckoutCannotBeLookedUp() throws Exception {
        confirmPayment.goDown();

        send(event("checkout.session.completed", "cs_test_1"), SECRET).andExpect(status().isInternalServerError());
    }

    private ResultActions send(String payload, String secret) throws Exception {
        return mvc.perform(post("/stripe/webhook")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Stripe-Signature", signature(payload, secret))
                .content(payload));
    }

    private static String event(String type, String objectId) {
        return """
                {"id":"evt_test_1","object":"event","api_version":"2020-08-27","type":"%s",\
                "data":{"object":{"id":"%s","object":"checkout.session"}}}""".formatted(type, objectId);
    }

    private static String signature(String payload, String secret) throws Exception {
        var timestamp = Webhook.Util.getTimeNow();
        var hmac = Webhook.Util.computeHmacSha256(secret, timestamp + "." + payload);
        return "t=" + timestamp + ",v1=" + hmac;
    }
}
