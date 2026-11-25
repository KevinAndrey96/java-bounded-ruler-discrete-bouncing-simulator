package com.kevinandrey.simulation.bouncing.solver;

import com.kevinandrey.simulation.bouncing.domain.BounceRule;
import com.kevinandrey.simulation.bouncing.domain.Direction;
import com.kevinandrey.simulation.bouncing.domain.RulerConfiguration;
import com.kevinandrey.simulation.bouncing.domain.RulerState;

/**
 * Reference solver that replays every step of the walk in {@code O(M)} time and {@code O(1)} memory.
 */
public final class StepByStepSolver implements BouncingSolver {

    public static final String NAME = "step";

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public String complexity() {
        return "O(M)";
    }

    @Override
    public long replaySteps(RulerConfiguration configuration) {
        return configuration.steps();
    }

    @Override
    public RulerState solve(RulerConfiguration configuration) {
        return replay(configuration.length(), configuration.start(), configuration.direction(), configuration.steps());
    }

    static RulerState replay(long length, long start, Direction direction, long steps) {
        long position = start;
        Direction current = direction;
        for (long step = 0; step < steps; step++) {
            current = BounceRule.nextDirection(position, current, length);
            position += current.sign();
        }

        return new RulerState(position, current, steps);
    }
}
