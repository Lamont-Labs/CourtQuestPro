package com.courtquestpro

import android.graphics.Bitmap
import android.os.Build
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.google.gson.Gson
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.security.MessageDigest
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/** Runs through the shipping Me route and editor. Output files contain real Android pixels. */
class CardStudioRebuildTest {
    @get:Rule val ui=createAndroidComposeRule<MainActivity>()
    private val out get()=File(ui.activity.filesDir,"player-card-proof").apply{mkdirs()}
    private fun node(tag:String)=ui.onNodeWithTag(tag,useUnmergedTree=true)
    private fun has(tag:String)=ui.onAllNodesWithTag(tag,useUnmergedTree=true).fetchSemanticsNodes().isNotEmpty()
    private fun wait(tag:String)=ui.waitUntil(20_000){has(tag)}
    private fun studioTitleDisplayed() = listOf("Player Card Studio","Card Studio","Studio").any { title ->
        try { ui.onNodeWithText(title,useUnmergedTree=true).assertIsDisplayed(); true }
        catch (_:AssertionError) { false }
    }
    private fun waitStudio() {
        ui.waitUntil(20_000) {
            // AnimatedContent exposes both routes during its transition.
            // Wait until the outgoing Me route and its dock are actually gone
            // so pixel captures cannot accidentally show the previous screen.
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
    private fun click(tag:String) {
        ui.waitForIdle()
        node(tag).assertIsDisplayed().performClick()
        ui.waitForIdle()
    }
    private fun pick(tag:String) {
        ui.waitForIdle()
        // Choices live in horizontal LazyRows, so an off-screen option has no
        // semantics node yet. First expose its vertically stacked rail, then
        // scroll that rail horizontally to compose the requested option.
        val rail="${tag.substringBeforeLast('-')}-rail"
        if(has(rail)) {
            node(rail).performScrollTo().assertIsDisplayed();ui.waitForIdle()
            node(rail).performScrollToNode(hasTestTag(tag))
            ui.waitForIdle()
            node(tag).assertIsDisplayed().performClick()
        } else {
            node(tag).performScrollTo().assertIsDisplayed();ui.waitForIdle()
            node(tag).performClick()
        }
        ui.waitForIdle()
    }
    private fun prefs()=ui.activity.getSharedPreferences("court_quest_pro",0).getString("state",null)!!
    private fun ready() {
        val artworkTags=PlayerCardStyle.entries.map{"card-surface-${it.name.lowercase()}-ready"}
        try {
            ui.waitUntil(20_000) {
                PlayerCardStyle.entries.any {has("card-surface-${it.name.lowercase()}-ready")}
            }
        } catch (timeout:androidx.compose.ui.test.ComposeTimeoutException) {
            val state=listOf("player-card-studio-screen","me-player-card-studio","dock-me",
                "player-card-preview").plus(artworkTags).joinToString { "$it=${has(it)}" }
            runCatching {
                File(out,"artwork-readiness-timeout.txt").writeText(
                    "expected=rendered-card-face; $state")
                val bitmap=InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
                if(bitmap!=null)try { write("artwork-readiness-timeout",bitmap) } finally { bitmap.recycle() }
            }
            throw AssertionError("Timed out waiting for the rendered athlete and card face; $state",timeout)
        }
        ui.waitForIdle()
    }
    private fun studio() {
        assertTrue(Build.HARDWARE.contains("ranchu"))
        ui.waitUntil(20_000){has("welcome-swipe-ball")||has("dock-me")}
        if(has("welcome-swipe-ball"))click("welcome-swipe-ball")
        wait("dock-me")
        if(has("tour-skip"))click("tour-skip")
        click("dock-me")
        if(has("tour-skip"))click("tour-skip")
        wait("me-player-card-studio");pick("me-player-card-studio");waitStudio()
    }
    private fun write(name:String,bitmap:Bitmap):String {
        val file=File(out,"$name.png")
        file.outputStream().use { assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG,100,it)) }
        return MessageDigest.getInstance("SHA-256").digest(file.readBytes()).joinToString(""){"%02x".format(it)}
    }
    private fun screen(name:String) {
        ui.waitForIdle()
        if(name=="after-studio") {
            node("player-card-studio-screen").assertIsDisplayed()
            assertFalse("Me profile is still visible",has("me-player-card-studio"))
            assertFalse("Player navigation dock is still visible",has("dock-me"))
        }
        val presented=CountDownLatch(1)
        ui.runOnUiThread {
            val root=ui.activity.window.decorView
            if(Build.VERSION.SDK_INT>=29 && root.isHardwareAccelerated) {
                root.viewTreeObserver.registerFrameCommitCallback { presented.countDown() }
                root.invalidate()
            } else root.postOnAnimation { root.postOnAnimation { presented.countDown() } }
        }
        assertTrue("Android must present $name before capture",presented.await(5,TimeUnit.SECONDS))
        val bmp=checkNotNull(InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot())
        try {write(name,bmp)} finally {bmp.recycle()}
    }
    private fun crop(name:String,tag:String):String {
        val bmp=node(tag).captureToImage().asAndroidBitmap()
        return write(name,bmp)
    }
    private fun menu(tag:String) {click("card-menu");wait(tag);click(tag)}
    private fun description()=node("player-card-preview").fetchSemanticsNode().config[SemanticsProperties.ContentDescription].joinToString()
    private fun showFront() {
        if(has("player-card-back")) {click("player-card-preview");ui.waitUntil(5_000){!has("player-card-back")}}
    }

