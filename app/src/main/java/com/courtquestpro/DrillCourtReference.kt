package com.courtquestpro

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/**
 * Simplified overhead positioning reference, not an anatomical form demonstration.
 * The court and arrows are schematic; the written coaching cues remain authoritative.
 */
@Composable
internal fun DrillCourtReference(drill: Drill) {
    val orange = CourtQuestDesign.accentVivid
    val court = Color(0xFF9AA3AF)
    val hasKnownRoute = drill.id in setOf(
        "s1", "s2", "s3", "b1", "b2", "b3", "f1", "f2", "f3",
        "d1", "d2", "d3", "r1", "r2", "r3", "p1", "p2", "p3"
    )
    CourtQuestPremiumSurface(Modifier.fillMaxWidth(), PaddingValues(16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("COURT POSITION REFERENCE", color = orange,
                style = MaterialTheme.typography.labelMedium)
            Canvas(
                Modifier.fillMaxWidth().height(188.dp).semantics {
                    contentDescription = if (hasKnownRoute) {
                        "Schematic court route for ${drill.title}; orange marks show the sequence."
                    } else {
                        "Court outline only. No route is inferred for this custom drill."
                    }
                }
            ) {
                val w = size.width
                val h = size.height
                val left = w * .08f
                val right = w * .92f
                val top = h * .08f
                val bottom = h * .92f
                val mid = w * .5f
                val lineWidth = 2.dp.toPx()
                drawRoundRect(color = court.copy(alpha = .65f),
                    topLeft = Offset(left, top),
                    size = androidx.compose.ui.geometry.Size(right-left, bottom-top),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(10.dp.toPx()),
                    style = Stroke(lineWidth))
                drawLine(court.copy(alpha = .55f), Offset(left, h*.72f), Offset(right, h*.72f), lineWidth)
                drawRect(court.copy(alpha = .6f),
                    topLeft = Offset(w*.35f, top),
                    size = androidx.compose.ui.geometry.Size(w*.3f,h*.35f),
                    style = Stroke(lineWidth))
                drawCircle(court, 7.dp.toPx(), Offset(mid, h*.17f), style = Stroke(lineWidth))
                val normalizedPositions = when (drill.id) {
                    "s1" -> listOf(Offset(0.25f, 0.64f), Offset(0.38f, 0.53f), Offset(0.5f, 0.5f), Offset(0.62f, 0.53f), Offset(0.75f, 0.64f))
                    "s2" -> listOf(Offset(0.48f, 0.72f), Offset(0.5f, 0.56f), Offset(0.5f, 0.42f))
                    "s3" -> listOf(Offset(0.2f, 0.72f), Offset(0.38f, 0.55f), Offset(0.48f, 0.48f))
                    "b1" -> listOf(Offset(0.42f, 0.72f), Offset(0.55f, 0.72f), Offset(0.42f, 0.72f))
                    "b2" -> listOf(Offset(0.5f, 0.85f), Offset(0.5f, 0.67f), Offset(0.5f, 0.43f))
                    "b3" -> listOf(Offset(0.28f, 0.8f), Offset(0.42f, 0.67f), Offset(0.3f, 0.59f), Offset(0.58f, 0.4f))
                    "f1" -> listOf(Offset(0.22f, 0.78f), Offset(0.38f, 0.53f), Offset(0.5f, 0.23f))
                    "f2" -> listOf(Offset(0.22f, 0.8f), Offset(0.44f, 0.62f), Offset(0.32f, 0.49f), Offset(0.5f, 0.24f))
                    "f3" -> listOf(Offset(0.22f, 0.7f), Offset(0.44f, 0.38f), Offset(0.58f, 0.22f))
                    "d1" -> listOf(Offset(0.2f, 0.65f), Offset(0.5f, 0.65f), Offset(0.8f, 0.65f))
                    "d2" -> listOf(Offset(0.5f, 0.84f), Offset(0.5f, 0.58f), Offset(0.5f, 0.4f))
                    "d3" -> listOf(Offset(0.28f, 0.66f), Offset(0.5f, 0.57f), Offset(0.72f, 0.66f))
                    "r1" -> listOf(Offset(0.5f, 0.65f), Offset(0.5f, 0.48f), Offset(0.5f, 0.31f))
                    "r2" -> listOf(Offset(0.5f, 0.35f), Offset(0.48f, 0.5f), Offset(0.2f, 0.72f))
                    "r3" -> listOf(Offset(0.28f, 0.6f), Offset(0.48f, 0.43f), Offset(0.66f, 0.36f))
                    "p1" -> listOf(Offset(0.2f, 0.65f), Offset(0.5f, 0.65f), Offset(0.8f, 0.65f))
                    "p2" -> listOf(Offset(0.2f, 0.75f), Offset(0.4f, 0.6f), Offset(0.72f, 0.5f))
                    "p3" -> listOf(Offset(0.2f, 0.65f), Offset(0.5f, 0.48f), Offset(0.8f, 0.65f))
                    else -> emptyList()
                }
                val positions = normalizedPositions.map { Offset(it.x*w, it.y*h) }
                if (positions.isNotEmpty()) {
                val route = Path().apply {
                    moveTo(positions.first().x,positions.first().y)
                    positions.drop(1).forEach { lineTo(it.x,it.y) }
                }
                drawPath(route, orange.copy(alpha=.8f),
                    style=Stroke(width=3.dp.toPx(),cap=StrokeCap.Round))
                positions.forEachIndexed { i,p ->
                    drawCircle(if (i==0) orange else Color.White, 8.dp.toPx(),p)
                    drawCircle(Color(0xFF111318), 3.dp.toPx(),p)
                }
                }
            }
            Text(
                if (hasKnownRoute) {
                    "Schematic overhead guide: orange marks show a simplified route or spacing reference. Consult the written coaching steps for actual technique."
                } else {
                    "Generic court outline only. No movement is inferred for this custom drill; follow its saved instructions."
                },
                color = CourtQuestDesign.textSecondary,
                style = MaterialTheme.typography.bodySmall)
        }
    }
}
