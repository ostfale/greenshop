package de.ostfale.greenshop.adapter.out.stripe;

import com.stripe.StripeClient;
import com.stripe.exception.InvalidRequestException;
import com.stripe.exception.StripeException;
import com.stripe.param.ProductRetrieveParams;
import com.stripe.param.checkout.SessionCreateParams;
import com.stripe.param.checkout.SessionCreateParams.LineItem;
import com.stripe.param.checkout.SessionCreateParams.Locale;
import com.stripe.param.checkout.SessionCreateParams.Mode;
import com.stripe.param.checkout.SessionCreateParams.ShippingAddressCollection;
import com.stripe.param.checkout.SessionCreateParams.ShippingAddressCollection.AllowedCountry;
import com.stripe.param.checkout.SessionRetrieveParams;
import de.ostfale.greenshop.application.port.out.CheckoutSummary;
import de.ostfale.greenshop.application.port.out.PaymentPage;
import de.ostfale.greenshop.application.port.out.PaymentUnavailable;
import de.ostfale.greenshop.application.port.out.ProductNotForSale;
import de.ostfale.greenshop.config.ReturnAddresses;
import de.ostfale.greenshop.config.ShopProperties;
import de.ostfale.greenshop.domain.Money;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Optional;

/**
 * Stripe Checkout as the payment page. Two calls: the product with its default price, fresh,
 * so the amount is Stripe's and not the browser's — then a checkout session for it. The
 * customer picks how many on Stripe's page, between one and {@link #MOST_AT_ONCE}; Stripe adds
 * up the total. Stripe answers with the address of its hosted page.
 * <p>
 * The success address carries {@code {CHECKOUT_SESSION_ID}}, which Stripe replaces with the
 * id of the session on the way back. That id is the reference {@link #find} looks up.
 */
@Component
class StripePaymentPage implements PaymentPage {

    static final String SUCCESS_PATH =
            ReturnAddresses.SUCCESS + "?" + ReturnAddresses.REFERENCE + "={CHECKOUT_SESSION_ID}";
    static final String CANCEL_PATH = ReturnAddresses.CANCEL;
    static final long MOST_AT_ONCE = 10;

    private static final Logger log = LoggerFactory.getLogger(StripePaymentPage.class);

    private final StripeClient stripe;
    private final ShopProperties shop;

    StripePaymentPage(StripeClient stripe, ShopProperties shop) {
        this.stripe = stripe;
        this.shop = shop;
    }

    @Override
    public URI open(String productId) {
        try {
            var priceId = sellablePriceOf(productId);
            var params = SessionCreateParams.builder()
                    .setMode(Mode.PAYMENT)
                    .addLineItem(LineItem.builder()
                            .setPrice(priceId)
                            .setQuantity(1L)
                            .setAdjustableQuantity(LineItem.AdjustableQuantity.builder()
                                    .setEnabled(true)
                                    .setMinimum(1L)
                                    .setMaximum(MOST_AT_ONCE)
                                    .build())
                            .build())
                    .setShippingAddressCollection(ShippingAddressCollection.builder()
                            .addAllowedCountry(AllowedCountry.DE)
                            .build())
                    .setLocale(Locale.DE)
                    .setSuccessUrl(shop.address(SUCCESS_PATH))
                    .setCancelUrl(shop.address(CANCEL_PATH))
                    .build();
            var session = stripe.v1().checkout().sessions().create(params);
            log.debug("StripePaymentPage :: checkout session {} for {} with {}", session.getId(), productId, priceId);
            return URI.create(session.getUrl());
        } catch (StripeException e) {
            throw new PaymentUnavailable("StripePaymentPage :: checkout session failed: " + e.getMessage(), e);
        }
    }

    /**
     * The session with its line items. They are not part of a session by default and come
     * only with {@code expand}; an unknown id is a 404 from Stripe and an empty answer here.
     */
    @Override
    public Optional<CheckoutSummary> find(String reference) {
        try {
            var params = SessionRetrieveParams.builder().addExpand("line_items").build();
            var session = stripe.v1().checkout().sessions().retrieve(reference, params);
            var items = session.getLineItems().getData().stream()
                    .map(item -> new CheckoutSummary.Item(item.getDescription(), item.getQuantity()))
                    .toList();
            var total = Money.of(session.getAmountTotal(), session.getCurrency());
            log.debug("StripePaymentPage :: session {} is {}, payment {}",
                    reference, session.getStatus(), session.getPaymentStatus());
            return Optional.of(new CheckoutSummary(items, total, "paid".equals(session.getPaymentStatus())));
        } catch (InvalidRequestException e) {
            if (Integer.valueOf(404).equals(e.getStatusCode())) {
                return Optional.empty();
            }
            throw new PaymentUnavailable("StripePaymentPage :: session lookup failed: " + e.getMessage(), e);
        } catch (StripeException e) {
            throw new PaymentUnavailable("StripePaymentPage :: session lookup failed: " + e.getMessage(), e);
        }
    }

    /**
     * The id of the price to charge, looked up now. An unknown product comes back from Stripe
     * as 404 and is simply not for sale.
     */
    private String sellablePriceOf(String productId) throws StripeException {
        try {
            var params = ProductRetrieveParams.builder().addExpand("default_price").build();
            var product = stripe.v1().products().retrieve(productId, params);
            var price = product.getDefaultPriceObject();
            if (!Boolean.TRUE.equals(product.getActive()) || StripePrices.notForSale(price)) {
                throw new ProductNotForSale(productId);
            }
            return price.getId();
        } catch (InvalidRequestException e) {
            if (Integer.valueOf(404).equals(e.getStatusCode())) {
                throw new ProductNotForSale(productId);
            }
            throw e;
        }
    }
}
