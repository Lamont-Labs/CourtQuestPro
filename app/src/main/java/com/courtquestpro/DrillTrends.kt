package com.courtquestpro

internal object DrillTrends {
    data class Trend(val firstRate: Double, val latestRate: Double, val sessions: Int) {
        val changePoints: Double get() = (latestRate - firstRate) * 100.0
    }
    fun calculate(id: String, all: List<DrillResult>): Trend? {
        val values = all.withIndex().filter {
            it.value.drillId == id && it.value.attempted > 0 &&
                it.value.completed in 0..it.value.attempted
        }.sortedWith(compareBy<IndexedValue<DrillResult>> { it.value.date }.thenBy { it.index })
        if (values.size < 2) return null
        val first = values.first().value
        val last = values.last().value
        return Trend(first.completed.toDouble() / first.attempted,
            last.completed.toDouble() / last.attempted, values.size)
    }
}
