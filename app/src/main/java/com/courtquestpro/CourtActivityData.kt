package com.courtquestpro

import java.time.LocalDate

internal data class CourtActivityDay(val date: LocalDate, val count: Int)

/** Presentation-only projection: never infers activity from XP, plans, or missing records. */
internal fun courtActivityWindow(results: List<DrillResult>, today: LocalDate): List<CourtActivityDay> {
    val byDate = results.filter { it.attempted > 0 && it.completed in 0..it.attempted }
        .mapNotNull { result -> runCatching { LocalDate.parse(result.date) }.getOrNull() }
        .groupingBy { it }.eachCount()
    return (13 downTo 0).map { offset ->
        val date = today.minusDays(offset.toLong())
        CourtActivityDay(date, byDate[date] ?: 0)
    }
}
