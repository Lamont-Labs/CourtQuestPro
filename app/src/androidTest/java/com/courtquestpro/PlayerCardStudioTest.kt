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
        assertEquals(PlayerCardIdentity(PlayerCardStyle.BATTLE,PlayerCardPose.ACTION,PlayerCardFace.QUESTION,preservedAvatar),
            PlayerCardSelection.identity(compose.activity))
        ready(PlayerCardPose.ACTION);capture("me-selected-card")
        val after=stored()
        assertEquals(Gson().toJson(before),Gson().toJson(after))
        compose.activityRule.scenario.recreate()
        assertEquals(PlayerCardIdentity(PlayerCardStyle.BATTLE,PlayerCardPose.ACTION,PlayerCardFace.QUESTION,preservedAvatar),
            PlayerCardSelection.identity(compose.activity))
        File(compose.activity.filesDir,"player-card-proof/verified.json").writeText(
            """{"editions":3,"poses":3,"faces":3,"combinations":27,"profile_replaced":true,"training_state_unchanged":true,"preferences_persisted":true}""")
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
        choose("me-player-card-studio");wait("player-card-studio-screen")
        tap("segment-card-pose");choose("player-card-pose-standing")
        compose.activityRule.scenario.recreate();wait("player-card-studio-screen");ready(PlayerCardPose.STANDING)
        node("player-card-close").performClick();node("player-card-discard").performClick()
        wait("me-player-card-studio")
        assertEquals(PlayerCardPose.ACTION,PlayerCardSelection.identity(compose.activity).pose)
        ready(PlayerCardPose.ACTION)
        node("me-jersey-hero").assertDoesNotExist()
    }

    private val customizedAvatar get()=AvatarAppearance(AvatarHair.BRAIDS,AvatarHeadwear.ORANGE,
        AvatarBuild.POWER,AvatarSkin.LIGHT,AvatarShoes.VIOLET,AvatarGear.BOTH)

    @Test fun avatarOptionsRenderOnEveryPoseAndPersistWithoutTouchingTraining() {
        enter()
        val before=Gson().toJson(stored())
        choose("me-player-card-studio");wait("player-card-studio-screen")
        val allOptions=compose.activity.resources.configuration.fontScale<=1.05f
        fun options(prefix:String,values:List<String>) {
            (if(allOptions)values else values.takeLast(1)).forEach { option ->
                choose("avatar-$prefix-${option.lowercase()}");ready(currentPose)
                capture("avatar-${currentPose.name.lowercase()}-$prefix-${option.lowercase()}")
            }
        }
        for(pose in PlayerCardPose.entries) {
            currentPose=pose
            if(has("segment-avatar-hair"))tap("player-card-avatar")
            tap("segment-card-pose");choose("player-card-pose-${pose.name.lowercase()}");ready(pose)
            tap("player-card-avatar")
            choose("segment-avatar-hair")
            options("hair",AvatarHair.entries.map{it.name})
            options("headwear",AvatarHeadwear.entries.map{it.name})
            choose("segment-avatar-body")
            options("build",AvatarBuild.entries.map{it.name})
            options("skin",AvatarSkin.entries.map{it.name})
            choose("segment-avatar-gear")
            options("shoes",AvatarShoes.entries.map{it.name})
            options("gear",AvatarGear.entries.map{it.name})
        }
        tap("player-card-save");wait("me-player-card-studio");ready(PlayerCardPose.ACTION)
        assertEquals(customizedAvatar,PlayerCardSelection.identity(compose.activity).avatar)
        node("player-card-preview").assertContentDescriptionContains(customizedAvatar.description,substring=true)
        assertEquals(before,Gson().toJson(stored()))
        capture("me-custom-avatar")
        compose.activityRule.scenario.recreate()
        assertEquals(customizedAvatar,PlayerCardSelection.identity(compose.activity).avatar)
    }
    private var currentPose=PlayerCardPose.STANDING

    @Test fun customizedAvatarSurvivesColdStartAndUnsavedAvatarDraftCanBeDiscarded() {
        enter();ready(PlayerCardPose.ACTION)
        assertEquals(customizedAvatar,PlayerCardSelection.identity(compose.activity).avatar)
        choose("me-player-card-studio");wait("player-card-studio-screen")
        tap("player-card-avatar");choose("segment-avatar-body");choose("avatar-skin-deep")
        compose.activityRule.scenario.recreate();wait("player-card-studio-screen")
        node("avatar-skin-deep").performScrollTo().assertIsSelected()
        tap("player-card-close");tap("player-card-discard");wait("me-player-card-studio")
        assertEquals(customizedAvatar,PlayerCardSelection.identity(compose.activity).avatar)
        capture("me-custom-avatar-cold-start")
    }
}
