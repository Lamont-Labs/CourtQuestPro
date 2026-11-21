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
                "navigation-route-jerseyStudio","navigation-route-home","welcome-get-started")
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
    private fun studioTitleDisplayed() = listOf("Card Studio","Studio").any { title ->
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
        compose.waitUntil(20_000){has("welcome-get-started")||has("dock-me")}
        if(has("welcome-get-started")) tap("welcome-get-started")
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
    private fun ready(pose:PlayerCardPose) {
        wait("card-athlete-${pose.name.lowercase()}-ready")
        compose.waitUntil(20_000) {
            PlayerCardStyle.entries.any {has("card-surface-${it.name.lowercase()}-ready")}
        }
        compose.waitForIdle()
        node("card-artwork-error").assertDoesNotExist()
        if(has("avatar-inspection"))wait("avatar-inspection-ready")
    }
    @Test fun allEditionsPosesAndFacesAreRealAndSaveAsProfile() {
        val preservedAvatar=PlayerCardSelection.identity(compose.activity).avatar
        enter();ready(PlayerCardSelection.identity(compose.activity).pose)
        capture("me-before-edit")
        // The old separate jersey/profile hero must not coexist with the selected card.
        node("me-jersey-hero").assertDoesNotExist()
        val before=stored()
        choose("me-player-card-studio");waitStudio()
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
        // Preserve the established cold-start contract explicitly, not by enum ordering.
        tap("segment-card-edition");choose("player-card-style-battle")
        tap("segment-card-pose");choose("player-card-pose-action")
        tap("segment-card-face");choose("player-card-face-question");ready(PlayerCardPose.ACTION)
        val save=node("player-card-save")
        save.assertIsDisplayed().assertIsEnabled()
        val bounds=save.fetchSemanticsNode().boundsInRoot
        assertTrue("48dp save touch target",bounds.height>=48*compose.activity.resources.displayMetrics.density)
        save.performClick();wait("me-player-card-studio")
        assertEquals(PlayerCardIdentity(PlayerCardStyle.BATTLE,PlayerCardPose.ACTION,PlayerCardFace.QUESTION,preservedAvatar),
            PlayerCardSelection.identity(compose.activity))
        ready(PlayerCardPose.ACTION);capture("me-selected-card")
        val after=stored()
        assertEquals(Gson().toJson(before),Gson().toJson(after))
        compose.activityRule.scenario.recreate()
        assertEquals(PlayerCardIdentity(PlayerCardStyle.BATTLE,PlayerCardPose.ACTION,PlayerCardFace.QUESTION,preservedAvatar),
            PlayerCardSelection.identity(compose.activity))
        val gallery=PlayerCardStyle.entries.flatMap { s->PlayerCardPose.entries.flatMap { p->
            PlayerCardFace.entries.map { f->"${s.name.lowercase()}-${p.name.lowercase()}-${f.name.lowercase()}.png" }}}
        File(compose.activity.filesDir,"player-card-proof/verified.json").writeText(Gson().toJson(mapOf(
            "editions" to PlayerCardStyle.entries.size,"poses" to PlayerCardPose.entries.size,
            "faces" to PlayerCardFace.entries.size,"combinations" to gallery.size,"gallery_files" to gallery,
            "profile_replaced" to true,"training_state_unchanged" to true,"preferences_persisted" to true)))
    }
    @Test fun coldStartUsesSavedCardAndJerseyEditsSynchronizeWithoutCardResave() {
        enter()
        val persisted=PlayerCardSelection.identity(compose.activity)
        assertEquals(PlayerCardStyle.BATTLE,persisted.style)
        assertEquals(PlayerCardPose.ACTION,persisted.pose)
        assertEquals(PlayerCardFace.QUESTION,persisted.face)
        ready(PlayerCardPose.ACTION)
        val expectedNumber=if(stored().playerJersey.number=="08") "09" else "08"
        val expectedName=if(stored().playerName=="RIVER") "BLAKE" else "RIVER"
        choose("me-jersey-studio");wait("jersey-studio")
        tap("segment-jersey-editor-identity")
        node("jersey-number").performScrollTo().performTextReplacement(expectedNumber)
        node("jersey-name").performScrollTo().performTextReplacement(expectedName)
        node("jersey-save").assertIsEnabled()
        node("jersey-close").performClick()
        wait("jersey-save-exit");node("jersey-save-exit").assertIsEnabled().performClick()
        wait("me-player-card-studio");ready(PlayerCardPose.ACTION)
        node("player-card-preview").assertContentDescriptionContains("Jersey number $expectedNumber",substring=true)
        node("player-card-preview").assertContentDescriptionContains(expectedName,substring=true)
        assertEquals(expectedNumber,stored().playerJersey.number)
        capture("me-live-jersey-updated")
        // Unsaved card drafts may restore during recreation, but leaving with Discard never changes disk.
        choose("me-player-card-studio");waitStudio()
        tap("segment-card-pose");choose("player-card-pose-standing")
        compose.activityRule.scenario.recreate();waitStudio();ready(PlayerCardPose.STANDING)
        node("player-card-close").performClick();node("player-card-discard").performClick()
        wait("me-player-card-studio")
        assertEquals(PlayerCardPose.ACTION,PlayerCardSelection.identity(compose.activity).pose)
        ready(PlayerCardPose.ACTION)
        node("me-jersey-hero").assertDoesNotExist()
    }

    private val customizedAvatar get()=AvatarAppearance(AvatarHair.TOPKNOT,AvatarHeadwear.WRAP,
        AvatarBuild.SHOULDERS,AvatarSkin.COOL,AvatarShoes.CRIMSON,AvatarGear.FULL,AvatarFootwear.STRAP)

    /** Isolate only rendered athlete/card pixels; option controls and labels cannot make this pass. */
    private fun renderedSignature():String {
        compose.waitForIdle()
        val bitmap=node(if(has("avatar-inspection"))"avatar-inspection" else "player-card-preview")
            .captureToImage().asAndroidBitmap()
        val pixels=IntArray(bitmap.width*bitmap.height)
        bitmap.getPixels(pixels,0,bitmap.width,0,0,bitmap.width,bitmap.height)
        val digest=MessageDigest.getInstance("SHA-256")
        digest.update(java.nio.ByteBuffer.allocate(pixels.size*4).apply{asIntBuffer().put(pixels)}.array())
        bitmap.recycle()
        return digest.digest().joinToString(""){"%02x".format(it)}
    }

    @Test fun avatarOptionsRenderOnEveryPoseAndPersistWithoutTouchingTraining() {
        enter()
        val before=Gson().toJson(stored())
        choose("me-player-card-studio");waitStudio()
        val allOptions=compose.activity.resources.configuration.fontScale<=1.05f &&
            compose.activity.resources.configuration.orientation!=android.content.res.Configuration.ORIENTATION_LANDSCAPE
        val pixelEvidence=linkedMapOf<String,Map<String,String>>()
        fun options(prefix:String,values:List<String>) {
            val hashes=linkedMapOf<String,String>()
            (if(allOptions)values else values.takeLast(1)).forEach { option ->
                choose("avatar-$prefix-${option.lowercase()}")
                if(prefix=="hair") {
                    wait("card-athlete-${currentPose.name.lowercase()}-${option.lowercase()}-ready")
                    wait("avatar-inspection-ready")
                    compose.waitForIdle()
                } else ready(currentPose)
                hashes[option]=renderedSignature()
                capture("avatar-${currentPose.name.lowercase()}-$prefix-${option.lowercase()}")
            }
            assertEquals("Each $prefix choice must change actual rendered artwork for $currentPose",hashes.size,hashes.values.toSet().size)
            pixelEvidence["${currentPose.name}-$prefix"]=hashes
        }
        for(pose in PlayerCardPose.entries) {
            currentPose=pose
            if(has("segment-avatar-hair"))tap("player-card-avatar")
            tap("segment-card-pose");choose("player-card-pose-${pose.name.lowercase()}");ready(pose)
            tap("player-card-avatar")
            ready(pose);capture("avatar-${pose.name.lowercase()}-studio-entry")
            tap("segment-avatar-hair")
            options("hair",AvatarHair.entries.map{it.name})
            options("ink",HairInk.entries.map{it.name})
            options("headwear",AvatarHeadwear.entries.map{it.name})
            tap("segment-avatar-body")
            options("build",AvatarBuild.entries.map{it.name})
            options("skin",AvatarSkin.entries.map{it.name})
            tap("segment-avatar-gear")
            options("footwear",AvatarFootwear.entries.map{it.name})
            options("shoes",AvatarShoes.entries.map{it.name})
            options("gear",AvatarGear.entries.map{it.name})
        }
        tap("segment-avatar-hair");choose("avatar-hair-topknot");choose("avatar-ink-original")
        tap("segment-avatar-body");choose("avatar-skin-cool")
        tap("player-card-avatar");tap("segment-card-pose");choose("player-card-pose-action");ready(PlayerCardPose.ACTION)
        tap("player-card-save");wait("me-player-card-studio");ready(PlayerCardPose.ACTION)
        assertEquals(customizedAvatar,PlayerCardSelection.identity(compose.activity).avatar)
        node("player-card-preview").assertContentDescriptionContains(customizedAvatar.description,substring=true)
        assertEquals(before,Gson().toJson(stored()))
        capture("me-custom-avatar")
        compose.activityRule.scenario.recreate()
        assertEquals(customizedAvatar,PlayerCardSelection.identity(compose.activity).avatar)
        File(compose.activity.filesDir,"player-card-proof/avatar-rendered-differences.json").writeText(Gson().toJson(pixelEvidence))
        assertTrue("Bounded decoded cache",PlayerCardMaterials.cacheBytes<=48*1024*1024)
        File(compose.activity.filesDir,"player-card-proof/avatar-memory.json").writeText(Gson().toJson(mapOf(
            "cache_bytes" to PlayerCardMaterials.cacheBytes,"largest_decoded_plate_bytes" to PlayerCardMaterials.largestPlateBytes,
            "registered_patch_bytes" to PlayerCardMaterials.patchCacheBytes,
            "heap_limit_bytes" to Runtime.getRuntime().maxMemory(),
            "device_is_emulator" to true,"physical_phone_performance_verified" to false)))
    }
    private var currentPose=PlayerCardPose.STANDING

    private val legacyIdentity get()=PlayerCardIdentity(PlayerCardStyle.CLASSIC,PlayerCardPose.DRIBBLING,
        PlayerCardFace.BLANK,AvatarAppearance(AvatarHair.BRAIDS,AvatarHeadwear.WHITE,AvatarBuild.LEAN,
            AvatarSkin.DEEP,AvatarShoes.ORANGE,AvatarGear.BOTH))

    @Test fun legacyV3CosmeticsUpgradeWithoutChangingTraining() {
        enter();ready(PlayerCardPose.DRIBBLING)
        val before=Gson().toJson(stored())
        assertFalse(compose.activity.getSharedPreferences("courtquest_player_card",0).contains("avatar_footwear_v3"))
        assertEquals(legacyIdentity,PlayerCardSelection.identity(compose.activity))
        var persisted=false
        compose.runOnUiThread { persisted=PlayerCardSelection.write(compose.activity,legacyIdentity) }
        assertTrue(persisted)
        assertEquals(before,Gson().toJson(stored()))
        compose.activityRule.scenario.recreate()
        assertEquals(legacyIdentity,PlayerCardSelection.identity(compose.activity))
        enter();ready(PlayerCardPose.DRIBBLING)
        assertEquals(before,Gson().toJson(stored()))
        capture("legacy-v3-preserved")
    }
    @Test fun legacyV3CosmeticsSurviveColdStart() {
        enter();ready(PlayerCardPose.DRIBBLING)
        assertEquals(legacyIdentity,PlayerCardSelection.identity(compose.activity))
        node("player-card-preview").assertContentDescriptionContains(legacyIdentity.avatar.description,substring=true)
    }
    @Test fun savedUniformTemplatesRenderAcrossEveryAthletePoseWithoutChangingTraining() {
        enter()
        val before=Gson().toJson(stored().copy(jerseyDesign=null))
        val identity=PlayerCardSelection.identity(compose.activity)
        val hashes=linkedMapOf<String,MutableSet<String>>()
        for(template in JerseyTemplate.entries) {
            choose("me-jersey-studio");wait("jersey-studio")
            tap("segment-jersey-editor-style");choose("segment-${template.name.lowercase()}")
            tap("jersey-close");wait("jersey-save-exit");tap("jersey-save-exit")
            wait("me-player-card-studio")
            assertEquals(template,stored().playerJersey.design.template)
            ready(identity.pose)
            node("player-card-preview").assertContentDescriptionContains("${template.label} saved uniform",substring=true)
            choose("me-player-card-studio");waitStudio();tap("segment-card-pose")
            for(pose in PlayerCardPose.entries) {
                choose("player-card-pose-${pose.name.lowercase()}");ready(pose)
                hashes.getOrPut(pose.name){linkedSetOf()}.add(renderedSignature())
                capture("uniform-${template.name.lowercase()}-${pose.name.lowercase()}")
            }
            choose("player-card-pose-${identity.pose.name.lowercase()}");ready(identity.pose)
            tap("player-card-save");wait("me-player-card-studio")
            assertEquals(identity,PlayerCardSelection.identity(compose.activity))
        }
        hashes.forEach{(pose,values)->assertEquals("Real worn template differences for $pose",JerseyTemplate.entries.size,values.size)}
        assertEquals(before,Gson().toJson(stored().copy(jerseyDesign=null)))
        File(compose.activity.filesDir,"player-card-proof/uniform-matrix.json").writeText(Gson().toJson(hashes))
    }

    @Test fun customizedAvatarSurvivesColdStartAndUnsavedAvatarDraftCanBeDiscarded() {
        enter();ready(PlayerCardPose.ACTION)
        assertEquals(customizedAvatar,PlayerCardSelection.identity(compose.activity).avatar)
        choose("me-player-card-studio");waitStudio()
        tap("player-card-avatar");tap("segment-avatar-body");choose("avatar-skin-deep")
        compose.activityRule.scenario.recreate();waitStudio()
        node("avatar-skin-deep").performScrollTo().assertIsSelected()
        tap("player-card-close");tap("player-card-discard");wait("me-player-card-studio")
        assertEquals(customizedAvatar,PlayerCardSelection.identity(compose.activity).avatar)
        capture("me-custom-avatar-cold-start")
    }
}
