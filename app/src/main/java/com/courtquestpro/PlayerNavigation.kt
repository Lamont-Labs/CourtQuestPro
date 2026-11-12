package com.courtquestpro

internal enum class PlayerTab(val route: String, val label: String) {
    COURT("home", "Court"), TRAIN("library", "Train"), QUESTS("quests", "Quests"),
    PROGRESS("career", "Progress"), ME("me", "Me")
}

/** Route history contains only UI identifiers. Training evidence stays in SaveState. */
internal object PlayerNavigation {
    private val roots = PlayerTab.entries.map { it.route }
    private val trainingFlow = setOf("drill", "practice", "drillResult", "mission", "missionStepReceipt", "missionReceipt")
    private val known = roots + trainingFlow + listOf("settings", "missionBuilder", "drillEditor", "champSetup", "score", "recap", "gameHistory")

    fun retainsState(route: String): Boolean = route in roots || route == "settings"

    fun restore(history: List<String>): List<String> =
        history.takeIf { it.isNotEmpty() && it.first() == "home" && it.all { route -> route in known } } ?: listOf("home")

    fun open(history: List<String>, route: String): List<String> {
        val current = restore(history)
        if (route !in known) return current
        if (route in roots) return if (route == "home") listOf("home") else listOf("home", route)
        if (current.last() == route) return current
        if (route == "gameHistory") return listOf("home", "career", route)
        // Results replace completed recorders. Continue replaces the result rather than stacking another recorder.
        if (route in trainingFlow && current.last() in trainingFlow) return current.dropLast(1) + route
        if (route == "score" && current.last() == "champSetup") return current.dropLast(1) + route
        if (route == "recap" && current.last() == "score") return current.dropLast(1) + route
        return current + route
    }

    fun back(history: List<String>): List<String> = restore(history).let { if (it.size > 1) it.dropLast(1) else it }

    fun tab(history: List<String>): PlayerTab =
        restore(history).asReversed().firstNotNullOfOrNull { route -> PlayerTab.entries.firstOrNull { it.route == route } } ?: PlayerTab.COURT

    fun backLabel(history: List<String>): String = when (back(history).last()) {
        "library" -> "Back to Train"
        "quests" -> "Back to Quests"
        "career", "gameHistory" -> "Back to Progress"
        "me" -> "Back to Me"
        "missionBuilder" -> "Back to Mission Builder"
        else -> "Back to Court"
    }
}
