package com.courtquestpro

/**
 * Pure scorekeeping transitions. A null history denotes a legacy saved match:
 * its existing score is preserved, but past scorers cannot be reconstructed.
 */
internal object Scorekeeper {
    data class Score(val player: Int, val opponent: Int, val history: List<Boolean>?)

    fun point(score: Score, playerScored: Boolean): Score {
        if (score.player >= 11 || score.opponent >= 11) return score
        val history = score.history ?: emptyList()
        return if (playerScored) Score(score.player + 1, score.opponent, history + true)
        else Score(score.player, score.opponent + 1, history + false)
    }

    fun undo(score: Score): Score {
        val history = score.history ?: return score
        if (history.isEmpty()) return score
        val last = history.last()
        if (last && score.player <= 0 || !last && score.opponent <= 0) return score
        return if (last) Score(score.player - 1, score.opponent, history.dropLast(1))
        else Score(score.player, score.opponent - 1, history.dropLast(1))
    }
}
