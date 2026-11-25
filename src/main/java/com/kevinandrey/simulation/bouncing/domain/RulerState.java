package com.kevinandrey.simulation.bouncing.domain;

import java.util.Objects;

/**
 * Snapshot of the walker after a given number of steps.
 *
 * <p>The direction is the one carried by the walker at that instant. After at least one step it equals the
 * direction of the last displacement, which is the convention of the original simulation.</p>
 */
public final class RulerState {

    private final long position;
    private final Direction direction;
    private final long step;

    public RulerState(long position, Direction direction, long step) {
        this.position = position;
        this.direction = Objects.requireNonNull(direction, "direction");
        this.step = step;
    }

    public long position() {
        return position;
    }

    public Direction direction() {
        return direction;
    }

    public long step() {
        return step;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof RulerState)) {
            return false;
        }

        RulerState that = (RulerState) other;

        return position == that.position && step == that.step && direction == that.direction;
    }

    @Override
    public int hashCode() {
        return Objects.hash(position, direction, step);
    }

    @Override
    public String toString() {
        return "step=" + step + ", position=" + position + ", direction=" + direction.signedLabel();
    }
}
