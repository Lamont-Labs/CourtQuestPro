package com.courtquestpro

import java.time.LocalDate
import java.time.Instant
import java.time.ZoneId
import java.time.YearMonth
import java.time.format.DateTimeFormatter

internal enum class ProgressRange(val label: String, val days: Int?) {
    SEVEN_DAYS("7D", 7),
    THIRTY_DAYS("30D", 30),
    ALL_TIME("ALL", null)
}

internal data class CourtActivityBucket(val date: LocalDate, val label: String, val count: Int)
internal data class CourtPersonalBest(
    val result: DrillResult,
    val sampleCount: Int,
    val improved: Boolean
)

internal val courtSkillCategories = listOf(
    "Shooting", "Ball Handling", "Finishing", "Defense", "Rebounding", "Passing"
)

/**
 * Only valid saved results with real ISO dates contribute to summaries. Repeated copies
 * of the same saved event do not inflate activity, while separate Mission sessions remain distinct.
 */
internal fun validSavedDrillResults(results: List<DrillResult>): List<DrillResult> {
    val seen = mutableSetOf<String>()
    return results.filter { it.attempted > 0 && it.completed in 0..it.attempted }
        .filter { runCatching { LocalDate.parse(it.date) }.isSuccess }
        .filter { seen.add(drillResultIdentity(it)) }
}

internal fun drillResultIdentity(result: DrillResult): String = listOf(
    result.date, result.slotId, result.drillId, result.missionVersionId.orEmpty(),
    result.missionSessionId.orEmpty(), result.missionStepId.orEmpty()
).joinToString("|")

internal fun resultsInProgressRange(
    results: List<DrillResult>,
    today: LocalDate,
    range: ProgressRange
): List<DrillResult> {
    val start = range.days?.let { today.minusDays((it - 1).toLong()) }
    return validSavedDrillResults(results).filter { result ->
        val date = LocalDate.parse(result.date)
        !date.isAfter(today) && (start == null || !date.isBefore(start))
    }.sortedWith(compareBy<DrillResult> { it.date }.thenBy { it.recordedAt.orEmpty() })
}

internal fun trainingDaysInRange(
    results: List<DrillResult>,
    today: LocalDate,
    range: ProgressRange
): Int = resultsInProgressRange(results, today, range).map { it.date }.distinct().size

/** Daily ranges use real dated results; all-time is shown as twelve explicit monthly buckets. */
internal fun courtActivityBuckets(
    results: List<DrillResult>,
    today: LocalDate,
    range: ProgressRange
): List<CourtActivityBucket> {
    val filtered = resultsInProgressRange(results, today, range)
    return if (range.days != null) {
        val start = today.minusDays((range.days - 1).toLong())
        val counts = filtered.groupingBy { LocalDate.parse(it.date) }.eachCount()
        (0 until range.days).map { offset ->
            val date = start.plusDays(offset.toLong())
            CourtActivityBucket(date, date.format(DateTimeFormatter.ofPattern("d")), counts[date] ?: 0)
        }
    } else {
        val first = YearMonth.from(today).minusMonths(11)
        val counts = filtered.groupingBy { YearMonth.from(LocalDate.parse(it.date)) }.eachCount()
        (0 until 12).map { offset ->
            val month = first.plusMonths(offset.toLong())
            CourtActivityBucket(month.atDay(1), month.format(DateTimeFormatter.ofPattern("MMM")), counts[month] ?: 0)
        }
    }
}

internal fun drillCategory(result: DrillResult, catalog: List<Drill>): String? =
    result.drillSnapshot?.category?.takeIf { it in courtSkillCategories }
        ?: catalog.firstOrNull { it.id == result.drillId }?.category?.takeIf { it in courtSkillCategories }

internal fun validMissionReceipts(save: SaveState): List<MissionReceipt> =
    save.missionReceipts.orEmpty()
        .filter { receipt -> runCatching { MissionTraining.receiptValid(save, receipt) }.getOrDefault(false) }
        .distinctBy { it.id }
        .sortedBy { it.savedAt }

internal fun missionReceiptDate(receipt: MissionReceipt): LocalDate? = runCatching {
    Instant.parse(receipt.savedAt).atZone(ZoneId.systemDefault()).toLocalDate()
}.getOrNull()

/**
 * A personal best is only comparable when the saved drill definition, frozen measurement
 * schema, and attempt count all match. Legacy records without a frozen definition are omitted.
 */
internal fun comparablePersonalBests(results: List<DrillResult>): List<CourtPersonalBest> {
    data class Key(val drillId: String, val definition: Drill, val schema: String, val attempts: Int)
    val groups = validSavedDrillResults(results)
        .filter { it.drillSnapshot != null && !it.measurementSnapshot?.schemaId.isNullOrBlank() }
        .groupBy {
            Key(
                it.drillId,
                it.drillSnapshot!!,
                it.measurementSnapshot!!.schemaId,
                it.attempted
            )
        }
    return groups.values.mapNotNull { group ->
        val ordered = group.sortedWith(compareBy<DrillResult> { it.date }.thenBy { it.recordedAt.orEmpty() })
        if (ordered.size < 2) return@mapNotNull null
        var previousBest = ordered.first().completed
        var improved = false
        ordered.drop(1).forEach { result ->
            if (result.completed > previousBest) {
                previousBest = result.completed
                improved = true
            }
        }
        CourtPersonalBest(ordered.maxBy { it.completed }, ordered.size, improved)
    }.sortedWith(compareBy<CourtPersonalBest> { it.result.drillSnapshot?.title.orEmpty() }
        .thenByDescending { it.result.completed })
}

internal fun personalBestImprovements(results: List<DrillResult>): List<DrillResult> {
    data class Key(val drillId: String, val definition: Drill, val schema: String, val attempts: Int)
    return validSavedDrillResults(results)
        .filter { it.drillSnapshot != null && !it.measurementSnapshot?.schemaId.isNullOrBlank() }
        .groupBy {
            Key(it.drillId, it.drillSnapshot!!, it.measurementSnapshot!!.schemaId, it.attempted)
        }.values.flatMap { group ->
            val ordered = group.sortedWith(compareBy<DrillResult> { it.date }.thenBy { it.recordedAt.orEmpty() })
            var best = ordered.firstOrNull()?.completed ?: return@flatMap emptyList()
            ordered.drop(1).mapNotNull { result ->
                if (result.completed > best) {
                    best = result.completed
                    result
                } else null
            }
        }.sortedWith(compareBy<DrillResult> { it.date }.thenBy { it.recordedAt.orEmpty() })
}
