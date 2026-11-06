package com.courtquestpro

import org.junit.Assert.*
import org.junit.Test

class DrillTrendsTest {
    @Test fun requiresTwoValidSessions() {
        assertNull(DrillTrends.calculate("s1", listOf(DrillResult("2026-11-01", "a", "s1", 5, 10))))
    }

    @Test fun comparesOnlySameDrillChronologically() {
        val data = listOf(
            DrillResult("2026-11-03", "c", "s1", 8, 10),
            DrillResult("2026-11-02", "b", "b1", 0, 10),
            DrillResult("2026-11-01", "a", "s1", 4, 10)
        )
        val trend = DrillTrends.calculate("s1", data)!!
        assertEquals(2, trend.sessions)
        assertEquals(40.0, trend.changePoints, 0.00001)
    }

    @Test fun ignoresInvalidAttemptRecords() {
        val data = listOf(
            DrillResult("2026-11-01", "a", "s1", 1, 0),
            DrillResult("2026-11-02", "b", "s1", 4, 10),
            DrillResult("2026-11-03", "c", "s1", 3, 10)
        )
        assertEquals(-10.0, DrillTrends.calculate("s1", data)!!.changePoints, 0.00001)
    }
}
