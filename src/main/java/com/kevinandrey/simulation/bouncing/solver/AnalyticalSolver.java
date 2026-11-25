package com.kevinandrey.simulation.bouncing.solver;

import com.kevinandrey.simulation.bouncing.domain.Direction;
import com.kevinandrey.simulation.bouncing.domain.RulerConfiguration;
import com.kevinandrey.simulation.bouncing.domain.RulerState;

/**
 * Closed-form {@code O(1)} solver based on the periodic triangular wave of period {@code 2N}.
 *
 * <p>The reflected walk over {@code [0, N]} is unfolded into a uniform walk over the cyclic group
 * {@code Z_2N}. The virtual phase advances one unit per step and is folded back with
 * {@code T(u) = N - |N - u|}.</p>
 */
public final class AnalyticalSolver implements BouncingSolver {

    public static final String NAME = "analytical";

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public String complexity() {
        return "O(1)";
    }

    @Override
    public long replaySteps(RulerConfiguration configuration) {
        return 0;
    }

    @Override
    public RulerState solve(RulerConfiguration configuration) {
        long steps = configuration.steps();
        if (steps == 0) {
            return configuration.initialState();
        }

        long length = configuration.length();
        long phase = phase(configuration);
        long position = triangularWave(phase, length);
        Direction direction = phase >= 1 && phase <= length ? Direction.FORWARD : Direction.BACKWARD;

        return new RulerState(position, direction, steps);
    }

    /**
     * Computes the virtual phase {@code u_M = (u_0 + M) mod 2N} without overflow.
     *
     * @param configuration validated input
     * @return the phase inside {@code [0, 2N)}
     */
    static long phase(RulerConfiguration configuration) {
        long period = configuration.period();
        long offset = initialPhase(configuration.start(), configuration.direction(), period);

        return (offset + configuration.steps() % period) % period;
    }

    /**
     * Maps the initial state to its unfolded phase: {@code u_0 = P} when moving forward,
     * {@code u_0 = (2N - P) mod 2N} when moving backward.
     */
    static long initialPhase(long start, Direction direction, long period) {
        if (direction == Direction.FORWARD) {
            return start;
        }

        return Math.floorMod(period - start, period);
    }

    /**
     * Folds a phase of {@code Z_2N} onto the ruler.
     *
     * @param phase  value inside {@code [0, 2N)}
     * @param length highest mark {@code N}
     * @return {@code N - |N - phase|}
     */
    static long triangularWave(long phase, long length) {
        return length - Math.abs(length - phase);
    }
}
