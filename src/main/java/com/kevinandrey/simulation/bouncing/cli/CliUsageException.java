package com.kevinandrey.simulation.bouncing.cli;

/**
 * Signals invalid command line usage.
 */
public final class CliUsageException extends Exception {

    private static final long serialVersionUID = 1L;

    public CliUsageException(String message) {
        super(message);
    }
}
