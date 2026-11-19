package com.courtquestpro

import android.os.Build
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class TrainingEntryTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun randomDrillMatchesSearchAndDailyRecordingCanResumeAndSave() {
        assertTrue("Use a disposable Android emulator", Build.HARDWARE.contains("ranchu"))
        val guest = compose.onAllNodesWithTag("welcome-continue-guest", useUnmergedTree = true)
            .fetchSemanticsNodes().isNotEmpty()
        click(if (guest) "welcome-continue-guest" else "welcome-get-started")
        compose.waitUntil(timeoutMillis = 15_000) {
            compose.onAllNodesWithTag("tour-skip", useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()
        }
        click("tour-skip")
        click("dock-library")
        click("segment-library")
        node("train-library-content").assertIsDisplayed()
        captureDeviceFrame(compose.activity, "store-library")
        node("library-search").performScrollTo().performTextReplacement("Five-Spot Form Shooting")
        click("library-random-drill", scroll = true)
        compose.onNodeWithText("Five-Spot Form Shooting").assertIsDisplayed()

        click("dock-home")
        click("court-todays-focus", scroll = true)
        val active = state()
        val slot = active.slots.first { !it.complete }
        val drill = TrainingLibrary.catalog(active).single { it.id == slot.drillId }
        val required = requireNotNull(DrillTargets.minimum(drill))
        click("live-record-success", scroll = true)
        click("live-leave-session", scroll = true)
        assertEquals(1, state().drillDrafts.orEmpty().single { it.slotId == slot.id }.successful)
        click("court-todays-focus", scroll = true)
        repeat(required - 1) { click("live-record-success", scroll = true) }
        click("live-confirm-target", scroll = true)
        click("live-save-results", scroll = true)
        node("drill-saved-receipt").assertIsDisplayed()
        captureDeviceFrame(compose.activity, "store-receipt")
        assertEquals(required, state().drillResults.orEmpty().single { it.slotId == slot.id }.completed)
        androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().runOnMainSync {
            compose.activity.onBackPressedDispatcher.onBackPressed()
        }
        click("dock-career")
        node("progress-screen-title").assertIsDisplayed()
        node("progress-record-sessions").assert(hasAnyDescendant(hasText("1")))
        captureDeviceFrame(compose.activity, "store-progress")
        click("dock-quests")
        node("quests-screen-title").assertIsDisplayed()
        captureDeviceFrame(compose.activity, "store-quests")
    }

    private fun state(): SaveState {
        compose.waitForIdle()
        val prefs = compose.activity.getSharedPreferences("court_quest_pro", 0)
        return Gson().fromJson(prefs.getString("state", null), SaveState::class.java)
    }

    private fun node(tag: String) = compose.onNodeWithTag(tag, useUnmergedTree = true)

    private fun click(tag: String, scroll: Boolean = false) {
        val element = node(tag)
        if (scroll) element.performScrollTo()
        element.performClick()
    }
}
