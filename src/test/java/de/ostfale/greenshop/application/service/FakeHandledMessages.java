package de.ostfale.greenshop.application.service;

import de.ostfale.greenshop.application.port.out.HandledMessages;

import java.util.HashSet;
import java.util.Set;

/**
 * The ids a test has already seen go through.
 */
public class FakeHandledMessages implements HandledMessages {

    private final Set<String> ids = new HashSet<>();

    @Override
    public boolean alreadyHandled(String messageId) {
        return ids.contains(messageId);
    }

    @Override
    public void handled(String messageId) {
        ids.add(messageId);
    }

    public Set<String> ids() {
        return ids;
    }
}
