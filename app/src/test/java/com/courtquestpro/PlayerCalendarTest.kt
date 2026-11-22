package com.courtquestpro

import java.time.Instant
import java.time.ZoneId
import org.junit.Assert.*
import org.junit.Test

class PlayerCalendarTest {
    private val zone = ZoneId.of("America/Los_Angeles")
    @Test fun midnightChangesTheQuestDayWithoutWaitingForAnotherTap() {
        val before = Instant.parse("2026-11-13T07:59:59.500Z")
        assertEquals("2026-11-12", PlayerCalendar.today(before, zone))
        assertEquals(500L, PlayerCalendar.nextRefreshMillis(before, zone))
        assertEquals("2026-11-13", PlayerCalendar.today(before.plusMillis(500), zone))
    }
    @Test fun clockAndTimeZoneChangesAreRecheckedWithinThirtySeconds() {
        val now = Instant.parse("2026-11-13T02:00:00Z")
        assertEquals(30_000L, PlayerCalendar.nextRefreshMillis(now, zone))
        assertEquals("2026-11-12", PlayerCalendar.today(now, zone))
        assertEquals("2026-11-13", PlayerCalendar.today(now, ZoneId.of("UTC")))
    }
    @Test fun daylightSavingDayUsesTheNextLocalMidnight() {
        val before = Instant.parse("2026-03-09T06:59:59.999Z")
        assertEquals("2026-03-08", PlayerCalendar.today(before, zone))
        assertEquals(1L, PlayerCalendar.nextRefreshMillis(before, zone))
        assertEquals("2026-03-09", PlayerCalendar.today(before.plusMillis(1), zone))
    }
}
