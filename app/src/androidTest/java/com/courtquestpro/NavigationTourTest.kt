package com.courtquestpro

import android.os.Build
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test

class NavigationTourTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun everyGuideActionFitsAboveTheDockAndContinuesToTheCorrectTab() {
        assumeTrue("Run against a disposable Android emulator", Build.HARDWARE.contains("ranchu"))
        compose.onNodeWithTag("welcome-get-started").performClick()
        compose.waitUntil(timeoutMillis = 15_000) {
            compose.onAllNodesWithTag("player-bottom-dock").fetchSemanticsNodes().isNotEmpty()
        }

        if (compose.onAllNodesWithTag("tour-next").fetchSemanticsNodes().isEmpty()) {
            compose.onNodeWithTag("dock-me").performClick()
            compose.onNodeWithTag("me-settings-menu").performClick()
            compose.onNodeWithTag("me-start-walkthrough").performClick()
        }

        SoloNavigationTour.tabs.forEachIndexed { index, tab ->
            compose.onNodeWithTag("dock-${tab.route}").assertIsSelected()
            assertActionAboveDock("tour-next")
            assertActionAboveDock("tour-skip")
            if (index > 0) assertActionAboveDock("tour-back")
            compose.onNodeWithTag("tour-next").performClick()
        }
        compose.onNodeWithTag("navigation-tour").assertDoesNotExist()
    }

    private fun assertActionAboveDock(tag: String) {
        val action = compose.onNodeWithTag(tag).assertIsDisplayed().assertHasClickAction()
        val actionBounds = action.getUnclippedBoundsInRoot()
        val dockBounds = compose.onNodeWithTag("player-bottom-dock").getUnclippedBoundsInRoot()
        assertTrue("$tag is shorter than 48dp", actionBounds.bottom - actionBounds.top >= 48.dp)
        assertTrue("$tag is narrower than 48dp", actionBounds.right - actionBounds.left >= 48.dp)
        assertTrue("$tag overlaps the dock", actionBounds.bottom < dockBounds.top)
    }
}
