package de.ostfale.greenshop.application.port.out;

/**
 * The catalog could not be read — network, key, or the provider itself. The cause keeps the
 * details; the name is all the application needs to know.
 */
public class CatalogUnavailable extends RuntimeException {

    public CatalogUnavailable(String message, Throwable cause) {
        super(message, cause);
    }
}
