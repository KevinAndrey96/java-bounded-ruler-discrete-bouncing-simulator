package com.kevinandrey.simulation.bouncing.cli;

import com.kevinandrey.simulation.bouncing.benchmark.BenchmarkReport;
import com.kevinandrey.simulation.bouncing.benchmark.BenchmarkResult;
import com.kevinandrey.simulation.bouncing.benchmark.SolverBenchmark;
import com.kevinandrey.simulation.bouncing.domain.InvalidConfigurationException;
import com.kevinandrey.simulation.bouncing.domain.RulerConfiguration;
import com.kevinandrey.simulation.bouncing.domain.RulerState;
import com.kevinandrey.simulation.bouncing.gui.RulerDashboardFrame;
import com.kevinandrey.simulation.bouncing.scenario.Scenario;
import com.kevinandrey.simulation.bouncing.scenario.ScenarioCsvLoader;
import com.kevinandrey.simulation.bouncing.scenario.ScenarioOutcome;
import com.kevinandrey.simulation.bouncing.scenario.ScenarioRunner;
import com.kevinandrey.simulation.bouncing.solver.BouncingSolver;
import com.kevinandrey.simulation.bouncing.solver.SolverCatalog;
import com.kevinandrey.simulation.bouncing.trajectory.Trajectory;
import com.kevinandrey.simulation.bouncing.trajectory.TrajectoryCsvExporter;

import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Headless command line entry point.
 *
 * <p>Exit codes: {@code 0} success, {@code 1} verification failure, {@code 2} usage or validation error,
 * {@code 3} I/O error.</p>
 */
public final class BouncingCli {

    static final int EXIT_OK = 0;
    static final int EXIT_VERIFICATION_FAILED = 1;
    static final int EXIT_USAGE = 2;
    static final int EXIT_IO = 3;

    static final long MAX_TRACE_STEPS = 10_000L;

    private static final String RULE = "==============================================================";

    private final SolverCatalog catalog;
    private final CliParser parser;
    private final TrajectoryCsvExporter exporter;
    private final ScenarioCsvLoader scenarioLoader;

    public BouncingCli(SolverCatalog catalog, CliParser parser, TrajectoryCsvExporter exporter,
                       ScenarioCsvLoader scenarioLoader) {
        this.catalog = catalog;
        this.parser = parser;
        this.exporter = exporter;
        this.scenarioLoader = scenarioLoader;
    }

    public static void main(String[] args) {
        BouncingCli cli = new BouncingCli(
                SolverCatalog.defaults(), new CliParser(), new TrajectoryCsvExporter(), new ScenarioCsvLoader());
        int code = cli.run(args, System.out, System.err);
        if (code != EXIT_OK) {
            System.exit(code);
        }
    }

    /**
     * Executes the command line and reports through the given streams.
     *
     * @param args raw arguments
     * @param out  standard output
     * @param err  error output
     * @return the process exit code
     */
    public int run(String[] args, PrintStream out, PrintStream err) {
        CliOptions options;
        try {
            options = parser.parse(args);
        } catch (CliUsageException e) {
            err.println("error: " + e.getMessage());
            err.println("run with --help to list the available options");

            return EXIT_USAGE;
        }

        if (options.help()) {
            printHelp(out);

            return EXIT_OK;
        }

        try {
            return dispatch(options, out, err);
        } catch (InvalidConfigurationException e) {
            err.println("error: invalid configuration: " + e.getMessage());

            return EXIT_USAGE;
        } catch (IllegalArgumentException e) {
            err.println("error: " + e.getMessage());

            return EXIT_USAGE;
        } catch (IOException e) {
            err.println("error: io: " + e.getMessage());

            return EXIT_IO;
        }
    }

    private int dispatch(CliOptions options, PrintStream out, PrintStream err) throws IOException {
        if (options.scenarios() != null) {
            return runScenarios(Paths.get(options.scenarios()), out);
        }

        RulerConfiguration configuration = RulerConfiguration.of(
                options.length(), options.start(), options.direction(), options.steps());

        if (options.gui()) {
            return launchGui(configuration, err);
        }
        if (options.trace() && configuration.steps() > MAX_TRACE_STEPS) {
            throw new IllegalArgumentException(
                    "trace limited to " + MAX_TRACE_STEPS + " steps: got " + configuration.steps());
        }
        if (options.exportCsv() != null && configuration.steps() > TrajectoryCsvExporter.MAX_EXPORT_STEPS) {
            throw new IllegalArgumentException("trajectory export limited to "
                    + TrajectoryCsvExporter.MAX_EXPORT_STEPS + " steps: got " + configuration.steps());
        }

        printBanner(out);
        out.println("Configuration   : " + configuration);
        out.println("Period (2N)     : " + configuration.period());

        int code = options.benchmark()
                ? runBenchmark(configuration, options.iterations(), out)
                : runSolver(configuration, catalog.byName(options.solver()), out);

        if (options.trace()) {
            printTrace(configuration, out);
        }
        if (options.exportCsv() != null) {
            Path target = Paths.get(options.exportCsv());
            long rows = exporter.write(new Trajectory(configuration), target);
            out.println("Trajectory CSV  : " + target + " (" + rows + " states)");
        }

        return code;
    }

