package com.courtquestpro

import android.graphics.BitmapShader
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat
import java.util.Locale

internal enum class JerseyView(val label: String) { FRONT("Front"), BACK("Back") }

@Composable
internal fun CourtJerseyArtwork(
    jersey: PlayerJersey,
    modifier: Modifier = Modifier,
    view: JerseyView = JerseyView.FRONT,
    describe: Boolean = false,
    compactMark: Boolean = false,
    zoom: Boolean = false
) {
    val style = jersey.design.template
    val description = listOfNotNull("${view.label} of ${style.label} jersey", jersey.number?.let { "number $it" },
        jersey.name.takeIf { view == JerseyView.BACK && it.isNotBlank() }).joinToString(", ")
    val context = LocalContext.current
    val font = remember(context) { ResourcesCompat.getFont(context, R.font.barlow_condensed_extra_bold) }
    var pixelHeight by remember { mutableIntStateOf(0) }
    val bounds = modifier.onSizeChanged { pixelHeight = it.height }
    val loadingThreshold = with(LocalDensity.current) { 100.dp.roundToPx() }
    Box(if (describe) bounds.semantics { contentDescription = description } else bounds,
        contentAlignment = Alignment.Center) {
        if (pixelHeight == 0) return@Box
        val sample = when {
            !zoom && pixelHeight <= 150 -> 4
            !zoom && pixelHeight <= 360 -> 2
            else -> 1
        }
        var plate by remember(style, view, sample) { mutableStateOf(JerseyMaterials.cached(style, view, sample)) }
        LaunchedEffect(style, view, sample) {
            if (plate == null) plate = JerseyMaterials.load(context.assets, style, view, sample)
        }
        val loaded = plate
        if (loaded != null) {
            val ready = if (describe) Modifier.testTag("material-${style.name.lowercase()}-${view.name.lowercase()}") else Modifier
            Box(ready.fillMaxSize().clipToBounds().graphicsLayer {
                compositingStrategy = CompositingStrategy.Offscreen
            }.drawWithCache {
                val factor = minOf(size.width / loaded.width, size.height / loaded.height) * if (zoom) 1.8f else 1f
                val left = (size.width - loaded.width * factor) * .5f
                val top = if (zoom) size.height * .50f - loaded.height * factor * .40f else (size.height - loaded.height * factor) * .5f
                val design = jersey.design
                val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
                val body = textileDyes(design.fabric, design.secondary, design.side)
                val binding = textileDyes(design.collar, design.armhole)
                val marks = kitLettering(jersey, view, loaded, font, compactMark)
                onDrawBehind {
                    drawIntoCanvas { target ->
                        val canvas = target.nativeCanvas
                        canvas.save(); canvas.translate(left, top); canvas.scale(factor, factor)
                        for (i in 0..1) {
                            val layer = loaded.layers[i]
                            paint.colorFilter = if (i == 0) body else binding
                            canvas.drawBitmap(layer.image, null, layer.bounds, paint)
                        }
                        for (mark in marks) {
                            canvas.drawPath(mark.path, mark.edge)
                            canvas.drawPath(mark.path, mark.ink)
                        }
                        val highlights = loaded.layers[2]
                        paint.colorFilter = null
                        canvas.drawBitmap(highlights.image, null, highlights.bounds, paint)
                        canvas.restore()
                    }
                }
            })
        } else if (describe && pixelHeight > loadingThreshold) {
            CircularProgressIndicator(Modifier.size(20.dp), color = CourtQuestDesign.accent, strokeWidth = 2.dp)
        }
    }
}

private data class JerseyPrint(val path: Path, val edge: Paint, val ink: Paint)

