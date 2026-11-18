package com.courtquestpro

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.core.content.res.ResourcesCompat

internal enum class JerseyView(val label: String) { FRONT("Front"), BACK("Back") }

@Composable
internal fun CourtJerseyArtwork(
    jersey: PlayerJersey,
    modifier: Modifier = Modifier,
    view: JerseyView = JerseyView.FRONT,
    describe: Boolean = false
) {
    val design = jersey.design
    val outline = remember(design.template, view) { jerseyOutline(design.template, view) }
    val context = LocalContext.current
    val letters = remember(context) {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = ResourcesCompat.getFont(context, R.font.barlow_condensed_extra_bold)
            textAlign = Paint.Align.CENTER
        }
    }
    val description = listOfNotNull(
        "${view.label} of ${design.template.label} jersey", jersey.number?.let { "number $it" },
        jersey.name.takeIf { view == JerseyView.BACK && it.isNotBlank() }
    ).joinToString(", ")
    Canvas(if (describe) modifier.semantics { contentDescription = description } else modifier) {
        val jerseyScale = minOf(size.width / 220f, size.height / 240f)
        if (jerseyScale <= 0f) return@Canvas
        val fabric = Color(design.fabric)
        val shadow = lerp(fabric, Color.Black, .17f)
        val sheen = lerp(fabric, Color.White, .12f)
        withTransform({
            translate((size.width - 220 * jerseyScale) / 2, (size.height - 240 * jerseyScale) / 2)
            scale(jerseyScale, jerseyScale, pivot = Offset.Zero)
        }) {
            clipPath(outline.body) {
                drawRect(
                    brush = Brush.horizontalGradient(
                        0f to shadow, .17f to sheen, .23f to fabric, .77f to fabric, .87f to sheen, 1f to shadow,
                        endX = 220f
                    ),
                    size = Size(220f, 240f)
                )
                drawPath(outline.secondary, Color(design.secondary))
                drawPath(outline.panels, Color(design.side))
                drawPath(outline.collar, Color(design.collar), style = Stroke(9f))
                drawPath(outline.armholes, Color(design.armhole), style = Stroke(9f))
                drawLine(sheen, Offset(28f, 213f), Offset(192f, 213f), strokeWidth = 1.5f)
            }
            drawPath(outline.body, Color.White.copy(alpha = .28f), style = Stroke(1.35f))
            val numberY = when (design.template) {
                JerseyTemplate.CLASSIC -> 135f
                JerseyTemplate.FASTBREAK -> 145f
                JerseyTemplate.RETRO -> 128f
            } + if (view == JerseyView.BACK) 15f else 0f
            drawIntoCanvas { canvas ->
                fun mark(text: String, y: Float, height: Float, maxWidth: Float, ink: Int) {
                    if (text.isBlank()) return
                    letters.textSize = height
                    val measured = letters.measureText(text)
                    if (measured > maxWidth) letters.textSize *= maxWidth / measured
                    letters.color = ink
                    val baseline = y - (letters.ascent() + letters.descent()) / 2
                    canvas.nativeCanvas.drawText(text, 110f, baseline, letters)
                }
                if (view == JerseyView.BACK) mark(jersey.name, 76f, 27f, 114f, design.nameInk)
                jersey.number?.let { mark(it, numberY, 94f, 107f, design.numberInk) }
            }
        }
    }
}

private data class JerseyOutline(
    val body: Path, val secondary: Path, val panels: Path, val collar: Path, val armholes: Path
)

private fun jerseyOutline(template: JerseyTemplate, view: JerseyView): JerseyOutline {
    val back = view == JerseyView.BACK
    val leftNeck = if (template == JerseyTemplate.RETRO) 73f else 78f
    val rightNeck = 220f - leftNeck
    val shoulder = if (template == JerseyTemplate.FASTBREAK) 32f else 36f

    fun Path.neck() {
        when {
            back -> quadraticTo(110f, 46f, rightNeck, 19f)
            template == JerseyTemplate.CLASSIC -> quadraticTo(110f, 80f, rightNeck, 19f)
            template == JerseyTemplate.FASTBREAK -> { lineTo(110f, 62f); lineTo(rightNeck, 19f) }
            else -> { lineTo(85f, 54f); lineTo(135f, 54f); lineTo(rightNeck, 19f) }
        }
    }
    val body = Path().apply {
        moveTo(leftNeck, 19f)
        neck()
        lineTo(220f - shoulder, 25f)
        quadraticTo(175f, 65f, 195f, 90f)
        lineTo(199f, 216f)
        quadraticTo(110f, 232f, 21f, 216f)
        lineTo(25f, 90f)
        quadraticTo(45f, 65f, shoulder, 25f)
        close()
    }
    val collar = Path().apply { moveTo(leftNeck, 19f); neck() }
    val armholes = Path().apply {
        moveTo(shoulder, 25f); quadraticTo(45f, 65f, 25f, 90f)
        moveTo(220f - shoulder, 25f); quadraticTo(175f, 65f, 195f, 90f)
    }
    val secondary = Path().apply {
        when (template) {
            JerseyTemplate.CLASSIC -> {
                polygon(26f, 18f, 77f, 16f, 91f, 55f, 26f, 67f)
                polygon(194f, 18f, 143f, 16f, 129f, 55f, 194f, 67f)
            }
            JerseyTemplate.FASTBREAK -> {
                polygon(17f, 38f, 61f, 89f, 55f, 217f, 11f, 227f)
                polygon(203f, 38f, 159f, 89f, 165f, 217f, 209f, 227f)
            }
            JerseyTemplate.RETRO -> {
                polygon(15f, 175f, 53f, 195f, 167f, 195f, 205f, 175f, 205f, 232f, 15f, 232f)
            }
        }
    }
    val panels = Path().apply {
        when (template) {
            JerseyTemplate.CLASSIC -> {
                polygon(18f, 87f, 42f, 91f, 36f, 220f, 16f, 225f)
                polygon(202f, 87f, 178f, 91f, 184f, 220f, 204f, 225f)
            }
            JerseyTemplate.FASTBREAK -> {
                polygon(15f, 51f, 53f, 97f, 43f, 218f, 31f, 221f, 43f, 102f, 12f, 73f)
                polygon(205f, 51f, 167f, 97f, 177f, 218f, 189f, 221f, 177f, 102f, 208f, 73f)
            }
            JerseyTemplate.RETRO -> {
                polygon(12f, 102f, 47f, 102f, 47f, 145f, 57f, 145f, 57f, 190f, 44f, 190f, 44f, 224f, 12f, 224f)
                polygon(208f, 102f, 173f, 102f, 173f, 145f, 163f, 145f, 163f, 190f, 176f, 190f, 176f, 224f, 208f, 224f)
            }
        }
    }
    return JerseyOutline(body, secondary, panels, collar, armholes)
}

private fun Path.polygon(vararg points: Float) {
    moveTo(points[0], points[1])
    for (index in 2 until points.size step 2) lineTo(points[index], points[index + 1])
    close()
}
