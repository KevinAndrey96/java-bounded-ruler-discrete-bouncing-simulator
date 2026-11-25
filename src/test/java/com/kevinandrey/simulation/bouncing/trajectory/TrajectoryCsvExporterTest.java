package com.kevinandrey.simulation.bouncing.trajectory;

import com.kevinandrey.simulation.bouncing.domain.RulerConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TrajectoryCsvExporterTest {

    private final TrajectoryCsvExporter exporter = new TrajectoryCsvExporter();

    @Test
    void writesHeaderAndOneRowPerState() throws IOException {
        StringWriter writer = new StringWriter();
        long rows = exporter.write(new Trajectory(RulerConfiguration.of(5, 4, 1, 4)), writer);

        assertEquals(5, rows);
        assertEquals("step,position,direction\n"
                + "0,4,1\n"
                + "1,5,1\n"
                + "2,4,-1\n"
                + "3,3,-1\n"
                + "4,2,-1\n", writer.toString());
    }

    @Test
    void writesFilesCreatingParentDirectories(@TempDir Path directory) throws IOException {
        Path target = directory.resolve("nested").resolve("trajectory.csv");
        long rows = exporter.write(new Trajectory(RulerConfiguration.of(3, 0, -1, 7)), target);
        List<String> lines = Files.readAllLines(target, StandardCharsets.UTF_8);

        assertEquals(8, rows);
        assertEquals(9, lines.size());
        assertEquals("0,0,-1", lines.get(1));
        assertEquals("1,1,1", lines.get(2));
    }

    @Test
    void rejectsTrajectoriesAboveTheExportLimit(@TempDir Path directory) {
        Path target = directory.resolve("huge.csv");
        Trajectory huge = new Trajectory(
                RulerConfiguration.of(10, 0, 1, TrajectoryCsvExporter.MAX_EXPORT_STEPS + 1));

        assertThrows(IllegalArgumentException.class, () -> exporter.write(huge, new StringWriter()));
        assertThrows(IllegalArgumentException.class, () -> exporter.write(huge, target));
        assertFalse(Files.exists(target));
    }
}
