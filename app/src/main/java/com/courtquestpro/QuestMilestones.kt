package com.courtquestpro

/**
 * Version 1 of local, evidence-derived Quest milestones.
 * These are retrospective participation markers, not verified basketball skill.
 * Recompute from receipts rather than trusting mutable XP, streak or UI flags.
 */
internal object QuestMilestones {
    const val RULESET_VERSION = 1

    data class Milestone(val id: String, val title: String, val description: String, val achieved: Boolean, val evidenceCount: Int)

    fun evaluate(save: SaveState): List<Milestone> {
        val validDrills = save.drillResults.orEmpty().filter {
            it.date.isNotBlank() && it.drillId.isNotBlank() && it.slotId.isNotBlank() &&
                it.attempted > 0 && it.completed in 0..it.attempted
        }.distinctBy { result ->
            // A completed step may be represented by a daily or a Mission identity.
            if (!result.missionSessionId.isNullOrBlank() && !result.missionStepId.isNullOrBlank())
                "mission:${result.missionSessionId}:${result.missionStepId}"
            else "drill:${result.date}:${result.slotId}"
        }
        val uniqueDates = validDrills.map { it.date }.distinct().size
        val missionCount = save.missionReceipts.orEmpty()
            .filter { it.id.isNotBlank() && MissionTraining.receiptValid(save, it) }
            .map { it.id }.distinct().size
        return listOf(
            Milestone("first_recorded_drill_v1", "First rep recorded", "Save your first drill result.", validDrills.isNotEmpty(), validDrills.size),
            Milestone("five_recorded_drills_v1", "Five on the board", "Record five distinct drill results.", validDrills.size >= 5, validDrills.size),
            Milestone("three_training_days_v1", "Show up again", "Record drills on three different dates.", uniqueDates >= 3, uniqueDates),
            Milestone("first_mission_receipt_v1", "Mission complete", "Finish a saved Mission and keep its receipt.", missionCount >= 1, missionCount)
        )
    }
}
