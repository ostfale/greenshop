package de.ostfale.greenshop.adapter.out.memory;

import de.ostfale.greenshop.application.port.out.HandledMessages;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The ids of the messages already dealt with, in a set that grows for as long as the
 * application runs. A few hundred purchases a year make that a few thousand short strings; a
 * database would give them an age and throw the old ones away.
 */
@Component
class InMemoryHandledMessages implements HandledMessages {

    private final Set<String> ids = ConcurrentHashMap.newKeySet();

    @Override
    public boolean alreadyHandled(String messageId) {
        return ids.contains(messageId);
    }

    @Override
    public void handled(String messageId) {
        ids.add(messageId);
    }
}
