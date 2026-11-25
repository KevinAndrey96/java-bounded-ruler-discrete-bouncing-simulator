package com.kevinandrey.simulation.bouncing.cli;

import com.kevinandrey.simulation.bouncing.domain.Direction;
import com.kevinandrey.simulation.bouncing.solver.AnalyticalSolver;

/**
 * Parsed command line options.
 *
 * <p>Defaults reproduce the example of the original problem statement: {@code N=5, P=4, d=+1, M=4}.</p>
 */
public final class CliOptions {

    static final long DEFAULT_LENGTH = 5;
    static final long DEFAULT_START = 4;
    static final long DEFAULT_STEPS = 4;
    static final int DEFAULT_ITERATIONS = 5;

    private final long length;
    private final long start;
    private final Direction direction;
    private final long steps;
    private final String solver;
    private final boolean benchmark;
    private final int iterations;
    private final String exportCsv;
    private final String scenarios;
    private final boolean trace;
    private final boolean gui;
    private final boolean help;

    private CliOptions(Builder builder) {
        this.length = builder.length;
        this.start = builder.start;
        this.direction = builder.direction;
        this.steps = builder.steps;
        this.solver = builder.solver;
        this.benchmark = builder.benchmark;
        this.iterations = builder.iterations;
        this.exportCsv = builder.exportCsv;
        this.scenarios = builder.scenarios;
        this.trace = builder.trace;
        this.gui = builder.gui;
        this.help = builder.help;
    }

    static Builder builder() {
        return new Builder();
    }

    public long length() {
        return length;
    }

    public long start() {
        return start;
    }

    public Direction direction() {
        return direction;
    }

    public long steps() {
        return steps;
    }

    public String solver() {
        return solver;
    }

    public boolean benchmark() {
        return benchmark;
    }

    public int iterations() {
        return iterations;
    }

    public String exportCsv() {
        return exportCsv;
    }

    public String scenarios() {
        return scenarios;
    }

    public boolean trace() {
        return trace;
    }

    public boolean gui() {
        return gui;
    }

    public boolean help() {
        return help;
    }

    static final class Builder {

        private long length = DEFAULT_LENGTH;
        private long start = DEFAULT_START;
        private Direction direction = Direction.FORWARD;
        private long steps = DEFAULT_STEPS;
        private String solver = AnalyticalSolver.NAME;
        private boolean benchmark;
        private int iterations = DEFAULT_ITERATIONS;
        private String exportCsv;
        private String scenarios;
        private boolean trace;
        private boolean gui;
        private boolean help;

        Builder length(long value) {
            this.length = value;

            return this;
        }

        Builder start(long value) {
            this.start = value;

            return this;
        }

        Builder direction(Direction value) {
            this.direction = value;

            return this;
        }

        Builder steps(long value) {
            this.steps = value;

            return this;
        }

        Builder solver(String value) {
            this.solver = value;

            return this;
        }

        Builder benchmark(boolean value) {
            this.benchmark = value;

            return this;
        }

        Builder iterations(int value) {
            this.iterations = value;

            return this;
        }

        Builder exportCsv(String value) {
            this.exportCsv = value;

            return this;
        }

        Builder scenarios(String value) {
            this.scenarios = value;

            return this;
        }

        Builder trace(boolean value) {
            this.trace = value;

            return this;
        }

        Builder gui(boolean value) {
            this.gui = value;

            return this;
        }

        Builder help(boolean value) {
            this.help = value;

            return this;
        }

        CliOptions build() {
            return new CliOptions(this);
        }
    }
}
