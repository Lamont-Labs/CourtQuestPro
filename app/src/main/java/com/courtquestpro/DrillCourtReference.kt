package com.courtquestpro

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/** Resolution-independent coaching illustration. Coordinates are normalized to the half court. */
@Composable
internal fun DrillCourtReference(drill: Drill) {
    val orange = Color(0xFFFF650E)
    val line = Color(0xFF637080)
    val shape = RoundedCornerShape(22.dp)
    Column(Modifier.fillMaxWidth().border(1.dp, orange.copy(alpha = .45f), shape)
        .background(Brush.verticalGradient(listOf(Color(0xFF19202B), Color(0xFF080B10))), shape)
        .padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("COURT REFERENCE  /  ${TrainingLibrary.playerRequirement(drill).uppercase()}",
            color = orange, style = MaterialTheme.typography.labelMedium)
        Canvas(Modifier.fillMaxWidth().height(230.dp)) {
            val w = size.width; val h = size.height
            fun p(x: Float, y: Float) = Offset(x * w, y * h)
            fun stroke(a: Offset, b: Offset, color: Color = line, width: Float = 2f) =
                drawLine(color = color, start = a, end = b, strokeWidth = width.dp.toPx(), cap = StrokeCap.Round)
            drawRoundRect(Color(0xFF0E1620), size = size, cornerRadius = androidx.compose.ui.geometry.CornerRadius(14.dp.toPx()))
            drawRoundRect(line.copy(alpha = .75f), topLeft = p(.035f,.035f),
                size = Size(w*.93f,h*.93f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx()),
                style = Stroke(1.4.dp.toPx()))
            // Half-court key, basket, free-throw circle and three-point arc.
            drawRect(line.copy(alpha=.75f), topLeft=p(.37f,.035f), size=Size(w*.26f,h*.38f), style=Stroke(1.5.dp.toPx()))
            drawCircle(line, radius=w*.075f, center=p(.5f,.425f), style=Stroke(1.5.dp.toPx()))
            drawCircle(orange, radius=w*.023f, center=p(.5f,.105f), style=Stroke(2.5.dp.toPx()))
            stroke(p(.44f,.075f),p(.56f,.075f),orange,2.5f)
            val arc = Path().apply {
                moveTo(w*.12f,h*.035f)
                cubicTo(w*.06f,h*.78f,w*.94f,h*.78f,w*.88f,h*.035f)
            }
            drawPath(arc,line.copy(alpha=.65f),style=Stroke(1.5.dp.toPx()))
            // Each built-in drill has its own movement reference, not a category-wide template.
            val positions = when(drill.id) {
                "s1" -> listOf(.19f to .22f,.32f to .47f,.50f to .54f,.68f to .47f,.81f to .22f)
                "s2" -> listOf(.67f to .64f,.67f to .47f)
                "s3" -> listOf(.79f to .67f,.65f to .46f,.51f to .38f)
                "b1" -> listOf(.43f to .66f,.57f to .66f,.43f to .66f)
                "b2" -> listOf(.24f to .84f,.24f to .60f,.24f to .33f)
                "b3" -> listOf(.76f to .78f,.65f to .63f,.54f to .44f,.46f to .26f)
                "f1" -> listOf(.20f to .59f,.38f to .34f,.48f to .17f)
                "f2" -> listOf(.76f to .68f,.64f to .48f,.49f to .17f)
                "f3" -> listOf(.27f to .42f,.45f to .19f,.62f to .19f)
                "d1" -> listOf(.25f to .67f,.75f to .67f,.25f to .67f)
                "d2" -> listOf(.51f to .81f,.51f to .57f,.65f to .42f)
                "d3" -> listOf(.32f to .58f,.68f to .58f)
                "r1" -> listOf(.50f to .47f,.42f to .31f,.50f to .20f)
                "r2" -> listOf(.50f to .22f,.59f to .35f,.78f to .61f)
                "r3" -> listOf(.76f to .52f,.53f to .25f,.42f to .34f)
                "p1" -> listOf(.26f to .62f,.74f to .62f)
                "p2" -> listOf(.27f to .68f,.73f to .68f,.50f to .34f)
                "p3" -> listOf(.50f to .23f,.59f to .39f,.78f to .68f)
                else -> when(drill.category) {
                    "Defense" -> listOf(.30f to .52f,.50f to .65f,.70f to .52f)
                    "Passing" -> listOf(.27f to .55f,.73f to .55f)
                    "Rebounding" -> listOf(.38f to .26f,.62f to .26f)
                    "Ball Handling" -> listOf(.30f to .75f,.50f to .53f,.70f to .31f)
                    "Finishing" -> listOf(.23f to .55f,.38f to .32f,.50f to .19f)
                    else -> listOf(.22f to .55f,.50f to .54f,.78f to .55f)
                }
            }
            val path = Path()
            positions.forEachIndexed { index, (x,y) ->
                val point = p(x,y)
                if(index==0) path.moveTo(point.x,point.y) else path.lineTo(point.x,point.y)
            }
            if(positions.size>1) drawPath(path,orange.copy(alpha=.85f),style=Stroke(3.dp.toPx(),cap=StrokeCap.Round))
            positions.forEachIndexed { index, (x,y) ->
                val point=p(x,y)
                drawCircle(orange.copy(alpha=.16f),w*.043f,point)
                drawCircle(orange,w*.025f,point)
                drawCircle(Color(0xFF0A0D12),w*.012f,point)
                if(index>0) {
                    val prior=p(positions[index-1].first,positions[index-1].second)
                    val dx=point.x-prior.x;val dy=point.y-prior.y
                    val length=kotlin.math.sqrt(dx*dx+dy*dy).coerceAtLeast(1f)
                    val ux=dx/length;val uy=dy/length
                    val tip=Offset(prior.x+dx*.76f,prior.y+dy*.76f)
                    stroke(Offset(tip.x-ux*9-uy*6,tip.y-uy*9+ux*6),tip,orange,2f)
                    stroke(Offset(tip.x-ux*9+uy*6,tip.y-uy*9-ux*6),tip,orange,2f)
                }
            }
        }
        Text("Orange markers trace the drill-specific reference sequence. Follow the written instructions for footwork, timing and safety.",
            color=Color(0xFFB7C0CC),style=MaterialTheme.typography.bodySmall)
    }
}
