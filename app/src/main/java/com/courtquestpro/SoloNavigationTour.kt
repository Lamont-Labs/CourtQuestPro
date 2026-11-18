package com.courtquestpro

/** UI-only sequence. No player data or training state is changed by these hints. */
internal object SoloNavigationTour {
    val tabs = listOf(PlayerTab.COURT, PlayerTab.TRAIN, PlayerTab.QUESTS, PlayerTab.PROGRESS, PlayerTab.ME)
    val cues = listOf(
        "Your training headquarters. Start today’s session or pick up where you left off.",
        "Explore basketball drills, learn techniques, and build your own practice.",
        "Turn your training into challenges, milestones, and earned rewards.",
        "Track your improvement and review the sessions you have saved.",
        "Customize your player identity, manage settings, and review achievements."
    )
}
