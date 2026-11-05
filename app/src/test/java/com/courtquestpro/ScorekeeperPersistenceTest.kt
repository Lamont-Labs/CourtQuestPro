package com.courtquestpro

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Regression coverage for persisted scoring state across app restarts. */
class ScorekeeperPersistenceTest {
    private val gson = Gson()

    @Test fun roundTripPreservesScoringOrderAndUndo() {
        val original = SaveState(
            playerScore = 2,
            opponentScore = 1,
            pointHistory = mutableListOf(true, false, true),
            matchPlayer = "Vincent",
            matchOpponent = "Opponent"
        )
        val restored = gson.fromJson(gson.toJson(original), SaveState::class.java)
        assertEquals(listOf(true, false, true), restored.pointHistory)
        assertEquals("Vincent", restored.matchPlayer)
        val undone = Scorekeeper.undo(
            Scorekeeper.Score(restored.playerScore, restored.opponentScore, restored.pointHistory)
        )
        assertEquals(Scorekeeper.Score(1, 1, listOf(true, false)), undone)
    }

    @Test fun olderSavedJsonWithoutHistoryDoesNotGuessScorer() {
        val legacy = gson.fromJson(
            """{"playerScore":7,"opponentScore":5,"matchPlayer":"Vincent","matchOpponent":"Opponent"}""",
            SaveState::class.java
        )
        assertNull(legacy.pointHistory)
        assertEquals(
            Scorekeeper.Score(7, 5, null),
            Scorekeeper.undo(Scorekeeper.Score(legacy.playerScore, legacy.opponentScore, legacy.pointHistory))
        )
    }

    @Test fun newlyScoredPointAfterLegacyRestoreIsUndoable() {
        val legacy = gson.fromJson(
            """{"playerScore":7,"opponentScore":5}""",
            SaveState::class.java
        )
        val next = Scorekeeper.point(
            Scorekeeper.Score(legacy.playerScore, legacy.opponentScore, legacy.pointHistory),
            false
        )
        assertEquals(Scorekeeper.Score(7, 6, listOf(false)), next)
        assertEquals(Scorekeeper.Score(7, 5, emptyList()), Scorekeeper.undo(next))
    }
}
