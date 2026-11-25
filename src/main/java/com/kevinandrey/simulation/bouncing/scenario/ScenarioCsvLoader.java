package com.kevinandrey.simulation.bouncing.scenario;

import com.kevinandrey.simulation.bouncing.domain.Direction;
import com.kevinandrey.simulation.bouncing.domain.InvalidConfigurationException;
import com.kevinandrey.simulation.bouncing.domain.RulerConfiguration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Parses reference scenarios from CSV.
 *
 * <p>Expected columns: {@code name,length,start,direction,steps,expected_position,expected_direction}.
 * Blank lines and lines starting with {@code #} are ignored, the first non comment line is the header.</p>
 */
public final class ScenarioCsvLoader {

    private static final int COLUMNS = 7;

    /**
     * Loads the scenarios of a file.
     *
     * @param source CSV file
     * @return the parsed scenarios
     * @throws IOException when the file cannot be read
     */
    public List<Scenario> load(Path source) throws IOException {
        try (Reader reader = Files.newBufferedReader(source, StandardCharsets.UTF_8)) {
            return load(reader);
        }
    }

    /**
     * Loads the scenarios of a reader.
     *
     * @param source CSV content, not closed by this method
     * @return the parsed scenarios
     * @throws IOException when the content cannot be read
     */
    public List<Scenario> load(Reader source) throws IOException {
        BufferedReader reader = new BufferedReader(source);
        List<Scenario> scenarios = new ArrayList<>();
        boolean headerSkipped = false;
        int lineNumber = 0;
        String line;
        while ((line = reader.readLine()) != null) {
            lineNumber++;
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                continue;
            }
            if (!headerSkipped) {
                headerSkipped = true;
                continue;
            }
            scenarios.add(parse(trimmed, lineNumber));
        }

        return scenarios;
    }

    private Scenario parse(String line, int lineNumber) {
        String[] cells = line.split(",", -1);
        if (cells.length != COLUMNS) {
            throw new InvalidConfigurationException(
                    "scenario line " + lineNumber + ": expected " + COLUMNS + " columns, got " + cells.length);
        }

        try {
            RulerConfiguration configuration = RulerConfiguration.of(
                    Long.parseLong(cells[1].trim()),
                    Long.parseLong(cells[2].trim()),
                    Direction.parse(cells[3]),
                    Long.parseLong(cells[4].trim()));

            return new Scenario(
                    cells[0].trim(), configuration, Long.parseLong(cells[5].trim()), Direction.parse(cells[6]));
        } catch (NumberFormatException e) {
            throw new InvalidConfigurationException("scenario line " + lineNumber + ": " + e.getMessage());
        } catch (InvalidConfigurationException e) {
            throw new InvalidConfigurationException("scenario line " + lineNumber + ": " + e.getMessage());
        }
    }
}
