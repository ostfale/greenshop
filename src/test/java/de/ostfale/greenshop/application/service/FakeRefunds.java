package de.ostfale.greenshop.application.service;

import de.ostfale.greenshop.application.port.out.PaymentRefunds;
import de.ostfale.greenshop.application.port.out.RefundRefused;

import java.util.ArrayList;
import java.util.List;

/**
 * Writes down which payments were given back, or refuses when a test says so.
 */
public class FakeRefunds implements PaymentRefunds {

    private final List<String> refunded = new ArrayList<>();
    private boolean refusing;

    public void refuse() {
        refusing = true;
    }

    public List<String> refunded() {
        return refunded;
    }

    @Override
    public void refund(String payment) {
        if (refusing) {
            throw new RefundRefused("fake provider refuses to refund " + payment, null);
        }
        refunded.add(payment);
    }
}
