package com.courtquestpro

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class PlayerSetupTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun playerSetupPreservesDraftAndCreatesNamedProfile() {
        node("welcome-get-started").performClick()
        node("onboarding-player-name").assertIsDisplayed().performTextReplacement("Jordan Reed")
        node("onboarding-next").performClick()
        compose.activityRule.scenario.recreate()
        node("onboarding-back").performClick()
        node("onboarding-player-name").assertIsDisplayed()
        repeat(4) { node("onboarding-next").performClick() }
        node("dock-home").assertIsDisplayed()
        val prefs = compose.activity.getSharedPreferences("court_quest_pro", 0)
        assertTrue(prefs.getBoolean("onboarding_complete", false))
        assertEquals("Jordan Reed", Gson().fromJson(prefs.getString("state", null), SaveState::class.java).playerName)
    }

    private fun node(tag: String) = compose.onNodeWithTag(tag, useUnmergedTree = true)
}