    private int runSolver(RulerConfiguration configuration, BouncingSolver solver, PrintStream out) {
        RulerState state = solver.solve(configuration);
        out.println("Solver          : " + solver.name() + " " + solver.complexity());
        out.println("Final Position  : " + state.position());
        out.println("Final Direction : " + state.direction().signedLabel() + " (" + state.direction() + ")");

        return EXIT_OK;
    }

    private int runBenchmark(RulerConfiguration configuration, int iterations, PrintStream out) {
        BenchmarkReport report = new SolverBenchmark(iterations).run(configuration, catalog.all());
        out.println("Benchmark       : best of " + iterations + " timed runs per solver");
        out.println(String.format(Locale.ROOT, "%-12s %-15s %20s %10s %16s %14s",
                "solver", "complexity", "position", "direction", "time_ns", "speedup"));
        for (BenchmarkResult result : report.results()) {
            out.println(String.format(Locale.ROOT, "%-12s %-15s %20d %10s %16d %13.1fx",
                    result.solverName(), result.complexity(), result.state().position(),
                    result.state().direction().signedLabel(), result.bestNanos(), report.speedup(result)));
        }
        for (String skipped : report.skipped()) {
            out.println(String.format(Locale.ROOT, "%-12s skipped: replay cost above %d steps",
                    skipped, SolverBenchmark.DEFAULT_MAX_REPLAY_STEPS));
        }

        RulerState reference = report.baseline().state();
        out.println("Final Position  : " + reference.position());
        out.println("Final Direction : " + reference.direction().signedLabel() + " (" + reference.direction() + ")");
        if (!report.consistent()) {
            out.println("Consistency     : FAIL (solvers disagree)");

            return EXIT_VERIFICATION_FAILED;
        }
        out.println("Consistency     : PASS (all measured solvers agree)");

        return EXIT_OK;
    }

    private int runScenarios(Path source, PrintStream out) throws IOException {
        List<Scenario> scenarios = scenarioLoader.load(source);
        List<ScenarioOutcome> outcomes = new ScenarioRunner(catalog.all()).run(scenarios);

        printBanner(out);
        out.println("Scenario file   : " + source);
        int passed = 0;
        for (ScenarioOutcome outcome : outcomes) {
            Scenario scenario = outcome.scenario();
            String status = outcome.passed() ? "PASS" : "FAIL";
            if (outcome.passed()) {
                passed++;
            }
            out.println(String.format(Locale.ROOT, "[%s] %-28s %s -> expected P=%d d=%s, solvers %s",
                    status, scenario.name(), scenario.configuration(), scenario.expectedPosition(),
                    scenario.expectedDirection().signedLabel(), describe(outcome.statesBySolver())));
        }
        out.println("Scenarios       : " + passed + "/" + outcomes.size() + " passed");

        return passed == outcomes.size() ? EXIT_OK : EXIT_VERIFICATION_FAILED;
    }

    private static String describe(Map<String, RulerState> states) {
        StringBuilder text = new StringBuilder();
        for (Map.Entry<String, RulerState> entry : states.entrySet()) {
            if (text.length() > 0) {
                text.append(' ');
            }
            text.append(entry.getKey()).append('=')
                    .append(entry.getValue().position()).append('/')
                    .append(entry.getValue().direction().signedLabel());
        }

        return text.toString();
    }

    private static void printTrace(RulerConfiguration configuration, PrintStream out) {
        out.println("Trajectory      :");
        for (RulerState state : new Trajectory(configuration)) {
            out.println(String.format(Locale.ROOT, "  step %6d  position %6d  direction %s",
                    state.step(), state.position(), state.direction().signedLabel()));
        }
    }

    private static int launchGui(RulerConfiguration configuration, PrintStream err) {
        if (GraphicsEnvironment.isHeadless()) {
            err.println("error: gui requires a graphical environment, use the headless options instead");

            return EXIT_USAGE;
        }
        RulerDashboardFrame.launch(configuration);

        return EXIT_OK;
    }

    private static void printBanner(PrintStream out) {
        out.println(RULE);
        out.println(" Bounded Ruler Discrete Bouncing Simulator");
        out.println(RULE);
    }

    private void printHelp(PrintStream out) {
        out.println("Usage: java -jar bounded-ruler-simulator.jar [options]");
        out.println();
        out.println("Problem options (defaults reproduce the original example N=5, P=4, d=+1, M=4):");
        out.println("  -n, --length <N>        highest mark of the ruler, N >= 1");
        out.println("  -p, --start <P>         initial position, 0 <= P <= N");
        out.println("  -d, --direction <d>     initial direction: 1, -1, forward or backward");
        out.println("  -m, --steps <M>         number of steps after the initial instant, M >= 0");
        out.println();
        out.println("Execution options:");
        out.println("  -s, --solver <name>     one of " + catalog.names() + " (default analytical)");
        out.println("  -b, --benchmark         compare every solver and verify they agree");
        out.println("      --iterations <k>    timed runs per solver in benchmark mode (default "
                + CliOptions.DEFAULT_ITERATIONS + ")");
        out.println("      --trace             print every visited state (M <= " + MAX_TRACE_STEPS + ")");
        out.println("      --export-csv <path> export the trajectory as CSV (M <= "
                + TrajectoryCsvExporter.MAX_EXPORT_STEPS + ")");
        out.println("      --scenarios <path>  evaluate a reference scenario CSV with every solver");
        out.println("      --gui               open the interactive Swing dashboard");
        out.println("  -h, --help              show this help");
    }
}
