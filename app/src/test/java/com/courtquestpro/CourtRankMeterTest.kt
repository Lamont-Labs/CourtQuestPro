package com.courtquestpro

import org.junit.Assert.*
import org.junit.Test

class CourtRankMeterTest {
    @Test fun progressUsesExistingRanksAtEveryBoundary() {
        val starts = listOf(0, 300, 800, 1500, 2500, 4000, 6000)
        starts.forEachIndexed { index, xp ->
            val window = courtRankWindow(xp)
            assertEquals(xp, window.minimum)
            assertEquals(starts.getOrNull(index + 1), window.next)
            assertEquals(if(index == starts.lastIndex) 1f else 0f, window.fraction(xp), .0001f)
            if(index > 0) assertNotEquals(rankFor(xp - 1), rankFor(xp))
        }
    }
    @Test fun partialAndExtremeValuesDoNotInventLevels() {
        assertEquals(.5f, courtRankWindow(150).fraction(150), .0001f)
        assertEquals(0f, courtRankWindow(-10).fraction(-10), .0001f)
        assertEquals(1f, courtRankWindow(Int.MAX_VALUE).fraction(Int.MAX_VALUE), .0001f)
        assertNull(courtRankWindow(6000).next)
    }
}
