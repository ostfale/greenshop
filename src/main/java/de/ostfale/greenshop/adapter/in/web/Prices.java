package de.ostfale.greenshop.adapter.in.web;

import de.ostfale.greenshop.domain.Money;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Formats an amount for the page, in German: 2200 EUR becomes "22,00 €". Called from the
 * templates as {@code @prices.format(...)}.
 * <p>
 * The locale decides how it looks: comma, thousands dot, the sign behind the number. The
 * currency decides the decimals, twice: once to turn Stripe's smallest unit into a value,
 * once for the format — {@code setCurrency} alone keeps the two decimals of the euro.
 */
@Component("prices")
class Prices {

    public String format(Money money) {
        var fractionDigits = money.currency().getDefaultFractionDigits();
        var format = NumberFormat.getCurrencyInstance(Locale.GERMANY);
        format.setCurrency(money.currency());
        format.setMinimumFractionDigits(fractionDigits);
        format.setMaximumFractionDigits(fractionDigits);
        return format.format(BigDecimal.valueOf(money.amount(), fractionDigits));
    }
}
