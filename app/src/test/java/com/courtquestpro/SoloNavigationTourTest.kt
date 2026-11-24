package com.courtquestpro

import org.junit.Assert.*
import org.junit.Test

class SoloNavigationTourTest {
    @Test fun introductionStartsAtCourtAndCoversExactlyTheFivePlayerTabs() {
        assertEquals(PlayerTab.COURT, SoloNavigationTour.tabs.first())
        assertEquals(PlayerTab.entries.toSet(), SoloNavigationTour.tabs.toSet())
        assertEquals(7, SoloNavigationTour.tabs.size)
        assertEquals(SoloNavigationTour.tabs.size, SoloNavigationTour.cues.size)
        assertEquals(5, SoloNavigationTour.tabs.distinct().size)
        assertEquals(PlayerTab.ME, SoloNavigationTour.tabs.last())
    }

    @Test fun headingsDistinguishEveryInstructionalStep() {
        assertEquals(SoloNavigationTour.tabs.size, SoloNavigationTour.headings.size)
        assertEquals(SoloNavigationTour.headings.size, SoloNavigationTour.headings.distinct().size)
        SoloNavigationTour.headings.forEachIndexed { index, heading ->
            assertTrue(heading.startsWith(SoloNavigationTour.tabs[index].label))
        }
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
