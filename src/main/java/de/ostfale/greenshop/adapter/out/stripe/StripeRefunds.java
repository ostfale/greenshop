package de.ostfale.greenshop.adapter.out.stripe;

import com.stripe.StripeClient;
import com.stripe.exception.InvalidRequestException;
import com.stripe.exception.StripeException;
import com.stripe.param.RefundCreateParams;
import de.ostfale.greenshop.application.port.out.PaymentRefunds;
import de.ostfale.greenshop.application.port.out.PaymentUnavailable;
import de.ostfale.greenshop.application.port.out.RefundRefused;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * A refund on the payment intent of the checkout. No amount is given, so Stripe gives back
 * everything that is left on it — greenshop knows no partial refunds.
 * <p>
 * Stripe answers a payment it will not give back with an invalid request, not with a failure of
 * the connection: already refunded, never charged, too old. That is a refusal, and trying again
 * would end the same way.
 */
@Component
class StripeRefunds implements PaymentRefunds {

    private static final Logger log = LoggerFactory.getLogger(StripeRefunds.class);

    private final StripeClient stripe;

    StripeRefunds(StripeClient stripe) {
        this.stripe = stripe;
    }

    @Override
    public void refund(String payment) {
        if (payment == null || payment.isBlank()) {
            throw new RefundRefused("StripeRefunds :: the order went through no payment", null);
        }
        try {
            var params = RefundCreateParams.builder().setPaymentIntent(payment).build();
            var refund = stripe.v1().refunds().create(params);
            log.info("StripeRefunds :: refund {} on {} is {}", refund.getId(), payment, refund.getStatus());
        } catch (InvalidRequestException e) {
            throw new RefundRefused("StripeRefunds :: Stripe refuses to refund " + payment + ": " + e.getMessage(), e);
        } catch (StripeException e) {
            throw new PaymentUnavailable("StripeRefunds :: refund failed: " + e.getMessage(), e);
        }
    }
}
