package com.kevinandrey.simulation.bouncing.solver;

import com.kevinandrey.simulation.bouncing.domain.RulerConfiguration;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SolverCatalogTest {

    @Test
    void defaultsAreOrderedFromSlowestToFastest() {
        SolverCatalog catalog = SolverCatalog.defaults();

        assertEquals(Arrays.asList("step", "periodic", "analytical"), catalog.names());
        assertEquals("O(M)", catalog.byName("step").complexity());
        assertEquals("O(min(M, 2N))", catalog.byName(" PERIODIC ").complexity());
        assertEquals("O(1)", catalog.byName("analytical").complexity());
    }

    @Test
    void unknownSolverIsRejected() {
        IllegalArgumentException error =
                assertThrows(IllegalArgumentException.class, () -> SolverCatalog.defaults().byName("quantum"));
        assertEquals("unknown solver quantum: expected one of [step, periodic, analytical]", error.getMessage());
    }

    @Test
    void emptyCatalogIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new SolverCatalog(Collections.<BouncingSolver>emptyList()));
        assertThrows(IllegalArgumentException.class, () -> new SolverCatalog(null));
    }

    @Test
    void replayCostsReflectComplexity() {
        RulerConfiguration configuration = RulerConfiguration.of(10, 2, 1, 1_000_000);

        assertEquals(1_000_000, new StepByStepSolver().replaySteps(configuration));
        assertEquals(20, new PeriodicReductionSolver().replaySteps(configuration));
        assertEquals(0, new PeriodicReductionSolver().replaySteps(configuration.withSteps(0)));
        assertEquals(7, new PeriodicReductionSolver().replaySteps(configuration.withSteps(7)));
    }
}
