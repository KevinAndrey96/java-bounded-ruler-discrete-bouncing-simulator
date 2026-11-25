package com.kevinandrey.simulation.bouncing.cli;

import com.kevinandrey.simulation.bouncing.scenario.ScenarioCsvLoader;
import com.kevinandrey.simulation.bouncing.solver.SolverCatalog;
import com.kevinandrey.simulation.bouncing.trajectory.TrajectoryCsvExporter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BouncingCliTest {

    private final BouncingCli cli = new BouncingCli(
            SolverCatalog.defaults(), new CliParser(), new TrajectoryCsvExporter(), new ScenarioCsvLoader());

    @Test
    void defaultRunSolvesTheOriginalExample() {
        Result result = execute();

        assertEquals(BouncingCli.EXIT_OK, result.code);
        assertTrue(result.out.contains("Configuration   : N=5, P=4, d=+1, M=4"), result.out);
        assertTrue(result.out.contains("Solver          : analytical O(1)"), result.out);
        assertTrue(result.out.contains("Final Position  : 2"), result.out);
        assertTrue(result.out.contains("Final Direction : -1 (BACKWARD)"), result.out);
    }

    @Test
    void everySolverReportsTheSameAnswer() {
        for (String solver : new String[] {"step", "periodic", "analytical"}) {
            Result result = execute("-n", "10", "-p", "3", "-d", "-1", "-m", "1000003", "-s", solver);

            assertEquals(BouncingCli.EXIT_OK, result.code);
            assertTrue(result.out.contains("Final Position  : 0"), result.out);
            assertTrue(result.out.contains("Final Direction : -1 (BACKWARD)"), result.out);
        }
    }

    @Test
    void benchmarkComparesSolvers() {
        Result result = execute("--length", "50", "--start", "10", "--steps", "100000", "--benchmark",
                "--iterations", "2");

        assertEquals(BouncingCli.EXIT_OK, result.code);
        assertTrue(result.out.contains("Consistency     : PASS"), result.out);
        assertTrue(result.out.contains("step "), result.out);
        assertTrue(result.out.contains("periodic "), result.out);
        assertTrue(result.out.contains("analytical "), result.out);
    }

    @Test
    void benchmarkSkipsTheLinearSolverForAstronomicalSteps() {
        Result result = execute("--steps", "9223372036854775807", "--benchmark", "--iterations", "1");

        assertEquals(BouncingCli.EXIT_OK, result.code);
        assertTrue(result.out.matches("(?s).*\\bstep\\s+skipped: replay cost above.*"), result.out);
    }

    @Test
    void traceAndExportWriteTheTrajectory(@TempDir Path directory) throws IOException {
        Path target = directory.resolve("trajectory.csv");
        Result result = execute("--trace", "--export-csv", target.toString());
        List<String> lines = Files.readAllLines(target, StandardCharsets.UTF_8);

        assertEquals(BouncingCli.EXIT_OK, result.code);
        assertTrue(result.out.contains("step      4  position      2  direction -1"), result.out);
        assertTrue(result.out.contains("(5 states)"), result.out);
        assertEquals("step,position,direction", lines.get(0));
        assertEquals("4,2,-1", lines.get(5));
    }

    @Test
    void scenarioModeEvaluatesTheBundledSeeds() {
        Result result = execute("--scenarios", "scenarios/reference-scenarios.csv");

        assertEquals(BouncingCli.EXIT_OK, result.code, result.out + result.err);
        assertTrue(result.out.contains("Scenarios       : 14/14 passed"), result.out);
        assertFalse(result.out.contains("[FAIL]"), result.out);
    }

    @Test
    void failingScenarioReturnsVerificationExitCode(@TempDir Path directory) throws IOException {
        Path source = directory.resolve("wrong.csv");
        Files.write(source, ("name,length,start,direction,steps,expected_position,expected_direction\n"
                + "wrong,5,4,1,4,3,-1\n").getBytes(StandardCharsets.UTF_8));
        Result result = execute("--scenarios", source.toString());

        assertEquals(BouncingCli.EXIT_VERIFICATION_FAILED, result.code);
        assertTrue(result.out.contains("[FAIL] wrong"), result.out);
    }

    @Test
    void invalidInputReturnsUsageExitCode() {
        Result unknown = execute("--bogus");
        Result outside = execute("-n", "5", "-p", "9");
        Result solver = execute("-s", "quantum");
        Result trace = execute("-m", "10001", "--trace");
        Result export = execute("-m", "1000001", "--export-csv", "never.csv");

        assertEquals(BouncingCli.EXIT_USAGE, unknown.code);
        assertTrue(unknown.err.contains("error: unknown option --bogus"), unknown.err);
        assertEquals(BouncingCli.EXIT_USAGE, outside.code);
        assertTrue(outside.err.contains("error: invalid configuration: start must be inside [0, 5]: got 9"));
        assertEquals(BouncingCli.EXIT_USAGE, solver.code);
        assertEquals(BouncingCli.EXIT_USAGE, trace.code);
        assertTrue(trace.err.contains("trace limited to 10000 steps"), trace.err);
        assertEquals(BouncingCli.EXIT_USAGE, export.code);
        assertFalse(Files.exists(Paths.get("never.csv")));
    }

    @Test
    void missingScenarioFileReturnsIoExitCode() {
        Result result = execute("--scenarios", "does-not-exist.csv");

        assertEquals(BouncingCli.EXIT_IO, result.code);
        assertTrue(result.err.startsWith("error: io: "), result.err);
    }

    @Test
    void guiIsRejectedInHeadlessEnvironments() {
        Result result = execute("--gui");

        assertEquals(BouncingCli.EXIT_USAGE, result.code);
        assertTrue(result.err.contains("gui requires a graphical environment"), result.err);
    }

    @Test
    void helpListsEveryOption() {
        Result result = execute("--help");

        assertEquals(BouncingCli.EXIT_OK, result.code);
        for (String option : new String[] {"--length", "--start", "--direction", "--steps", "--solver",
            "--benchmark", "--iterations", "--trace", "--export-csv", "--scenarios", "--gui", "--help"}) {
            assertTrue(result.out.contains(option), option);
        }
    }

    private Result execute(String... args) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        try (PrintStream outStream = new PrintStream(out, true, "UTF-8");
             PrintStream errStream = new PrintStream(err, true, "UTF-8")) {
            int code = cli.run(args, outStream, errStream);

            return new Result(code, new String(out.toByteArray(), StandardCharsets.UTF_8),
                    new String(err.toByteArray(), StandardCharsets.UTF_8));
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("utf-8 encoding unavailable", e);
        }
    }

    private static final class Result {

        private final int code;
        private final String out;
        private final String err;

        Result(int code, String out, String err) {
            this.code = code;
            this.out = out;
            this.err = err;
        }
    }
}
