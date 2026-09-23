package de.ostfale.greenshop.domain.orders;

/**
 * How far the money of an order has come. An order starts either paid (a card, settled at the
 * till) or waiting (a method that settles later), and a waiting one ends paid or failed.
 */
public enum OrderStatus {
    AWAITING_PAYMENT,
    PAID,
    FAILED
}
