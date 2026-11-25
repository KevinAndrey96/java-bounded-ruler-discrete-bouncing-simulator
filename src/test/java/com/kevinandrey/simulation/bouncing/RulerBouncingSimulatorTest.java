package com.kevinandrey.simulation.bouncing;

import com.kevinandrey.simulation.bouncing.domain.Direction;
import com.kevinandrey.simulation.bouncing.domain.InvalidConfigurationException;
import com.kevinandrey.simulation.bouncing.domain.RulerConfiguration;
import com.kevinandrey.simulation.bouncing.domain.RulerState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RulerBouncingSimulatorTest {

    private final RulerBouncingSimulator simulator = new RulerBouncingSimulator();

    @Test
    void facadeStrategiesAgreeOnTheOriginalExample() {
        RulerState expected = new RulerState(2, Direction.BACKWARD, 4);

        assertEquals(expected, simulator.simulateStepByStep(5, 4, 1, 4));
        assertEquals(expected, simulator.solvePeriodic(5, 4, 1, 4));
        assertEquals(expected, simulator.solveAnalytical(5, 4, 1, 4));
    }

    @Test
    void facadeExposesTheTrajectory() {
        assertEquals(5, simulator.trajectory(RulerConfiguration.of(5, 4, 1, 4)).size());
    }

    @Test
    void facadeValidatesInput() {
        assertThrows(InvalidConfigurationException.class, () -> simulator.solveAnalytical(5, 4, 0, 4));
        assertThrows(IllegalArgumentException.class, () -> new RulerBouncingSimulator(null, null, null));
    }
}
