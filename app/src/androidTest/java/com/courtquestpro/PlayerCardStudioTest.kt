package com.courtquestpro

import android.graphics.Bitmap
import android.os.Build
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.google.gson.Gson
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.io.File

class PlayerCardStudioTest {
    @get:Rule val compose=createAndroidComposeRule<MainActivity>()
    private fun node(tag:String)=compose.onNodeWithTag(tag,useUnmergedTree=true)
    private fun has(tag:String)=compose.onAllNodesWithTag(tag,useUnmergedTree=true).fetchSemanticsNodes().isNotEmpty()
    private fun wait(tag:String)=compose.waitUntil(20_000){has(tag)}
    private fun tap(tag:String) {node(tag).performClick()}
    private fun choose(tag:String) {node(tag).performScrollTo().performClick()}
    private fun enter() {
        assertTrue("Only the disposable emulator",Build.HARDWARE.contains("ranchu"))
        compose.waitUntil(20_000){has("welcome-get-started")||has("dock-me")}
        if(has("welcome-get-started")) tap("welcome-get-started")
        wait("dock-me");if(has("tour-skip"))tap("tour-skip")
        tap("dock-me");if(has("tour-skip"))tap("tour-skip")
        wait("me-player-card")
    }
    private fun stored()=Gson().fromJson(compose.activity.getSharedPreferences("court_quest_pro",0).getString("state",null),SaveState::class.java)
    private fun capture(name:String) {
        compose.waitForIdle()
        val bitmap=checkNotNull(InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot())
        try {
            val out=File(compose.activity.filesDir,"player-card-proof").apply{mkdirs()}
            File(out,"$name.png").outputStream().use{assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG,100,it))}
        } finally {bitmap.recycle()}
    }
    private fun ready(pose:PlayerCardPose) {
        wait("card-athlete-${pose.name.lowercase()}-ready")
        compose.waitForIdle()
        node("card-artwork-error").assertDoesNotExist()
    }
    @Test fun allEditionsPosesAndFacesAreRealAndSaveAsProfile() {
        enter();ready(PlayerCardSelection.identity(compose.activity).pose)
        capture("me-before-edit")
        // The old separate jersey/profile hero must not coexist with the selected card.
        node("me-jersey-hero").assertDoesNotExist()
        val before=stored()
        choose("me-player-card-studio");wait("player-card-studio-screen")
        for(style in PlayerCardStyle.entries) {
            tap("segment-card-edition");choose("player-card-style-${style.name.lowercase()}")
            for(pose in PlayerCardPose.entries) {
                tap("segment-card-pose");choose("player-card-pose-${pose.name.lowercase()}");ready(pose)
                for(face in PlayerCardFace.entries) {
                    tap("segment-card-face");choose("player-card-face-${face.name.lowercase()}");ready(pose)
                    node("player-card-preview").assertContentDescriptionContains(style.label,substring=true)
                    capture("${style.name.lowercase()}-${pose.name.lowercase()}-${face.name.lowercase()}")
                }
            }
        }
        val save=node("player-card-save")
        save.assertIsDisplayed().assertIsEnabled()
        val bounds=save.fetchSemanticsNode().boundsInRoot
        assertTrue("48dp save touch target",bounds.height>=48*compose.activity.resources.displayMetrics.density)
        save.performClick();wait("me-player-card-studio")
        assertEquals(PlayerCardIdentity(PlayerCardStyle.BATTLE,PlayerCardPose.ACTION,PlayerCardFace.QUESTION),
            PlayerCardSelection.identity(compose.activity))
        ready(PlayerCardPose.ACTION);capture("me-selected-card")
        val after=stored()
        assertEquals(Gson().toJson(before),Gson().toJson(after))
        compose.activityRule.scenario.recreate()
        assertEquals(PlayerCardIdentity(PlayerCardStyle.BATTLE,PlayerCardPose.ACTION,PlayerCardFace.QUESTION),
            PlayerCardSelection.identity(compose.activity))
        File(compose.activity.filesDir,"player-card-proof/verified.json").writeText(
            """{"editions":3,"poses":3,"faces":3,"combinations":27,"profile_replaced":true,"training_state_unchanged":true,"preferences_persisted":true}""")
    }
    @Test fun coldStartUsesSavedCardAndJerseyEditsSynchronizeWithoutCardResave() {
        enter()
        assertEquals(PlayerCardIdentity(PlayerCardStyle.BATTLE,PlayerCardPose.ACTION,PlayerCardFace.QUESTION),
            PlayerCardSelection.identity(compose.activity))
        ready(PlayerCardPose.ACTION)
        choose("me-jersey-studio");wait("jersey-studio")
        tap("segment-jersey-editor-identity")
        node("jersey-number").performScrollTo().performTextReplacement("08")
        node("jersey-name").performScrollTo().performTextReplacement("RIVER")
        node("jersey-close").performClick()
        node("jersey-save-exit").performClick();wait("me-player-card-studio");ready(PlayerCardPose.ACTION)
        node("player-card-preview").assertContentDescriptionContains("Jersey number 08",substring=true)
        node("player-card-preview").assertContentDescriptionContains("RIVER",substring=true)
        assertEquals("08",stored().playerJersey.number)
        capture("me-live-jersey-updated")
        // Unsaved card drafts may restore during recreation, but leaving with Discard never changes disk.
        choose("me-player-card-studio");wait("player-card-studio-screen")
        tap("segment-card-pose");choose("player-card-pose-standing")
        compose.activityRule.scenario.recreate();wait("player-card-studio-screen");ready(PlayerCardPose.STANDING)
        node("player-card-close").performClick();node("player-card-discard").performClick()
        wait("me-player-card-studio")
        assertEquals(PlayerCardPose.ACTION,PlayerCardSelection.identity(compose.activity).pose)
        ready(PlayerCardPose.ACTION)
        node("me-jersey-hero").assertDoesNotExist()
    }
}
