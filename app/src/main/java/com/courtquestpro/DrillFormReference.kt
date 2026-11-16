package com.courtquestpro

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp

/**
 * Stylized pose cue, not a biomechanically validated illustration.
 * The associated drill-specific written coaching remains the source of technique instructions.
 */
@Composable
internal fun DrillFormReference(drill: Drill) {
    val accent = CourtQuestDesign.accentVivid
    val mode = when (drill.category) {
        "Shooting" -> "Shooting stance and release"
        "Ball Handling" -> "Low stance and ball control"
        "Finishing" -> "Approach and extension"
        "Defense" -> "Balanced defensive stance"
        "Rebounding" -> "Base, reach and secure"
        else -> "Step and passing extension"
    }
    val technique = DrillCoachingLibrary.forDrill(drill)
    val focus = when (drill.id) {
        "s1" -> "Hold a balanced follow-through at each spot"
        "s2" -> "Prepare hands early and shoot from a stable catch"
        "s3" -> "Stop under control before rising into the shot"
        "b1" -> "Keep hips low and the crossover below the waist"
        "b2" -> "Change speed without leaning or losing control"
        "b3" -> "Protect the ball while linking two moves"
        "f1" -> "Match takeoff foot and finishing hand"
        "f2" -> "Plant safely and finish with the outside hand"
        "f3" -> "Shield the ball on the far side of the rim"
        "d1" -> "Slide with a wide base; do not cross feet"
        "d2" -> "Shorten final steps to contest without contact"
        "d3" -> "Mirror the partner while maintaining spacing"
        "r1" -> "Establish legal position before locating the ball"
        "r2" -> "Land balanced, pivot, then make the outlet"
        "r3" -> "Track the bounce while keeping safe spacing"
        "p1" -> "Step toward the target with a controlled release"
        "p2" -> "Square to the target before releasing the pass"
        "p3" -> "Identify the target before stepping into the lane"
        else -> null
    }
    var showCorrection by rememberSaveable(drill.id) { mutableStateOf(false) }
    CourtQuestPremiumSurface(Modifier.fillMaxWidth(), PaddingValues(16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("PLAYER POSITION", color = accent, style = MaterialTheme.typography.labelMedium)
            Text(mode, color = CourtQuestDesign.textPrimary, style = MaterialTheme.typography.titleMedium)
            Canvas(Modifier.fillMaxWidth().height(190.dp)) {
                val w = size.width
                val h = size.height
                val stroke = 5.dp.toPx()
                fun pt(x: Float, y: Float) = Offset(w*x, h*y)
                fun limb(a: Offset, b: Offset, color: Color = Color.White) =
                    drawLine(color, a, b, stroke, cap = StrokeCap.Round)
                val low = drill.category in listOf("Defense", "Ball Handling", "Rebounding")
                val head = pt(.5f, if (low) .20f else .16f)
                val neck = pt(.5f, if (low) .32f else .28f)
                val hip = pt(.5f, if (low) .56f else .55f)
                val leftFoot = pt(if (low) .26f else .37f, .91f)
                val rightFoot = pt(if (low) .74f else .64f, .91f)
                drawLine(Color(0xFF808895), pt(.13f,.93f),pt(.87f,.93f),2.dp.toPx())
                drawCircle(Color(0xFF222833), 15.dp.toPx(), head)
                drawCircle(Color.White, 12.dp.toPx(), head)
                val jersey = Path().apply {
                    moveTo(w*.41f,h*.34f)
                    lineTo(w*.59f,h*.34f)
                    lineTo(w*.57f,h*.54f)
                    lineTo(w*.43f,h*.54f)
                    close()
                }
                drawPath(jersey, Color(0xFF303744))
                drawPath(jersey, accent.copy(alpha=.9f), style=Stroke(2.dp.toPx()))
                limb(hip,pt(.40f,.72f))
                limb(pt(.40f,.72f),leftFoot)
                limb(hip,pt(.61f,.72f))
                limb(pt(.61f,.72f),rightFoot)
                val ball = when (drill.category) {
                    "Shooting" -> pt(.67f,.19f)
                    "Ball Handling" -> pt(.73f,.77f)
                    "Finishing" -> pt(.72f,.13f)
                    "Defense" -> null
                    "Rebounding" -> pt(.60f,.12f)
                    else -> pt(.77f,.43f)
                }
                val leftHand = when (drill.category) {
                    "Defense" -> pt(.22f,.44f)
                    "Rebounding" -> pt(.43f,.18f)
                    else -> pt(.33f,.44f)
                }
                val rightHand = when (drill.category) {
                    "Shooting" -> pt(.65f,.24f)
                    "Finishing" -> pt(.71f,.16f)
                    "Rebounding" -> pt(.59f,.17f)
                    "Defense" -> pt(.78f,.44f)
                    "Ball Handling" -> pt(.70f,.63f)
                    else -> pt(.72f,.43f)
                }
                limb(pt(.5f,.35f),pt(.38f,.40f))
                limb(pt(.38f,.40f),leftHand)
                limb(pt(.5f,.35f),pt(.62f,.40f))
                limb(pt(.62f,.40f),rightHand)
                ball?.let {
                    drawCircle(accent, 12.dp.toPx(), it)
                    drawCircle(Color(0xFF111318), 12.dp.toPx(), it, style=Stroke(1.5.dp.toPx()))
                    drawLine(Color(0xFF111318), Offset(it.x-10.dp.toPx(),it.y),
                        Offset(it.x+10.dp.toPx(),it.y), 1.dp.toPx())
                }
            }
            if (focus != null && technique != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = !showCorrection,
                        onClick = { showCorrection = false },
                        label = { Text("Correct form") }
                    )
                    FilterChip(
                        selected = showCorrection,
                        onClick = { showCorrection = true },
                        label = { Text("Mistake & fix") }
                    )
                }
                if (showCorrection) {
                    Text("COMMON MISTAKE & CORRECTION", color = accent,
                        style = MaterialTheme.typography.labelMedium)
                    Text(technique.correction, color = CourtQuestDesign.textPrimary,
                        style = MaterialTheme.typography.bodyLarge)
                    Text("The pose above is a general reference, not an illustration of the mistake.",
                        color = CourtQuestDesign.textSecondary,
                        style = MaterialTheme.typography.bodySmall)
                } else {
                    Text("CORRECT FORM CHECK", color = accent,
                        style = MaterialTheme.typography.labelMedium)
                    Text(focus, color = CourtQuestDesign.textPrimary,
                        style = MaterialTheme.typography.bodyLarge)
                    Text(technique.checkpoint, color = CourtQuestDesign.textSecondary,
                        style = MaterialTheme.typography.bodyMedium)
                }
            }
            Text("Stylized silhouette and posture cue only. Use the setup, movement and technique-check instructions for this specific drill.",
                color = CourtQuestDesign.textSecondary, style = MaterialTheme.typography.bodySmall)
        }
    }
}
