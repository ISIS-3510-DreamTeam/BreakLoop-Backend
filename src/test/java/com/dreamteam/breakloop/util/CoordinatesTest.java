package com.dreamteam.breakloop.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CoordinatesTest {

    @Test
    void roundsToTwoDecimals() {
        assertEquals(4.71, Coordinates.round(4.7110));
        assertEquals(4.72, Coordinates.round(4.7150));
        assertEquals(-74.07, Coordinates.round(-74.0678));
    }

    @Test
    void roundsNegativeHalvesAwayFromZero() {
        assertEquals(-74.07, Coordinates.round(-74.065));
    }

    @Test
    void keepsNull() {
        assertNull(Coordinates.round(null));
    }
}
