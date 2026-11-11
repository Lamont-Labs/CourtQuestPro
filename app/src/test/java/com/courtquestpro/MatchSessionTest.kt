package com.courtquestpro

import com.google.gson.Gson
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MatchSessionTest {
    private val today = "2026-11-10"
    private fun active() = SaveState(activeDate = today, matchPlayer = "Jordan", matchOpponent = "Casey")

    @Test fun namedGameCanResumeBeforeTheFirstPoint() {
        assertTrue(active().hasResumableMatch(today))
    }

    @Test fun restartRetainsAResumableGameAndItsUndoOrder() {
        val original = active().apply {
            playerScore = 2
            opponentScore = 1
            pointHistory = mutableListOf(true, false, true)
        }
        val restored = Gson().fromJson(Gson().toJson(original), SaveState::class.java)
        assertTrue(restored.hasResumableMatch(today))
        val undone = Scorekeeper.undo(Scorekeeper.Score(restored.playerScore, restored.opponentScore, restored.pointHistory))
        org.junit.Assert.assertEquals(Scorekeeper.Score(1, 1, listOf(true, false)), undone)
    }

    @Test fun completedAndWinningScoresDoNotOfferResume() {
        assertFalse(active().apply { championshipDone = true }.hasResumableMatch(today))
        assertFalse(active().apply { playerScore = 11 }.hasResumableMatch(today))
        assertFalse(active().apply { opponentScore = 11 }.hasResumableMatch(today))
    }

    @Test fun previousDayAndMissingPlayerNamesDoNotOfferResume() {
        assertFalse(active().hasResumableMatch("2026-11-11"))
        assertFalse(active().apply { matchPlayer = "  " }.hasResumableMatch(today))
        assertFalse(active().apply { matchOpponent = "" }.hasResumableMatch(today))
    }

    @Test fun corruptNegativeScoresDoNotOfferResume() {
        assertFalse(active().apply { playerScore = -1 }.hasResumableMatch(today))
        assertFalse(active().apply { opponentScore = -1 }.hasResumableMatch(today))
    }

    @Test fun legacyGameWithoutUndoHistoryStillPreservesItsScore() {
        val game = active().apply { playerScore = 7; opponentScore = 5; pointHistory = null }
        assertTrue(game.hasResumableMatch(today))
        org.junit.Assert.assertEquals(Scorekeeper.Score(7, 5, null),
            Scorekeeper.undo(Scorekeeper.Score(game.playerScore, game.opponentScore, game.pointHistory)))
    }
}
