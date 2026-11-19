package com.courtquestpro

import org.junit.Assert.*
import org.junit.Test

/** Contract checks for the offline, profile-scoped Settings destination registry. */
class MeSettingsPageTest {
    @Test fun settingsDestinationsHaveStableUniqueKeys() {
        val pages = MeSettingsPage.entries
        assertEquals(8, pages.size)
        assertEquals(pages.size, pages.map { it.name }.distinct().size)
        assertEquals(MeSettingsPage.HOME, pages.first())
    }

    @Test fun everySettingsDestinationHasAccessibleCopy() {
        MeSettingsPage.entries.forEach { page ->
            assertTrue(page.name, page.title.isNotBlank())
            assertTrue(page.name, page.subtitle.isNotBlank())
        }
    }

    @Test fun allRequestedInformationDestinationsExist() {
        assertTrue(MeSettingsPage.entries.containsAll(listOf(
            MeSettingsPage.PLAYER, MeSettingsPage.PREFERENCES,
            MeSettingsPage.HELP, MeSettingsPage.HOW_TO,
            MeSettingsPage.ABOUT, MeSettingsPage.NOTICES, MeSettingsPage.PRIVACY
        )))
    }

    @Test fun replayTourRemainsASeparateExistingNavigationFlow() {
        assertEquals(5, SoloNavigationTour.tabs.size)
        assertEquals(SoloNavigationTour.tabs.size, SoloNavigationTour.cues.size)
        assertEquals(PlayerTab.COURT, SoloNavigationTour.tabs.first())
        assertEquals(PlayerTab.ME, SoloNavigationTour.tabs.last())
    }
}
