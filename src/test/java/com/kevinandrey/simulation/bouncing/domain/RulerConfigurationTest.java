package com.kevinandrey.simulation.bouncing.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RulerConfigurationTest {

    @Test
    void exposesValidatedValues() {
        RulerConfiguration configuration = RulerConfiguration.of(5, 4, 1, 4);

        assertEquals(5, configuration.length());
        assertEquals(4, configuration.start());
        assertEquals(Direction.FORWARD, configuration.direction());
        assertEquals(4, configuration.steps());
        assertEquals(10, configuration.period());
        assertEquals(new RulerState(4, Direction.FORWARD, 0), configuration.initialState());
        assertEquals("N=5, P=4, d=+1, M=4", configuration.toString());
    }

    @Test
    void acceptsClosedIntervalBoundaries() {
        assertEquals(0, RulerConfiguration.of(1, 0, -1, 0).start());
        assertEquals(1, RulerConfiguration.of(1, 1, 1, 0).start());
        assertEquals(RulerConfiguration.MAX_LENGTH,
                RulerConfiguration.of(RulerConfiguration.MAX_LENGTH, 0, 1, Long.MAX_VALUE).length());
    }

    @ParameterizedTest
    @CsvSource({
        "0, 0, 0, length must be between 1 and 2305843009213693951: got 0",
        "-3, 0, 0, length must be between 1 and 2305843009213693951: got -3",
        "5, -1, 0, 'start must be inside [0, 5]: got -1'",
        "5, 6, 0, 'start must be inside [0, 5]: got 6'",
        "5, 2, -1, steps must be non negative: got -1"
    })
    void rejectsInvalidInput(long length, long start, long steps, String message) {
        InvalidConfigurationException error = assertThrows(InvalidConfigurationException.class,
                () -> RulerConfiguration.of(length, start, Direction.FORWARD, steps));
        assertEquals(message, error.getMessage());
    }

    @Test
    void rejectsLengthAboveOverflowGuard() {
        assertThrows(InvalidConfigurationException.class,
                () -> RulerConfiguration.of(RulerConfiguration.MAX_LENGTH + 1, 0, 1, 0));
    }

    @Test
    void rejectsMissingDirection() {
        assertThrows(InvalidConfigurationException.class, () -> RulerConfiguration.of(5, 2, null, 1));
    }

    @Test
    void withStepsKeepsTheRestOfTheConfiguration() {
        RulerConfiguration configuration = RulerConfiguration.of(9, 3, -1, 100);
        RulerConfiguration shorter = configuration.withSteps(7);

        assertEquals(RulerConfiguration.of(9, 3, -1, 7), shorter);
        assertNotEquals(configuration, shorter);
    }

    @Test
    void equalityIsValueBased() {
        RulerConfiguration first = RulerConfiguration.of(7, 2, -1, 11);
        RulerConfiguration second = RulerConfiguration.of(7, 2, -1, 11);

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
        assertTrue(first.equals(first));
        assertNotEquals(first, RulerConfiguration.of(7, 2, 1, 11));
        assertNotEquals(first, "N=7");
    }
}
