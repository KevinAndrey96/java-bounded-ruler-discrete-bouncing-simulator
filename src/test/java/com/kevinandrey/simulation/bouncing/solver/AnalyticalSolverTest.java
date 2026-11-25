package com.kevinandrey.simulation.bouncing.solver;

import com.kevinandrey.simulation.bouncing.domain.Direction;
import com.kevinandrey.simulation.bouncing.domain.RulerConfiguration;
import com.kevinandrey.simulation.bouncing.domain.RulerState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AnalyticalSolverTest {

    private final AnalyticalSolver solver = new AnalyticalSolver();

    @Test
    void reproducesTheOriginalProblemStatementExample() {
        RulerState state = solver.solve(RulerConfiguration.of(5, 4, 1, 4));

        assertEquals(2, state.position());
        assertEquals(Direction.BACKWARD, state.direction());
        assertEquals(4, state.step());
    }

    @ParameterizedTest(name = "N={0} P={1} d={2} M={3} -> P={4} d={5}")
    @CsvSource({
        "5, 4, 1, 0, 4, 1",
        "5, 4, 1, 1, 5, 1",
        "5, 4, 1, 2, 4, -1",
        "5, 0, -1, 1, 1, 1",
        "5, 5, -1, 1, 4, -1",
        "5, 0, 1, 10, 0, -1",
        "5, 0, 1, 5, 5, 1",
        "1, 0, 1, 3, 1, 1",
        "1, 1, 1, 3, 0, -1",
        "10, 3, -1, 1000003, 0, -1"
    })
    void matchesHandComputedCases(long length, long start, int sign, long steps, long position, int finalSign) {
        RulerState state = solver.solve(RulerConfiguration.of(length, start, sign, steps));

        assertEquals(position, state.position());
        assertEquals(Direction.fromSign(finalSign), state.direction());
    }

    @Test
    void zeroStepsReturnsTheInitialStateEvenFacingAWall() {
        RulerConfiguration configuration = RulerConfiguration.of(8, 8, 1, 0);

        assertEquals(configuration.initialState(), solver.solve(configuration));
    }

    @Test
    void largestSupportedInputDoesNotOverflow() {
        RulerState state = solver.solve(
                RulerConfiguration.of(RulerConfiguration.MAX_LENGTH, 0, 1, Long.MAX_VALUE));

        assertEquals(3, state.position());
        assertEquals(Direction.FORWARD, state.direction());
    }

    @Test
    void positionAlwaysStaysInsideTheRuler() {
        long length = RulerConfiguration.MAX_LENGTH;
        for (long start : new long[] {0, 1, length / 2, length - 1, length}) {
            for (Direction direction : Direction.values()) {
                RulerState state = solver.solve(RulerConfiguration.of(length, start, direction, Long.MAX_VALUE));

                assertTrue(state.position() >= 0 && state.position() <= length, state.toString());
            }
        }
    }

    @Test
    void triangularWaveFoldsThePhase() {
        assertEquals(0, AnalyticalSolver.triangularWave(0, 5));
        assertEquals(5, AnalyticalSolver.triangularWave(5, 5));
        assertEquals(4, AnalyticalSolver.triangularWave(6, 5));
        assertEquals(1, AnalyticalSolver.triangularWave(9, 5));
    }

    @Test
    void initialPhaseUnfoldsBackwardStates() {
        assertEquals(3, AnalyticalSolver.initialPhase(3, Direction.FORWARD, 10));
        assertEquals(7, AnalyticalSolver.initialPhase(3, Direction.BACKWARD, 10));
        assertEquals(0, AnalyticalSolver.initialPhase(0, Direction.BACKWARD, 10));
        assertEquals(5, AnalyticalSolver.initialPhase(5, Direction.BACKWARD, 10));
    }

    @Test
    void metadata() {
        assertEquals("analytical", solver.name());
        assertEquals("O(1)", solver.complexity());
        assertEquals(0, solver.replaySteps(RulerConfiguration.of(5, 1, 1, Long.MAX_VALUE)));
    }
}
