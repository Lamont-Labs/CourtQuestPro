package com.courtquestpro

import kotlin.math.PI
import kotlin.math.sin

/** The Compose shot is fitted to the actual backboard image and stage pixel bounds. */
internal data class WelcomeShotRect(val left: Float, val top: Float, val width: Float, val height: Float)

internal data class WelcomeShotLayout(
    val stageWidthPx: Float,
    val stageHeightPx: Float,
    val board: WelcomeShotRect,
    val ballSizePx: Float,
    val netHeightPx: Float
)

internal data class WelcomeShotFrame(
    val ballCenterX: Float,
    val ballCenterY: Float,
    val ballScale: Float,
    val ballAlpha: Float,
    val shadowScale: Float,
    val shadowAlpha: Float,
    val rimCenterX: Float,
    val rimCenterY: Float,
    val rimOpeningWidthPx: Float,
    val netScaleY: Float,
    val netTranslationY: Float,
    val hoopReveal: Float,
    val behindNet: Boolean,
    val floorY: Float
)

internal object WelcomeShotGeometry {
    private const val BOARD_SOURCE_WIDTH = 1024f
    private const val BOARD_SOURCE_HEIGHT = 637f
    private const val RIM_CENTER_X = 512f
    private const val RIM_CENTER_Y = 461f
    private const val RIM_OPENING_WIDTH = 292f
    private const val BALL_VISIBLE_FRACTION = .964f
    private const val BALL_DIAMETER_TO_OPENING = .52f
    private const val APEX_PROGRESS = .54f
    private const val RIM_ENTRY_PROGRESS = .67f
    private const val NET_EXIT_PROGRESS = .87f
    private const val FADE_START_PROGRESS = .93f

    fun frame(progress: Float, layout: WelcomeShotLayout): WelcomeShotFrame {
        require(layout.stageWidthPx > 0f && layout.stageHeightPx > 0f)
        require(layout.board.width > 0f && layout.board.height > 0f)
        require(layout.ballSizePx > 0f && layout.netHeightPx > 0f)

        val p = progress.coerceIn(0f, 1f)
        val rimX = layout.board.left + layout.board.width * RIM_CENTER_X / BOARD_SOURCE_WIDTH
        val rimY = layout.board.top + layout.board.height * RIM_CENTER_Y / BOARD_SOURCE_HEIGHT
        val opening = layout.board.width * RIM_OPENING_WIDTH / BOARD_SOURCE_WIDTH
        val visibleBallSize = layout.ballSizePx * BALL_VISIBLE_FRACTION
        val entryScale = (opening * BALL_DIAMETER_TO_OPENING / visibleBallSize).coerceIn(.12f, .76f)
        val entryDiameter = visibleBallSize * entryScale
        val restY = layout.stageHeightPx - visibleBallSize * .5f
        val apexY = rimY - entryDiameter * .5f - opening * .075f
        val netExitY = rimY + layout.netHeightPx * .68f
        val exitY = layout.stageHeightPx + visibleBallSize * .55f

        val centerY = when {
            p <= APEX_PROGRESS -> lerp(restY, apexY, easeOutCubic(p / APEX_PROGRESS))
            p <= RIM_ENTRY_PROGRESS -> lerp(
                apexY, rimY, easeInOutCubic((p - APEX_PROGRESS) / (RIM_ENTRY_PROGRESS - APEX_PROGRESS))
            )
            p <= NET_EXIT_PROGRESS -> lerp(
                rimY, netExitY, easeInOutCubic((p - RIM_ENTRY_PROGRESS) / (NET_EXIT_PROGRESS - RIM_ENTRY_PROGRESS))
            )
            else -> lerp(
                netExitY, exitY, easeInCubic((p - NET_EXIT_PROGRESS) / (1f - NET_EXIT_PROGRESS))
            )
        }
        val scale = when {
            p <= APEX_PROGRESS -> lerp(1f, entryScale, easeInCubic(p / APEX_PROGRESS))
            p <= RIM_ENTRY_PROGRESS -> lerp(
                entryScale, entryScale * .96f, (p - APEX_PROGRESS) / (RIM_ENTRY_PROGRESS - APEX_PROGRESS)
            )
            p <= NET_EXIT_PROGRESS -> lerp(
                entryScale * .96f, entryScale * 1.12f, (p - RIM_ENTRY_PROGRESS) / (NET_EXIT_PROGRESS - RIM_ENTRY_PROGRESS)
            )
            else -> lerp(
                entryScale * 1.12f, entryScale * 1.30f, (p - NET_EXIT_PROGRESS) / (1f - NET_EXIT_PROGRESS)
            )
        }
        val fade = if (p <= FADE_START_PROGRESS) 1f
        else (1f - (p - FADE_START_PROGRESS) / (1f - FADE_START_PROGRESS)).coerceIn(0f, 1f)
        val netProgress = ((p - RIM_ENTRY_PROGRESS) / (NET_EXIT_PROGRESS - RIM_ENTRY_PROGRESS)).coerceIn(0f, 1f)
        val netWave = sin(netProgress * PI * 3.0).toFloat() * (1f - netProgress)
        val shadowLift = ((restY - centerY) / layout.stageHeightPx).coerceIn(0f, 1f)

        return WelcomeShotFrame(
            ballCenterX = lerp(layout.stageWidthPx * .5f, rimX,
                easeInOutCubic((p / RIM_ENTRY_PROGRESS).coerceIn(0f, 1f))),
            ballCenterY = centerY,
            ballScale = scale,
            ballAlpha = fade,
            shadowScale = (1f - shadowLift * .63f).coerceIn(.28f, 1f),
            shadowAlpha = (.33f - shadowLift * .20f).coerceIn(.08f, .33f),
            rimCenterX = rimX,
            rimCenterY = rimY,
            rimOpeningWidthPx = opening,
            netScaleY = (1f + netProgress * .08f + netWave * .12f).coerceIn(.90f, 1.20f),
            netTranslationY = layout.netHeightPx * netProgress * .09f + layout.netHeightPx * netWave * .035f,
            hoopReveal = (p / .34f).coerceIn(0f, 1f),
            behindNet = p >= RIM_ENTRY_PROGRESS,
            floorY = layout.stageHeightPx
        )
    }

    private fun lerp(start: Float, end: Float, amount: Float) =
        start + (end - start) * amount.coerceIn(0f, 1f)

    private fun easeOutCubic(t: Float): Float {
        val inverse = 1f - t.coerceIn(0f, 1f)
        return 1f - inverse * inverse * inverse
    }

    private fun easeInCubic(t: Float) = t.coerceIn(0f, 1f).let { it * it * it }

    private fun easeInOutCubic(t: Float): Float {
        val value = t.coerceIn(0f, 1f)
        return if (value < .5f) 4f * value * value * value
        else 1f - (-2f * value + 2f).let { it * it * it } / 2f
    }
}
