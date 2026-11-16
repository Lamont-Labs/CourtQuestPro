package com.courtquestpro

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
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
                drawCircle(Color.White, 12.dp.toPx(), head)
                limb(neck, hip)
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
                ball?.let { drawCircle(accent, 10.dp.toPx(),it) }
            }
            Text("Illustrative posture cue only. Use the setup, movement and technique-check instructions for this specific drill.",
                color = CourtQuestDesign.textSecondary, style = MaterialTheme.typography.bodySmall)
        }
    }
}
