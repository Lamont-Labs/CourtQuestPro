package com.courtquestpro

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WelcomeShotGeometryTest {
    private fun layout(width: Float, height: Float, board: WelcomeShotRect, ballSize: Float, netHeight: Float) =
        WelcomeShotLayout(width, height, board, ballSize, netHeight)

    @Test
    fun shotStartsOnStageRisesAboveMeasuredRimThenFallsThroughNetAndExits() {
        val layout = layout(
            420f, 760f,
            WelcomeShotRect(20f, 18f, 380f, 236.5f),
            ballSize = 240f,
            netHeight = 135f
        )
        val rest = WelcomeShotGeometry.frame(0f, layout)
        val apex = WelcomeShotGeometry.frame(.54f, layout)
        val entry = WelcomeShotGeometry.frame(.67f, layout)
        val passage = WelcomeShotGeometry.frame(.80f, layout)
        val exit = WelcomeShotGeometry.frame(1f, layout)

        assertTrue("Ball begins below the measured rim", rest.ballCenterY > rest.rimCenterY)
        assertTrue("Ball clears the measured rim at its apex", apex.ballCenterY < apex.rimCenterY)
        assertEquals("Ball reaches the rim plane", entry.rimCenterY, entry.ballCenterY, .01f)
        assertTrue("Ball descends into the measured net", passage.ballCenterY > passage.rimCenterY)
        assertTrue("Ball is behind net strands during passage", passage.behindNet)
        assertTrue("Ball exits the stage before routing", exit.ballCenterY > layout.stageHeightPx)
        assertEquals("Ball is invisible at shot completion", 0f, exit.ballAlpha, 0f)
        assertTrue("Ball fits the opening at rim entry",
            layout.ballSizePx * .964f * entry.ballScale <= entry.rimOpeningWidthPx * .53f)
    }

    @Test
    fun measuredShotGeometryRemainsValidForCompactAndLandscapeStages() {
        val layouts = listOf(
            layout(350f, 470f, WelcomeShotRect(7f, 14f, 336f, 209f), 220f, 115f),
            layout(740f, 300f, WelcomeShotRect(235f, 10.5f, 270f, 168.1f), 162f, 92f)
        )
        layouts.forEach { layout ->
            val rest = WelcomeShotGeometry.frame(0f, layout)
            val apex = WelcomeShotGeometry.frame(.54f, layout)
            val entry = WelcomeShotGeometry.frame(.67f, layout)
            assertTrue(rest.ballCenterX in 0f..layout.stageWidthPx)
            assertTrue(apex.ballCenterY < entry.rimCenterY)
            assertEquals(entry.rimCenterX, entry.ballCenterX, .01f)
            assertTrue(entry.rimOpeningWidthPx > 0f)
        }
    }
}
