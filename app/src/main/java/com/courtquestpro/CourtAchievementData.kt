package com.courtquestpro

import java.time.LocalDate
import java.time.ZoneId

internal enum class AchievementCategory(val label: String) {
    CONSISTENCY("Consistency"),
    PRACTICE("Practice"),
    VOLUME("Practice volume"),
    SKILLS("Skills"),
    MILESTONES("Milestones"),
    RECORDS("Personal bests")
}

internal data class AchievementProgress(
    val id: String,
    val title: String,
    val category: AchievementCategory,
    val description: String,
    val current: Int,
    val target: Int,
    val earnedDate: LocalDate?
) {
    val earned: Boolean get() = current >= target
    val displayProgress: Int get() = current.coerceIn(0, target)
}

/**
 * Achievements are projections of saved evidence rather than mutable counters. Reopening
 * the app or processing the same result again therefore cannot duplicate an unlock.
 */
internal fun playerAchievements(
    save: SaveState,
    catalog: List<Drill>,
    today: LocalDate = LocalDate.now(ZoneId.systemDefault())
): List<AchievementProgress> {
    val results = validSavedDrillResults(save.drillResults.orEmpty())
        .filter { !LocalDate.parse(it.date).isAfter(today) }
        .sortedWith(compareBy<DrillResult> { it.date }.thenBy { it.recordedAt.orEmpty() })
    val dates = results.map { LocalDate.parse(it.date) }.distinct().sorted()
    val practiceResults = results.filter { it.practice }
    val categories = results.mapNotNull { drillCategory(it, catalog) }
        .distinct()
    val firstPerCategory = results.mapNotNull { result ->
        drillCategory(result, catalog)?.let { it to LocalDate.parse(result.date) }
    }.groupBy({ it.first }, { it.second }).mapValues { (_, datesForSkill) -> datesForSkill.minOrNull()!! }
    val firstAllSkillsDate = firstPerCategory.values.takeIf { it.size == courtSkillCategories.size }?.maxOrNull()

    val validReceipts = validMissionReceipts(save).filter { receipt ->
        missionReceiptDate(receipt)?.let { !it.isAfter(today) } ?: true
    }
    val missionDate = validReceipts.firstOrNull()?.let(::missionReceiptDate)

    val bestImprovements = personalBestImprovements(results)
    val earliestImprovement = bestImprovements.firstOrNull()?.let { LocalDate.parse(it.date) }

    return listOf(
        AchievementProgress(
            "first-saved-drill", "First saved drill", AchievementCategory.MILESTONES,
            "Complete and save one drill.", results.size.coerceAtMost(1), 1,
            results.firstOrNull()?.let { LocalDate.parse(it.date) }
        ),
        AchievementProgress(
            "ten-saved-drills", "Ten drills recorded", AchievementCategory.VOLUME,
            "Save ten valid drill results.", results.size.coerceAtMost(10), 10,
            results.getOrNull(9)?.let { LocalDate.parse(it.date) }
        ),
        AchievementProgress(
            "three-training-days", "Three training days", AchievementCategory.CONSISTENCY,
            "Train on three different calendar days.", dates.size.coerceAtMost(3), 3,
            dates.getOrNull(2)
        ),
        AchievementProgress(
            "five-extra-practices", "Extra practice regular", AchievementCategory.PRACTICE,
            "Save five extra-practice drills.", practiceResults.size.coerceAtMost(5), 5,
            practiceResults.getOrNull(4)?.let { LocalDate.parse(it.date) }
        ),
        AchievementProgress(
            "six-skill-categories", "Six-skill sampler", AchievementCategory.SKILLS,
            "Save at least one drill in each listed skill category.", categories.size.coerceAtMost(courtSkillCategories.size),
            courtSkillCategories.size, firstAllSkillsDate
        ),
        AchievementProgress(
            "first-mission-complete", "Mission completed", AchievementCategory.MILESTONES,
            "Complete a Mission with a valid saved receipt.", validReceipts.size.coerceAtMost(1), 1,
            missionDate
        ),
        AchievementProgress(
            "improved-comparable-record", "Raised a personal best", AchievementCategory.RECORDS,
            "Beat an earlier result for the same drill, measurement, and attempt count.",
            bestImprovements.size.coerceAtMost(1), 1, earliestImprovement
        )
    )
}
