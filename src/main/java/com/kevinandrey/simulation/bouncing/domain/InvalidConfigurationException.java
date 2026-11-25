package com.kevinandrey.simulation.bouncing.domain;

/**
 * Signals that a ruler configuration violates the domain invariants.
 */
public final class InvalidConfigurationException extends IllegalArgumentException {

    private static final long serialVersionUID = 1L;

    public InvalidConfigurationException(String message) {
        super(message);
    }
}
