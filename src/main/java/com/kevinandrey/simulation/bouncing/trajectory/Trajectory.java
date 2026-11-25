package com.kevinandrey.simulation.bouncing.trajectory;

import com.kevinandrey.simulation.bouncing.domain.BounceRule;
import com.kevinandrey.simulation.bouncing.domain.RulerConfiguration;
import com.kevinandrey.simulation.bouncing.domain.RulerState;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Lazy sequence of the {@code M + 1} states visited by the walker, from step {@code 0} to step {@code M}.
 *
 * <p>States are generated on demand, so iterating a long trajectory requires {@code O(1)} memory.</p>
 */
public final class Trajectory implements Iterable<RulerState> {

    private final RulerConfiguration configuration;

    public Trajectory(RulerConfiguration configuration) {
        if (configuration == null) {
            throw new IllegalArgumentException("configuration is required");
        }
        this.configuration = configuration;
    }

    public RulerConfiguration configuration() {
        return configuration;
    }

    /**
     * Returns the number of states produced by the iterator.
     *
     * @return {@code M + 1}
     */
    public long size() {
        return configuration.steps() + 1;
    }

    @Override
    public Iterator<RulerState> iterator() {
        return new StateIterator(configuration);
    }

    private static final class StateIterator implements Iterator<RulerState> {

        private final long length;
        private final long lastStep;
        private RulerState next;

        StateIterator(RulerConfiguration configuration) {
            this.length = configuration.length();
            this.lastStep = configuration.steps();
            this.next = configuration.initialState();
        }

        @Override
        public boolean hasNext() {
            return next != null;
        }

        @Override
        public RulerState next() {
            if (next == null) {
                throw new NoSuchElementException("trajectory exhausted");
            }

            RulerState current = next;
            next = current.step() < lastStep ? BounceRule.advance(current, length) : null;

            return current;
        }
    }
}
