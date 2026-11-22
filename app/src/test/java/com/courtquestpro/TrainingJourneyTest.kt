package com.courtquestpro

import org.junit.Assert.*
import org.junit.Test

class TrainingJourneyTest {
    private fun state() = SaveState(activeDate = "2026-11-11", slots = mutableListOf(
        Slot("first", "s1", false), Slot("second", "b1", false), Slot("third", "f1", false)))

    @Test fun untouchedWorkoutStartsAtFirstUnfinishedSlot() {
        val save = state()
        save.slots[0].complete = true
        assertEquals(1, nextDailySlot(save))
    }

    @Test fun currentDraftResumesBeforeUntouchedSlots() {
        val save = state()
        save.drillDrafts = mutableListOf(DrillDraft(save.activeDate, "third", "f1", 3, 5, 120))
        assertEquals(2, nextDailySlot(save))
    }

    @Test fun invalidAndExpiredDraftsDoNotChangeTheNextDrill() {
        val save = state()
        save.drillDrafts = mutableListOf(
            DrillDraft("2026-11-10", "second", "b1", 1, 2, 120),
            DrillDraft(save.activeDate, "third", "f1", 6, 2, 120))
        assertEquals(0, nextDailySlot(save))
    }

    @Test fun receiptAndContinueChooseTheSameRemainingDrill() {
        val save = state()
        save.slots[0].complete = true
        val shownOnReceipt = nextDailySlot(save)
        repeat(20) { assertEquals(shownOnReceipt, nextDailySlot(save)) }
        assertEquals("b1", save.slots[shownOnReceipt!!].drillId)
    }

    @Test fun completedWorkoutHasNoNextDrill() {
        val save = state()
        save.slots.forEach { it.complete = true }
        save.drillDrafts = mutableListOf(DrillDraft(save.activeDate, "third", "f1", 3, 5, 120))
        assertNull(nextDailySlot(save))
    }

    @Test fun emptyWorkoutHasNoNextDrill() { assertNull(nextDailySlot(SaveState())) }
}
