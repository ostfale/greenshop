package de.ostfale.greenshop.application.port.out;

/**
 * Somebody asked to buy what is not for sale (any more): an unknown id, an archived product,
 * or one whose price cannot be paid once.
 */
public class ProductNotForSale extends RuntimeException {

    private final String productId;

    public ProductNotForSale(String productId) {
        super("product not for sale: " + productId);
        this.productId = productId;
    }

    public String productId() {
        return productId;
    }
}
