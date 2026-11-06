package com.courtquestpro

import org.junit.Assert.assertEquals
import org.junit.Test

class ScorekeeperTest {
    private val empty = Scorekeeper.Score(0, 0, emptyList())

    @Test fun undoReversesLastScorerEvenWhenTrailing() {
        val score = Scorekeeper.point(Scorekeeper.point(empty, true), false)
        assertEquals(Scorekeeper.Score(1, 0, listOf(true)), Scorekeeper.undo(score))
    }

    @Test fun undoOnTieUsesHistoryNotScoreComparison() {
        val score = Scorekeeper.Score(4, 4, listOf(false, true, false))
        assertEquals(Scorekeeper.Score(4, 3, listOf(false, true)), Scorekeeper.undo(score))
    }

    @Test fun repeatedUndoStopsAtEmptyHistory() {
        val twice = Scorekeeper.point(Scorekeeper.point(empty, true), false)
        assertEquals(empty, Scorekeeper.undo(Scorekeeper.undo(twice)))
        assertEquals(empty, Scorekeeper.undo(empty))
    }

    @Test fun legacyScoreCannotBeGuessed() {
        val legacy = Scorekeeper.Score(5, 3, null)
        assertEquals(legacy, Scorekeeper.undo(legacy))
        assertEquals(Scorekeeper.Score(6, 3, listOf(true)), Scorekeeper.point(legacy, true))
    }

    @Test fun scoringStopsAtEleven() {
        val final = Scorekeeper.Score(11, 9, listOf(true))
        assertEquals(final, Scorekeeper.point(final, false))
        assertEquals(Scorekeeper.Score(10, 9, emptyList()), Scorekeeper.undo(final))
    }

    @Test fun impossibleUndoDoesNotCreateNegativeScore() {
        val corrupt = Scorekeeper.Score(0, 2, listOf(true))
        assertEquals(corrupt, Scorekeeper.undo(corrupt))
    }
}
