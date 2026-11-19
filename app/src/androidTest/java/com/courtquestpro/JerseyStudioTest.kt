package com.courtquestpro

import android.graphics.Bitmap
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.view.FrameMetrics
import android.view.Window
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.google.gson.Gson
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.util.Collections
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class JerseyStudioTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    private val kits = listOf(JerseyTemplate.ARENA, JerseyTemplate.CLASSIC, JerseyTemplate.FASTBREAK, JerseyTemplate.RETRO)
    private val longName = "ALEXANDRIA MONTGOMERY-WASHINGTON"
    private val colors = listOf("royal-blue", "white", "orange", "gold", "navy", "white", "sky-blue")
    private val values = listOf("#2259D1", "#FFFFFF", "#FF6F27", "#FFD05B", "#172950", "#FFFFFF", "#64B5F6")
    private val out get() = File(compose.activity.filesDir, "jersey-proof").apply { mkdirs() }
    private fun node(tag: String) = compose.onNodeWithTag(tag, useUnmergedTree = true)
    private fun has(tag: String) = compose.onAllNodesWithTag(tag, useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()
    private fun wait(tag: String) = compose.waitUntil(20_000) { has(tag) }
    private fun tap(tag: String) { node(tag).performClick() }
    private fun panel(tag: String) = node(tag).performScrollTo()
    private fun choose(tag: String) { panel(tag).performClick() }
    private fun edit(tag: String, value: String) { panel(tag).performTextReplacement(value) }
    private fun tool(name: String) { tap("segment-jersey-editor-$name") }
    private fun ready(style: JerseyTemplate, side: JerseyView) {
        wait("material-${style.name.lowercase()}-${side.name.lowercase()}"); compose.waitForIdle()
    }
    private fun enter() {
        assertTrue("Use the disposable Android emulator", Build.HARDWARE.contains("ranchu"))
        compose.waitUntil(20_000) { has("welcome-get-started") || has("dock-me") }
        if (has("welcome-get-started")) tap("welcome-get-started")
        wait("dock-me"); if (has("tour-skip")) tap("tour-skip")
        tap("dock-me"); if (has("tour-skip")) tap("tour-skip")
        choose("me-jersey-studio"); wait("jersey-preview")
    }
    private fun stored(): SaveState {
        val json = compose.activity.getSharedPreferences("court_quest_pro", 0).getString("state", null)
        return Gson().fromJson(json, SaveState::class.java)
    }
    private fun branding(brand: ChestBrand) {
        tool("identity"); choose("jersey-brand"); tap("jersey-brand-${brand.name.lowercase()}")
    }
    private fun zone(part: JerseyPart) {
        choose("jersey-part-picker"); tap("jersey-part-${part.name.lowercase()}")
    }
    private fun frame() {
        compose.waitForIdle()
        val latch = CountDownLatch(1)
        compose.runOnUiThread {
            val root = compose.activity.window.decorView
            if (Build.VERSION.SDK_INT >= 29 && root.isHardwareAccelerated) {
                root.viewTreeObserver.registerFrameCommitCallback { latch.countDown() }; root.invalidate()
            } else root.postOnAnimation { root.postOnAnimation { latch.countDown() } }
        }
        assertTrue("Timed out waiting for a presented Android frame", latch.await(5, TimeUnit.SECONDS))
    }
    private fun saveImage(bitmap: Bitmap, name: String) {
        File(out, "$name.png").outputStream().use { assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG, 95, it)) }
    }
    private fun capture(name: String, garment: Boolean = true) {
        frame()
        val shot = checkNotNull(InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot())
        try {
            saveImage(shot, name)
            if (garment) {
                // Crop the displayed Android frame. A second PixelCopy can fail while the IME is active.
                val bounds = node("jersey-preview").fetchSemanticsNode().boundsInWindow
                val left = kotlin.math.floor(bounds.left.toDouble()).toInt().coerceIn(0, shot.width - 1)
                val top = kotlin.math.floor(bounds.top.toDouble()).toInt().coerceIn(0, shot.height - 1)
                val right = kotlin.math.ceil(bounds.right.toDouble()).toInt().coerceIn(left + 1, shot.width)
                val bottom = kotlin.math.ceil(bounds.bottom.toDouble()).toInt().coerceIn(top + 1, shot.height)
                val crop = Bitmap.createBitmap(shot, left, top, right - left, bottom - top)
                try { saveImage(crop, "$name-garment") } finally { if (crop !== shot) crop.recycle() }
            }
        } finally { shot.recycle() }
    }
    private fun matrix(group: String, sides: List<JerseyView> = JerseyView.entries) {
        tool("style")
        for (kit in kits) {
            choose("segment-${kit.name.lowercase()}")
            for (side in sides) {
                tap("segment-${side.name.lowercase()}"); ready(kit, side)
                capture("$group-${kit.name.lowercase()}-${side.name.lowercase()}")
            }
        }
    }

    @Test fun customizationRestoresAcrossRecreationAndReturnsToCourt() {
        enter(); tool("identity"); edit("jersey-name", "RIVERS"); edit("jersey-number", "7")
        matrix("default")
        tool("identity"); edit("jersey-number", "7X"); node("jersey-save").assertIsNotEnabled()
        edit("jersey-number", "07"); edit("jersey-name", longName)
        branding(ChestBrand.CUSTOM); node("jersey-save").assertIsNotEnabled(); edit("jersey-team", "EAST RIDGE"); capture("identity-keyboard", false)
        panel("jersey-team").performImeAction(); node("jersey-close").assertIsDisplayed(); capture("identity-controls", false)
        tool("colors")
        JerseyPart.entries.forEach { part -> zone(part); choose("jersey-swatch-${colors[part.ordinal]}") }
        zone(JerseyPart.FABRIC); choose("jersey-edit-hex"); edit("jersey-hex", "#XY"); node("jersey-save").assertIsNotEnabled()
        edit("jersey-hex", "#2259D1"); choose("jersey-edit-hex")
        capture("color-controls"); tap("jersey-zoom"); ready(JerseyTemplate.RETRO, JerseyView.BACK); capture("fabric-detail"); tap("jersey-zoom")
        compose.activityRule.scenario.recreate(); wait("jersey-preview")
        matrix("custom")
        branding(ChestBrand.NONE); matrix("plain", listOf(JerseyView.FRONT))
        branding(ChestBrand.CUSTOM); tap("jersey-save"); node("jersey-save").assertContentDescriptionEquals("Jersey saved")
        compose.activityRule.scenario.recreate(); wait("jersey-preview"); assertCustom()
        tap("jersey-close"); wait("dock-me"); tap("dock-home"); wait("court-player-jersey")
        node("court-player-jersey").assertContentDescriptionEquals("Front of Retro jersey, number 07"); capture("saved-dock", false)
        tap("dock-me"); choose("me-jersey-studio"); ready(JerseyTemplate.RETRO, JerseyView.FRONT)
        tool("identity"); edit("jersey-number", "99"); tap("jersey-close"); compose.onNodeWithText("Discard", useUnmergedTree = true).performClick()
        wait("dock-me"); assertCustom()
    }

    @Test fun coldStartRestoresPreviouslySavedJersey() {
        enter(); ready(JerseyTemplate.RETRO, JerseyView.FRONT); assertCustom()
        tap("segment-back"); ready(JerseyTemplate.RETRO, JerseyView.BACK)
        tap("jersey-close"); wait("dock-me")
    }

    @Test fun upgradedV56JerseyIsVisibleBeforeAnySave() {
        enter(); ready(JerseyTemplate.RETRO, JerseyView.FRONT)
        val old = stored().playerJersey
        assertEquals("00", old.number); assertEquals("Morgan Lane", old.name)
        assertEquals("COURTQUEST", old.design.chestText)
        assertEquals(listOf("#21344A", "#E1B95B", "#6D3848", "#BD7A5D", "#819F98", "#F3E7D3", "#D9C4AE"),
            JerseyPart.entries.map { jerseyHex(old.design.color(it)) })
        capture("upgraded-v56"); tap("jersey-close"); wait("dock-me")
    }

    private fun assertCustom() {
        val j = stored().playerJersey
        assertEquals("07", j.number); assertEquals(longName, j.name); assertEquals("EAST RIDGE", j.design.chestText)
        assertEquals(JerseyTemplate.RETRO, j.design.template); assertEquals(values, JerseyPart.entries.map { jerseyHex(j.design.color(it)) })
    }

    private fun percentile95(values: List<Long>): Double? = values.filter { it >= 0 }.sorted().let {
        if (it.isEmpty()) null else it[(it.lastIndex * .95).toInt()] / 1_000_000.0
    }

    @Test fun paletteFrameTimingAndRapidInput() {
        enter(); tool("colors"); zone(JerseyPart.FABRIC); ready(JerseyTemplate.RETRO, JerseyView.FRONT)
        val timings = Collections.synchronizedList(mutableListOf<Long>())
        val drawTimes = Collections.synchronizedList(mutableListOf<Long>())
        val gpuTimes = Collections.synchronizedList(mutableListOf<Long>())
        val thread = HandlerThread("jersey-frame-metrics").apply { start() }
        val window = compose.activity.window
        val listener = Window.OnFrameMetricsAvailableListener { _, metrics, _ ->
            timings += metrics.getMetric(FrameMetrics.TOTAL_DURATION)
            drawTimes += metrics.getMetric(FrameMetrics.DRAW_DURATION)
            if (Build.VERSION.SDK_INT >= 31) gpuTimes += metrics.getMetric(FrameMetrics.GPU_DURATION)
        }
        val palette = listOf("royal-blue", "crimson", "white", "gold")
        val plate = JerseyMaterials.cached(JerseyTemplate.RETRO, JerseyView.FRONT, 1)
        assertNotNull(plate)
        compose.runOnUiThread { window.addOnFrameMetricsAvailableListener(listener, Handler(thread.looper)) }
        try {
            repeat(36) { i -> choose("jersey-swatch-${palette[i % palette.size]}"); frame() }
            val taps = palette.map { node("jersey-swatch-$it").fetchSemanticsNode().config[SemanticsActions.OnClick].action!! }
            compose.runOnIdle { repeat(102) { taps[it % taps.size].invoke() } }
            node("jersey-save").assertIsEnabled().performClick(); assertEquals("#B51733", jerseyHex(stored().playerJersey.design.fabric))
            assertSame(plate, JerseyMaterials.cached(JerseyTemplate.RETRO, JerseyView.FRONT, 1))
        } finally {
            compose.runOnUiThread { window.removeOnFrameMetricsAvailableListener(listener) }; thread.quitSafely(); thread.join(2500)
        }
        val ms = synchronized(timings) { timings.filter { it > 0 }.sorted().map { it / 1_000_000.0 } }
        assertTrue("Observed ${ms.size} frames", ms.size >= 24)
        val p95 = ms[(ms.lastIndex * .95).toInt()]
        File(out, "performance.json").writeText(Gson().toJson(mapOf("frames" to ms.size, "p50_ms" to ms[ms.size / 2], "p95_ms" to p95,
            "max_ms" to ms.last(), "frames_over_32ms" to ms.count { it > 32 }, "burst_inputs" to 102,
            "draw_p95_ms" to percentile95(drawTimes), "gpu_p95_ms" to percentile95(gpuTimes),
            "plate_bytes" to plate?.byteCount, "api" to Build.VERSION.SDK_INT)))
        assertTrue("Warm palette p95 ${p95}ms", p95 < 150)
    }
}
