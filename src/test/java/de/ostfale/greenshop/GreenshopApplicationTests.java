package de.ostfale.greenshop;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {"stripe.secret-key=sk_test_dummy", "stripe.webhook-secret=whsec_dummy"})
class GreenshopApplicationTests {

    @Test
    void contextLoads() {
    }

}
