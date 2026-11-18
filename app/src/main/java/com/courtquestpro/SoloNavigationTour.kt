package com.courtquestpro

/** UI-only sequence. No player data or training state is changed by these hints. */
internal object SoloNavigationTour {
    val tabs = listOf(PlayerTab.COURT, PlayerTab.TRAIN, PlayerTab.QUESTS, PlayerTab.PROGRESS, PlayerTab.ME)
    val cues = listOf(
        "Start today’s session or pick up where you left off.",
        "Explore drills, learn techniques, and build your own practice.",
        "See which challenges you can complete through training.",
        "Review saved sessions and compare your results.",
        "Customize your jersey, manage settings, and see achievements."
    )
}
