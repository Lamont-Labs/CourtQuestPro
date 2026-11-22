package com.courtquestpro

import java.time.Duration
import java.time.Instant
import java.time.ZoneId

/** Refresh on resume and at local midnight; bounded checks also follow system clock/time-zone changes. */
internal object PlayerCalendar {
    fun today(now: Instant, zone: ZoneId): String = now.atZone(zone).toLocalDate().toString()

    fun nextRefreshMillis(now: Instant, zone: ZoneId): Long {
        val midnight = now.atZone(zone).toLocalDate().plusDays(1).atStartOfDay(zone).toInstant()
        return Duration.between(now, midnight).toMillis().coerceIn(1L, 30_000L)
    }
}