    @Test fun cardAndUniformStudioTabsNavigateWithoutLeavingEditor() {
        studio()
        node("player-card-tab-card").assertIsDisplayed()
        click("player-card-tab-uniform")
        wait("jersey-studio")
        node("jersey-studio").assertIsDisplayed()
        assertFalse("Card preview should not remain behind Uniform Studio",has("player-card-studio-screen"))
        click("player-card-tab-card")
        waitStudio()
        node("player-card-tab-card").assertIsDisplayed()
        assertFalse("Uniform Studio should close when returning to Card",has("jersey-studio"))
    }

    @Test fun cardEditionDraftSurvivesUniformTabRoundTrip() {
        studio()
        val original=PlayerCardSelection.identity(ui.activity).style
        val changed=PlayerCardStyle.entries.first { it!=original }
        pick("player-card-style-${changed.name.lowercase()}")
        wait("card-unsaved")
        click("player-card-tab-uniform")
        wait("jersey-studio")
        click("player-card-tab-card")
        waitStudio()
        wait("card-unsaved")
        // Switching editor tabs must not silently commit a draft card edition.
        assertEquals(original,PlayerCardSelection.identity(ui.activity).style)
        click("player-card-close")
        click("player-card-discard")
        wait("me-player-card-studio")
        assertEquals(original,PlayerCardSelection.identity(ui.activity).style)
    }

    @Test fun twoSidesShowLiveJerseyRealStatsAndPersistSelectedEdition() {
        studio()
        val saved=PlayerCardSelection.identity(ui.activity)
        val state=prefs()
        val player=Gson().fromJson(state,SaveState::class.java)
        val record=PlayerCardRecord.from(player)
        ready()
        screen("after-studio");crop("after-front","player-card-preview")
        val frontHashes=mutableSetOf<String>()
        val backHashes=mutableSetOf<String>()
        for(style in PlayerCardStyle.entries) {
            pick("player-card-style-${style.name.lowercase()}")
            wait("card-surface-${style.name.lowercase()}-ready");ready()
            assertTrue(description(),description().contains("jersey collectible card"))
            assertTrue(description(),description().contains(player.playerName.ifBlank{"Your player"}))
            val front=crop("edition-${style.name.lowercase()}-front","player-card-preview")
            assertTrue("${style.label} front should have its own composition",frontHashes.add(front))
            click("player-card-preview");wait("player-card-back");ui.waitForIdle()
            assertTrue(description(),description().contains(record.description))
            val backHash=crop("edition-${style.name.lowercase()}-back","player-card-preview")
            assertTrue("${style.label} reverse should carry its material",backHashes.add(backHash))
            if(style==PlayerCardStyle.HOLOGRAPHIC) {
                screen("back-screen")
                ui.activityRule.scenario.recreate();wait("player-card-back")
                assertTrue(description().contains("Player record"))
            }
            showFront()
        }
        repeat(3){click("player-card-preview");wait("player-card-back");showFront()}
        // Two deterministic intermediate frames on opposite sides of the 90° crossing.
        ui.mainClock.autoAdvance=false
        try {
            node("player-card-preview").performClick()
            ui.mainClock.advanceTimeByFrame();ui.mainClock.advanceTimeBy(96)
            val a=crop("flip-outgoing","player-card-preview")
            ui.mainClock.advanceTimeBy(112)
            val b=crop("flip-incoming","player-card-preview")
            assertNotEquals(a,b)
            ui.mainClock.advanceTimeBy(440)
        } finally {ui.mainClock.autoAdvance=true}
        wait("player-card-back");showFront()
        node("player-card-avatar").assertDoesNotExist()
        node("segment-card-pose").assertDoesNotExist()
        node("segment-card-face").assertDoesNotExist()
        val selected=PlayerCardStyle.entries.first{it!=saved.style}
        pick("player-card-style-${selected.name.lowercase()}");wait("card-unsaved")
        ready()
        click("player-card-save");wait("me-player-card-studio");ready()
        screen("saved-profile")
        assertEquals(saved.copy(style=selected),PlayerCardSelection.identity(ui.activity))
        assertEquals(state,prefs())
    }

