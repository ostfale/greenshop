package de.ostfale.greenshop.adapter.in.stripe;

import de.ostfale.greenshop.application.port.in.ConfirmPayment;
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
    public void checkoutCompleted(String reference) {
        record("completed " + reference);
    }

    @Override
    public void paymentSucceeded(String reference) {
        record("succeeded " + reference);
    }

    @Override
    public void paymentFailed(String reference) {
        record("failed " + reference);
    }

    private void record(String call) {
        if (unavailable) {
            throw new PaymentUnavailable("fake provider is down", null);
        }
        calls.add(call);
    }
}
