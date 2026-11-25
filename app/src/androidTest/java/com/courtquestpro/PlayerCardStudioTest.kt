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
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.security.MessageDigest
import androidx.compose.ui.graphics.asAndroidBitmap

class PlayerCardStudioTest {
    @get:Rule val compose=createAndroidComposeRule<MainActivity>()
    private fun node(tag:String)=compose.onNodeWithTag(tag,useUnmergedTree=true)
    private fun has(tag:String)=compose.onAllNodesWithTag(tag,useUnmergedTree=true).fetchSemanticsNodes().isNotEmpty()
    private fun wait(tag:String) {
        try { compose.waitUntil(20_000){has(tag)} }
        catch (timeout:androidx.compose.ui.test.ComposeTimeoutException) {
            val tags=listOf("me-player-card-studio","player-card-studio-screen","dock-me","player-card-saving",
                "player-card-save-error","player-card-save-state-idle","player-card-save-state-saving",
                "player-card-save-state-saved","player-card-save-state-back-requested",
                "player-card-save-state-failed","navigation-route-me","navigation-route-playerCardStudio",
                "navigation-route-jerseyStudio","navigation-route-home","welcome-enter-ball")
                .filter { runCatching{has(it)}.getOrDefault(false) }
            val dir=File(compose.activity.filesDir,"player-card-proof").apply{mkdirs()}
            File(dir,"wait-timeout-$tag.txt").writeText("expected=$tag\nvisibleTags=$tags")
            runCatching {
                val image=InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
                File(dir,"wait-timeout-$tag.png").outputStream().use{image.compress(Bitmap.CompressFormat.PNG,100,it)}
                image.recycle()
            }
            throw AssertionError("Timed out waiting for $tag; visible tags: $tags",timeout)
        }
    }
    private fun studioTitleDisplayed() = listOf("Card & Uniform Studio","Studio").any { title ->
        try { compose.onNodeWithText(title,useUnmergedTree=true).assertIsDisplayed(); true }
        catch (_:AssertionError) { false }
    }
    private fun waitStudio() {
        compose.waitUntil(20_000) {
            // Do not accept the outgoing Me composition during AnimatedContent.
            if(!has("player-card-studio-screen") || has("me-player-card-studio") || has("dock-me")) false else try {
                node("player-card-studio-screen").assertIsDisplayed()
                studioTitleDisplayed()
            } catch (_:AssertionError) { false }
        }
        node("player-card-studio-screen").assertIsDisplayed()
        assertTrue("Card Studio title is visible",studioTitleDisplayed())
        assertFalse("Me profile is still visible",has("me-player-card-studio"))
        assertFalse("Player navigation dock is still visible",has("dock-me"))
    }
    private fun tap(tag:String) {
        compose.waitForIdle()
        node(tag).assertIsDisplayed().performClick()
        compose.waitForIdle()
    }
    private fun choose(tag:String) {
        compose.waitForIdle()
        val rail="${tag.substringBeforeLast('-')}-rail"
        val target=node(tag)
        if(has(rail)) {
            node(rail).performScrollTo().assertIsDisplayed()
            compose.waitForIdle()
            node(rail).performScrollToNode(hasTestTag(tag))
            compose.waitForIdle()
            target.assertIsDisplayed()
        } else {
            try {
                target.performScrollTo()
                compose.waitForIdle()
            } catch (error:AssertionError) {
                if(error.message?.contains("no parent layout with a Scroll SemanticsAction")!=true) throw error
                target.assertIsDisplayed()
            }
            target.assertIsDisplayed()
        }
        target.performClick()
        compose.waitForIdle()
    }
    private fun enter() {
        assertTrue("Only the disposable emulator",Build.HARDWARE.contains("ranchu"))
        compose.waitUntil(20_000){has("welcome-enter-ball")||has("dock-me")}
        if(has("welcome-enter-ball")) tap("welcome-enter-ball")
        wait("dock-me");if(has("tour-skip"))tap("tour-skip")
        tap("dock-me");if(has("tour-skip"))tap("tour-skip")
        wait("me-player-card")
    }
    private fun stored()=Gson().fromJson(compose.activity.getSharedPreferences("court_quest_pro",0).getString("state",null),SaveState::class.java)
    private fun capture(name:String) {
        compose.waitForIdle()
        val presented=CountDownLatch(1)
        compose.runOnUiThread {
            val root=compose.activity.window.decorView
            if(Build.VERSION.SDK_INT>=29 && root.isHardwareAccelerated) {
                root.viewTreeObserver.registerFrameCommitCallback { presented.countDown() }
                root.invalidate()
            } else root.postOnAnimation { root.postOnAnimation { presented.countDown() } }
        }
        assertTrue("Android must present the selected card before capture",presented.await(5,TimeUnit.SECONDS))
        val bitmap=checkNotNull(InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot())
        try {
            val out=File(compose.activity.filesDir,"player-card-proof").apply{mkdirs()}
            File(out,"$name.png").outputStream().use{assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG,100,it))}
        } finally {bitmap.recycle()}
    }
    private fun ready() {
        node("player-card-preview").assertIsDisplayed()
        compose.waitUntil(20_000) {
            val pose=PlayerCardSelection.identity(compose.activity).pose.name.lowercase()
            PlayerCardStyle.entries.any {has("card-surface-${it.name.lowercase()}-ready")}
        }
        compose.waitForIdle()
    }
    @Test fun allEditionsUseSavedJerseyAndFlipToPersistedPlayerRecord() {
        enter();ready()
        capture("me-before-edit")
        node("me-jersey-hero").assertDoesNotExist()
        val before=stored()
        val beforeIdentity=PlayerCardSelection.identity(compose.activity)
        choose("me-player-card-studio");waitStudio()
        for(style in PlayerCardStyle.entries) {
            choose("player-card-style-${style.name.lowercase()}")
            ready()
            node("player-card-preview").assertContentDescriptionContains(style.label,substring=true)
            node("player-card-preview").assertContentDescriptionContains("jersey collectible card",substring=true)
            node("player-card-preview").assertContentDescriptionContains(stored().playerJersey.design.template.label,substring=true)
            capture("edition-${style.name.lowercase()}")
        }
        node("player-card-avatar").assertDoesNotExist()
        node("segment-card-pose").assertDoesNotExist()
        node("segment-card-face").assertDoesNotExist()
        val record=PlayerCardRecord.from(stored())
        node("player-card-preview").performClick()
        node("player-card-back").assertExists()
        node("player-card-preview").assertContentDescriptionContains(record.description,substring=true)
        capture("player-record-back")
        choose("player-card-style-battle");ready()
        val save=node("player-card-save")
        save.assertIsDisplayed().assertIsEnabled()
        val bounds=save.fetchSemanticsNode().boundsInRoot
        assertTrue("48dp save touch target",bounds.height>=48*compose.activity.resources.displayMetrics.density)
        save.performClick();wait("me-player-card-studio")
        assertEquals(beforeIdentity.copy(style=PlayerCardStyle.BATTLE),PlayerCardSelection.identity(compose.activity))
        ready();capture("me-selected-card")
        assertEquals(Gson().toJson(before),Gson().toJson(stored()))
        compose.activityRule.scenario.recreate()
        assertEquals(beforeIdentity.copy(style=PlayerCardStyle.BATTLE),PlayerCardSelection.identity(compose.activity))
        ready();capture("me-selected-card-cold-start")
        File(compose.activity.filesDir,"player-card-proof/verified.json").writeText(Gson().toJson(mapOf(
            "editions" to PlayerCardStyle.entries.map{it.name},"live_jersey" to true,
            "flip_record" to record.description,"avatar_editor_hidden" to true,
            "training_state_unchanged" to true,"preferences_persisted" to true)))
    }
    @Test fun coldStartUsesSavedCardAndJerseyEditsSynchronizeWithoutCardResave() {
        enter()
        val persisted=PlayerCardSelection.identity(compose.activity)
        assertEquals(PlayerCardStyle.BATTLE,persisted.style)
        assertEquals(PlayerCardPose.ACTION,persisted.pose)
        assertEquals(PlayerCardFace.QUESTION,persisted.face)
        ready()
        val expectedNumber=if(stored().playerJersey.number=="08") "09" else "08"
        val expectedName=if(stored().playerName=="RIVER") "BLAKE" else "RIVER"
        choose("me-jersey-studio");wait("jersey-studio")
        tap("segment-jersey-editor-identity")
        node("jersey-number").performScrollTo().performTextReplacement(expectedNumber)
        node("jersey-name").performScrollTo().performTextReplacement(expectedName)
        node("jersey-save").assertIsEnabled()
        node("jersey-close").performClick()
        wait("jersey-save-exit");node("jersey-save-exit").assertIsEnabled().performClick()
        wait("me-player-card-studio");ready()
        node("player-card-preview").assertContentDescriptionContains("Jersey number $expectedNumber",substring=true)
        node("player-card-preview").assertContentDescriptionContains(expectedName,substring=true)
        assertEquals(expectedNumber,stored().playerJersey.number)
        capture("me-live-jersey-updated")
        // Unsaved edition drafts may restore during recreation, but Discard never changes saved identity.
        choose("me-player-card-studio");waitStudio()
        choose("player-card-style-classic")
        compose.activityRule.scenario.recreate();waitStudio();ready()
        node("player-card-close").performClick();node("player-card-discard").performClick()
        wait("me-player-card-studio")
        assertEquals(persisted,PlayerCardSelection.identity(compose.activity))
        ready()
        node("me-jersey-hero").assertDoesNotExist()
    }

    /** Hash the rendered collectible itself so jersey-template differences are not UI-label changes. */
    private fun renderedSignature():String {
        compose.waitForIdle()
        val bitmap=node("player-card-preview").captureToImage().asAndroidBitmap()
        val pixels=IntArray(bitmap.width*bitmap.height)
        bitmap.getPixels(pixels,0,bitmap.width,0,0,bitmap.width,bitmap.height)
        val digest=MessageDigest.getInstance("SHA-256")
        digest.update(java.nio.ByteBuffer.allocate(pixels.size*4).apply{asIntBuffer().put(pixels)}.array())
        bitmap.recycle()
        return digest.digest().joinToString(""){"%02x".format(it)}
    }

    @Test fun activeStudioHidesAvatarControlsAndPreservesLegacyAppearancePreferences() {
        enter();ready()
        val before=Gson().toJson(stored())
        val beforeIdentity=PlayerCardSelection.identity(compose.activity)
        choose("me-player-card-studio");waitStudio()
        node("player-card-avatar").assertDoesNotExist()
        node("segment-card-pose").assertDoesNotExist()
        node("segment-card-face").assertDoesNotExist()
        node("segment-avatar-hair").assertDoesNotExist()
        node("avatar-hair-topknot").assertDoesNotExist()
        val selectedStyle=PlayerCardStyle.entries.first{it!=beforeIdentity.style}
        choose("player-card-style-${selectedStyle.name.lowercase()}")
        ready()
        tap("player-card-save");wait("me-player-card-studio");ready()
        assertEquals(beforeIdentity.copy(style=selectedStyle),PlayerCardSelection.identity(compose.activity))
        assertEquals(before,Gson().toJson(stored()))
        capture("legacy-avatar-preferences-preserved")
        compose.activityRule.scenario.recreate()
        assertEquals(beforeIdentity.copy(style=selectedStyle),PlayerCardSelection.identity(compose.activity))
        ready()
        assertEquals(before,Gson().toJson(stored()))
        File(compose.activity.filesDir,"player-card-proof/legacy-appearance-preserved.json").writeText(
            Gson().toJson(mapOf("active_avatar_editor" to false,"legacy_identity" to beforeIdentity,
                "selected_edition" to selectedStyle.name,"training_state_unchanged" to true)))
    }

    private val legacyIdentity get()=PlayerCardIdentity(PlayerCardStyle.CLASSIC,PlayerCardPose.DRIBBLING,
        PlayerCardFace.BLANK)

    @Test fun legacyV3CosmeticsUpgradeWithoutChangingTraining() {
        enter();ready()
        val before=Gson().toJson(stored())
        assertFalse(compose.activity.getSharedPreferences("courtquest_player_card",0).contains("avatar_footwear_v3"))
        assertEquals(legacyIdentity,PlayerCardSelection.identity(compose.activity))
        var persisted=false
        compose.runOnUiThread { persisted=PlayerCardSelection.write(compose.activity,legacyIdentity) }
        assertTrue(persisted)
        assertEquals(before,Gson().toJson(stored()))
        compose.activityRule.scenario.recreate()
        assertEquals(legacyIdentity,PlayerCardSelection.identity(compose.activity))
        enter();ready()
        assertEquals(before,Gson().toJson(stored()))
        capture("legacy-v3-preserved")
    }
    @Test fun legacyV3CosmeticsSurviveColdStart() {
        enter();ready()
        assertEquals(legacyIdentity,PlayerCardSelection.identity(compose.activity))
        node("player-card-preview").assertContentDescriptionContains(
            "Jersey number ${stored().playerJersey.number}",substring=true)
    }
    @Test fun savedUniformTemplatesUpdateCollectibleArtworkWithoutChangingTraining() {
        enter();ready()
        val before=Gson().toJson(stored().copy(jerseyDesign=null))
        val identity=PlayerCardSelection.identity(compose.activity)
        val hashes=linkedMapOf<String,String>()
        for(template in JerseyTemplate.entries) {
            choose("me-jersey-studio");wait("jersey-studio")
            tap("segment-jersey-editor-style");choose("segment-${template.name.lowercase()}")
            tap("jersey-close");wait("jersey-save-exit");tap("jersey-save-exit")
            wait("me-player-card-studio")
            assertEquals(template,stored().playerJersey.design.template)
            ready()
            node("player-card-preview").assertContentDescriptionContains("${template.label} saved uniform",substring=true)
            hashes[template.name]=renderedSignature()
            capture("uniform-${template.name.lowercase()}")
            assertEquals(identity,PlayerCardSelection.identity(compose.activity))
        }
        assertEquals("Each saved template changes the collectible artwork",JerseyTemplate.entries.size,hashes.values.toSet().size)
        assertEquals(before,Gson().toJson(stored().copy(jerseyDesign=null)))
        File(compose.activity.filesDir,"player-card-proof/uniform-matrix.json").writeText(Gson().toJson(hashes))
    }

    @Test fun cardStudioOpensExistingJerseyStudioAndReturnsWithoutDataLoss() {
        enter();ready()
        val before=Gson().toJson(stored())
        val identity=PlayerCardSelection.identity(compose.activity)
        val jersey=stored().playerJersey
        choose("me-player-card-studio");waitStudio()
        tap("segment-player-card-tab-uniform");wait("jersey-studio")
        assertEquals(jersey,stored().playerJersey)
        compose.activity.onBackPressedDispatcher.onBackPressed()
        waitStudio();ready()
        assertEquals(identity,PlayerCardSelection.identity(compose.activity))
        assertEquals(before,Gson().toJson(stored()))
        node("player-card-preview").assertContentDescriptionContains(
            "${jersey.design.template.label} saved uniform",substring=true)
    }
}
