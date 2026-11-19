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

    val description = listOfNotNull(
        "${view.label} of ${design.template.label} jersey", jersey.number?.let { "number $it" },
        jersey.name.takeIf { view == JerseyView.BACK && it.isNotBlank() }
    ).joinToString(", ")
    val artwork = if (describe) modifier.semantics { contentDescription = description } else modifier
    if (design.template == JerseyTemplate.ARENA) {
        CourtArenaJerseyArtwork(jersey, artwork, view)
        return
    }
    val outline = remember(design.template, view) { jerseyOutline(design.template, view) }
    val context = LocalContext.current
    val letters = remember(context) {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = ResourcesCompat.getFont(context, R.font.barlow_condensed_extra_bold)
            textAlign = Paint.Align.CENTER
        }
    }
    Canvas(artwork) {
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
                // Material shading is independent of the seven editable ink zones.
                // Narrow translucent highlights simulate drape without washing out selected colors.
                drawRect(
                    brush = Brush.horizontalGradient(
                        0f to Color.Black.copy(alpha = .22f),
                        .12f to Color.White.copy(alpha = .11f),
                        .27f to Color.Transparent,
                        .43f to Color.Black.copy(alpha = .09f),
                        .58f to Color.White.copy(alpha = .065f),
                        .76f to Color.Transparent,
                        .91f to Color.White.copy(alpha = .13f),
                        1f to Color.Black.copy(alpha = .28f),
                        endX = 220f
                    ),
                    size = Size(220f, 240f)
                )
                // Curved textile folds follow the drape of a hanging garment rather than
                // reading as straight, flat vector panels. Keep these translucent so
                // every user-selected fabric color remains recognizable.
                val foldPositions = listOf(48f, 69f, 91f, 127f, 151f, 173f)
                foldPositions.forEachIndexed { index, x ->
                    val drift = if (index % 2 == 0) 7f else -8f
                    val fold = Path().apply {
                        moveTo(x, 55f)
                        cubicTo(x + drift, 96f, x - drift * .6f, 150f, x + drift * .5f, 216f)
                    }
                    drawPath(fold, Color.Black.copy(alpha = .14f), style = Stroke(5.5f))
                    val reflected = Path().apply {
                        moveTo(x + 3.2f, 55f)
                        cubicTo(x + drift + 3.2f, 96f, x - drift * .6f + 3.2f, 150f, x + drift * .5f + 3.2f, 216f)
                    }
                    drawPath(reflected, Color.White.copy(alpha = .105f), style = Stroke(2.5f))
                }
                // Hem stitching is part of the fabric construction, not a colored block.
                for (x in 30..189 step 3) {
                    drawLine(Color.White.copy(alpha = .19f), Offset(x.toFloat(), 210.5f),
                        Offset(x + 1.2f, 210.5f), strokeWidth = .55f)
                }
                // Micro-mesh: a sparse, scale-aware perforation pattern clipped to the garment.
                // Keep contrast low enough for both light and dark fabric choices.
                val meshInk = if (jerseyContrast(design.fabric, 0xFF000000.toInt()) > 7.0)
                    Color.Black.copy(alpha = .075f) else Color.White.copy(alpha = .085f)
                for (y in 28..222 step 5) {
                    for (x in 30..190 step 5) {
                        val stagger = if ((y / 5) % 2 == 0) 0f else 2.5f
                        drawCircle(meshInk, radius = .46f, center = Offset(x + stagger, y.toFloat()))
                    }
                }
                // Fine seam detail follows garment edges, retaining visible fabric depth.
                drawPath(outline.collar, Color.Black.copy(alpha = .30f), style = Stroke(1.3f))
                drawPath(outline.armholes, Color.Black.copy(alpha = .28f), style = Stroke(1.3f))
                drawLine(Color.White.copy(alpha = .20f), Offset(37f, 206f), Offset(183f, 206f), strokeWidth = .8f)
                drawLine(Color.Black.copy(alpha = .30f), Offset(28f, 213f), Offset(192f, 213f), strokeWidth = 2.3f)
                drawLine(sheen.copy(alpha = .55f), Offset(28f, 214.5f), Offset(192f, 214.5f), strokeWidth = .75f)
            }
            drawPath(outline.body, Color.White.copy(alpha = .28f), style = Stroke(1.35f))
            val numberY = when (design.template) {
                JerseyTemplate.CLASSIC -> 135f
                JerseyTemplate.FASTBREAK -> 145f
                JerseyTemplate.RETRO -> 140f
                JerseyTemplate.ARENA -> error("Arena uses its textile artwork")
            } + if (view == JerseyView.BACK) 15f else 0f
            drawIntoCanvas { canvas ->
                fun mark(text: String, y: Float, height: Float, maxWidth: Float, ink: Int) {
                    if (text.isBlank()) return
                    letters.textSize = height
                    val measured = letters.measureText(text)
                    if (measured > maxWidth) letters.textSize *= maxWidth / measured
                    val baseline = y - (letters.ascent() + letters.descent()) / 2
                    // Render the outline, inset shadow and face separately to avoid flat decals.
                    // Reuse the seven existing user-controlled colors; no persisted schema change.
                    val outlineColor = if (jerseyContrast(ink, design.fabric) < 4.5)
                        design.collar else design.side
                    letters.style = Paint.Style.STROKE
                    letters.strokeJoin = Paint.Join.ROUND
                    letters.strokeWidth = if (height > 60f) 5.5f else 2.4f
                    letters.color = outlineColor
                    canvas.nativeCanvas.drawText(text, 110f, baseline, letters)
                    letters.strokeWidth = if (height > 60f) 2.3f else 1.1f
                    letters.color = android.graphics.Color.BLACK
                    letters.alpha = 110
                    canvas.nativeCanvas.drawText(text, 110f, baseline + 1.2f, letters)
                    letters.alpha = 255
                    letters.style = Paint.Style.FILL
                    letters.color = ink
                    canvas.nativeCanvas.drawText(text, 110f, baseline, letters)
                }
                if (view == JerseyView.BACK) mark(jersey.name, 76f, 27f, 114f, design.nameInk)
                jersey.number?.let { mark(it, numberY, 64f, 92f, design.numberInk) }
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
            JerseyTemplate.ARENA -> error("Arena uses its textile artwork")
            JerseyTemplate.RETRO -> {
                polygon(23f, 206f, 57f, 212f, 163f, 212f, 197f, 206f, 197f, 221f, 23f, 221f)
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
            JerseyTemplate.ARENA -> error("Arena uses its textile artwork")
            JerseyTemplate.RETRO -> {
                polygon(24f, 96f, 41f, 101f, 45f, 174f, 38f, 216f, 24f, 216f)
                polygon(196f, 96f, 179f, 101f, 175f, 174f, 182f, 216f, 196f, 216f)
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
