package com.courtquestpro

import java.time.LocalDate

/** Descriptive evidence only. A rate is not a certified basketball skill score. */
internal data class ComparableTrainingEvidence(
    val drillId: String,
    val schemaId: String,
    val sessions: Int,
    val attempts: Int,
    val successes: Int,
    val firstRate: Double,
    val latestRate: Double
) {
    val aggregateRate: Double get() = successes.toDouble() / attempts
    val changePoints: Double get() = (latestRate - firstRate) * 100.0
}

/**
 * Require three distinct saved sessions and at least 30 total attempts.
 * Only a frozen identical drill definition and measurement schema can be compared.
 * Do not infer ability, readiness to advance, fatigue or injury from these counters.
 */
internal object TrainingEvidence {
    const val MIN_SESSIONS = 3
    const val MIN_ATTEMPTS = 30

    fun comparable(save: SaveState, drillId: String, asOf: LocalDate): ComparableTrainingEvidence? {
        val valid = validSavedDrillResults(save.drillResults.orEmpty()).filter { result ->
            result.drillId == drillId &&
                !LocalDate.parse(result.date).isAfter(asOf) &&
                result.drillSnapshot != null &&
                !result.measurementSnapshot?.schemaId.isNullOrBlank()
        }
        val groups = valid.groupBy { result ->
            result.drillSnapshot!! to result.measurementSnapshot!!.schemaId
        }
        val qualified = groups.entries.mapNotNull { (key, results) ->
            val ordered = results.sortedWith(compareBy<DrillResult> { it.date }
                .thenBy { it.recordedAt.orEmpty() }
                .thenBy { it.slotId })
            val attempts = ordered.sumOf { it.attempted.toLong() }
            if (ordered.size < MIN_SESSIONS || attempts < MIN_ATTEMPTS ||
                attempts > Int.MAX_VALUE) return@mapNotNull null
            val successes = ordered.sumOf { it.completed.toLong() }
            ComparableTrainingEvidence(
                drillId, key.second, ordered.size, attempts.toInt(), successes.toInt(),
                ordered.first().completed.toDouble() / ordered.first().attempted,
                ordered.last().completed.toDouble() / ordered.last().attempted
            )
        }
        // Multiple definition/schema groups cannot be combined into a single claim.
        return qualified.singleOrNull()
    }
}
