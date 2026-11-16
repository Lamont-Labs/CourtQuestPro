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
import androidx.compose.ui.unit.dp

/**
 * Simplified overhead positioning reference, not an anatomical form demonstration.
 * The court and arrows are schematic; the written coaching cues remain authoritative.
 */
@Composable
internal fun DrillCourtReference(drill: Drill) {
    val orange = CourtQuestDesign.accentVivid
    val court = Color(0xFF9AA3AF)
    CourtQuestPremiumSurface(Modifier.fillMaxWidth(), PaddingValues(16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("COURT POSITION REFERENCE", color = orange,
                style = MaterialTheme.typography.labelMedium)
            Canvas(Modifier.fillMaxWidth().height(188.dp)) {
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
                val positions = when (drill.category) {
                    "Shooting" -> listOf(Offset(.25f,.65f),Offset(.5f,.61f),Offset(.75f,.65f))
                    "Ball Handling" -> listOf(Offset(.28f,.76f),Offset(.5f,.61f),Offset(.72f,.46f))
                    "Finishing" -> listOf(Offset(.24f,.64f),Offset(.40f,.43f),Offset(.5f,.25f))
                    "Defense" -> listOf(Offset(.24f,.66f),Offset(.50f,.66f),Offset(.76f,.66f))
                    "Rebounding" -> listOf(Offset(.32f,.47f),Offset(.5f,.34f),Offset(.68f,.47f))
                    else -> listOf(Offset(.22f,.7f),Offset(.5f,.58f),Offset(.78f,.7f))
                }.map { Offset(it.x*w,it.y*h) }
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
            Text("Schematic overhead guide: orange marks the starting point; the line shows a general movement or spacing pattern. Not a drill-specific footwork demonstration.",
                color = CourtQuestDesign.textSecondary,
                style = MaterialTheme.typography.bodySmall)
        }
    }
}
