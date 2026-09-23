package de.ostfale.greenshop.adapter.in.stripe;

import de.ostfale.greenshop.application.port.in.ConfirmPayment;
import de.ostfale.greenshop.application.port.in.PaymentNotification;
import de.ostfale.greenshop.application.port.out.PaymentUnavailable;

import java.util.ArrayList;
import java.util.List;

/**
 * Writes down which call came with which reference, or fails when a test says so.
 */
class FakeConfirmPayment implements ConfirmPayment {

    private final List<String> calls = new ArrayList<>();
    private boolean unavailable;

    List<String> calls() {
        return calls;
    }

    void goDown() {
        unavailable = true;
    }

    /**
     * The fake is a bean and lives as long as the cached test context, so each test starts
     * with a clean one.
     */
    void reset() {
        calls.clear();
        unavailable = false;
    }

    @Override
    public void checkoutCompleted(PaymentNotification notification) {
        record("completed", notification);
    }

    @Override
    public void paymentSucceeded(PaymentNotification notification) {
        record("succeeded", notification);
    }

    @Override
    public void paymentFailed(PaymentNotification notification) {
        record("failed", notification);
    }

    @Override
    public void paymentRefunded(PaymentNotification notification) {
        record("refunded", notification);
    }

    private void record(String call, PaymentNotification notification) {
        if (unavailable) {
            throw new PaymentUnavailable("fake provider is down", null);
        }
        calls.add(call + " " + notification.reference() + " by " + notification.messageId());
    }
}
