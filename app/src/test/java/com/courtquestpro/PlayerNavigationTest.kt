package com.courtquestpro

import com.google.gson.Gson
import org.junit.Assert.*
import org.junit.Test

class PlayerNavigationTest {
    @Test fun rootsReturnToCourtWithoutCyclingThroughOtherTabs() {
        var history = listOf("home")
        for (route in listOf("library", "quests", "career", "me")) {
            history = PlayerNavigation.open(history, route)
            assertEquals(listOf("home", route), history)
            assertEquals(listOf("home"), PlayerNavigation.back(history))
        }
    }
    @Test fun builderAndProfileBackReturnToTheirOpener() {
        assertEquals(listOf("home", "library"), PlayerNavigation.back(PlayerNavigation.open(listOf("home", "library"), "missionBuilder")))
        assertEquals(listOf("home", "me"), PlayerNavigation.back(PlayerNavigation.open(listOf("home", "me"), "settings")))
        assertEquals(listOf("home", "career"), PlayerNavigation.back(PlayerNavigation.open(listOf("home", "career"), "missionReceipt")))
    }
    @Test fun completionReplacesRecorderAndNextStepDoesNotGrowHistory() {
        var history = listOf("home", "library", "missionBuilder")
        history = PlayerNavigation.open(history, "mission")
        repeat(12) {
            history = PlayerNavigation.open(history, "missionStepReceipt")
            assertEquals("missionBuilder", PlayerNavigation.back(history).last())
            history = PlayerNavigation.open(history, "mission")
            assertEquals(4, history.size)
        }
        history = PlayerNavigation.open(PlayerNavigation.open(history, "missionStepReceipt"), "missionReceipt")
        assertEquals(listOf("home", "library", "missionBuilder"), PlayerNavigation.back(history))
    }
    @Test fun tabSelectionAndSameDestinationAreIdempotent() {
        assertEquals(listOf("home", "library"), PlayerNavigation.open(listOf("home", "library"), "library"))
        assertEquals(listOf("home", "me", "settings"), PlayerNavigation.open(listOf("home", "me", "settings"), "settings"))
        assertEquals(listOf("home"), PlayerNavigation.back(listOf("home")))
    }
    @Test fun restorationRejectsUnknownRoutesAndKeepsKnownHistory() {
        val history = listOf("home", "me", "settings")
        val restored = Gson().fromJson(Gson().toJson(history), Array<String>::class.java).toList()
        assertEquals(history, PlayerNavigation.restore(restored))
        assertEquals(listOf("home"), PlayerNavigation.restore(listOf("home", "coachAdmin")))
        assertEquals(listOf("home"), PlayerNavigation.restore(emptyList()))
    }
    @Test fun editorVisitsNeverReuseRetainedScreenState() {
        assertTrue(PlayerNavigation.retainsState("library"))
        assertTrue(PlayerNavigation.retainsState("settings"))
        assertTrue(PlayerNavigation.retainsState("career"))
        assertFalse(PlayerNavigation.retainsState("drillEditor"))
        assertFalse(PlayerNavigation.retainsState("drill"))
        assertFalse(PlayerNavigation.retainsState("mission"))
    }
    @Test fun dailyAndPracticeReceiptsReturnToOriginWithoutReopeningRecorder() {
        for (recorder in listOf("drill", "practice")) {
            val receipt = PlayerNavigation.open(listOf("home", "library", recorder), "drillResult")
            assertEquals(listOf("home", "library"), PlayerNavigation.back(receipt))
        }
    }
    @Test fun allFiveDestinationsHaveDistinctOwningTabs() {
        assertEquals(listOf("Court", "Train", "Quests", "Progress", "Me"), PlayerTab.entries.map { it.label })
        assertEquals(PlayerTab.ME, PlayerNavigation.tab(listOf("home", "me", "settings")))
        assertEquals(PlayerTab.TRAIN, PlayerNavigation.tab(listOf("home", "library", "missionBuilder")))
        assertEquals(PlayerTab.PROGRESS, PlayerNavigation.tab(listOf("home", "career", "missionReceipt")))
    }
}
