package de.ostfale.greenshop.application.port.out;

/**
 * Which messages of the payment provider have been dealt with. The provider delivers at least
 * once, so the same message can arrive again — after a retry, or because it was simply sent
 * twice.
 */
public interface HandledMessages {

    boolean alreadyHandled(String messageId);

    /**
     * Called after a message has been dealt with, never before: a message that failed halfway
     * is delivered again on purpose.
     */
    void handled(String messageId);
}
