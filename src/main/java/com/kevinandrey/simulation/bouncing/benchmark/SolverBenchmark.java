package com.kevinandrey.simulation.bouncing.benchmark;

import com.kevinandrey.simulation.bouncing.domain.RulerConfiguration;
import com.kevinandrey.simulation.bouncing.domain.RulerState;
import com.kevinandrey.simulation.bouncing.solver.BouncingSolver;

import java.util.ArrayList;
import java.util.List;

/**
 * Wall-clock micro benchmark that keeps the best of several runs per solver.
 *
 * <p>One untimed warm-up run is executed per solver so the JIT compiles the hot loop before measuring.
 * Solvers whose replay cost exceeds the configured budget are reported as skipped instead of measured.</p>
 */
public final class SolverBenchmark {

    /**
     * Default replay budget, roughly a couple of seconds for the {@code O(M)} loop on commodity hardware.
     */
    public static final long DEFAULT_MAX_REPLAY_STEPS = 2_000_000_000L;

    private final int iterations;
    private final long maxReplaySteps;

    public SolverBenchmark(int iterations) {
        this(iterations, DEFAULT_MAX_REPLAY_STEPS);
    }

    public SolverBenchmark(int iterations, long maxReplaySteps) {
        if (iterations < 1) {
            throw new IllegalArgumentException("benchmark iterations must be positive: got " + iterations);
        }
        if (maxReplaySteps < 0) {
            throw new IllegalArgumentException("benchmark replay budget must be non negative: got " + maxReplaySteps);
        }
        this.iterations = iterations;
        this.maxReplaySteps = maxReplaySteps;
    }

    /**
     * Benchmarks every solver over the same configuration.
     *
     * @param configuration validated input
     * @param solvers       solvers to compare, the first measured one is the speedup baseline
     * @return the report
     */
    public BenchmarkReport run(RulerConfiguration configuration, List<BouncingSolver> solvers) {
        if (solvers == null || solvers.isEmpty()) {
            throw new IllegalArgumentException("benchmark requires at least one solver");
        }

        List<BenchmarkResult> results = new ArrayList<>();
        List<String> skipped = new ArrayList<>();
        for (BouncingSolver solver : solvers) {
            if (solver.replaySteps(configuration) > maxReplaySteps) {
                skipped.add(solver.name());
                continue;
            }
            results.add(measure(configuration, solver));
        }

        return new BenchmarkReport(configuration, iterations, results, skipped);
    }

    private BenchmarkResult measure(RulerConfiguration configuration, BouncingSolver solver) {
        RulerState state = solver.solve(configuration);
        long best = Long.MAX_VALUE;
        for (int run = 0; run < iterations; run++) {
            long begin = System.nanoTime();
            state = solver.solve(configuration);
            long elapsed = System.nanoTime() - begin;
            best = Math.min(best, elapsed);
        }

        return new BenchmarkResult(solver.name(), solver.complexity(), state, Math.max(1L, best));
    }
}
