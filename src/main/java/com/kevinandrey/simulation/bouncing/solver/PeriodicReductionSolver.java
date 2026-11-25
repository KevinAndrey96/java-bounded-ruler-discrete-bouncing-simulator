package com.kevinandrey.simulation.bouncing.solver;

import com.kevinandrey.simulation.bouncing.domain.RulerConfiguration;
import com.kevinandrey.simulation.bouncing.domain.RulerState;

/**
 * Hybrid solver that exploits the {@code 2N} periodicity and replays at most {@code 2N} steps.
 *
 * <p>After the first step every state lies on a cycle of exact length {@code 2N}, therefore the state after
 * {@code M >= 1} steps equals the state after {@code ((M - 1) mod 2N) + 1} steps.</p>
 */
public final class PeriodicReductionSolver implements BouncingSolver {

    public static final String NAME = "periodic";

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public String complexity() {
        return "O(min(M, 2N))";
    }

    @Override
    public long replaySteps(RulerConfiguration configuration) {
        return reducedSteps(configuration);
    }

    @Override
    public RulerState solve(RulerConfiguration configuration) {
        long steps = configuration.steps();
        if (steps == 0) {
            return configuration.initialState();
        }

        RulerState state = StepByStepSolver.replay(configuration.length(), configuration.start(),
                configuration.direction(), reducedSteps(configuration));

        return new RulerState(state.position(), state.direction(), steps);
    }

    private static long reducedSteps(RulerConfiguration configuration) {
        long steps = configuration.steps();
        if (steps == 0) {
            return 0;
        }

        return ((steps - 1) % configuration.period()) + 1;
    }
}
