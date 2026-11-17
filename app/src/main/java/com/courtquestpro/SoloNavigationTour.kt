package com.courtquestpro

/** UI-only sequence. No player data or training state is changed by these hints. */
internal object SoloNavigationTour {
    val tabs = listOf(PlayerTab.COURT, PlayerTab.TRAIN, PlayerTab.QUESTS, PlayerTab.PROGRESS, PlayerTab.ME)
    val cues = listOf(
        "Start or resume your session.",
        "Find a drill. Build your plan.",
        "Choose a personal challenge.",
        "Review your recorded sessions.",
        "Your jersey and preferences."
    )
}
