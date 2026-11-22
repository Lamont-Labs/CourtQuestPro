package com.courtquestpro

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import java.time.DayOfWeek

internal const val DAILY_CHECK_IN_XP = 25
internal const val WEEKLY_CONSISTENCY_XP = 100
internal const val WEEKLY_CONSISTENCY_DAYS = 3

internal data class QuestProgress(
    val id: String,
    val title: String,
    val objective: String,
    val current: Int,
    val target: Int,
    val rewardXp: Int,
    val completedAt: LocalDate?,
    val rewardGranted: Boolean
) {
    val complete: Boolean get() = current >= target
}

internal data class QuestCompletion(
    val id: String,
    val title: String,
    val periodLabel: String,
    val rewardXp: Int,
    val earnedDate: LocalDate
)

internal data class DailyPlanProgress(
    val current: Int,
    val target: Int,
    val earnedXp: Int,
    val possibleXp: Int
) {
    val complete: Boolean get() = target > 0 && current >= target
}

/**
 * New quest rewards apply only to results recorded after the feature was first enabled.
 * This additive timestamp prevents an app update from granting retroactive quest XP for
 * old saved training while still allowing idempotent recovery after an interrupted save.
 */
internal object CourtQuestEngine {
    private const val DAILY_KEY = "quest_daily_first_saved_drill_v1_"
    private const val WEEKLY_KEY = "quest_weekly_three_training_days_v1_"
    private val localZone: ZoneId get() = ZoneId.systemDefault()

    fun activate(save: SaveState, now: Instant): Boolean {
        val existing = save.questSystemActivatedAt?.let { runCatching { Instant.parse(it) }.getOrNull() }
        if (existing != null) return false
        save.questSystemActivatedAt = now.toString()
        return true
    }

    fun dailyProgress(save: SaveState, today: LocalDate, now: Instant): QuestProgress {
        val eligible = eligibleResults(save, now).filter { it.date == today.toString() }
        val key = dailyKey(today)
        return QuestProgress(
            id = key,
            title = "Save a drill today",
            objective = "Record one completed drill. Only saved results count.",
            current = eligible.size.coerceAtMost(1),
            target = 1,
            rewardXp = DAILY_CHECK_IN_XP,
            completedAt = today.takeIf { eligible.isNotEmpty() },
            rewardGranted = key in save.awardedKeys
        )
    }

    fun weeklyProgress(save: SaveState, today: LocalDate, now: Instant): QuestProgress {
        val weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val activeDays = eligibleResults(save, now)
            .mapNotNull { result ->
                runCatching { LocalDate.parse(result.date) }.getOrNull()
                    ?.takeIf { !it.isBefore(weekStart) && !it.isAfter(today) }
            }
            .distinct()
            .sorted()
        val key = weeklyKey(weekStart)
        return QuestProgress(
            id = key,
            title = "Train on three different days",
            objective = "Save training on three days from Monday through Sunday.",
            current = activeDays.size.coerceAtMost(WEEKLY_CONSISTENCY_DAYS),
            target = WEEKLY_CONSISTENCY_DAYS,
            rewardXp = WEEKLY_CONSISTENCY_XP,
            completedAt = activeDays.getOrNull(WEEKLY_CONSISTENCY_DAYS - 1),
            rewardGranted = key in save.awardedKeys
        )
    }

    /** Runs after saved-result writes and on launch; awarded keys make replay harmless. */
    fun reconcile(save: SaveState, today: LocalDate, now: Instant): Boolean {
        if (save.questSystemActivatedAt?.let { runCatching { Instant.parse(it) }.getOrNull() } == null) {
            return false
        }
        var changed = false
        val daily = dailyProgress(save, today, now)
        if (daily.complete) changed = awardXpOnce(save, daily.id, daily.rewardXp) || changed
        val weekly = weeklyProgress(save, today, now)
        if (weekly.complete) changed = awardXpOnce(save, weekly.id, weekly.rewardXp) || changed
        return changed
    }

    fun dailyPlanProgress(save: SaveState, today: LocalDate): DailyPlanProgress {
        val slots = save.slots
        if (save.activeDate != today.toString() || slots.isEmpty()) {
            return DailyPlanProgress(0, 0, 0, 0)
        }
        val recorded = validSavedDrillResults(save.drillResults.orEmpty())
            .filter { it.date == today.toString() && !it.practice }
            .map { it.slotId to it.drillId }
            .toSet()
        val current = slots.count { (it.id to it.drillId) in recorded }
        val earnedDrillXp = slots.count { "drill_${today}_${it.id}" in save.awardedKeys } * 50
        val earned = earnedDrillXp + (if ("six_$today" in save.awardedKeys) 100 else 0)
        val possible = slots.size * 50 + (if (slots.size == 6) 100 else 0)
        return DailyPlanProgress(current, slots.size, earned, possible)
    }

    fun completedHistory(save: SaveState): List<QuestCompletion> {
        val daily = save.awardedKeys.mapNotNull { key ->
            if (!key.startsWith(DAILY_KEY)) return@mapNotNull null
            val date = runCatching { LocalDate.parse(key.removePrefix(DAILY_KEY)) }.getOrNull()
                ?: return@mapNotNull null
            QuestCompletion(key, "Daily check-in", date.toString(), DAILY_CHECK_IN_XP, date)
        }
        val dailyPlans = save.awardedKeys.mapNotNull { key ->
            if (!key.startsWith("six_")) return@mapNotNull null
            val date = runCatching { LocalDate.parse(key.removePrefix("six_")) }.getOrNull()
                ?: return@mapNotNull null
            QuestCompletion(key, "Six-drill daily plan", date.toString(), 100, date)
        }
        val weekly = save.awardedKeys.mapNotNull { key ->
            if (!key.startsWith(WEEKLY_KEY)) return@mapNotNull null
            val date = runCatching { LocalDate.parse(key.removePrefix(WEEKLY_KEY)) }.getOrNull()
                ?: return@mapNotNull null
            QuestCompletion(
                key, "Three-day training week", "Week of $date",
                WEEKLY_CONSISTENCY_XP, date
            )
        }
        return (daily + dailyPlans + weekly).distinctBy { it.id }.sortedByDescending { it.earnedDate }
    }

    fun dailyKey(date: LocalDate): String = "$DAILY_KEY$date"
    fun weeklyKey(monday: LocalDate): String = "$WEEKLY_KEY$monday"

    private fun eligibleResults(save: SaveState, now: Instant): List<DrillResult> {
        val activatedAt = save.questSystemActivatedAt
            ?.let { runCatching { Instant.parse(it) }.getOrNull() } ?: return emptyList()
        return validSavedDrillResults(save.drillResults.orEmpty()).filter { result ->
            val recordedAt = result.recordedAt?.let { runCatching { Instant.parse(it) }.getOrNull() }
                ?: return@filter false
            val recordedDate = runCatching { LocalDate.parse(result.date) }.getOrNull()
                ?: return@filter false
            !recordedAt.isBefore(activatedAt) && !recordedAt.isAfter(now) &&
                recordedAt.atZone(localZone).toLocalDate() == recordedDate
        }
    }
}

internal fun awardXpOnce(save: SaveState, key: String, xp: Int): Boolean {
    require(key.isNotBlank()) { "An XP award needs a stable idempotency key." }
    require(xp > 0) { "An XP award must be positive." }
    if (key in save.awardedKeys) return false
    save.awardedKeys.add(key)
    save.totalXp = (save.totalXp.toLong() + xp).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
    return true
}
