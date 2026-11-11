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
                drawRect(Brush.radialGradient(listOf(CourtQuestDesign.accentVivid.copy(alpha = 0.085f), Color.Transparent),
                    center = Offset(size.width * 1.1f, size.height * 0.28f), radius = size.width * 1.2f))
                val stroke = Stroke(1.dp.toPx())
                val ink = Color.White.copy(alpha = 0.035f)
                drawCircle(ink, size.width * 0.36f, Offset(size.width * 0.86f, size.height * 0.35f), style = stroke)
                drawRect(ink, Offset(size.width * 0.60f, size.height * 0.13f), Size(size.width * 0.5f, size.height * 0.23f), style = stroke)
            }
            content()
        }
    }
}
