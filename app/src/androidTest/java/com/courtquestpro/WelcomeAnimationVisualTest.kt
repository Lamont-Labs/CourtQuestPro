package com.courtquestpro

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.animation.ValueAnimator
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.security.MessageDigest
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/** Records actual rendered frames from the production Android welcome experience. */
class WelcomeAnimationVisualTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    private fun has(tag: String) =
        compose.onAllNodesWithTag(tag, useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()

    private fun awaitPresentedFrame(name: String) {
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        val presented = CountDownLatch(1)
        compose.runOnUiThread {
            val root = compose.activity.window.decorView
            if (Build.VERSION.SDK_INT >= 29 && root.isHardwareAccelerated) {
                root.viewTreeObserver.registerFrameCommitCallback { presented.countDown() }
                root.invalidate()
            } else {
                root.postOnAnimation { root.postOnAnimation { presented.countDown() } }
            }
        }
        assertTrue("Android must present $name before capture", presented.await(5, TimeUnit.SECONDS))
    }

    private fun capturePresented(name: String): String {
        awaitPresentedFrame(name)
        val bitmap = checkNotNull(InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot())
        val file = File(compose.activity.filesDir, "player-card-proof/$name.png")
        file.parentFile?.mkdirs()
        try {
            file.outputStream().use { assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)) }
        } finally {
            bitmap.recycle()
        }
        return MessageDigest.getInstance("SHA-256").digest(file.readBytes()).joinToString("") { "%02x".format(it) }
    }

    private fun progressDescription() =
        compose.onNodeWithTag("welcome-shot-ball", useUnmergedTree = true)
            .fetchSemanticsNode().config[SemanticsProperties.StateDescription]

    private fun restingBallIsVisible(): Boolean {
        awaitPresentedFrame("resting basketball sample")
        val bitmap = InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot() ?: return false
        try {
            val bounds = compose.onNodeWithTag("welcome-shot-ball", useUnmergedTree = true)
                .fetchSemanticsNode().boundsInWindow
            val left = bounds.left.toInt().coerceIn(0, bitmap.width)
            val right = bounds.right.toInt().coerceIn(left, bitmap.width)
            val top = bounds.top.toInt().coerceIn(0, bitmap.height)
            val bottom = bounds.bottom.toInt().coerceIn(top, bitmap.height)
            var orangeSamples = 0
            for (y in top until bottom step 4) {
                for (x in left until right step 4) {
                    val pixel = bitmap.getPixel(x, y)
                    val red = Color.red(pixel)
                    val green = Color.green(pixel)
                    val blue = Color.blue(pixel)
                    if (red > 115 && green in 20..150 && blue < 80 && red > green * 1.25) {
                        orangeSamples++
                    }
                }
            }
            val proof = File(compose.activity.filesDir, "player-card-proof/resting-ball-sampling.txt")
            proof.parentFile?.mkdirs()
            val diagnostic = "bitmap=${bitmap.width}x${bitmap.height} bounds=$bounds orangeSamples=$orangeSamples"
            proof.appendText("$diagnostic\n")
            android.util.Log.i("CourtQuestWelcomeQA", diagnostic)
            return orangeSamples > 100
        } finally {
            bitmap.recycle()
        }
    }

    @Test
    fun swipeShotCapturesLaunchApexRimNetAndExitOnAndroid() {
        assertTrue("Run on the disposable Android emulator", Build.HARDWARE.contains("ranchu"))
        compose.waitUntil(20_000) { has("welcome-swipe-ball") }
        compose.waitUntil(20_000) { has("welcome-shot-ball") }
        try {
            compose.waitUntil(10_000) { restingBallIsVisible() }
        } catch (failure: androidx.compose.ui.test.ComposeTimeoutException) {
            capturePresented("welcome-resting-ball-failure")
            throw failure
        }
        val ballBounds = compose.onNodeWithTag("welcome-shot-ball", useUnmergedTree = true).getUnclippedBoundsInRoot()
        val promptBounds = compose.onNodeWithTag("welcome-swipe-prompt", useUnmergedTree = true).getUnclippedBoundsInRoot()
        assertTrue("The swipe instruction sits below the resting basketball", promptBounds.top >= ballBounds.bottom)
        val frames = linkedMapOf<String, String>()
        compose.mainClock.autoAdvance = false
        try {
            frames["rest"] = capturePresented("welcome-shot-rest")
            compose.onNodeWithTag("welcome-swipe-ball", useUnmergedTree = true)
                .performTouchInput { swipeUp() }

            var elapsed = 0L
            listOf(
                Triple("launch", 180L, 10..20),
                Triple("ascent", 430L, 30..37),
                Triple("apex", 720L, 50..58),
                Triple("rim-entry", 900L, 64..73),
                Triple("net-passage", 1_080L, 78..86),
                Triple("exit-before-transition", 1_250L, 90..98),
                Triple("complete-before-transition", 1_320L, 100..100)
            ).forEach { (name, target, expectedPercent) ->
                compose.mainClock.advanceTimeBy(target - elapsed, ignoreFrameDuration = true)
                elapsed = target
                if (name == "complete-before-transition") {
                    // The gesture starts the tween on the next Compose frame; wait
                    // for its actual terminal value before capturing the last frame.
                    var extraFrame = 0
                    while (extraFrame < 12 && !progressDescription().contains("100 percent")) {
                        compose.mainClock.advanceTimeBy(16, ignoreFrameDuration = true)
                        extraFrame++
                    }
                }
                frames[name] = capturePresented("welcome-shot-$name")
                val percent = Regex("""(\d+) percent""").find(progressDescription())?.groupValues?.get(1)?.toInt()
                assertTrue("$name should reach its shot phase (${progressDescription()})",
                    percent != null && percent in expectedPercent)
            }
            assertEquals("Each timeline point is a distinct Android-rendered frame",
                frames.size, frames.values.toSet().size)
            File(compose.activity.filesDir, "player-card-proof/welcome-shot-frames.json")
                .writeText(Gson().toJson(frames))
            compose.mainClock.advanceTimeBy(120, ignoreFrameDuration = true)
            compose.mainClock.autoAdvance = true
            compose.waitUntil(10_000) { !has("welcome-swipe-ball") }
        } finally {
            compose.mainClock.autoAdvance = true
        }
    }

    @Test
    fun reducedMotionTapSkipsFlightAndStillEntersTheApp() {
        assertTrue("Run on the disposable Android emulator", Build.HARDWARE.contains("ranchu"))
        assertTrue("The verifier must disable Android animator scale for this case",
            !ValueAnimator.areAnimatorsEnabled())
        compose.waitUntil(20_000) { has("welcome-swipe-ball") }
        compose.onNodeWithTag("welcome-swipe-ball", useUnmergedTree = true).performClick()
        compose.waitUntil(5_000) { !has("welcome-swipe-ball") }
    }
}
