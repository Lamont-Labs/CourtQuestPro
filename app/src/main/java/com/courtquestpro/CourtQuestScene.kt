package com.courtquestpro

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze

/** A quiet court behind the glass, never a filter over text or controls. */
@Composable
internal fun CourtQuestScene(content: @Composable () -> Unit) {
    val haze = remember { HazeState() }
    CompositionLocalProvider(LocalCourtHaze provides haze) {
        Box(Modifier.fillMaxSize().background(CourtQuestDesign.background)) {
            Canvas(Modifier.matchParentSize().haze(haze)) {
                drawRect(CourtQuestDesign.background)
                drawRect(Brush.radialGradient(listOf(CourtQuestDesign.accentVivid.copy(alpha = 0.018f), Color.Transparent),
                    center = Offset(size.width * 1.08f, size.height * 0.21f), radius = size.width * 0.92f))
                val stroke = Stroke(1.dp.toPx())
                val ink = Color.White.copy(alpha = 0.023f)
                drawCircle(ink, size.width * 0.34f, Offset(size.width * 0.92f, size.height * 0.30f), style = stroke)
                drawRect(ink, Offset(size.width * 0.62f, size.height * 0.12f), Size(size.width * 0.46f, size.height * 0.22f), style = stroke)
            }
            content()
        }
    }
}
