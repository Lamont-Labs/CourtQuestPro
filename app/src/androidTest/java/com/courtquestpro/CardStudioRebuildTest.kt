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

/** Runs through the shipping Me route and editor. Output files contain real Android pixels. */
class CardStudioRebuildTest {
    @get:Rule val ui=createAndroidComposeRule<MainActivity>()
    private val out get()=File(ui.activity.filesDir,"player-card-proof").apply{mkdirs()}
    private fun node(tag:String)=ui.onNodeWithTag(tag,useUnmergedTree=true)
    private fun has(tag:String)=ui.onAllNodesWithTag(tag,useUnmergedTree=true).fetchSemanticsNodes().isNotEmpty()
    private fun wait(tag:String)=ui.waitUntil(20_000){has(tag)}
    private fun studioTitleDisplayed() = listOf("Card Studio","Studio").any { title ->
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
    private fun click(tag:String) { node(tag).assertIsDisplayed().performClick() }
    private fun pick(tag:String) {
        // Choices live in horizontal LazyRows, so an off-screen option has no
        // semantics node yet. First expose its vertically stacked rail, then
        // scroll that rail horizontally to compose the requested option.
        val rail="${tag.substringBeforeLast('-')}-rail"
        if(has(rail)) {
            node(rail).performScrollTo().assertIsDisplayed()
            node(rail).performScrollToNode(hasTestTag(tag))
            node(tag).assertIsDisplayed().performClick()
        } else {
            node(tag).performScrollTo().assertIsDisplayed().performClick()
        }
    }
    private fun prefs()=ui.activity.getSharedPreferences("court_quest_pro",0).getString("state",null)!!
    private fun ready(pose:PlayerCardPose,hair:AvatarHair) {
        val expected="card-athlete-${pose.name.lowercase()}-${hair.name.lowercase()}-ready"
        try {
            ui.waitUntil(20_000){has(expected)||has("card-artwork-error")}
        } catch (timeout:androidx.compose.ui.test.ComposeTimeoutException) {
            val state=listOf("player-card-studio-screen","me-player-card-studio","dock-me",
                "card-artwork-loading","card-artwork-error").joinToString { "$it=${has(it)}" }
            runCatching {
                File(out,"artwork-readiness-timeout.txt").writeText(
                    "expected=$expected; $state; cacheBytes=${PlayerCardMaterials.cacheBytes}; patchCacheBytes=${PlayerCardMaterials.patchCacheBytes}")
                val bitmap=InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
                if(bitmap!=null)try { write("artwork-readiness-timeout",bitmap) } finally { bitmap.recycle() }
            }
            throw AssertionError("Timed out waiting for $expected; $state",timeout)
        }
        ui.waitForIdle()
        node("card-artwork-error").assertDoesNotExist()
    }
    private fun studio() {
        assertTrue(Build.HARDWARE.contains("ranchu"))
        ui.waitUntil(20_000){has("welcome-get-started")||has("dock-me")}
        if(has("welcome-get-started"))click("welcome-get-started")
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

    @Test fun twoSidesFlipStatsRestorationAndEditor() {
        studio()
        val saved=PlayerCardSelection.identity(ui.activity)
        val state=prefs()
        val record=PlayerCardRecord.from(Gson().fromJson(state,SaveState::class.java))
        ready(saved.pose,saved.avatar.hair)
        screen("after-studio");crop("after-front","player-card-preview")
        val frontHashes=mutableSetOf<String>()
        val backHashes=mutableSetOf<String>()
        for(style in PlayerCardStyle.entries) {
            pick("player-card-style-${style.name.lowercase()}")
            wait("card-surface-${style.name.lowercase()}-ready");ready(saved.pose,saved.avatar.hair)
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
        menu("card-reset-all")
        click("segment-card-pose");pick("player-card-pose-action");wait("card-unsaved")
        menu("card-reset-category");ready(PlayerCardPose.STANDING,AvatarHair.ORIGINAL)
        click("segment-card-edition");pick("player-card-style-battle")
        click("segment-card-pose");pick("player-card-pose-action")
        click("segment-card-face");pick("player-card-face-question")
        click("player-card-avatar");pick("avatar-hair-straight");pick("avatar-ink-chestnut")
        click("segment-avatar-body");pick("avatar-skin-light")
        click("segment-avatar-gear");pick("avatar-footwear-high")
        ready(PlayerCardPose.ACTION,AvatarHair.STRAIGHT)
        click("player-card-save");wait("me-player-card-studio");ready(PlayerCardPose.ACTION,AvatarHair.STRAIGHT)
        screen("saved-profile")
        assertEquals(state,prefs())
    }

    @Test fun restoredCardUniformAndDiscard() {
        val expected=PlayerCardSelection.identity(ui.activity)
        assertEquals(PlayerCardStyle.BATTLE,expected.style);assertEquals(PlayerCardPose.ACTION,expected.pose)
        assertEquals(AvatarSkin.LIGHT,expected.avatar.skin);assertEquals(AvatarHair.STRAIGHT,expected.avatar.hair)
        assertEquals(HairInk.CHESTNUT,expected.avatar.hairInk);assertEquals(AvatarFootwear.HIGH,expected.avatar.footwear)
        studio();ready(expected.pose,expected.avatar.hair);screen("cold-restored")
        val before=prefs()
        click("player-card-avatar");pick("avatar-hair-afro");ready(expected.pose,AvatarHair.AFRO)
        ui.activityRule.scenario.recreate();wait("card-unsaved");ready(expected.pose,AvatarHair.AFRO)
        click("player-card-close");click("player-card-discard");wait("me-player-card-studio")
        assertEquals(expected,PlayerCardSelection.identity(ui.activity));assertEquals(before,prefs())
        pick("me-player-card-studio")
        click("card-live-jersey");wait("jersey-studio");click("segment-jersey-editor-identity")
        node("jersey-number").performScrollTo().performTextReplacement("00")
        node("jersey-name").performScrollTo().performTextReplacement("Avery West")
        click("jersey-close");wait("jersey-save-exit");click("jersey-save-exit")
        waitStudio();ready(expected.pose,expected.avatar.hair)
        assertTrue(description(),description().contains("Jersey number 00"))
        assertTrue(description().contains("Avery West"));screen("live-uniform")
        val old=Gson().fromJson(before,SaveState::class.java)
        val new=Gson().fromJson(prefs(),SaveState::class.java)
        assertEquals(old.drillResults,new.drillResults);assertEquals(old.matches,new.matches);assertEquals(old.totalXp,new.totalXp)
        assertEquals(expected,PlayerCardSelection.identity(ui.activity))
    }

    @Test fun everyOptionAtEachPose() {
        studio();menu("card-reset-all")
        val p=PlayerCardPose.valueOf(InstrumentationRegistry.getArguments().getString("pose")!!)
        click("segment-card-pose");pick("player-card-pose-${p.name.lowercase()}")
        click("segment-card-face");pick("player-card-face-blank");ready(p,AvatarHair.ORIGINAL)
        val faceHashes=mutableSetOf<String>()
        for(face in PlayerCardFace.entries) {
            pick("player-card-face-${face.name.lowercase()}")
            ready(p,AvatarHair.ORIGINAL)
            val hash=crop("${p.name.lowercase()}-face-${face.name.lowercase()}","player-card-preview")
            assertTrue("${p.name}/${face.label} face treatment must render differently",faceHashes.add(hash))
        }
        // Use the featureless skin-matched face for appearance captures so
        // complexion and hairline changes remain visible in every pose.
        pick("player-card-face-blank");ready(p,AvatarHair.ORIGINAL)
        screen("pose-${p.name.lowercase()}")
        click("player-card-avatar")
        fun options(group:String,names:List<String>,original:String) {
            val hashes=mutableSetOf<String>()
            for(value in names) {
                pick("avatar-$group-${value.lowercase()}")
                val hair=if(group=="hair")AvatarHair.valueOf(value) else AvatarHair.ORIGINAL
                ready(p,hair)
                val hash=crop("${p.name.lowercase()}-$group-${value.lowercase()}","card-athlete-${p.name.lowercase()}-ready")
                assertTrue("${p.name}/$group/$value produced duplicate artwork",hashes.add(hash))
            }
            pick("avatar-$group-${original.lowercase()}")
        }
        options("hair",AvatarHair.entries.map{it.name},"ORIGINAL")
        options("ink",HairInk.entries.map{it.name},"ORIGINAL")
        options("headwear",AvatarHeadwear.entries.map{it.name},"NONE")
        click("card-inspect");wait("avatar-inspection-ready");screen("head-detail-${p.name.lowercase()}");click("card-inspect")
        click("segment-avatar-body")
        options("skin",AvatarSkin.entries.map{it.name},"ORIGINAL")
        options("build",AvatarBuild.entries.map{it.name},"ORIGINAL")
        click("segment-avatar-gear")
        options("footwear",AvatarFootwear.entries.map{it.name},"ORIGINAL")
        options("shoes",AvatarShoes.entries.map{it.name},"ORIGINAL")
        options("gear",AvatarGear.entries.map{it.name},"NONE")
        click("card-inspect");wait("avatar-inspection-ready");screen("foot-detail-${p.name.lowercase()}")
    }

    @Test fun accessibleLayoutAndNoMotionRecord() {
        studio()
        val choice=PlayerCardSelection.identity(ui.activity)
        ready(choice.pose,choice.avatar.hair)
        screen("layout-front")
        click("player-card-preview");wait("player-card-back");screen("layout-back")
        click("card-menu");ui.onNodeWithText("Read player statistics").performClick()
        ui.onNodeWithText("Training").assertExists();screen("readable-record")
        ui.onNodeWithText("Done").performClick()
        node("player-card-save").assertIsDisplayed().assertHasClickAction()
        click("segment-card-pose");pick("player-card-pose-ready")
        click("player-card-close");if(has("player-card-discard"))click("player-card-discard")
        wait("me-player-card-studio")
    }
}
