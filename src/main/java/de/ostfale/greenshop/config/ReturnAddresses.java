package de.ostfale.greenshop.config;

/**
 * The two addresses the payment provider sends the customer back to, and the name of the
 * parameter that carries the reference of the checkout. The web adapter serves them, the
 * Stripe adapter hands them out — written here once, so the two cannot drift apart.
 */
public final class ReturnAddresses {

    public static final String SUCCESS = "/checkout/success";
    public static final String CANCEL = "/checkout/cancel";
    public static final String REFERENCE = "session_id";

    private ReturnAddresses() {
    }
}
