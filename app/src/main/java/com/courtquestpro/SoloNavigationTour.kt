package com.courtquestpro

/** UI-only sequence. No player data or training state is changed by these hints. */
internal object SoloNavigationTour {
    val tabs = listOf(PlayerTab.COURT, PlayerTab.TRAIN, PlayerTab.TRAIN, PlayerTab.QUESTS, PlayerTab.PROGRESS, PlayerTab.ME, PlayerTab.ME)
    val cues = listOf(
        "Start or resume today’s training. Swap a drill or build a Mission for your own routine.",
        "Open Library to search by skill or equipment. View technique guides and favorite drills.",
        "Start a drill to record practice, or open Custom to create your own exercise.",
        "Complete daily and weekly challenges through saved training results. Resume a Mission or review earned rewards.",
        "Review saved attempts, compare results for the same drill and discover your next practice priority.",
        "Customize your jersey and player card in their studios.",
        "Edit your profile or restart this walkthrough from the Me menu."
    )
}
