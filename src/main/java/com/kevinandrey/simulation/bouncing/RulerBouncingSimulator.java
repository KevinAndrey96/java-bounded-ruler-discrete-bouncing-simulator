package com.kevinandrey.simulation.bouncing;

import com.kevinandrey.simulation.bouncing.domain.RulerConfiguration;
import com.kevinandrey.simulation.bouncing.domain.RulerState;
import com.kevinandrey.simulation.bouncing.solver.AnalyticalSolver;
import com.kevinandrey.simulation.bouncing.solver.BouncingSolver;
import com.kevinandrey.simulation.bouncing.solver.PeriodicReductionSolver;
import com.kevinandrey.simulation.bouncing.solver.StepByStepSolver;
import com.kevinandrey.simulation.bouncing.trajectory.Trajectory;

/**
 * Application facade of the bounded ruler problem.
 *
 * <p>Exposes the {@code O(M)} reference simulation, the {@code O(min(M, 2N))} periodic reduction and the
 * {@code O(1)} closed-form solution behind a single entry point.</p>
 */
public final class RulerBouncingSimulator {

    private final BouncingSolver stepByStep;
    private final BouncingSolver periodic;
    private final BouncingSolver analytical;

    public RulerBouncingSimulator() {
        this(new StepByStepSolver(), new PeriodicReductionSolver(), new AnalyticalSolver());
    }

    public RulerBouncingSimulator(BouncingSolver stepByStep, BouncingSolver periodic, BouncingSolver analytical) {
        if (stepByStep == null || periodic == null || analytical == null) {
            throw new IllegalArgumentException("all solvers are required");
        }
        this.stepByStep = stepByStep;
        this.periodic = periodic;
        this.analytical = analytical;
    }

    /**
     * Replays the walk one step at a time.
     *
     * @param length    highest mark {@code N}
     * @param start     initial position {@code P}
     * @param direction initial direction sign {@code d}
     * @param steps     number of steps {@code M}
     * @return the final state
     */
    public RulerState simulateStepByStep(long length, long start, int direction, long steps) {
        return stepByStep.solve(RulerConfiguration.of(length, start, direction, steps));
    }

    /**
     * Replays at most one full period of the walk.
     *
     * @param length    highest mark {@code N}
     * @param start     initial position {@code P}
     * @param direction initial direction sign {@code d}
     * @param steps     number of steps {@code M}
     * @return the final state
     */
    public RulerState solvePeriodic(long length, long start, int direction, long steps) {
        return periodic.solve(RulerConfiguration.of(length, start, direction, steps));
    }

    /**
     * Evaluates the closed-form triangular wave solution.
     *
     * @param length    highest mark {@code N}
     * @param start     initial position {@code P}
     * @param direction initial direction sign {@code d}
     * @param steps     number of steps {@code M}
     * @return the final state
     */
    public RulerState solveAnalytical(long length, long start, int direction, long steps) {
        return analytical.solve(RulerConfiguration.of(length, start, direction, steps));
    }

    /**
     * Returns the lazy trajectory of the configuration.
     *
     * @param configuration validated input
     * @return the trajectory
     */
    public Trajectory trajectory(RulerConfiguration configuration) {
        return new Trajectory(configuration);
    }
}
