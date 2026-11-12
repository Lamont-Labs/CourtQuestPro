package com.courtquestpro

import com.google.gson.Gson
import org.junit.Assert.*
import org.junit.Test

class QuestMilestonesTest {
    private fun drill(day: String, slot: String, attempts: Int = 10, made: Int = 6) =
        DrillResult(day, slot, "s1", made, attempts)

    @Test fun emptyProfileHasNoEarnedMilestones() {
        val milestones = QuestMilestones.evaluate(SaveState())
        assertEquals(QuestMilestones.RULESET_VERSION, 1)
        assertEquals(4, milestones.size)
        assertTrue(milestones.none { it.achieved })
    }

    @Test fun repeatedDailyRecordsDoNotMultiplyEvidence() {
        val save = SaveState(drillResults = mutableListOf(
            drill("2026-11-10", "slot-a"),
            drill("2026-11-10", "slot-a"),
            drill("2026-11-11", "slot-b")
        ))
        val milestones = QuestMilestones.evaluate(save).associateBy { it.id }
        assertTrue(milestones.getValue("first_recorded_drill_v1").achieved)
        assertEquals(2, milestones.getValue("five_recorded_drills_v1").evidenceCount)
        assertFalse(milestones.getValue("three_training_days_v1").achieved)
    }

    @Test fun malformedResultsAreExcluded() {
        val save = SaveState(drillResults = mutableListOf(
            drill("2026-11-10", "invalid-negative", -1),
            drill("2026-11-10", "invalid-made", 2, 3),
            drill("", "missing-date")
        ))
        assertTrue(QuestMilestones.evaluate(save).none { it.achieved })
    }

    @Test fun milestonesSurviveJsonRoundTripAndCountDistinctDates() {
        val save = SaveState(drillResults = mutableListOf(
            drill("2026-11-10", "a"), drill("2026-11-10", "b"),
            drill("2026-11-11", "c"), drill("2026-11-12", "d"),
            drill("2026-11-12", "e")
        ))
        val restored = Gson().fromJson(Gson().toJson(save), SaveState::class.java)
        val milestones = QuestMilestones.evaluate(restored).associateBy { it.id }
        assertTrue(milestones.getValue("five_recorded_drills_v1").achieved)
        assertTrue(milestones.getValue("three_training_days_v1").achieved)
        assertFalse(milestones.getValue("first_mission_receipt_v1").achieved)
    }

    @Test fun forgedMissionReceiptDoesNotEarnMilestone() {
        val fake = MissionReceipt("fake", "missing-session", "missing-version",
            emptyList(), "not-a-hash", "2026-11-12T12:00:00Z")
        val save = SaveState(missionReceipts = mutableListOf(fake))
        val mission = QuestMilestones.evaluate(save).single { it.id == "first_mission_receipt_v1" }
        assertFalse(mission.achieved)
        assertEquals(0, mission.evidenceCount)
    }
}
