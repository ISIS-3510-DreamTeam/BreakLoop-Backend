package com.dreamteam.breakloop.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class Coordinates {

    private Coordinates() {
    }

    /** Rounds to 2 decimals (~1 km), half away from zero. Null stays null. */
    public static Double round(Double value) {

        if (value == null) {
            return null;
        }

        return BigDecimal.valueOf(value)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
