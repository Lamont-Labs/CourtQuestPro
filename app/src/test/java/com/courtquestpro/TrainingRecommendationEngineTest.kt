package com.courtquestpro

import com.google.gson.Gson
import java.time.LocalDate
import org.junit.Assert.*
import org.junit.Test

class TrainingRecommendationEngineTest {
    private val day = LocalDate.parse("2026-11-18")
    private fun state() = SaveState(difficulty = "Intermediate", weeklyPlan = "Shooting")

    @Test fun sameInputsProduceIdenticalVersionedReceipt() {
        val save = state()
        val first = TrainingRecommendationEngine.evaluate(save, day)
        assertEquals(TrainingRecommendationEngine.VERSION, first.version)
        assertEquals(first, TrainingRecommendationEngine.evaluate(save, day))
        assertEquals(first, TrainingRecommendationEngine.evaluate(
            Gson().fromJson(Gson().toJson(save), SaveState::class.java), day))
        assertEquals(4, first.selectedIds.size)
        assertTrue(first.selectedIds.all { it in first.eligibleIds })
    }

    @Test fun excludesAdvancedAndArchivedCustomDrills() {
        val save = state()
        val custom = TrainingLibrary.saveCustom(save, DrillInput(
            "Corner form", "Shooting", "Beginner", 4, 5,
            "Make five controlled shots from a comfortable corner."
        ))!!
        TrainingLibrary.archive(save, custom.id)
        val receipt = TrainingRecommendationEngine.evaluate(save, day, 100)
        assertFalse(custom.id in receipt.eligibleIds)
        assertFalse(custom.id in receipt.selectedIds)
        assertFalse("s3" in receipt.eligibleIds)
        assertTrue("s3" in receipt.excludedIds)
    }

    @Test fun doesNotMutateActivePlanOrDrafts() {
        val save = state()
        save.activeDate = day.toString()
        save.slots = mutableListOf(Slot("slot-a", "s1"))
        save.drillDrafts = mutableListOf(DrillDraft(day.toString(), "slot-a", "s1",
            successful = 2, attempted = 3, remainingSeconds = 120))
        val before = Gson().toJson(save)
        TrainingRecommendationEngine.evaluate(save, day)
        assertEquals(before, Gson().toJson(save))
    }

    @Test fun zeroLimitIsValidAndNegativeLimitRejected() {
        assertTrue(TrainingRecommendationEngine.evaluate(state(), day, 0).selectedIds.isEmpty())
        assertThrows(IllegalArgumentException::class.java) {
            TrainingRecommendationEngine.evaluate(state(), day, -1)
        }
    }

    @Test fun futureRecordsCannotChangeEarlierRecommendations() {
        val save = state()
        val before = TrainingRecommendationEngine.evaluate(save, day)
        save.drillResults = mutableListOf(DrillResult("2026-12-01", "later", "s1", 5, 6))
        assertEquals(before, TrainingRecommendationEngine.evaluate(save, day))
    }
}
