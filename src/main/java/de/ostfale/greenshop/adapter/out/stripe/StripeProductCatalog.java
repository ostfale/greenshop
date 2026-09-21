package de.ostfale.greenshop.adapter.out.stripe;

import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.model.Price;
import com.stripe.param.ProductListParams;
import de.ostfale.greenshop.application.port.out.CatalogUnavailable;
import de.ostfale.greenshop.application.port.out.ProductCatalog;
import de.ostfale.greenshop.domain.Money;
import de.ostfale.greenshop.domain.products.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The catalog is Stripe's product list. One call brings the products together with their
 * default price ({@code expand}); a product without a usable price is left out, not guessed at.
 */
@Component
class StripeProductCatalog implements ProductCatalog {

    private static final Logger log = LoggerFactory.getLogger(StripeProductCatalog.class);

    private final StripeClient stripe;

    StripeProductCatalog(StripeClient stripe) {
        this.stripe = stripe;
    }

    @Override
    public List<Product> productsForSale() {
        var params = ProductListParams.builder()
                .setActive(true)
                .addExpand("data.default_price")
                .setLimit(100L)
                .build();
        try {
            var products = new ArrayList<Product>();
            for (var stripeProduct : stripe.v1().products().list(params).autoPagingIterable()) {
                toProduct(stripeProduct.getId(), stripeProduct.getName(), stripeProduct.getDefaultPriceObject())
                        .ifPresentOrElse(
                        products::add,
                        () -> log.debug("StripeProductCatalog :: skipped {} ({}), no usable default price",
                                stripeProduct.getId(), stripeProduct.getName()));
            }
            log.debug("StripeProductCatalog :: {} products for sale", products.size());
            return products;
        } catch (StripeException e) {
            throw new CatalogUnavailable("StripeProductCatalog :: Stripe product list failed: " + e.getMessage(), e);
        } catch (RuntimeException e) {
            // the paging iterator fetches further pages lazily and wraps a StripeException
            if (e.getCause() instanceof StripeException cause) {
                throw new CatalogUnavailable("StripeProductCatalog :: Stripe product list failed: " + cause.getMessage(), cause);
            }
            throw e;
        }
    }

    /**
     * Takes the fields, not Stripe's product: its class shares the name with ours.
     */
    private static Optional<Product> toProduct(String id, String name, Price price) {
        if (price == null || price.getUnitAmount() == null) {
            return Optional.empty();
        }
        var money = Money.of(price.getUnitAmount(), price.getCurrency());
        return Optional.of(new Product(id, name, money));
    }
}
