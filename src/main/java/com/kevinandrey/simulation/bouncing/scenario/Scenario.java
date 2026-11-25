package com.kevinandrey.simulation.bouncing.scenario;

import com.kevinandrey.simulation.bouncing.domain.Direction;
import com.kevinandrey.simulation.bouncing.domain.RulerConfiguration;

/**
 * Reference case with a known expected final state.
 */
public final class Scenario {

    private final String name;
    private final RulerConfiguration configuration;
    private final long expectedPosition;
    private final Direction expectedDirection;

    public Scenario(String name, RulerConfiguration configuration, long expectedPosition, Direction expectedDirection) {
        this.name = name;
        this.configuration = configuration;
        this.expectedPosition = expectedPosition;
        this.expectedDirection = expectedDirection;
    }

    public String name() {
        return name;
    }

    public RulerConfiguration configuration() {
        return configuration;
    }

    public long expectedPosition() {
        return expectedPosition;
    }

    public Direction expectedDirection() {
        return expectedDirection;
    }
}
