package com.kevinandrey.simulation.bouncing.cli;

import com.kevinandrey.simulation.bouncing.domain.Direction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CliParserTest {

    private final CliParser parser = new CliParser();

    @Test
    void defaultsReproduceTheOriginalExample() throws CliUsageException {
        CliOptions options = parser.parse(new String[0]);

        assertEquals(5, options.length());
        assertEquals(4, options.start());
        assertEquals(Direction.FORWARD, options.direction());
        assertEquals(4, options.steps());
        assertEquals("analytical", options.solver());
        assertEquals(5, options.iterations());
        assertFalse(options.benchmark());
        assertFalse(options.trace());
        assertFalse(options.gui());
        assertFalse(options.help());
        assertNull(options.exportCsv());
        assertNull(options.scenarios());
    }

    @Test
    void parsesLongAndShortOptions() throws CliUsageException {
        CliOptions options = parser.parse(new String[] {
            "--length", "1_000", "-p", "7", "--direction", "-1", "-m", "123456789", "-s", "step",
            "--benchmark", "--iterations", "3", "--export-csv", "out.csv", "--trace", "--scenarios", "s.csv",
            "--gui", "-h"
        });

        assertEquals(1000, options.length());
        assertEquals(7, options.start());
        assertEquals(Direction.BACKWARD, options.direction());
        assertEquals(123456789, options.steps());
        assertEquals("step", options.solver());
        assertTrue(options.benchmark());
        assertEquals(3, options.iterations());
        assertEquals("out.csv", options.exportCsv());
        assertTrue(options.trace());
        assertEquals("s.csv", options.scenarios());
        assertTrue(options.gui());
        assertTrue(options.help());
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
        "--unknown|unknown option --unknown",
        "--length|missing value for --length",
        "--steps abc|invalid integer for --steps: abc",
        "-d 2|direction must be 1, -1, forward or backward: got 2",
        "--iterations 0|iterations must be between 1 and 2147483647: got 0"
    })
    void rejectsInvalidUsage(String raw, String message) {
        CliUsageException error = assertThrows(CliUsageException.class, () -> parser.parse(raw.split(" ")));

        assertEquals(message, error.getMessage());
    }
}
