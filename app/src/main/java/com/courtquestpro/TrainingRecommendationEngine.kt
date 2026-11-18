package com.courtquestpro

import java.time.LocalDate

/** A reproducible, non-mutating recommendation receipt. No claims about athletic ability. */
internal data class TrainingRecommendationReceipt(
    val version: String,
    val date: String,
    val focus: String,
    val difficulty: String,
    val eligibleIds: List<String>,
    val selectedIds: List<String>,
    val explanations: Map<String, String>,
    val excludedIds: List<String>
)

/**
 * Offline recommendation v1: preferences, recency and favorites only.
 * No skill progression is inferred until compatible measurement evidence is validated.
 * The caller owns the plan; this engine never replaces active slots or drafts.
 */
internal object TrainingRecommendationEngine {
    const val VERSION = "offline-evidence-v2"

    fun evaluate(save: SaveState, date: LocalDate, limit: Int = 4): TrainingRecommendationReceipt {
        require(limit >= 0)
        val allowed = TrainingLibrary.levels.indexOf(save.difficulty).coerceAtLeast(0)
        val catalog = TrainingLibrary.available(save).sortedBy { it.id }
        val eligible = catalog.filter {
            TrainingLibrary.levels.indexOf(it.level) in 0..allowed
        }
        val eligibleIds = eligible.map { it.id }
        val recent = save.drillResults.orEmpty()
            .filter { runCatching { LocalDate.parse(it.date) }.getOrNull()?.let { day ->
                !day.isAfter(date)
            } == true }
            .sortedWith(compareByDescending<DrillResult> { it.date }
                .thenByDescending { it.recordedAt.orEmpty() }
                .thenBy { it.drillId })
            .take(6).map { it.drillId }.toSet()
        val favorites = save.favoriteDrillIds.orEmpty().toSet()
        val selected = eligible.sortedWith(
            compareByDescending<Drill> { it.category == save.weeklyPlan }
                .thenBy { it.id in recent }
                .thenByDescending { it.id in favorites }
                .thenBy { it.id }
        ).take(limit)
        val explanations = selected.associate { drill ->
            val preference = when {
                drill.category == save.weeklyPlan ->
                    "Matches your selected ${save.weeklyPlan.lowercase()} focus."
                drill.id in favorites -> "You marked this drill as a favorite."
                drill.id !in recent -> "Adds variety to your recent saved training."
                else -> "Fits your selected training difficulty."
            }
            val evidence = TrainingEvidence.comparable(save, drill.id, date)
            val explanation = if (evidence == null) preference else {
                val percent = (evidence.aggregateRate * 100).toInt()
                "$preference You recorded ${evidence.successes}/${evidence.attempts} successful attempts " +
                    "across ${evidence.sessions} comparable sessions ($percent%); self-recorded."
            }
            drill.id to explanation
        }
        return TrainingRecommendationReceipt(
            VERSION, date.toString(), save.weeklyPlan, save.difficulty,
            eligibleIds, selected.map { it.id }, explanations,
            catalog.map { it.id }.filterNot { it in eligibleIds }
        )
    }
}
