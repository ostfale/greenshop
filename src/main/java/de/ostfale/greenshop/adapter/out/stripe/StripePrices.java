package de.ostfale.greenshop.adapter.out.stripe;

import com.stripe.model.Price;

/**
 * When a Stripe price cannot be sold in this shop: it is missing, it names no fixed amount,
 * or it is not paid once. A recurring price belongs to a subscription, not to a purchase.
 * The catalog and the payment page ask the same question, so it is asked here once — and
 * both ask it to turn a price away, so that is how it is put.
 */
final class StripePrices {

    private StripePrices() {
    }

    static boolean notForSale(Price price) {
        if (price == null) {
            return true;
        }
        return price.getUnitAmount() == null
                || !"one_time".equals(price.getType());
    }
}
