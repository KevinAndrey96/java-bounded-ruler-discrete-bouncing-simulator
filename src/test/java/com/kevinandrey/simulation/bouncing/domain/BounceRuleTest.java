package com.kevinandrey.simulation.bouncing.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class BounceRuleTest {

    @ParameterizedTest
    @CsvSource({
        "5, 1, 5, -1",
        "0, -1, 0, 1",
        "2, 1, 5, 1",
        "2, -1, 5, -1",
        "5, -1, 5, -1",
        "0, 1, 5, 1"
    })
    void reflectsOnlyWhenFacingAWall(long position, int sign, long length, int expected) {
        Direction next = BounceRule.nextDirection(position, Direction.fromSign(sign), length);

        assertEquals(Direction.fromSign(expected), next);
    }

    @Test
    void advanceMovesOneMarkAndIncrementsStep() {
        RulerState atWall = new RulerState(5, Direction.FORWARD, 3);

        assertEquals(new RulerState(4, Direction.BACKWARD, 4), BounceRule.advance(atWall, 5));
        assertEquals(new RulerState(1, Direction.FORWARD, 1),
                BounceRule.advance(new RulerState(0, Direction.BACKWARD, 0), 5));
    }

    @Test
    void stateEqualityAndText() {
        RulerState state = new RulerState(2, Direction.BACKWARD, 4);

        assertEquals(new RulerState(2, Direction.BACKWARD, 4), state);
        assertEquals(state.hashCode(), new RulerState(2, Direction.BACKWARD, 4).hashCode());
        assertNotEquals(new RulerState(2, Direction.FORWARD, 4), state);
        assertNotEquals(state, "state");
        assertEquals("step=4, position=2, direction=-1", state.toString());
    }
}
