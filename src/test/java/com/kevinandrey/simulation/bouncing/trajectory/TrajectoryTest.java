package com.kevinandrey.simulation.bouncing.trajectory;

import com.kevinandrey.simulation.bouncing.domain.RulerConfiguration;
import com.kevinandrey.simulation.bouncing.domain.RulerState;
import com.kevinandrey.simulation.bouncing.solver.AnalyticalSolver;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrajectoryTest {

    @Test
    void producesEveryStateOfTheOriginalExample() {
        List<Long> positions = new ArrayList<>();
        for (RulerState state : new Trajectory(RulerConfiguration.of(5, 4, 1, 4))) {
            positions.add(state.position());
        }

        assertEquals(Arrays.asList(4L, 5L, 4L, 3L, 2L), positions);
    }

    @Test
    void consecutiveStatesDifferByOneMarkAndMatchTheClosedForm() {
        RulerConfiguration configuration = RulerConfiguration.of(7, 0, -1, 200);
        AnalyticalSolver analytical = new AnalyticalSolver();
        RulerState previous = null;
        for (RulerState state : new Trajectory(configuration)) {
            assertEquals(analytical.solve(configuration.withSteps(state.step())), state);
            assertTrue(state.position() >= 0 && state.position() <= 7);
            if (previous != null) {
                assertEquals(1, Math.abs(state.position() - previous.position()));
                assertEquals(previous.step() + 1, state.step());
            }
            previous = state;
        }

        assertEquals(201, new Trajectory(configuration).size());
    }

    @Test
    void iteratorIsExhaustedAfterTheLastStep() {
        Iterator<RulerState> iterator = new Trajectory(RulerConfiguration.of(3, 1, 1, 0)).iterator();

        assertTrue(iterator.hasNext());
        assertEquals(0, iterator.next().step());
        assertFalse(iterator.hasNext());
        assertThrows(NoSuchElementException.class, iterator::next);
    }

    @Test
    void requiresConfiguration() {
        assertThrows(IllegalArgumentException.class, () -> new Trajectory(null));
    }
}
