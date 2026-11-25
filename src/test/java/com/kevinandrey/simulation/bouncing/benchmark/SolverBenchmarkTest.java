package com.kevinandrey.simulation.bouncing.benchmark;

import com.kevinandrey.simulation.bouncing.domain.Direction;
import com.kevinandrey.simulation.bouncing.domain.RulerConfiguration;
import com.kevinandrey.simulation.bouncing.domain.RulerState;
import com.kevinandrey.simulation.bouncing.solver.BouncingSolver;
import com.kevinandrey.simulation.bouncing.solver.SolverCatalog;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SolverBenchmarkTest {

    @Test
    void measuresEverySolverAndChecksConsistency() {
        RulerConfiguration configuration = RulerConfiguration.of(37, 5, -1, 250_000);
        BenchmarkReport report = new SolverBenchmark(2).run(configuration, SolverCatalog.defaults().all());

        assertEquals(3, report.results().size());
        assertEquals("step", report.baseline().solverName());
        assertTrue(report.consistent());
        assertTrue(report.skipped().isEmpty());
        assertEquals(2, report.iterations());
        assertEquals(configuration, report.configuration());
        for (BenchmarkResult result : report.results()) {
            assertTrue(result.bestNanos() >= 1);
            assertTrue(report.speedup(result) > 0);
        }
        assertEquals(1.0, report.speedup(report.baseline()), 1e-12);
    }

    @Test
    void skipsSolversAboveTheReplayBudget() {
        RulerConfiguration configuration = RulerConfiguration.of(10, 0, 1, 1_000_000);
        BenchmarkReport report = new SolverBenchmark(1, 100).run(configuration, SolverCatalog.defaults().all());

        assertEquals(Collections.singletonList("step"), report.skipped());
        assertEquals("periodic", report.baseline().solverName());
        assertTrue(report.consistent());
    }

    @Test
    void detectsInconsistentSolvers() {
        BouncingSolver liar = new BouncingSolver() {
            @Override
            public String name() {
                return "liar";
            }

            @Override
            public String complexity() {
                return "O(1)";
            }

            @Override
            public long replaySteps(RulerConfiguration configuration) {
                return 0;
            }

            @Override
            public RulerState solve(RulerConfiguration configuration) {
                return new RulerState(0, Direction.FORWARD, configuration.steps());
            }
        };
        BenchmarkReport report = new SolverBenchmark(1).run(RulerConfiguration.of(5, 4, 1, 4),
                Arrays.asList(SolverCatalog.defaults().byName("analytical"), liar));

        assertFalse(report.consistent());
    }

    @Test
    void rejectsInvalidSetup() {
        assertThrows(IllegalArgumentException.class, () -> new SolverBenchmark(0));
        assertThrows(IllegalArgumentException.class, () -> new SolverBenchmark(1, -1));
        assertThrows(IllegalArgumentException.class,
                () -> new SolverBenchmark(1).run(RulerConfiguration.of(5, 4, 1, 4), null));
        assertThrows(IllegalArgumentException.class, () -> new BenchmarkReport(
                RulerConfiguration.of(5, 4, 1, 4), 1, Collections.<BenchmarkResult>emptyList(), null));
    }
}
