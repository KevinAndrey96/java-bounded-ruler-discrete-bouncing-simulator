package com.kevinandrey.simulation.bouncing.solver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Immutable registry of available solvers, resolved by name.
 */
public final class SolverCatalog {

    private final List<BouncingSolver> solvers;

    public SolverCatalog(List<BouncingSolver> solvers) {
        if (solvers == null || solvers.isEmpty()) {
            throw new IllegalArgumentException("solver catalog requires at least one solver");
        }
        this.solvers = Collections.unmodifiableList(new ArrayList<>(solvers));
    }

    /**
     * Builds the catalog with the three built-in strategies ordered from slowest to fastest.
     *
     * @return the default catalog
     */
    public static SolverCatalog defaults() {
        return new SolverCatalog(Arrays.<BouncingSolver>asList(
                new StepByStepSolver(), new PeriodicReductionSolver(), new AnalyticalSolver()));
    }

    public List<BouncingSolver> all() {
        return solvers;
    }

    /**
     * Resolves a solver by its name.
     *
     * @param name solver name, case insensitive
     * @return the solver
     * @throws IllegalArgumentException when no solver matches
     */
    public BouncingSolver byName(String name) {
        String normalized = name == null ? "" : name.trim().toLowerCase(Locale.ROOT);
        for (BouncingSolver solver : solvers) {
            if (solver.name().equals(normalized)) {
                return solver;
            }
        }

        throw new IllegalArgumentException("unknown solver " + name + ": expected one of " + names());
    }

    public List<String> names() {
        List<String> names = new ArrayList<>();
        for (BouncingSolver solver : solvers) {
            names.add(solver.name());
        }

        return names;
    }
}
