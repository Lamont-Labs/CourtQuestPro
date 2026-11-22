package com.courtquestpro

import android.os.Build
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class NavigationTourTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun everyGuideActionFitsAboveTheDockAndContinuesToTheCorrectTab() {
        assertTrue("Run against a disposable Android emulator", Build.HARDWARE.contains("ranchu"))
        click("welcome-swipe-ball")
        compose.waitUntil(timeoutMillis = 15_000) {
            compose.onAllNodesWithTag("dock-home", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        if (compose.onAllNodesWithTag("tour-next", useUnmergedTree = true)
                .fetchSemanticsNodes().isEmpty()) {
            click("dock-me")
            click("me-settings-menu")
            click("me-start-walkthrough")
        }

        SoloNavigationTour.tabs.forEachIndexed { index, tab ->
            node("dock-${tab.route}").assertIsSelected()
            assertActionAboveDock("tour-next")
            assertActionAboveDock("tour-skip")
            if (index > 0) assertActionAboveDock("tour-back")
            click("tour-next")
        }
        node("navigation-tour").assertDoesNotExist()
    }

    private fun node(tag: String) = compose.onNodeWithTag(tag, useUnmergedTree = true)

    private fun click(tag: String) {
        node(tag).assertHasClickAction().performSemanticsAction(SemanticsActions.OnClick) { it() }
    }

    private fun assertActionAboveDock(tag: String) {
        val action = node(tag).assertIsDisplayed().assertHasClickAction()
        val actionBounds = action.getUnclippedBoundsInRoot()
        val dockBounds = node("player-bottom-dock").getUnclippedBoundsInRoot()
        assertTrue("$tag is shorter than 48dp", actionBounds.bottom - actionBounds.top >= 48.dp)
        assertTrue("$tag is narrower than 48dp", actionBounds.right - actionBounds.left >= 48.dp)
        assertTrue("$tag overlaps the dock", actionBounds.bottom < dockBounds.top)
    }
}
