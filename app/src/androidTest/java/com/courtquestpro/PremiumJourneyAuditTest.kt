package com.courtquestpro

import android.graphics.Bitmap
import android.os.Build
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.semantics.getOrNull
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/** Same source-compatible journey is captured on the frozen baseline and on the candidate. */
class PremiumJourneyAuditTest {
    @get:Rule val compose=createAndroidComposeRule<MainActivity>()
    private fun node(tag:String)=compose.onNodeWithTag(tag,useUnmergedTree=true)
    private fun has(tag:String)=compose.onAllNodesWithTag(tag,useUnmergedTree=true).fetchSemanticsNodes().isNotEmpty()
    private fun wait(tag:String)=compose.waitUntil(20_000){has(tag)}
    private fun reach(tag:String) {
        if(!has(tag))compose.onNode(hasScrollToIndexAction(),useUnmergedTree=true).performScrollToNode(hasTestTag(tag))
        node(tag).performScrollTo().assertIsDisplayed()
    }
    private fun click(tag:String,scroll:Boolean=false) {
        if(scroll)reach(tag)
        node(tag).assertIsDisplayed().assertHasClickAction().performClick()
        compose.waitForIdle()
    }
    private fun back() {
        compose.runOnUiThread{compose.activity.onBackPressedDispatcher.onBackPressed()}
        compose.waitForIdle()
    }
    private fun capture(name:String) {
        compose.waitForIdle()
        val latch=CountDownLatch(1)
        compose.runOnUiThread {
            val root=compose.activity.window.decorView
            if(Build.VERSION.SDK_INT>=29&&root.isHardwareAccelerated) {
                root.viewTreeObserver.registerFrameCommitCallback{latch.countDown()};root.invalidate()
            }else root.postOnAnimation{root.postOnAnimation{latch.countDown()}}
        }
        assertTrue("Presented Android frame",latch.await(5,TimeUnit.SECONDS))
        val bitmap=checkNotNull(InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot())
        try{
            val folder=File(compose.activity.filesDir,"app-quality-proof").apply{mkdirs()}
            File(folder,"$name.png").outputStream().use{assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG,100,it))}
        }finally{bitmap.recycle()}
    }
    @Test fun everySoloDestinationAndSettingsPageIsReachable() {
        assertTrue("Disposable emulator only",Build.HARDWARE.contains("ranchu"))
        val baseline=InstrumentationRegistry.getArguments().getString("courtquestBaseline")=="true"
        compose.waitUntil(20_000){has("welcome-get-started")||has("dock-home")}
        if(has("welcome-get-started")){capture("welcome");click("welcome-get-started")}
        wait("dock-home")
        if(has("tour-skip")){capture("walkthrough");click("tour-skip")}
        for(route in listOf("home","library","quests","career","me")) {
            click("dock-$route");node("dock-$route").assertIsSelected()
            if(route=="library")click("segment-today")
            if(route=="me"){
                click("segment-profile",true)
                wait("card-athlete-${PlayerCardSelection.identity(compose.activity).pose.name.lowercase()}-ready")
                if(!baseline) {
                    val dock=node("player-bottom-dock").getUnclippedBoundsInRoot()
                    val card=node("player-card-preview").getUnclippedBoundsInRoot()
                    val action=node("me-player-card-studio").getUnclippedBoundsInRoot()
                    assertTrue("Entire primary profile card fits before the dock",card.top.value>=0&&card.bottom<=dock.top)
                    assertTrue("Card Studio action fits in the first viewport",action.bottom<=dock.top)
                }
            }
            capture(route)
        }
        compose.onAllNodesWithText("Milestones",useUnmergedTree=true).onLast().performScrollTo()
        if(!baseline) {
            compose.onNode(SemanticsMatcher("Earned milestone count"){ n->
                n.config.getOrNull(androidx.compose.ui.semantics.SemanticsProperties.Text)
                    ?.any{it.text.matches(Regex("\\d+ earned"))}==true
            },useUnmergedTree=true).performScrollTo().assertIsDisplayed()
        }
        capture("me-milestones")
        click("me-player-card-studio",true);wait("player-card-studio-screen")
        wait("card-athlete-${PlayerCardSelection.identity(compose.activity).pose.name.lowercase()}-ready")
        node("player-card-save").assertIsDisplayed().assertIsEnabled()
        capture("card-studio");click("player-card-close");wait("me-player-card-studio")
        click("me-jersey-studio",true);wait("jersey-studio");capture("jersey-studio")
        click("jersey-close");wait("me-player-card-studio")
        click("segment-settings",true);capture("settings")
        for(page in listOf("player","preferences","help","how-to","about","notices")) {
            click("me-settings-$page",true);capture("settings-$page")
            if(!baseline&&page=="help"){back();wait("me-settings-player")}
            else click("me-settings-back",true)
        }
        click("segment-profile",true)
        click("dock-library");click("segment-library")
        reach("library-search");node("library-search").performTextReplacement("Five-Spot Form Shooting")
        compose.activityRule.scenario.onActivity { activity ->
            val imm=activity.getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            imm.hideSoftInputFromWindow(activity.window.decorView.windowToken,0)
        }
        click("library-drill-s1",true);capture("drill-detail")
        click("drill-enlarge-art",true);wait("drill-art-viewer");capture("drill-art")
        click("drill-close-art");back();wait("dock-library")
        click("dock-home");click("court-todays-focus",true)
        reach("live-record-success");capture("recorder");click("live-leave-session",true);wait("dock-home")
    }
}
