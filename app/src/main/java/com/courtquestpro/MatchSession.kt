package com.courtquestpro

/** Resume existing same-day state without changing the persisted schema. */
internal fun SaveState.hasResumableMatch(today: String): Boolean =
    activeDate == today && !championshipDone &&
        matchPlayer.isNotBlank() && matchOpponent.isNotBlank() &&
        playerScore in 0..10 && opponentScore in 0..10
