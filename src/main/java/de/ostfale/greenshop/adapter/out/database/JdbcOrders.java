package de.ostfale.greenshop.adapter.out.database;

import de.ostfale.greenshop.application.port.out.Orders;
import de.ostfale.greenshop.domain.Money;
import de.ostfale.greenshop.domain.orders.Order;
import de.ostfale.greenshop.domain.orders.OrderLine;
import de.ostfale.greenshop.domain.orders.OrderStatus;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Two tables, written as one: an order and the lines it was sold with. Saving replaces both —
 * the same order comes back as a whole, never as a change to part of it.
 */
@Component
class JdbcOrders implements Orders {

    private final JdbcClient db;

    JdbcOrders(JdbcClient db) {
        this.db = db;
    }

    @Override
    public Optional<Order> find(String reference) {
        return db.sql("select * from customer_order where reference = :reference")
                .param("reference", reference)
                .query(JdbcOrders::toRow)
                .optional()
                .map(this::withLines);
    }

    @Override
    public Optional<Order> findByPayment(String payment) {
        return db.sql("select * from customer_order where payment = :payment")
                .param("payment", payment)
                .query(JdbcOrders::toRow)
                .optional()
                .map(this::withLines);
    }

    @Override
    public List<Order> all() {
        return db.sql("select * from customer_order order by placed_at desc")
                .query(JdbcOrders::toRow)
                .list().stream()
                .map(this::withLines)
                .toList();
    }

    /**
     * {@code merge ... key(reference)} is H2's way of writing a row whether or not it is there
     * yet. PostgreSQL would say {@code insert ... on conflict do update} instead.
     */
    @Override
    @Transactional
    public void save(Order order) {
        db.sql("""
                        merge into customer_order (reference, payment, total_amount, total_currency, status, placed_at)
                        key (reference)
                        values (:reference, :payment, :amount, :currency, :status, :placedAt)""")
                .param("reference", order.reference())
                .param("payment", order.payment())
                .param("amount", order.total().amount())
                .param("currency", order.total().currency().getCurrencyCode())
                .param("status", order.status().name())
                .param("placedAt", Timestamp.from(order.placedAt()))
                .update();

        db.sql("delete from order_line where order_reference = :reference")
                .param("reference", order.reference())
                .update();
        var lines = order.lines();
        for (int lineNo = 0; lineNo < lines.size(); lineNo++) {
            db.sql("""
                            insert into order_line (order_reference, line_no, name, quantity)
                            values (:reference, :lineNo, :name, :quantity)""")
                    .param("reference", order.reference())
                    .param("lineNo", lineNo)
                    .param("name", lines.get(lineNo).name())
                    .param("quantity", lines.get(lineNo).quantity())
                    .update();
        }
    }

    private Order withLines(Row row) {
        var lines = db.sql("select name, quantity from order_line where order_reference = :reference order by line_no")
                .param("reference", row.reference())
                .query((ResultSet line, int number) -> new OrderLine(line.getString("name"), line.getLong("quantity")))
                .list();
        return new Order(row.reference(), row.payment(), lines, row.total(), row.status(), row.placedAt());
    }

    private static Row toRow(ResultSet row, int number) throws SQLException {
        return new Row(
                row.getString("reference"),
                row.getString("payment"),
                Money.of(row.getLong("total_amount"), row.getString("total_currency")),
                OrderStatus.valueOf(row.getString("status")),
                row.getTimestamp("placed_at").toInstant());
    }

    /**
     * An order without its lines. They come from the second table, and an order needs at least
     * one of them — so the row waits here until it has them.
     */
    private record Row(String reference, String payment, Money total, OrderStatus status, Instant placedAt) {
    }
}
