package com.kevinandrey.simulation.bouncing.trajectory;

import com.kevinandrey.simulation.bouncing.domain.RulerState;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Writes a trajectory as CSV with the columns {@code step,position,direction}.
 */
public final class TrajectoryCsvExporter {

    /**
     * Maximum number of steps accepted for export, which bounds the file at roughly 20 MB.
     */
    public static final long MAX_EXPORT_STEPS = 1_000_000L;

    static final String HEADER = "step,position,direction";

    /**
     * Streams the trajectory into the writer.
     *
     * @param trajectory trajectory to export
     * @param writer     destination, not closed by this method
     * @return number of data rows written
     * @throws IOException when the destination cannot be written
     */
    public long write(Trajectory trajectory, Writer writer) throws IOException {
        requireExportable(trajectory);

        writer.write(HEADER);
        writer.write('\n');
        long rows = 0;
        for (RulerState state : trajectory) {
            writer.write(Long.toString(state.step()));
            writer.write(',');
            writer.write(Long.toString(state.position()));
            writer.write(',');
            writer.write(Integer.toString(state.direction().sign()));
            writer.write('\n');
            rows++;
        }
        writer.flush();

        return rows;
    }

    /**
     * Writes the trajectory into a file, creating parent directories when needed.
     *
     * @param trajectory trajectory to export
     * @param target     destination file
     * @return number of data rows written
     * @throws IOException when the file cannot be written
     */
    public long write(Trajectory trajectory, Path target) throws IOException {
        requireExportable(trajectory);
        Path parent = target.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        try (Writer writer = new BufferedWriter(
                new OutputStreamWriter(Files.newOutputStream(target), StandardCharsets.UTF_8))) {
            return write(trajectory, writer);
        }
    }

    private static void requireExportable(Trajectory trajectory) {
        long steps = trajectory.configuration().steps();
        if (steps > MAX_EXPORT_STEPS) {
            throw new IllegalArgumentException(
                    "trajectory export limited to " + MAX_EXPORT_STEPS + " steps: got " + steps);
        }
    }
}
