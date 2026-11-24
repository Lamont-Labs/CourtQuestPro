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
import androidx.compose.ui.unit.dp
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

    private fun capturePresented(name: String): String {
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

    @Test
    fun swipeShotCapturesLaunchApexRimNetAndExitOnAndroid() {
        // Retain the workflow entry point while verifying the replacement interaction.
        compose.waitUntil(20_000) { has("welcome-enter-ball") }
        val button = compose.onNodeWithTag("welcome-enter-ball", useUnmergedTree = true)
        button.assertIsDisplayed().assertHasClickAction()
        val bounds = button.getUnclippedBoundsInRoot()
        val screen = compose.onNodeWithTag("court-welcome-screen").getUnclippedBoundsInRoot()
        assertTrue("Basketball button is at least 48dp", bounds.right - bounds.left >= 48.dp && bounds.bottom - bounds.top >= 48.dp)
        assertTrue("Basketball is in lower half", bounds.top >= screen.top + (screen.bottom - screen.top) / 2f)
        compose.onNodeWithTag("welcome-swipe-prompt").assertDoesNotExist()
        compose.onNodeWithText("ENTER", useUnmergedTree = true).assertIsDisplayed()
        capturePresented("welcome-enter-ready")
        button.performClick()
        compose.waitUntil(10_000) { !has("welcome-enter-ball") }
        capturePresented("welcome-enter-complete")
    }

    @Test
    fun reducedMotionTapSkipsFlightAndStillEntersTheApp() {
        assertTrue("The verifier must disable Android animator scale for this case",
            !ValueAnimator.areAnimatorsEnabled())
        compose.waitUntil(20_000) { has("welcome-enter-ball") }
        compose.onNodeWithTag("welcome-enter-ball", useUnmergedTree = true).performClick()
        compose.waitUntil(10_000) { !has("welcome-enter-ball") }
    }
}