private fun kitLettering(jersey: PlayerJersey, view: JerseyView, plate: JerseyPlate, font: Typeface?, compact: Boolean): List<JerseyPrint> {
    val design = jersey.design
    val front = view == JerseyView.FRONT
    val w = plate.width.toFloat(); val h = plate.height.toFloat()
    val baseline = if (design.template == JerseyTemplate.ARENA) .294f else .32f
    val layer = plate.layers.first()
    val cloth = BitmapShader(layer.image, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP).apply {
        setLocalMatrix(Matrix().apply { setRectToRect(RectF(0f, 0f, layer.image.width.toFloat(), layer.image.height.toFloat()),
            layer.bounds, Matrix.ScaleToFit.FILL) })
    }
    val prints = mutableListOf<JerseyPrint>()
    fun mark(text: String, center: Float, cap: Float, maxWidth: Float, color: Int, number: Boolean) {
        if (text.isBlank()) return
        val type = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = font; textSize = 100f }
        val path = Path().apply { type.getTextPath(text, 0, text.length, 0f, 0f, this) }
        val box = RectF().also { path.computeBounds(it, true) }
        if (box.isEmpty) return
        val scale = minOf(w * maxWidth / box.width(), h * cap / box.height())
        path.transform(Matrix().apply {
            setScale(scale, scale)
            postTranslate(w * .5f - box.centerX() * scale, h * center - box.centerY() * scale)
        })
        val trim = if (jerseyContrast(design.side, design.fabric) >= 2.0 && jerseyContrast(design.side, color) >= 1.7) design.side
            else if (jerseyContrast(android.graphics.Color.BLACK, color) > jerseyContrast(android.graphics.Color.WHITE, color))
                android.graphics.Color.BLACK else android.graphics.Color.WHITE
        val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = trim; style = Paint.Style.STROKE; strokeJoin = Paint.Join.ROUND
            strokeWidth = box.height() * scale * if (number) .070f else .045f
        }
        // Heat-transfer lettering picks up a quarter of the photographed cloth shadow.
        val mix = FloatArray(20)
        for (row in 0..2) {
            val channel = (color ushr (16 - 8 * row)) and 255
            mix[row * 5] = channel / 255f * .25f; mix[row * 5 + 4] = channel * .75f
        }
        mix[18] = 1f
        val ink = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply { shader = cloth; colorFilter = ColorMatrixColorFilter(mix) }
        prints += JerseyPrint(path, stroke, ink)
    }
    if (!compact) {
        val text = (if (front) design.chestText else jersey.name).uppercase(Locale.ROOT)
        val center = if (front) baseline else .204f
        val cap = if (front) .046f else .049f
        // The actual printable chest/back panel is narrower than the full
        // bitmap: reserve clear space between the letters and armhole seams.
        val widthLimit = if (front) .55f else .49f
        val measure = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = font; textSize = 100f }
        val capHeight = android.graphics.Rect().also { measure.getTextBounds("H", 0, 1, it) }.height().coerceAtLeast(1)
        val naturalWidth = measure.measureText(text) * h * cap / capHeight
        val breaks = text.indices.filter { i -> i > 1 && i < text.lastIndex - 1 && (text[i] == ' ' || text[i] == '-') }
        val split = if (naturalWidth > w * widthLimit * 1.55f) breaks.minByOrNull { i ->
            kotlin.math.abs(measure.measureText(text.substring(0, i + 1).trimEnd()) - measure.measureText(text.substring(i + 1).trimStart()))
        } else null
        if (split != null) {
            val first = text.substring(0, split + 1).trimEnd()
            val remaining = text.substring(split + 1).trimStart()
            // Long surnames must remain readable rather than shrinking a full
            // back print to a single tiny line. Use three balanced lines only
            // when the second line would otherwise exceed the print area.
            val remainderWidth = measure.measureText(remaining) * h * .032f / capHeight
            val secondBreaks = remaining.indices.filter { i ->
                i > 1 && i < remaining.lastIndex - 1 && (remaining[i] == ' ' || remaining[i] == '-')
            }
            val secondSplit = if (!front && remainderWidth > w * widthLimit * 1.2f)
                secondBreaks.minByOrNull { i ->
                    kotlin.math.abs(measure.measureText(remaining.substring(0, i + 1).trimEnd()) -
                        measure.measureText(remaining.substring(i + 1).trimStart()))
                } else null
            if (secondSplit != null) {
                mark(first, center - .032f, .028f, widthLimit, design.nameInk, false)
                mark(remaining.substring(0, secondSplit + 1).trimEnd(), center, .028f, widthLimit, design.nameInk, false)
                mark(remaining.substring(secondSplit + 1).trimStart(), center + .032f, .028f, widthLimit, design.nameInk, false)
            } else {
                mark(first, center - .019f, .032f, widthLimit, design.nameInk, false)
                mark(remaining, center + .019f, .032f, widthLimit, design.nameInk, false)
            }
        } else mark(text, center, cap, widthLimit, design.nameInk, false)
    }
    jersey.number?.let {
        val center = when { compact -> .49f; !front -> .405f; design.chestText.isBlank() -> .424f; else -> baseline + .157f }
        mark(it, center, if (compact) .255f else if (front) .163f else .192f,
            if (compact) .69f else .53f, design.numberInk, true)
    }
    return prints
}
