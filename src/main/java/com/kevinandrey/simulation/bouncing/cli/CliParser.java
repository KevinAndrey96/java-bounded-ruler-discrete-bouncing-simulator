package com.kevinandrey.simulation.bouncing.cli;

import com.kevinandrey.simulation.bouncing.domain.Direction;
import com.kevinandrey.simulation.bouncing.domain.InvalidConfigurationException;

/**
 * Translates raw arguments into {@link CliOptions}.
 */
public final class CliParser {

    /**
     * Parses the arguments.
     *
     * @param args raw command line arguments
     * @return the parsed options
     * @throws CliUsageException when an option is unknown, incomplete or malformed
     */
    public CliOptions parse(String[] args) throws CliUsageException {
        CliOptions.Builder builder = CliOptions.builder();
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            switch (arg) {
                case "--length":
                case "-n":
                    builder.length(parseLong(arg, valueOf(args, ++i, arg)));
                    break;
                case "--start":
                case "-p":
                    builder.start(parseLong(arg, valueOf(args, ++i, arg)));
                    break;
                case "--direction":
                case "-d":
                    builder.direction(parseDirection(valueOf(args, ++i, arg)));
                    break;
                case "--steps":
                case "-m":
                    builder.steps(parseLong(arg, valueOf(args, ++i, arg)));
                    break;
                case "--solver":
                case "-s":
                    builder.solver(valueOf(args, ++i, arg));
                    break;
                case "--iterations":
                    builder.iterations(parseIterations(valueOf(args, ++i, arg)));
                    break;
                case "--export-csv":
                    builder.exportCsv(valueOf(args, ++i, arg));
                    break;
                case "--scenarios":
                    builder.scenarios(valueOf(args, ++i, arg));
                    break;
                case "--benchmark":
                case "-b":
                    builder.benchmark(true);
                    break;
                case "--trace":
                    builder.trace(true);
                    break;
                case "--gui":
                    builder.gui(true);
                    break;
                case "--help":
                case "-h":
                    builder.help(true);
                    break;
                default:
                    throw new CliUsageException("unknown option " + arg);
            }
        }

        return builder.build();
    }

    private static String valueOf(String[] args, int index, String option) throws CliUsageException {
        if (index >= args.length) {
            throw new CliUsageException("missing value for " + option);
        }

        return args[index];
    }

    private static long parseLong(String option, String raw) throws CliUsageException {
        try {
            return Long.parseLong(raw.trim().replace("_", ""));
        } catch (NumberFormatException e) {
            throw new CliUsageException("invalid integer for " + option + ": " + raw);
        }
    }

    private static int parseIterations(String raw) throws CliUsageException {
        long value = parseLong("--iterations", raw);
        if (value < 1 || value > Integer.MAX_VALUE) {
            throw new CliUsageException("iterations must be between 1 and " + Integer.MAX_VALUE + ": got " + raw);
        }

        return (int) value;
    }

    private static Direction parseDirection(String raw) throws CliUsageException {
        try {
            return Direction.parse(raw);
        } catch (InvalidConfigurationException e) {
            throw new CliUsageException(e.getMessage());
        }
    }
}
