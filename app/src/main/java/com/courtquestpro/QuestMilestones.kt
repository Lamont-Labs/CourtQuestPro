package com.courtquestpro

/**
 * Version 1 of local, evidence-derived Quest milestones.
 * These are retrospective participation markers, not verified basketball skill.
 * Recompute from receipts rather than trusting mutable XP, streak or UI flags.
 */
internal object QuestMilestones {
    private fun validDate(value: String): Boolean = runCatching {
        java.time.LocalDate.parse(value).toString() == value
    }.getOrDefault(false)

    const val RULESET_VERSION = 1

    data class Milestone(val id: String, val title: String, val description: String, val achieved: Boolean, val evidenceCount: Int)

    private fun validDrills(save: SaveState): List<DrillResult> = save.drillResults.orEmpty().filter {
        validDate(it.date) && it.drillId.isNotBlank() && it.slotId.isNotBlank() &&
            it.attempted > 0 && it.completed in 0..it.attempted
    }.distinctBy { result ->
        if (!result.missionSessionId.isNullOrBlank() && !result.missionStepId.isNullOrBlank())
            "mission:${result.missionSessionId}:${result.missionStepId}"
        else "drill:${result.date}:${result.slotId}"
    }

    /** Deterministic evidence snapshot for an award. Stable ordering, no display text. */
    fun evidenceSnapshot(save: SaveState, milestoneId: String): List<String> {
        val drills = validDrills(save)
        val missionReceipts = save.missionReceipts.orEmpty()
            .filter { it.id.isNotBlank() && MissionTraining.receiptValid(save, it) }
            .distinctBy { it.sessionId }
        val selected = when (milestoneId) {
            "first_recorded_drill_v1", "five_recorded_drills_v1" -> drills.map {
                "drill|${it.date}|${it.slotId}|${it.drillId}|${it.completed}|${it.attempted}|${it.practice}|${it.missionSessionId.orEmpty()}|${it.missionStepId.orEmpty()}"
            }
            "three_training_days_v1" -> drills.map { it.date }.distinct().map { "day|$it" }
            "first_mission_receipt_v1" -> missionReceipts.map {
                "mission|${it.sessionId}|${it.missionVersionId}|${it.resultHash}"
            }
            else -> emptyList()
        }
        return selected.sorted()
    }

    fun evaluate(save: SaveState): List<Milestone> {
        val validDrills = validDrills(save)
        val uniqueDates = validDrills.map { it.date }.distinct().size
        val missionCount = save.missionReceipts.orEmpty()
            .filter { it.id.isNotBlank() && MissionTraining.receiptValid(save, it) }
            .map { it.sessionId }.distinct().size
        return listOf(
            Milestone("first_recorded_drill_v1", "First rep recorded", "Save your first drill result.", validDrills.isNotEmpty(), validDrills.size),
            Milestone("five_recorded_drills_v1", "Five on the board", "Record five distinct drill results.", validDrills.size >= 5, validDrills.size),
            Milestone("three_training_days_v1", "Show up again", "Record drills on three different dates.", uniqueDates >= 3, uniqueDates),
            Milestone("first_mission_receipt_v1", "Mission complete", "Finish a saved Mission and keep its receipt.", missionCount >= 1, missionCount)
        )
    }
}
