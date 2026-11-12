package com.courtquestpro

import com.google.gson.Gson
import org.junit.Assert.*
import org.junit.Test

class QuestAwardsTest {
    private fun recorded(slot: String) = DrillResult("2026-11-12", slot, "s1", 6, 10)

    @Test fun noEvidenceProducesNoAwards() {
        val save = SaveState()
        assertEquals(0, QuestAwards.reconcile(save, "2026-11-12T20:00:00Z"))
        assertTrue(save.questAwardReceipts.orEmpty().isEmpty())
    }

    @Test fun firstAwardIsIdempotentAndSurvivesPersistence() {
        val save = SaveState(drillResults = mutableListOf(recorded("a")))
        assertEquals(1, QuestAwards.reconcile(save, "2026-11-12T20:00:00Z"))
        val first = save.questAwardReceipts!!.single()
        assertEquals("first_recorded_drill_v1", first.id)
        assertEquals(64, first.evidenceFingerprint.length)
        assertEquals("SELF_RECORDED", first.evidenceClass)
        assertEquals(0, QuestAwards.reconcile(save, "2026-11-12T21:00:00Z"))
        val restored = Gson().fromJson(Gson().toJson(save), SaveState::class.java)
        assertEquals(0, QuestAwards.reconcile(restored, "2026-11-13T01:00:00Z"))
        assertEquals(first, restored.questAwardReceipts!!.single())
    }

    @Test fun laterEvidenceAddsOnlyNewAwards() {
        val save = SaveState(drillResults = mutableListOf(recorded("a")))
        assertEquals(1, QuestAwards.reconcile(save, "2026-11-12T20:00:00Z"))
        for (i in 2..5) save.drillResults!!.add(recorded("slot-$i"))
        assertEquals(1, QuestAwards.reconcile(save, "2026-11-12T21:00:00Z"))
        assertEquals(2, save.questAwardReceipts!!.size)
        assertEquals(0, QuestAwards.reconcile(save, "2026-11-12T22:00:00Z"))
    }

    @Test fun invalidTimestampDoesNotWriteReceipts() {
        val save = SaveState(drillResults = mutableListOf(recorded("a")))
        assertEquals(0, QuestAwards.reconcile(save, "not-a-timestamp"))
        assertNull(save.questAwardReceipts)
    }
}
