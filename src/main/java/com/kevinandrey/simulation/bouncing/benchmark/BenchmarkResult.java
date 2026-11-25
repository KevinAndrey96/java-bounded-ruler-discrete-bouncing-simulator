package com.kevinandrey.simulation.bouncing.benchmark;

import com.kevinandrey.simulation.bouncing.domain.RulerState;

/**
 * Timing and output of a single solver inside a benchmark run.
 */
public final class BenchmarkResult {

    private final String solverName;
    private final String complexity;
    private final RulerState state;
    private final long bestNanos;

    public BenchmarkResult(String solverName, String complexity, RulerState state, long bestNanos) {
        this.solverName = solverName;
        this.complexity = complexity;
        this.state = state;
        this.bestNanos = bestNanos;
    }

    public String solverName() {
        return solverName;
    }

    public String complexity() {
        return complexity;
    }

    public RulerState state() {
        return state;
    }

    /**
     * Returns the best observed wall time, never lower than one nanosecond.
     *
     * @return elapsed nanoseconds
     */
    public long bestNanos() {
        return bestNanos;
    }
}
