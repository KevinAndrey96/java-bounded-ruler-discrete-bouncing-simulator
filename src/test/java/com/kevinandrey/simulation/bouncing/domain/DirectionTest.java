package com.kevinandrey.simulation.bouncing.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DirectionTest {

    @Test
    void signsAndReversal() {
        assertEquals(1, Direction.FORWARD.sign());
        assertEquals(-1, Direction.BACKWARD.sign());
        assertEquals(Direction.BACKWARD, Direction.FORWARD.reverse());
        assertEquals(Direction.FORWARD, Direction.BACKWARD.reverse());
        assertEquals("+1", Direction.FORWARD.signedLabel());
        assertEquals("-1", Direction.BACKWARD.signedLabel());
    }

    @Test
    void fromSignResolvesUnitValues() {
        assertEquals(Direction.FORWARD, Direction.fromSign(1));
        assertEquals(Direction.BACKWARD, Direction.fromSign(-1));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 2, -2, Integer.MAX_VALUE})
    void fromSignRejectsNonUnitValues(int sign) {
        InvalidConfigurationException error =
                assertThrows(InvalidConfigurationException.class, () -> Direction.fromSign(sign));
        assertEquals("direction must be 1 or -1: got " + sign, error.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"1", "+1", "forward", "FORWARD", " right "})
    void parseForwardAliases(String raw) {
        assertEquals(Direction.FORWARD, Direction.parse(raw));
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1", "backward", "Left"})
    void parseBackwardAliases(String raw) {
        assertEquals(Direction.BACKWARD, Direction.parse(raw));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "0", "up", "+2"})
    void parseRejectsUnknownValues(String raw) {
        assertThrows(InvalidConfigurationException.class, () -> Direction.parse(raw));
    }

    @Test
    void parseRejectsNull() {
        assertThrows(InvalidConfigurationException.class, () -> Direction.parse(null));
    }
}
