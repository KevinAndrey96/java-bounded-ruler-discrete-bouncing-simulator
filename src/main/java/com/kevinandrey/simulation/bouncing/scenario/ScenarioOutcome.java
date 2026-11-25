package com.kevinandrey.simulation.bouncing.scenario;

import com.kevinandrey.simulation.bouncing.domain.RulerState;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Result of evaluating one scenario with every solver.
 */
public final class ScenarioOutcome {

    private final Scenario scenario;
    private final Map<String, RulerState> statesBySolver;

    public ScenarioOutcome(Scenario scenario, Map<String, RulerState> statesBySolver) {
        this.scenario = scenario;
        this.statesBySolver = Collections.unmodifiableMap(new LinkedHashMap<>(statesBySolver));
    }

    public Scenario scenario() {
        return scenario;
    }

    public Map<String, RulerState> statesBySolver() {
        return statesBySolver;
    }

    /**
     * Checks that every solver reached the expected position and direction.
     *
     * @return {@code true} when all solvers match the expectation
     */
    public boolean passed() {
        for (RulerState state : statesBySolver.values()) {
            if (state.position() != scenario.expectedPosition() || state.direction() != scenario.expectedDirection()) {
                return false;
            }
        }

        return true;
    }
}