    @Test fun restoredCardUniformAndDiscard() {
        fun stage(label:String) { android.util.Log.i("CQ_CARD_RESTORE_STAGE",label); File(out,"restore-stage.txt").appendText(label+"\n") }
        val expected=PlayerCardSelection.identity(ui.activity)
        stage("start");studio();ready();stage("before-cold-restored-capture");screen("cold-restored");stage("after-cold-restored-capture")
        val before=prefs()
        val changed=PlayerCardStyle.entries.first{it!=expected.style}
        pick("player-card-style-${changed.name.lowercase()}")
        stage("before-activity-recreate");ui.activityRule.scenario.recreate();stage("after-activity-recreate");waitStudio();wait("card-unsaved");ready()
        click("player-card-close");click("player-card-discard");wait("me-player-card-studio")
        assertEquals(expected,PlayerCardSelection.identity(ui.activity));assertEquals(before,prefs())
        pick("me-player-card-studio")
        stage("before-jersey-navigation");waitStudio()
        val jerseyBefore=prefs()
        click("segment-player-card-tab-uniform")
        wait("jersey-studio");click("segment-jersey-editor-identity")
        node("jersey-name").performScrollTo().performTextReplacement("Discard this")
        click("segment-player-card-tab-card");wait("jersey-save-exit");click("jersey-discard")
        waitStudio();ready();assertEquals("Leaving Uniform discards only after confirmation",jerseyBefore,prefs())
        click("segment-player-card-tab-uniform")
        wait("jersey-studio");click("segment-jersey-editor-identity")
        node("jersey-number").performScrollTo().performTextReplacement("00")
        node("jersey-name").performScrollTo().performTextReplacement("Avery West")
        click("jersey-close");wait("jersey-save-exit");click("jersey-save-exit")
        waitStudio();ready()
        assertTrue(description(),description().contains("Jersey number 00"))
        assertTrue(description().contains("Avery West"));stage("before-live-uniform-capture");screen("live-uniform");stage("after-live-uniform-capture")
        val old=Gson().fromJson(before,SaveState::class.java)
        val new=Gson().fromJson(prefs(),SaveState::class.java)
        assertEquals(old.drillResults,new.drillResults);assertEquals(old.matches,new.matches);assertEquals(old.totalXp,new.totalXp)
        assertEquals(expected,PlayerCardSelection.identity(ui.activity))
    }

    @Test fun everyOptionAtEachPose() {
        val requestedPose=InstrumentationRegistry.getArguments().getString("pose")
            ?.let { PlayerCardPose.valueOf(it) }
        val saved=PlayerCardSelection.identity(ui.activity)
        if (requestedPose != null) {
            assertTrue("Pose fixture must persist",PlayerCardSelection.write(ui.activity,saved.copy(pose=requestedPose)))
        }
        studio()
        val before=prefs()
        ready();screen("jersey-card-${requestedPose?.name?.lowercase() ?: "saved"}")
        val editions=mutableSetOf<String>()
        for(style in PlayerCardStyle.entries) {
            pick("player-card-style-${style.name.lowercase()}")
            wait("card-surface-${style.name.lowercase()}-ready");ready()
            assertTrue(description(),description().contains(style.label))
            assertTrue("${style.label} is a distinct card composition",editions.add(crop(
                "${style.name.lowercase()}-jersey-front","player-card-preview")))
        }
        assertEquals("All four card editions render distinctly",PlayerCardStyle.entries.size,editions.size)
        val selected=PlayerCardStyle.entries.first{it!=saved.style}
        pick("player-card-style-${selected.name.lowercase()}")
        click("player-card-save");wait("me-player-card-studio");ready()
        assertEquals(saved.copy(style=selected,pose=requestedPose ?: saved.pose),PlayerCardSelection.identity(ui.activity))
        assertEquals(before,prefs())
        assertTrue("Original saved card preferences are restored",PlayerCardSelection.write(ui.activity,saved))
    }

    @Test fun accessibleLayoutAndNoMotionRecord() {
        android.util.Log.i("CQ_ACCESS_STAGE","start")
        studio()
        android.util.Log.i("CQ_ACCESS_STAGE","studio-ready")
        val choice=PlayerCardSelection.identity(ui.activity)
        ready()
        android.util.Log.i("CQ_ACCESS_STAGE","artwork-ready")
        screen("layout-front")
        android.util.Log.i("CQ_ACCESS_STAGE","front-captured")
        click("player-card-preview");wait("player-card-back");screen("layout-back")
        android.util.Log.i("CQ_ACCESS_STAGE","back-captured")
        click("card-menu");ui.onNodeWithText("Read player statistics").performClick()
        ui.onNodeWithText("Training").assertExists();screen("readable-record")
        android.util.Log.i("CQ_ACCESS_STAGE","record-captured")
        ui.onNodeWithText("Done").performClick()
        node("player-card-save").assertIsDisplayed().assertHasClickAction()
        node("player-card-avatar").assertDoesNotExist()
        node("segment-card-pose").assertDoesNotExist()
        pick("player-card-style-${PlayerCardStyle.entries.first{it!=choice.style}.name.lowercase()}")
        wait("card-unsaved")
        click("player-card-close");if(has("player-card-discard"))click("player-card-discard")
        wait("me-player-card-studio")
        android.util.Log.i("CQ_ACCESS_STAGE","completed")
        assertEquals(choice,PlayerCardSelection.identity(ui.activity))
    }
}
