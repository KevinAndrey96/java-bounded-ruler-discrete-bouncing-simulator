package com.kevinandrey.simulation.bouncing.benchmark;

import com.kevinandrey.simulation.bouncing.domain.RulerConfiguration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Aggregated outcome of benchmarking several solvers over the same configuration.
 */
public final class BenchmarkReport {

    private final RulerConfiguration configuration;
    private final int iterations;
    private final List<BenchmarkResult> results;
    private final List<String> skipped;

    public BenchmarkReport(RulerConfiguration configuration, int iterations, List<BenchmarkResult> results,
                           List<String> skipped) {
        if (results == null || results.isEmpty()) {
            throw new IllegalArgumentException("benchmark report requires at least one measured solver");
        }
        this.configuration = configuration;
        this.iterations = iterations;
        this.results = Collections.unmodifiableList(new ArrayList<>(results));
        this.skipped = skipped == null
                ? Collections.<String>emptyList()
                : Collections.unmodifiableList(new ArrayList<>(skipped));
    }

    public RulerConfiguration configuration() {
        return configuration;
    }

    public int iterations() {
        return iterations;
    }

    public List<BenchmarkResult> results() {
        return results;
    }

    /**
     * Returns the names of solvers skipped because their replay cost exceeded the budget.
     *
     * @return skipped solver names
     */
    public List<String> skipped() {
        return skipped;
    }

    /**
     * Returns the first result, used as the speedup baseline.
     *
     * @return the baseline result
     */
    public BenchmarkResult baseline() {
        return results.get(0);
    }

    /**
     * Checks that every solver produced the same final state.
     *
     * @return {@code true} when all states are equal
     */
    public boolean consistent() {
        BenchmarkResult reference = baseline();
        for (BenchmarkResult result : results) {
            if (!reference.state().equals(result.state())) {
                return false;
            }
        }

        return true;
    }

    /**
     * Returns how many times faster a result is compared to the baseline.
     *
     * @param result result to compare
     * @return baseline time divided by result time
     */
    public double speedup(BenchmarkResult result) {
        return (double) baseline().bestNanos() / (double) result.bestNanos();
    }
}
