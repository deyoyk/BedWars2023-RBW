package com.tomkeuper.bedwars.listeners;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HeightLimitTest {

    @Test
    void farBelowLimitShowsNothing() {
        assertEquals(HeightLimitListener.FAR, HeightLimitListener.proximity(200, 100));
        assertEquals(HeightLimitListener.FAR, HeightLimitListener.proximity(200, 194));
    }

    @Test
    void countdownStartsAtFiveBlocksBelowLimit() {
        assertEquals(5, HeightLimitListener.proximity(200, 195));
    }

    @Test
    void countdownCountsDownToZero() {
        assertEquals(4, HeightLimitListener.proximity(200, 196));
        assertEquals(3, HeightLimitListener.proximity(200, 197));
        assertEquals(2, HeightLimitListener.proximity(200, 198));
        assertEquals(1, HeightLimitListener.proximity(200, 199));
    }

    @Test
    void atOrAboveLimitShowsReached() {
        assertEquals(0, HeightLimitListener.proximity(200, 200));
        assertEquals(0, HeightLimitListener.proximity(200, 250));
    }
}
