package com.kevinandrey.simulation.bouncing.domain;

import java.util.Locale;

/**
 * Displacement direction over the ruler: {@code +1} moves towards {@code N}, {@code -1} towards {@code 0}.
 */
public enum Direction {
    FORWARD(1),
    BACKWARD(-1);

    private final int sign;

    Direction(int sign) {
        this.sign = sign;
    }

    /**
     * Returns the signed unit displacement of this direction.
     *
     * @return {@code +1} for {@link #FORWARD}, {@code -1} for {@link #BACKWARD}
     */
    public int sign() {
        return sign;
    }

    /**
     * Returns the opposite direction (elastic reflection).
     *
     * @return the reversed direction
     */
    public Direction reverse() {
        return this == FORWARD ? BACKWARD : FORWARD;
    }

    /**
     * Returns the signed label used in reports, such as {@code +1} or {@code -1}.
     *
     * @return the signed label
     */
    public String signedLabel() {
        return sign > 0 ? "+1" : "-1";
    }

    /**
     * Resolves a direction from its numeric sign.
     *
     * @param sign {@code 1} or {@code -1}
     * @return the matching direction
     * @throws InvalidConfigurationException when the sign is not a unit value
     */
    public static Direction fromSign(int sign) {
        if (sign == 1) {
            return FORWARD;
        }
        if (sign == -1) {
            return BACKWARD;
        }

        throw new InvalidConfigurationException("direction must be 1 or -1: got " + sign);
    }

    /**
     * Parses a direction from user input, accepting numeric and verbal forms.
     *
     * @param raw value such as {@code 1}, {@code +1}, {@code -1}, {@code forward}, {@code right}, {@code left}
     * @return the parsed direction
     * @throws InvalidConfigurationException when the value is not recognized
     */
    public static Direction parse(String raw) {
        if (raw == null) {
            throw new InvalidConfigurationException("direction is required");
        }

        String normalized = raw.trim().toLowerCase(Locale.ROOT);
        if ("1".equals(normalized) || "+1".equals(normalized) || "forward".equals(normalized) || "right".equals(normalized)) {
            return FORWARD;
        }
        if ("-1".equals(normalized) || "backward".equals(normalized) || "left".equals(normalized)) {
            return BACKWARD;
        }

        throw new InvalidConfigurationException("direction must be 1, -1, forward or backward: got " + raw);
    }
}
