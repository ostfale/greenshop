package de.ostfale.greenshop.adapter.out.database;

import de.ostfale.greenshop.application.port.out.HandledMessages;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;

/**
 * The ids of the messages already dealt with, with the moment they were. Nothing removes them
 * yet; a few hundred purchases a year keep the table small, and the stamp is there for the day
 * somebody wants to.
 */
@Component
class JdbcHandledMessages implements HandledMessages {

    private final JdbcClient db;
    private final Clock clock;

    JdbcHandledMessages(JdbcClient db, Clock clock) {
        this.db = db;
        this.clock = clock;
    }

    @Override
    public boolean alreadyHandled(String messageId) {
        return db.sql("select count(*) from handled_message where id = :id")
                .param("id", messageId)
                .query(Long.class)
                .single() > 0;
    }

    @Override
    public void handled(String messageId) {
        db.sql("merge into handled_message (id, handled_at) key (id) values (:id, :handledAt)")
                .param("id", messageId)
                .param("handledAt", Timestamp.from(Instant.now(clock)))
                .update();
    }
}
