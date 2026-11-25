package com.kevinandrey.simulation.bouncing.scenario;

import com.kevinandrey.simulation.bouncing.domain.RulerState;
import com.kevinandrey.simulation.bouncing.solver.BouncingSolver;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Evaluates reference scenarios against a set of solvers.
 *
 * <p>Solvers whose replay cost exceeds {@link #MAX_REPLAY_STEPS} are skipped for that scenario, so cases with
 * astronomically large {@code N} or {@code M} remain verifiable through the closed-form strategy.</p>
 */
public final class ScenarioRunner {

    /**
     * Largest number of unit steps a solver may replay during scenario evaluation.
     */
    public static final long MAX_REPLAY_STEPS = 50_000_000L;

    private final List<BouncingSolver> solvers;

    public ScenarioRunner(List<BouncingSolver> solvers) {
        if (solvers == null || solvers.isEmpty()) {
            throw new IllegalArgumentException("scenario runner requires at least one solver");
        }
        this.solvers = new ArrayList<>(solvers);
    }

    /**
     * Evaluates all scenarios.
     *
     * @param scenarios reference cases
     * @return one outcome per scenario, in input order
     */
    public List<ScenarioOutcome> run(List<Scenario> scenarios) {
        List<ScenarioOutcome> outcomes = new ArrayList<>();
        for (Scenario scenario : scenarios) {
            outcomes.add(evaluate(scenario));
        }

        return outcomes;
    }

    private ScenarioOutcome evaluate(Scenario scenario) {
        Map<String, RulerState> states = new LinkedHashMap<>();
        for (BouncingSolver solver : solvers) {
            if (solver.replaySteps(scenario.configuration()) > MAX_REPLAY_STEPS) {
                continue;
            }
            states.put(solver.name(), solver.solve(scenario.configuration()));
        }

        return new ScenarioOutcome(scenario, states);
    }
}
