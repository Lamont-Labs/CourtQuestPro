package com.courtquestpro

import com.google.gson.Gson
import org.junit.Assert.*
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class PlayerProgressionTest {
    private val zone = ZoneId.systemDefault()
    private val wednesday = LocalDate.of(2026, 11, 18)

    private fun at(date: LocalDate, hour: Int = 12): Instant =
        date.atTime(hour, 0).atZone(zone).toInstant()

    private fun result(
        date: LocalDate,
        slot: String,
        drill: Drill = drills.first(),
        completed: Int = 3,
        attempted: Int = 5,
        practice: Boolean = false,
        schema: String? = "test-measurement-v1",
        recordedAt: Instant? = at(date)
    ) = DrillResult(
        date = date.toString(),
        slotId = slot,
        drillId = drill.id,
        completed = completed,
        attempted = attempted,
        practice = practice,
        drillSnapshot = drill,
        measurementSnapshot = schema?.let {
            MissionMeasurement(it, attempted, "makes", "attempts", "Make", "Saved test evidence")
        },
        recordedAt = recordedAt?.toString()
    )

    private fun activatedSave(start: Instant = at(wednesday.minusDays(7), 0)) =
        SaveState().also { it.questSystemActivatedAt = start.toString() }

    @Test
    fun newQuestActivationDoesNotAwardOrCountLegacyResults() {
        val save = SaveState(totalXp = 420)
        save.awardedKeys.add("drill_${wednesday}_old-slot")
        save.drillResults = mutableListOf(result(wednesday, "old-slot", recordedAt = at(wednesday, 8)))

        assertTrue(CourtQuestEngine.activate(save, at(wednesday, 9)))
        assertEquals(0, CourtQuestEngine.dailyProgress(save, wednesday, at(wednesday, 10)).current)
        assertFalse(CourtQuestEngine.reconcile(save, wednesday, at(wednesday, 10)))
        assertEquals(420, save.totalXp)
        assertTrue(save.awardedKeys.contains("drill_${wednesday}_old-slot"))
    }

    @Test
    fun abandonedDraftsAndInvalidResultsDoNotCompleteOrRewardDailyQuest() {
        val now = at(wednesday, 18)
        val save = activatedSave(at(wednesday, 8)).also {
            it.drillDrafts = mutableListOf(
                DrillDraft(
                    date = wednesday.toString(),
                    slotId = "draft",
                    drillId = "s1",
                    successful = 2,
                    attempted = 3,
                    remainingSeconds = 60,
                    targetConfirmed = true
                )
            )
            it.drillResults = mutableListOf(
                result(wednesday, "invalid", completed = 6, attempted = 5, recordedAt = at(wednesday, 10)),
                result(wednesday, "unrecorded", recordedAt = null)
            )
        }

        assertEquals(0, CourtQuestEngine.dailyProgress(save, wednesday, now).current)
        assertFalse(CourtQuestEngine.reconcile(save, wednesday, now))
        assertEquals(0, save.totalXp)
        assertTrue(save.awardedKeys.isEmpty())
    }

    @Test
    fun dailyAndWeeklyRewardsAreStableAcrossReplayAndPersistence() {
        val now = at(wednesday, 18)
        val monday = wednesday.minusDays(2)
        val save = activatedSave(at(monday, 0)).also {
            it.drillResults = mutableListOf(
                result(monday, "monday"),
                result(monday.plusDays(1), "tuesday"),
                result(wednesday, "wednesday")
            )
        }

        assertEquals(3, CourtQuestEngine.weeklyProgress(save, wednesday, now).current)
        assertTrue(CourtQuestEngine.weeklyProgress(save, wednesday, now).complete)
        assertTrue(CourtQuestEngine.dailyProgress(save, wednesday, now).complete)
        assertTrue(CourtQuestEngine.reconcile(save, wednesday, now))
        assertEquals(DAILY_CHECK_IN_XP + WEEKLY_CONSISTENCY_XP, save.totalXp)

        val persisted = Gson().fromJson(Gson().toJson(save), SaveState::class.java)
        assertFalse(CourtQuestEngine.reconcile(persisted, wednesday, now))
        assertFalse(CourtQuestEngine.reconcile(persisted, wednesday, now))
        assertEquals(save.totalXp, persisted.totalXp)
        assertEquals(save.awardedKeys.toSet(), persisted.awardedKeys.toSet())
        assertEquals(2, CourtQuestEngine.completedHistory(persisted).size)
    }

    @Test
    fun weeklyQuestCountsDistinctCurrentWeekDaysAndUsesMondayKey() {
        val monday = wednesday.minusDays(2)
        val save = activatedSave(at(monday, 0)).also {
            it.drillResults = mutableListOf(
                result(monday, "slot-1"),
                result(monday, "duplicate-result"),
                result(monday.plusDays(1), "slot-2")
            )
        }
        val twoDays = CourtQuestEngine.weeklyProgress(save, wednesday, at(wednesday, 18))
        assertEquals(2, twoDays.current)
        assertFalse(twoDays.complete)
        assertEquals(CourtQuestEngine.weeklyKey(monday), twoDays.id)

        save.drillResults!!.add(result(wednesday, "slot-3"))
        assertEquals(3, CourtQuestEngine.weeklyProgress(save, wednesday, at(wednesday, 18)).current)
        assertEquals(1, CourtQuestEngine.weeklyProgress(save, monday, at(monday, 18)).current)
    }

    @Test
    fun xpAwardKeyPreventsDuplicatesAndPreservesExistingAwards() {
        val save = SaveState(totalXp = 150)
        assertTrue(awardXpOnce(save, "daily-1", 50))
        assertFalse(awardXpOnce(save, "daily-1", 50))
        assertEquals(200, save.totalXp)
        save.awardedKeys.add("legacy-key")
        assertFalse(awardXpOnce(save, "legacy-key", 10))
        assertEquals(200, save.totalXp)
    }

    @Test
    fun existingDailyPlanProgressUsesSavedSlotsAndOriginalAwardKeys() {
        val slots = (0 until 6).map { index ->
            Slot("plan-$index", drills[index].id)
        }
        val save = SaveState(activeDate = wednesday.toString()).also {
            it.slots = slots.toMutableList()
            it.drillResults = mutableListOf(
                result(wednesday, "plan-0", drills[0]),
                result(wednesday, "extra-practice", drills[1], practice = true)
            )
            it.awardedKeys.add("drill_${wednesday}_plan-0")
        }
        val partial = CourtQuestEngine.dailyPlanProgress(save, wednesday)
        assertEquals(1, partial.current)
        assertEquals(6, partial.target)
        assertEquals(50, partial.earnedXp)
        assertEquals(400, partial.possibleXp)

        save.drillResults = slots.mapIndexed { index, slot ->
            result(wednesday, slot.id, drills[index])
        }.toMutableList()
        save.awardedKeys.addAll(slots.map { "drill_${wednesday}_${it.id}" })
        save.awardedKeys.add("six_$wednesday")
        val complete = CourtQuestEngine.dailyPlanProgress(save, wednesday)
        assertTrue(complete.complete)
        assertEquals(6, complete.current)
        assertEquals(400, complete.earnedXp)
    }

    @Test
    fun oldSaveJsonMigratesAdditivelyWithoutLosingExistingFields() {
        val oldJson = """{"playerName":"Saved Player","totalXp":735,"awardedKeys":["drill_old"],"slots":[],"drillResults":[]}"""
        val oldSave = Gson().fromJson(oldJson, SaveState::class.java)
        assertNull(oldSave.questSystemActivatedAt)
        assertEquals("Saved Player", oldSave.playerName)
        assertEquals(735, oldSave.totalXp)
        assertEquals(listOf("drill_old"), oldSave.awardedKeys)

        CourtQuestEngine.activate(oldSave, at(wednesday))
        val restored = Gson().fromJson(Gson().toJson(oldSave), SaveState::class.java)
        assertEquals(oldSave.questSystemActivatedAt, restored.questSystemActivatedAt)
        assertEquals(735, restored.totalXp)
        assertEquals(listOf("drill_old"), restored.awardedKeys)
    }

    @Test
    fun progressRangesCountValidSavedEvidenceAndChartAllTimeByMonth() {
        val today = wednesday
        val records = listOf(
            result(today, "today"),
            result(today.minusDays(6), "week"),
            result(today.minusDays(7), "month"),
            result(today.plusDays(1), "future"),
            result(today, "bad", completed = -1)
        )
        assertEquals(2, resultsInProgressRange(records, today, ProgressRange.SEVEN_DAYS).size)
        assertEquals(3, resultsInProgressRange(records, today, ProgressRange.THIRTY_DAYS).size)
        val all = resultsInProgressRange(records, today, ProgressRange.ALL_TIME)
        assertEquals(3, all.size)
        assertEquals(12, courtActivityBuckets(records, today, ProgressRange.ALL_TIME).size)
        assertEquals(2, courtActivityBuckets(records, today, ProgressRange.SEVEN_DAYS).sumOf { it.count })
    }

    @Test
    fun achievementsUseSavedEvidenceAndOnlyCompareLikeForLikeRecords() {
        val base = wednesday.minusDays(2)
        val skillDrills = courtSkillCategories.map { category ->
            drills.first { it.category == category }
        }
        val saved = (0 until 10).map { index ->
            val drill = skillDrills[index % courtSkillCategories.size]
            result(
                base.plusDays((index / 4).toLong()),
                "saved-$index",
                drill = drill,
                completed = index + 1,
                attempted = 20,
                practice = index < 5
            )
        }
        val sameDrill = skillDrills.first()
        val bestRecords = listOf(
            result(base, "best-baseline", sameDrill, completed = 2, attempted = 20),
            result(base.plusDays(1), "best-raised", sameDrill, completed = 7, attempted = 20)
        )
        val save = SaveState().also { it.drillResults = (saved + bestRecords).toMutableList() }
        val achievements = playerAchievements(save, drills, wednesday.plusDays(10))

        assertTrue(achievements.first { it.id == "first-saved-drill" }.earned)
        assertTrue(achievements.first { it.id == "ten-saved-drills" }.earned)
        assertTrue(achievements.first { it.id == "three-training-days" }.earned)
        assertTrue(achievements.first { it.id == "five-extra-practices" }.earned)
        assertTrue(achievements.first { it.id == "six-skill-categories" }.earned)
        assertFalse(achievements.first { it.id == "first-mission-complete" }.earned)
        assertTrue(achievements.first { it.id == "improved-comparable-record" }.earned)
        assertEquals(achievements, playerAchievements(save, drills, wednesday.plusDays(10)))

        val unrelated = listOf(
            result(base, "five-attempts", drills.first(), 3, 5),
            result(base.plusDays(1), "ten-attempts", drills.first(), 8, 10)
        )
        assertTrue(comparablePersonalBests(unrelated).isEmpty())
        assertTrue(personalBestImprovements(unrelated).isEmpty())
    }

    @Test
    fun missionAchievementRequiresAValidReceipt() {
        val achievements = playerAchievements(SaveState(), drills, wednesday)
        val mission = achievements.first { it.id == "first-mission-complete" }
        assertEquals(0, mission.current)
        assertFalse(mission.earned)
    }

    @Test
    fun futureDatedRecordsDoNotInflateProgressOrUnlockAchievements() {
        val save = SaveState().also {
            it.drillResults = mutableListOf(result(wednesday.plusDays(1), "future"))
        }
        assertTrue(resultsInProgressRange(save.drillResults.orEmpty(), wednesday, ProgressRange.ALL_TIME).isEmpty())
        assertFalse(playerAchievements(save, drills, wednesday).first {
            it.id == "first-saved-drill"
        }.earned)
    }
}
