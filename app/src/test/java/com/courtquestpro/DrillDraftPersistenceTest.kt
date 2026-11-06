package com.courtquestpro

import com.google.gson.Gson
import org.junit.Assert.*
import org.junit.Test

class DrillDraftPersistenceTest {
    private val gson = Gson()

    @Test fun draftSurvivesSaveRoundTripWithoutAwardingXp() {
        val state = SaveState(
            totalXp = 0,
            drillDrafts = mutableListOf(DrillDraft("2026-11-05", "slot-1", "s1", 8, 13, 85, true))
        )
        val restored = gson.fromJson(gson.toJson(state), SaveState::class.java)
        assertEquals(state.drillDrafts, restored.drillDrafts)
        assertEquals(0, restored.totalXp)
        assertTrue(restored.drillResults.orEmpty().isEmpty())
    }

    @Test fun oldSaveWithoutDraftsIsReadable() {
        val restored = gson.fromJson("""{"playerName":"Vincent","totalXp":50}""", SaveState::class.java)
        assertNull(restored.drillDrafts)
        assertEquals(50, restored.totalXp)
    }
}
