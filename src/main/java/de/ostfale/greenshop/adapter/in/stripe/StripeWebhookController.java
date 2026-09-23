package de.ostfale.greenshop.adapter.in.stripe;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import de.ostfale.greenshop.application.port.in.ConfirmPayment;
import de.ostfale.greenshop.application.port.in.PaymentNotification;
import de.ostfale.greenshop.application.port.out.PaymentUnavailable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.json.JsonMapper;

/**
 * Where Stripe reports what happened to a checkout.
 * <p>
 * The body is taken as the raw string it arrived as: the signature is computed over exactly
 * those bytes, and anything Spring parsed and wrote back would no longer match. A message
 * without a valid signature is refused with 400 before anything else is read from it.
 * <p>
 * From the event only the id of the session is taken, out of the raw JSON. The SDK can turn
 * the event's object into a {@code Session} only when the event was written in the API version
 * the SDK was built for; the id is there in every version, and the session is looked up fresh
 * by the service anyway.
 * <p>
 * Stripe wants a quick 2xx. Types nobody here cares about are acknowledged and dropped. When
 * the session cannot be looked up, the answer is 500, so that Stripe tries again later; when
 * the message contradicts the order, it is acknowledged, because trying again would not help.
 */
@RestController
class StripeWebhookController {

    static final String CHECKOUT_COMPLETED = "checkout.session.completed";
    static final String ASYNC_PAYMENT_SUCCEEDED = "checkout.session.async_payment_succeeded";
    static final String ASYNC_PAYMENT_FAILED = "checkout.session.async_payment_failed";

    private static final Logger log = LoggerFactory.getLogger(StripeWebhookController.class);

    private final ConfirmPayment confirmPayment;
    private final String signingSecret;

    StripeWebhookController(ConfirmPayment confirmPayment, StripeWebhookProperties properties) {
        this.confirmPayment = confirmPayment;
        this.signingSecret = properties.webhookSecret();
    }

    @PostMapping("/stripe/webhook")
    ResponseEntity<Void> receive(@RequestBody String payload, @RequestHeader("Stripe-Signature") String signature) {
        Event event;
        try {
            event = Webhook.constructEvent(payload, signature, signingSecret);
        } catch (SignatureVerificationException e) {
            log.warn("StripeWebhookController :: signature refused: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }

        log.debug("StripeWebhookController :: {} {} (API {})", event.getType(), event.getId(), event.getApiVersion());
        try {
            switch (event.getType()) {
                case CHECKOUT_COMPLETED -> confirmPayment.checkoutCompleted(notificationOf(event));
                case ASYNC_PAYMENT_SUCCEEDED -> confirmPayment.paymentSucceeded(notificationOf(event));
                case ASYNC_PAYMENT_FAILED -> confirmPayment.paymentFailed(notificationOf(event));
                default -> log.debug("StripeWebhookController :: {} ignored", event.getType());
            }
        } catch (PaymentUnavailable e) {
            log.warn("StripeWebhookController :: {} not handled, Stripe will retry: {}", event.getId(), e.getMessage());
            return ResponseEntity.internalServerError().build();
        } catch (IllegalStateException e) {
            // a message that contradicts what we know, such as "failed" for a paid order:
            // asking again would not change the answer, so Stripe is told it arrived
            log.warn("StripeWebhookController :: {} contradicts the order and is dropped: {}", event.getId(), e.getMessage());
        }
        return ResponseEntity.ok().build();
    }

    /**
     * The id of the event names the message, the id inside its object names the checkout. The
     * first keeps a message from being dealt with twice, the second finds the order.
     */
    private static PaymentNotification notificationOf(Event event) {
        var raw = event.getDataObjectDeserializer().getRawJson();
        var sessionId = JsonMapper.shared().readTree(raw).path("id").asString();
        return new PaymentNotification(event.getId(), sessionId);
    }
}
