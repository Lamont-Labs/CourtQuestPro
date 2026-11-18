package com.courtquestpro

import android.os.Build
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class JerseyStudioTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun customizationRestoresAcrossRecreationAndReturnsToCourt() {
        assertTrue("Use a disposable Android emulator", Build.HARDWARE.contains("ranchu"))
        if (has("welcome-get-started")) click("welcome-get-started")
        compose.waitUntil(timeoutMillis = 15_000) { has("dock-me") }
        if (has("tour-skip")) click("tour-skip")
        click("dock-me")
        if (has("tour-skip")) click("tour-skip")
        node("me-jersey-studio").performScrollTo().performClick()
        node("jersey-number").performScrollTo().performTextReplacement("00")
        node("jersey-name").performScrollTo().performTextReplacement("Jersey QA")
        click("segment-jersey-editor-style", scroll = true)
        click("segment-fastbreak", scroll = true)
        click("segment-jersey-editor-colors", scroll = true)
        click("jersey-swatch-royal-blue", scroll = true)
        node("jersey-save").assertIsEnabled().performClick()

        compose.activityRule.scenario.recreate()
        node("jersey-preview").performScrollTo()
            .assertContentDescriptionEquals("Back of Fastbreak jersey, number 00, Jersey QA")
        click("jersey-close", scroll = true)
        click("dock-home")
        node("court-player-jersey").assertIsDisplayed()
            .assertContentDescriptionEquals("Front of Fastbreak jersey, number 00")
        click("dock-me")
        node("me-jersey-studio").performScrollTo().performClick()
        node("jersey-preview").assertContentDescriptionEquals("Front of Fastbreak jersey, number 00")
    }

    private fun has(tag: String): Boolean =
        compose.onAllNodesWithTag(tag, useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()

    private fun node(tag: String) = compose.onNodeWithTag(tag, useUnmergedTree = true)

    private fun click(tag: String, scroll: Boolean = false) {
        val element = node(tag)
        if (scroll) element.performScrollTo()
        element.performClick()
    }
}
