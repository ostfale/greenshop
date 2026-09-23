package de.ostfale.greenshop.application.port.in;

import de.ostfale.greenshop.application.port.out.PaymentUnavailable;

/**
 * The payment provider reports on a checkout. Each message may come more than once and in any
 * order; the answer is the same order either way. A message about a checkout that did not
 * start in this shop is dropped.
 */
public interface ConfirmPayment {

    /**
     * The customer finished the checkout. Paid already or still waiting, depending on the
     * method.
     *
     * @throws PaymentUnavailable if the checkout cannot be looked up
     */
    void checkoutCompleted(PaymentNotification notification);

    /**
     * A payment that settles later has come in.
     *
     * @throws PaymentUnavailable if the checkout cannot be looked up
     */
    void paymentSucceeded(PaymentNotification notification);

    /**
     * A payment that settles later has failed.
     *
     * @throws PaymentUnavailable if the checkout cannot be looked up
     */
    void paymentFailed(PaymentNotification notification);
}
