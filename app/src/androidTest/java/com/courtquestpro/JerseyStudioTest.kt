package com.courtquestpro

import android.graphics.Bitmap
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
import androidx.test.platform.app.InstrumentationRegistry
import com.google.gson.Gson
import org.junit.Assert.assertEquals
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
        click("segment-arena", scroll = true)
        click("segment-jersey-editor-colors", scroll = true)
        click("jersey-swatch-royal-blue", scroll = true)
        click("jersey-colors-done", scroll = true)
        node("jersey-save").assertIsEnabled().performClick()
        assertSavedJersey()

        compose.activityRule.scenario.recreate()
        node("jersey-preview").performScrollTo()
            .assertContentDescriptionEquals("Back of Arena jersey, number 00, Jersey QA")
        capture("jersey-studio-back")
        click("jersey-close", scroll = true)
        // Capture the real 58dp dock, not just the large Studio preview.
        // Regression evidence for number placement and icon scale on device.
        compose.waitUntil(timeoutMillis = 15_000) { !has("jersey-preview") && has("dock-me") }
        node("dock-me").assertIsDisplayed()
        click("dock-home")
        node("court-player-jersey").assertIsDisplayed()
            .assertContentDescriptionEquals("Front of Arena jersey, number 00")
        // Capture after an independently verified Court navigation frame.
        // The prior screenshot was taken during the Studio-to-dock transition.
        capture("jersey-dock-compact")
        capture("jersey-court")
        click("dock-me")
        node("me-jersey-studio").performScrollTo().performClick()
        compose.waitUntil(timeoutMillis = 15_000) { has("jersey-preview") }
        node("jersey-preview").assertContentDescriptionEquals("Front of Arena jersey, number 00")
        assertSavedJersey()
        // The capture must be taken after the editor is actually displayed.
        // Force a Studio navigation frame before capturing the approved Arena baseline.
        click("segment-jersey-editor-style", scroll = true)
        click("segment-arena", scroll = true)
        node("jersey-preview").assertIsDisplayed()
        capture("jersey-style-arena-front")
        capture("jersey-studio-front")
        // Visual regression evidence for every garment, not only the Arena renderer.
        // A green Arena screenshot cannot validate the separate Canvas styles.
        click("segment-jersey-editor-style", scroll = true)
        for (template in listOf(JerseyTemplate.CLASSIC, JerseyTemplate.FASTBREAK, JerseyTemplate.RETRO)) {
            click("segment-${template.name.lowercase()}", scroll = true)
            node("jersey-preview").assertContentDescriptionEquals(
                "Front of ${template.label} jersey, number 00"
            )
            capture("jersey-style-${template.name.lowercase()}-front")
        }
    }

    @Test fun coldStartRestoresPreviouslySavedJersey() {
        assertTrue("Use a disposable Android emulator", Build.HARDWARE.contains("ranchu"))
        if (has("welcome-get-started")) click("welcome-get-started")
        compose.waitUntil(timeoutMillis = 15_000) { has("dock-me") }
        assertSavedJersey()
        node("court-player-jersey").assertContentDescriptionEquals("Front of Arena jersey, number 00")
    }

    private fun assertSavedJersey() {
        val prefs = compose.activity.getSharedPreferences("court_quest_pro", 0)
        compose.runOnIdle {
            val save = Gson().fromJson(prefs.getString("state", null), SaveState::class.java)
            assertEquals("Jersey QA", save.playerName)
            assertEquals("00", save.playerJersey.number)
            assertEquals(JerseyTemplate.ARENA, save.playerJersey.design.template)
            assertEquals("#2259D1", jerseyHex(save.playerJersey.design.fabric))
        }
    }

    private fun capture(name: String) {
        compose.waitForIdle()
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.waitForIdleSync()
        val image = instrumentation.uiAutomation.takeScreenshot()
        checkNotNull(image).usePng(name + ".png")
    }

    private fun Bitmap.usePng(name: String) {
        compose.activity.openFileOutput(name, 0).use { output -> compress(Bitmap.CompressFormat.PNG, 100, output) }
        recycle()
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
