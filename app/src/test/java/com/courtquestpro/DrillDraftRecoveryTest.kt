package com.courtquestpro

import org.junit.Assert.*
import org.junit.Test

class DrillDraftRecoveryTest {
    private val date = "2026-11-05"
    private val slot = Slot("slot-a", "s1", false)
    private fun draft(date: String = this.date, successful: Int = 4, attempted: Int = 7,
                      remaining: Int = 180) =
        DrillDraft(date, "slot-a", "s1", successful, attempted, remaining)

    @Test fun acceptsOnlyMatchingActiveValidDrafts() {
        assertNotNull(DrillDraftRecovery.recover(listOf(draft()), date, slot, 360))
        assertNull(DrillDraftRecovery.recover(listOf(draft("2026-11-04")), date, slot, 360))
        assertNull(DrillDraftRecovery.recover(listOf(draft(successful = 8)), date, slot, 360))
        assertNull(DrillDraftRecovery.recover(listOf(draft(remaining = 400)), date, slot, 360))
        assertNull(DrillDraftRecovery.recover(listOf(draft()), date, slot.copy(complete = true), 360))
    }

    @Test fun pruningRemovesOldAndInvalidDrafts() {
        val input = listOf(draft("2026-11-04"), draft(successful = 9), draft())
        val output = DrillDraftRecovery.prune(input, date, listOf(slot)) { 360 }
        assertEquals(listOf(draft()), output)
    }

    @Test fun latestValidDraftWinsForSameSlot() {
        val input = listOf(draft(successful = 2), draft(successful = 5))
        val output = DrillDraftRecovery.prune(input, date, listOf(slot)) { 360 }
        assertEquals(1, output.size)
        assertEquals(5, output.single().successful)
    }
}
