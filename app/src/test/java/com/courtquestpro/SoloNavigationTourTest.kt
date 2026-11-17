package com.courtquestpro

import org.junit.Assert.*
import org.junit.Test

class SoloNavigationTourTest {
    @Test fun introductionStartsAtCourtAndCoversExactlyTheFivePlayerTabs() {
        assertEquals(PlayerTab.COURT, SoloNavigationTour.tabs.first())
        assertEquals(PlayerTab.entries.toSet(), SoloNavigationTour.tabs.toSet())
        assertEquals(5, SoloNavigationTour.tabs.size)
        assertEquals(5, SoloNavigationTour.cues.size)
        assertEquals(5, SoloNavigationTour.tabs.distinct().size)
        assertEquals(PlayerTab.ME, SoloNavigationTour.tabs.last())
    }

    @Test fun everyHintHasAPersonalActionAndPreservesNavigationState() {
        val initial = listOf("home", "library", "missionBuilder")
        SoloNavigationTour.tabs.forEachIndexed { index, tab ->
            assertTrue(SoloNavigationTour.cues[index].isNotBlank())
            assertFalse(SoloNavigationTour.cues[index].contains("team", ignoreCase = true))
            assertEquals(tab, PlayerNavigation.tab(PlayerNavigation.open(initial, tab.route)))
        }
        assertEquals(listOf("home", "library", "missionBuilder"), initial)
    }
}
