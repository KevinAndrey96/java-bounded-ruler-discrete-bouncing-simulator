package com.kevinandrey.simulation.bouncing.solver;

import com.kevinandrey.simulation.bouncing.domain.RulerConfiguration;
import com.kevinandrey.simulation.bouncing.domain.RulerState;

/**
 * Strategy that computes the walker state after {@code M} steps.
 */
public interface BouncingSolver {

    /**
     * Returns the stable identifier used by the CLI and reports.
     *
     * @return the solver name
     */
    String name();

    /**
     * Returns the asymptotic time complexity in big O notation.
     *
     * @return the complexity label
     */
    String complexity();

    /**
     * Returns how many unit steps the solver replays for the configuration, used to bound its cost.
     *
     * @param configuration validated input
     * @return number of replayed steps, {@code 0} for closed-form solvers
     */
    long replaySteps(RulerConfiguration configuration);

    /**
     * Computes the final state for the given configuration.
     *
     * @param configuration validated input
     * @return the state after {@code configuration.steps()} steps
     */
    RulerState solve(RulerConfiguration configuration);
}
