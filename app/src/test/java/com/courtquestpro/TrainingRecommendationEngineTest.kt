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

    @Test fun visibleSuggestionsMatchReceiptAndExplanations() {
        val save = state()
        val receipt = TrainingRecommendationEngine.evaluate(save, day)
        val visible = TrainingLibrary.suggestions(save, day)
        assertEquals(receipt.selectedIds, visible.map { it.drill.id })
        assertEquals(receipt.selectedIds.map { receipt.explanations[it] }, visible.map { it.reason })
    }

    @Test fun oldAndFutureResultsDoNotAlterEarlierDateReceipt() {
        val save = state()
        val original = TrainingRecommendationEngine.evaluate(save, day)
        save.drillResults = mutableListOf(
            DrillResult("2026-12-01", "future", "s1", 5, 6)
        )
        assertEquals(original, TrainingRecommendationEngine.evaluate(save, day))
        assertEquals(original.selectedIds, TrainingLibrary.suggestions(save, day).map { it.drill.id })
    }

    @Test fun qualifiedEvidenceAppearsInPlayerFacingExplanation() {
        val save = state()
        val drill = drills.first { it.id == "s1" }
        val schema = MissionMeasurement("shots-made-v1", 3, "Shots made",
            "Shots attempted", "+ MAKE", "Count made baskets.")
        save.drillResults = mutableListOf(
            DrillResult("2026-11-15", "one", "s1", 5, 10,
                drillSnapshot = drill, measurementSnapshot = schema),
            DrillResult("2026-11-16", "two", "s1", 6, 10,
                drillSnapshot = drill, measurementSnapshot = schema),
            DrillResult("2026-11-17", "three", "s1", 7, 10,
                drillSnapshot = drill, measurementSnapshot = schema)
        )
        val receipt = TrainingRecommendationEngine.evaluate(save, day)
        assertTrue("s1" in receipt.selectedIds)
        assertTrue(receipt.explanations["s1"].orEmpty().contains("18/30"))
        assertTrue(receipt.explanations["s1"].orEmpty().contains("self-recorded"))
        assertEquals(receipt.explanations["s1"],
            TrainingLibrary.suggestions(save, day).first { it.drill.id == "s1" }.reason)
    }

    @Test fun incompatibleSchemasDoNotProduceEvidenceClaims() {
        val save = state()
        val drill = drills.first { it.id == "s1" }
        save.drillResults = mutableListOf(
            DrillResult("2026-11-15", "one", "s1", 5, 10,
                drillSnapshot = drill, measurementSnapshot = MissionMeasurement(
                    "schema-a", 3, "Made", "Attempted", "+ MAKE", "A")),
            DrillResult("2026-11-16", "two", "s1", 6, 10,
                drillSnapshot = drill, measurementSnapshot = MissionMeasurement(
                    "schema-b", 3, "Made", "Attempted", "+ MAKE", "B")),
            DrillResult("2026-11-17", "three", "s1", 7, 10,
                drillSnapshot = drill, measurementSnapshot = MissionMeasurement(
                    "schema-c", 3, "Made", "Attempted", "+ MAKE", "C"))
        )
        assertFalse(TrainingRecommendationEngine.evaluate(save, day)
            .explanations["s1"].orEmpty().contains("self-recorded"))
    }

    @Test fun recordedReceiptSurvivesJsonRoundTripWithoutChangingTraining() {
        val save = state()
        save.slots = mutableListOf(Slot("daily", "s1"))
        val originalSlots = Gson().toJson(save.slots)
        val receipt = TrainingRecommendationEngine.evaluate(save, day)
        TrainingRecommendationEngine.record(save, receipt)
        TrainingRecommendationEngine.record(save, receipt)
        val restored = Gson().fromJson(Gson().toJson(save), SaveState::class.java)
        assertEquals(listOf(receipt), restored.recommendationReceipts)
        assertEquals(originalSlots, Gson().toJson(restored.slots))
        assertEquals(receipt.favoriteIds, restored.recommendationReceipts!!.single().favoriteIds)
    }

    @Test fun boundedHistoryAndLegacySaveCompatibility() {
        val save = Gson().fromJson("""{"playerName":"Avery"}""", SaveState::class.java)
        assertNull(save.recommendationReceipts)
        repeat(40) { dayOffset ->
            val date = day.plusDays(dayOffset.toLong())
            TrainingRecommendationEngine.record(save,
                TrainingRecommendationEngine.evaluate(save, date))
        }
        assertEquals(TrainingRecommendationEngine.MAX_SAVED_RECEIPTS,
            save.recommendationReceipts!!.size)
        assertEquals(day.plusDays(10).toString(), save.recommendationReceipts!!.first().date)
    }

    @Test fun savedDailyReceiptReplaysAcrossRestartAndPreferenceChanges() {
        val save = state()
        val original = TrainingRecommendationEngine.evaluate(save, day)
        TrainingRecommendationEngine.record(save, original)
        val restored = Gson().fromJson(Gson().toJson(save), SaveState::class.java)
        restored.weeklyPlan = "Defense"
        restored.favoriteDrillIds = mutableListOf("d1")
        val shown = TrainingLibrary.suggestions(restored, day)
        assertEquals(original.selectedIds, shown.map { it.drill.id })
        assertEquals(original.selectedIds.map { original.explanations[it] },
            shown.map { it.reason })
        assertEquals(original, restored.recommendationReceipts!!.single())
    }

    @Test fun archivedDrillIsNotDisplayedEvenWhenPresentInFrozenReceipt() {
        val save = state()
        val custom = TrainingLibrary.saveCustom(save, DrillInput(
            "Frozen shot", "Shooting", "Beginner", 4, 5,
            "Practice controlled shots from the same spot."))!!
        val receipt = TrainingRecommendationEngine.evaluate(save, day, 100)
        assertTrue(custom.id in receipt.selectedIds)
        TrainingRecommendationEngine.record(save, receipt)
        TrainingLibrary.archive(save, custom.id)
        assertFalse(TrainingLibrary.suggestions(save, day).any { it.drill.id == custom.id })
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
