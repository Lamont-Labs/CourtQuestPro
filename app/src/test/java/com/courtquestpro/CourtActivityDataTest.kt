package com.courtquestpro

import java.time.LocalDate
import org.junit.Assert.*
import org.junit.Test

class CourtActivityDataTest {
    private val today = LocalDate.of(2026, 11, 15)
    private fun result(date: LocalDate, completed: Int = 3, attempted: Int = 5) =
        DrillResult(date.toString(), "slot", "s1", completed, attempted)

    @Test fun emptyHistoryIsFourteenRealZeroDays() {
        val days = courtActivityWindow(emptyList(), today)
        assertEquals(14, days.size)
        assertEquals(today.minusDays(13), days.first().date)
        assertEquals(today, days.last().date)
        assertTrue(days.all { it.count == 0 })
    }
    @Test fun dateBoundariesAndActualSessionCountsArePreserved() {
        val days = courtActivityWindow(listOf(
            result(today), result(today), result(today.minusDays(13)),
            result(today.minusDays(14)), result(today.plusDays(1))), today)
        assertEquals(1, days.first().count)
        assertEquals(2, days.last().count)
        assertEquals(3, days.sumOf { it.count })
    }
    @Test fun InvalidOrUnrecordedResultsNeverInventActivity() {
        val malformed = result(today).copy(date = "not-a-date")
        val days = courtActivityWindow(listOf(malformed, result(today, 0, 0),
            result(today, 8, 5), result(today, -1, 5)), today)
        assertTrue(days.all { it.count == 0 })
    }
}
