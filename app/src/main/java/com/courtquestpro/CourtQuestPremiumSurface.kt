package com.courtquestpro

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeChild

internal val LocalCourtHaze = staticCompositionLocalOf<HazeState?> { null }

/** Reflections and supported backdrop blur are behind the unfiltered foreground. */
@Composable
internal fun CourtQuestPremiumSurface(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(18.dp),
    highlighted: Boolean = false,
    blurred: Boolean = false,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(if (highlighted) 24.dp else 20.dp)
    val state = LocalCourtHaze.current
    val blur = if (blurred && state != null) Modifier.hazeChild(state) {
        backgroundColor = CourtQuestDesign.background
        tints = listOf(HazeTint(CourtQuestDesign.surface.copy(alpha = 0.86f)))
        fallbackTint = HazeTint(CourtQuestDesign.surface.copy(alpha = 0.97f))
        blurRadius = 18.dp
        noiseFactor = 0.02f
    } else Modifier
    Box(modifier.shadow(if (highlighted) 18.dp else 8.dp, shape).clip(shape).then(blur)
        .background(Brush.linearGradient(listOf(
            Color(0xF025272B), Color(0xF0111215), Color(0xFA090A0C))))
        .drawBehind {
            val reflection = Path().apply {
                moveTo(0f, 0f); lineTo(size.width, 0f)
                lineTo(size.width, size.height * 0.025f)
                lineTo(0f, size.height * 0.22f); close()
            }
            drawPath(reflection, Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.065f), Color.Transparent)))
            if (highlighted) drawRect(Brush.radialGradient(
                listOf(CourtQuestDesign.accentVivid.copy(alpha = 0.14f), Color.Transparent),
                center = Offset(size.width, size.height), radius = size.width * 0.9f))
        }
        .border(1.dp, Brush.linearGradient(listOf(
            Color.White.copy(alpha = 0.32f), Color.White.copy(alpha = 0.06f),
            (if (highlighted) CourtQuestDesign.accentVivid else Color.White).copy(alpha = 0.23f),
            Color.White.copy(alpha = 0.08f))), shape)
        .padding(contentPadding)) { content() }
}
