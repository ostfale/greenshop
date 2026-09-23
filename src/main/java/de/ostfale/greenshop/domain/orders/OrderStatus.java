package de.ostfale.greenshop.domain.orders;

/**
 * How far the money of an order has come. An order starts either paid (a card, settled at the
 * till) or waiting (a method that settles later); a waiting one ends paid or failed, and a paid
 * one can be given back.
 */
public enum OrderStatus {
    AWAITING_PAYMENT,
    PAID,
    FAILED,
    REFUNDED
}
