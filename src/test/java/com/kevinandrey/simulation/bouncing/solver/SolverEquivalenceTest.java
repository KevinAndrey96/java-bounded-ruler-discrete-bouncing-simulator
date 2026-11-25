package com.kevinandrey.simulation.bouncing.solver;

import com.kevinandrey.simulation.bouncing.domain.Direction;
import com.kevinandrey.simulation.bouncing.domain.RulerConfiguration;
import com.kevinandrey.simulation.bouncing.domain.RulerState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Proves that the three strategies are observationally equivalent over an exhaustive grid and random samples.
 */
class SolverEquivalenceTest {

    private final BouncingSolver step = new StepByStepSolver();
    private final BouncingSolver periodic = new PeriodicReductionSolver();
    private final BouncingSolver analytical = new AnalyticalSolver();

    @ParameterizedTest(name = "N={0}")
    @ValueSource(longs = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 17, 24, 31})
    void exhaustiveGridAgrees(long length) {
        long maxSteps = 4 * length + 5;
        for (long start = 0; start <= length; start++) {
            for (Direction direction : Direction.values()) {
                for (long steps = 0; steps <= maxSteps; steps++) {
                    RulerConfiguration configuration = RulerConfiguration.of(length, start, direction, steps);
                    RulerState expected = step.solve(configuration);

                    assertEquals(expected, periodic.solve(configuration), configuration.toString());
                    assertEquals(expected, analytical.solve(configuration), configuration.toString());
                }
            }
        }
    }

    @Test
    void randomSamplesAgree() {
        Random random = new Random(20171125L);
        for (int sample = 0; sample < 2_000; sample++) {
            long length = 1 + random.nextInt(500);
            long start = (long) random.nextInt((int) length + 1);
            Direction direction = random.nextBoolean() ? Direction.FORWARD : Direction.BACKWARD;
            long steps = random.nextInt(20_000);
            RulerConfiguration configuration = RulerConfiguration.of(length, start, direction, steps);
            RulerState expected = step.solve(configuration);

            assertEquals(expected, periodic.solve(configuration), configuration.toString());
            assertEquals(expected, analytical.solve(configuration), configuration.toString());
        }
    }

    @Test
    void periodicAndAnalyticalAgreeOnAstronomicalStepCounts() {
        for (long length = 1; length <= 64; length++) {
            for (long start = 0; start <= length; start++) {
                for (Direction direction : Direction.values()) {
                    RulerConfiguration configuration =
                            RulerConfiguration.of(length, start, direction, Long.MAX_VALUE - start);

                    assertEquals(periodic.solve(configuration), analytical.solve(configuration),
                            configuration.toString());
                }
            }
        }
    }
}
