package de.ostfale.greenshop.application.port.out;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class CheckoutSummaryTest {

    @Test
    void refusesALineWithoutAPiece() {
        assertThatIllegalArgumentException().isThrownBy(() -> new CheckoutSummary.Item("Schal", 0));
    }
}
