package com.kevinandrey.simulation.bouncing.domain;

import java.util.Objects;

/**
 * Immutable and validated input of the bounded ruler problem.
 *
 * <ul>
 *   <li>{@code N}: highest mark of the ruler, the walk happens over {@code [0, N]}</li>
 *   <li>{@code P}: initial position, {@code 0 <= P <= N}</li>
 *   <li>{@code d}: initial direction, {@code +1} or {@code -1}</li>
 *   <li>{@code M}: number of steps performed after the initial instant, {@code M >= 0}</li>
 * </ul>
 */
public final class RulerConfiguration {

    /**
     * Upper bound of {@code N} that keeps {@code 4N} representable as a signed 64-bit integer.
     */
    public static final long MAX_LENGTH = Long.MAX_VALUE / 4;

    private final long length;
    private final long start;
    private final Direction direction;
    private final long steps;

    private RulerConfiguration(long length, long start, Direction direction, long steps) {
        this.length = length;
        this.start = start;
        this.direction = direction;
        this.steps = steps;
    }

    /**
     * Creates a validated configuration.
     *
     * @param length    highest mark {@code N}, between {@code 1} and {@link #MAX_LENGTH}
     * @param start     initial position {@code P} inside {@code [0, N]}
     * @param direction initial direction {@code d}
     * @param steps     number of steps {@code M}, non negative
     * @return the configuration
     * @throws InvalidConfigurationException when any invariant is violated
     */
    public static RulerConfiguration of(long length, long start, Direction direction, long steps) {
        if (length < 1 || length > MAX_LENGTH) {
            throw new InvalidConfigurationException("length must be between 1 and " + MAX_LENGTH + ": got " + length);
        }
        if (start < 0 || start > length) {
            throw new InvalidConfigurationException("start must be inside [0, " + length + "]: got " + start);
        }
        if (direction == null) {
            throw new InvalidConfigurationException("direction is required");
        }
        if (steps < 0) {
            throw new InvalidConfigurationException("steps must be non negative: got " + steps);
        }

        return new RulerConfiguration(length, start, direction, steps);
    }

    /**
     * Creates a validated configuration from a numeric direction sign.
     *
     * @param length        highest mark {@code N}
     * @param start         initial position {@code P}
     * @param directionSign {@code 1} or {@code -1}
     * @param steps         number of steps {@code M}
     * @return the configuration
     */
    public static RulerConfiguration of(long length, long start, int directionSign, long steps) {
        return of(length, start, Direction.fromSign(directionSign), steps);
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

    /**
     * Returns the fundamental period {@code 2N} of the reflected walk.
     *
     * @return the period
     */
    public long period() {
        return 2 * length;
    }

    /**
     * Returns the initial state at step zero.
     *
     * @return the initial state
     */
    public RulerState initialState() {
        return new RulerState(start, direction, 0);
    }

    /**
     * Returns a copy of this configuration with a different number of steps.
     *
     * @param newSteps the new number of steps
     * @return the new configuration
     */
    public RulerConfiguration withSteps(long newSteps) {
        return of(length, start, direction, newSteps);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof RulerConfiguration)) {
            return false;
        }

        RulerConfiguration that = (RulerConfiguration) other;

        return length == that.length && start == that.start && steps == that.steps && direction == that.direction;
    }

    @Override
    public int hashCode() {
        return Objects.hash(length, start, direction, steps);
    }

    @Override
    public String toString() {
        return "N=" + length + ", P=" + start + ", d=" + direction.signedLabel() + ", M=" + steps;
    }
}
