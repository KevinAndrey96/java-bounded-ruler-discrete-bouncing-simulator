package com.kevinandrey.simulation.bouncing.scenario;

import com.kevinandrey.simulation.bouncing.domain.Direction;
import com.kevinandrey.simulation.bouncing.domain.InvalidConfigurationException;
import com.kevinandrey.simulation.bouncing.domain.RulerConfiguration;
import com.kevinandrey.simulation.bouncing.solver.SolverCatalog;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScenarioTest {

    private static final String HEADER = "name,length,start,direction,steps,expected_position,expected_direction\n";

    private final ScenarioCsvLoader loader = new ScenarioCsvLoader();
    private final ScenarioRunner runner = new ScenarioRunner(SolverCatalog.defaults().all());

    @Test
    void parsesScenariosIgnoringCommentsAndBlankLines() throws IOException {
        List<Scenario> scenarios = loader.load(new StringReader(
                "# comment\n\n" + HEADER + "example,5,4,1,4,2,-1\nwall, 5, 5, forward, 1, 4, backward\n"));

        assertEquals(2, scenarios.size());
        assertEquals("example", scenarios.get(0).name());
        assertEquals(RulerConfiguration.of(5, 4, 1, 4), scenarios.get(0).configuration());
        assertEquals(2, scenarios.get(0).expectedPosition());
        assertEquals(Direction.BACKWARD, scenarios.get(0).expectedDirection());
        assertEquals(Direction.BACKWARD, scenarios.get(1).expectedDirection());
    }

    @Test
    void reportsLineNumbersOnMalformedInput() {
        InvalidConfigurationException columns = assertThrows(InvalidConfigurationException.class,
                () -> loader.load(new StringReader(HEADER + "broken,5,4\n")));
        assertEquals("scenario line 2: expected 7 columns, got 3", columns.getMessage());

        InvalidConfigurationException number = assertThrows(InvalidConfigurationException.class,
                () -> loader.load(new StringReader(HEADER + "nan,five,4,1,4,2,-1\n")));
        assertTrue(number.getMessage().startsWith("scenario line 2: "));

        InvalidConfigurationException domain = assertThrows(InvalidConfigurationException.class,
                () -> loader.load(new StringReader(HEADER + "outside,5,9,1,4,2,-1\n")));
        assertEquals("scenario line 2: start must be inside [0, 5]: got 9", domain.getMessage());
    }

    @Test
    void bundledReferenceScenariosPassWithEverySolver() throws IOException {
        Path source = Paths.get("scenarios", "reference-scenarios.csv");
        List<ScenarioOutcome> outcomes = runner.run(loader.load(source));

        assertEquals(14, outcomes.size());
        for (ScenarioOutcome outcome : outcomes) {
            assertTrue(outcome.passed(), outcome.scenario().name() + " " + outcome.statesBySolver());
            assertTrue(outcome.statesBySolver().containsKey("analytical"));
        }
    }

    @Test
    void expensiveSolversAreSkippedForAstronomicalInputs() {
        Scenario huge = new Scenario("huge", RulerConfiguration.of(1_000_000_000_000L, 1, -1, 999_999_999_999_999L),
                2, Direction.BACKWARD);
        ScenarioOutcome outcome = runner.run(Collections.singletonList(huge)).get(0);

        assertEquals(Collections.singleton("analytical"), outcome.statesBySolver().keySet());
        assertTrue(outcome.passed());
    }

    @Test
    void wrongExpectationFails() {
        Scenario wrong = new Scenario("wrong", RulerConfiguration.of(5, 4, 1, 4), 3, Direction.BACKWARD);

        assertFalse(runner.run(Collections.singletonList(wrong)).get(0).passed());
    }

    @Test
    void runnerRequiresSolvers() {
        assertThrows(IllegalArgumentException.class, () -> new ScenarioRunner(null));
    }
}
