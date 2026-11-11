package com.courtquestpro

import com.google.gson.Gson
import org.junit.Assert.*
import org.junit.Test
import kotlin.random.Random

class TrainingLibraryTest {
    private val day = "2026-11-11"
    private fun input(name: String = "Corner touch shots", goal: Int = 5) =
        DrillInput(name, "Shooting", "Beginner", 4, goal, "Set your feet and make controlled shots from the corner.")
    private fun state() = SaveState(activeDate = day, difficulty = "Intermediate", weeklyPlan = "Shooting",
        slots = mutableListOf(Slot("daily-a", "s1"), Slot("daily-b", "b1")))

    @Test fun rejectsInvalidDefinitionsWithoutWriting() {
        val save = state()
        for (bad in listOf(input("").copy(title = " "), input().copy(goal = 0), input().copy(goal = 501),
            input().copy(minutes = 0), input().copy(minutes = 61), input().copy(instructions = ""),
            input().copy(category = "Unknown"), input().copy(level = "Unknown"))) {
            assertNotNull(TrainingLibrary.validation(bad))
            assertNull(TrainingLibrary.saveCustom(save, bad))
        }
        assertTrue(save.customDrills.orEmpty().isEmpty())
    }
    @Test fun customTargetCanBeCompletedAndRoundTrips() {
        val save = state()
        val drill = TrainingLibrary.saveCustom(save, input())!!
        assertFalse(DrillTargets.eligible(drill, 4, 5))
        assertTrue(DrillTargets.eligible(drill, 5, 6))
        val restored = Gson().fromJson(Gson().toJson(save), SaveState::class.java)
        assertEquals(drill, TrainingLibrary.catalog(restored).first { it.id == drill.id })
    }
    @Test fun editsKeepOldQueuedDefinitionAndMoveFavorite() {
        val save = state()
        val original = TrainingLibrary.saveCustom(save, input())!!
        save.slots[0] = Slot("daily-a", original.id)
        TrainingLibrary.toggleFavorite(save, original.id)
        val edited = TrainingLibrary.saveCustom(save, input("Revised corner shots", 8), original.id)!!
        assertNotEquals(original.id, edited.id)
        assertEquals(original.id, save.slots[0].drillId)
        assertEquals(5, DrillTargets.minimum(TrainingLibrary.catalog(save).first { it.id == original.id }))
        assertFalse(TrainingLibrary.available(save).any { it.id == original.id })
        assertTrue(save.favoriteDrillIds.orEmpty().contains(edited.id))
        assertFalse(save.favoriteDrillIds.orEmpty().contains(original.id))
    }
    @Test fun archiveRemovesFutureSelectionButRetainsHistoricalDefinition() {
        val save = state()
        val drill = TrainingLibrary.saveCustom(save, input())!!
        assertTrue(TrainingLibrary.archive(save, drill.id))
        assertFalse(TrainingLibrary.available(save).any { it.id == drill.id })
        assertTrue(TrainingLibrary.catalog(save).any { it.id == drill.id })
    }
    @Test fun legacyJsonHasUsableBuiltinLibrary() {
        val save = Gson().fromJson("""{"playerName":"Avery","totalXp":150}""", SaveState::class.java)
        assertEquals(18, TrainingLibrary.available(save).size)
        assertNull(save.practiceSession)
        assertEquals(150, save.totalXp)
    }
    @Test fun swapPreservesSlotIdentityAndRejectsLockedOrIneligibleChoices() {
        val save = state()
        assertTrue(TrainingLibrary.swap(save, "daily-a", "s2"))
        assertEquals("daily-a", save.slots[0].id)
        assertEquals("s2", save.slots[0].drillId)
        assertFalse(TrainingLibrary.swap(save, "daily-a", "b2"))
        assertFalse(TrainingLibrary.swap(save, "daily-a", "s3"))
        save.drillDrafts = mutableListOf(DrillDraft(day, "daily-a", "s2", remainingSeconds = 360))
        assertFalse(TrainingLibrary.swap(save, "daily-a", "s1"))
        save.slots[1].complete = true
        assertFalse(TrainingLibrary.swap(save, "daily-b", "b2"))
    }
    @Test fun shuffleChangesChoicesWithoutReplacingCompletedOrStartedSlots() {
        val save = state()
        save.slots += Slot("daily-c", "f1")
        save.slots[1].complete = true
        save.drillDrafts = mutableListOf(DrillDraft(day, "daily-c", "f1", successful = 1, attempted = 1, remainingSeconds = 300))
        save.awardedKeys += "drill_${day}_daily-b"
        assertEquals(1, TrainingLibrary.shuffle(save, Random(1)))
        assertEquals(listOf("daily-a", "daily-b", "daily-c"), save.slots.map { it.id })
        assertEquals(listOf("s2", "b1", "f1"), save.slots.map { it.drillId })
        assertEquals(1, save.drillDrafts!!.single().successful)
        assertEquals(listOf("drill_${day}_daily-b"), save.awardedKeys)
    }
    @Test fun suggestionsStayWithinLevelAndExplainFocus() {
        val save = state()
        val choices = TrainingLibrary.suggestions(save)
        assertTrue(choices.isNotEmpty())
        assertEquals("Shooting", choices.first().drill.category)
        assertTrue(choices.first().reason.contains("focus", ignoreCase = true))
        assertFalse(choices.any { it.drill.level in listOf("Advanced", "Expert", "Elite") })
    }
    @Test fun practiceResumesAndSavesExactlyOnceWithoutChangingDailyAwards() {
        val save = state().apply { totalXp = 100 }
        val drill = TrainingLibrary.saveCustom(save, input())!!
        assertTrue(TrainingLibrary.startPractice(save, drill.id, day))
        assertFalse(TrainingLibrary.startPractice(save, "s1", day))
        val session = save.practiceSession!!
        val draft = session.draft.copy(successful = 5, attempted = 6, targetConfirmed = true)
        assertTrue(TrainingLibrary.updatePracticeDraft(save, draft))
        val restored = Gson().fromJson(Gson().toJson(save), SaveState::class.java)
        val result = TrainingLibrary.finishPractice(restored, session.id, day)!!
        assertTrue(result.practice)
        assertEquals(5, result.completed)
        assertEquals(drill, result.drillSnapshot)
        assertNull(TrainingLibrary.finishPractice(restored, session.id, day))
        assertEquals(1, restored.drillResults!!.size)
        assertEquals(100, restored.totalXp)
        assertFalse(restored.slots.any { it.complete })
    }
    @Test fun practiceCannotSaveBelowTargetOrWithoutConfirmation() {
        val save = state()
        assertTrue(TrainingLibrary.startPractice(save, "s1", day))
        val session = save.practiceSession!!
        assertNull(TrainingLibrary.finishPractice(save, session.id, day))
        TrainingLibrary.updatePracticeDraft(save, session.draft.copy(successful = 15, attempted = 15))
        assertNull(TrainingLibrary.finishPractice(save, session.id, day))
        assertNotNull(save.practiceSession)
    }
    @Test fun practiceDiscardRequiresTheCurrentSessionIdentity() {
        val save = state()
        TrainingLibrary.startPractice(save, "s1", day)
        assertFalse(TrainingLibrary.discardPractice(save, "wrong-session"))
        assertNotNull(save.practiceSession)
        assertTrue(TrainingLibrary.discardPractice(save, save.practiceSession!!.id))
        assertNull(save.practiceSession)
        assertTrue(save.drillResults.orEmpty().isEmpty())
    }
}
