package com.kevinandrey.simulation.bouncing.domain;

/**
 * Elastic reflection rule applied before every unit displacement.
 *
 * <p>A walker standing on an edge and facing outwards reverses its direction first and then moves one mark.
 * This reproduces the semantics of the original 2017 simulation exactly.</p>
 */
public final class BounceRule {

    private BounceRule() {
    }

    /**
     * Returns the direction to use for the next displacement.
     *
     * @param position  current position
     * @param direction current direction
     * @param length    highest mark {@code N}
     * @return the direction of the next displacement
     */
    public static Direction nextDirection(long position, Direction direction, long length) {
        if (direction == Direction.FORWARD && position >= length) {
            return Direction.BACKWARD;
        }
        if (direction == Direction.BACKWARD && position <= 0) {
            return Direction.FORWARD;
        }

        return direction;
    }

    /**
     * Applies one step of the walk.
     *
     * @param state  current state
     * @param length highest mark {@code N}
     * @return the state after one step
     */
    public static RulerState advance(RulerState state, long length) {
        Direction next = nextDirection(state.position(), state.direction(), length);

        return new RulerState(state.position() + next.sign(), next, state.step() + 1);
    }
}
