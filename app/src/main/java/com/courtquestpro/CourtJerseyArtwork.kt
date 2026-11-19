package com.courtquestpro

import android.graphics.Paint
import android.graphics.Path
import android.graphics.PathMeasure
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.core.content.res.ResourcesCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.math.roundToInt

internal enum class JerseyView(val label: String) { FRONT("Front"), BACK("Back") }

internal val JerseyArtworkReady = SemanticsPropertyKey<Boolean>("JerseyArtworkReady")

@Composable
internal fun CourtJerseyArtwork(
    jersey: PlayerJersey,
    modifier: Modifier = Modifier,
    view: JerseyView = JerseyView.FRONT,
    describe: Boolean = false,
    compact: Boolean = false
) {
    val context = LocalContext.current
    val design = jersey.design
    val template = design.template
    val colors = listOf(design.fabric, design.secondary, design.side, design.collar, design.armhole)
    val raster = JerseyRaster(template, view, compact, colors)
    var image by remember(context.resources, template, view, compact) {
        mutableStateOf<Pair<JerseyRaster, ImageBitmap>?>(null)
    }
    LaunchedEffect(context.resources, raster) {
        val rendered = withContext(Dispatchers.Default) {
            JerseyPhotoMaterial.render(context.resources, raster)
        }
        image = raster to rendered
    }

    val lettering = remember(context) {
        Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
            typeface = ResourcesCompat.getFont(context, R.font.barlow_condensed_extra_bold)
            strokeJoin = Paint.Join.ROUND
        }
    }
    val artwork = modifier.semantics {
        this[JerseyArtworkReady] = image?.first == raster
        if (describe) {
            contentDescription = listOfNotNull(
                "${view.label} of ${template.label} jersey", jersey.number?.let { "number $it" },
                jersey.name.takeIf { view == JerseyView.BACK && it.isNotBlank() }
            ).joinToString(", ")
        }
    }

    Canvas(artwork) {
        val art = image?.second ?: return@Canvas
        val factor = minOf(size.width / art.width, size.height / art.height)
        if (factor <= 0f) return@Canvas
        val width = (art.width * factor).roundToInt().coerceAtLeast(1)
        val height = (art.height * factor).roundToInt().coerceAtLeast(1)
        val x = ((size.width - width) / 2).roundToInt()
        val y = ((size.height - height) / 2).roundToInt()
        drawImage(art, dstOffset = IntOffset(x, y), dstSize = IntSize(width, height))

        drawIntoCanvas { target ->
            val canvas = target.nativeCanvas
            val front = view == JerseyView.FRONT
            fun mark(text: String, fraction: Float, scale: Float, limit: Float, ink: Int, arched: Boolean, border: Int?) {
                if (text.isBlank()) return
                lettering.textSize = height * scale
                val measured = lettering.measureText(text)
                if (measured > width * limit) lettering.textSize *= width * limit / measured
                val baseline = y + height * fraction
                val path = if (arched) Path().apply {
                    moveTo(x.toFloat(), baseline + height * .010f)
                    quadTo(x + width * .5f, baseline - height * .038f, x + width.toFloat(), baseline + height * .010f)
                } else null
                val lineLength = path?.let { PathMeasure(it, false).length } ?: width.toFloat()
                val margin = (lineLength - lettering.measureText(text)) / 2
                fun draw() {
                    if (path != null) canvas.drawTextOnPath(text, path, margin, 0f, lettering)
                    else canvas.drawText(text, x + margin, baseline, lettering)
                }
                val cutline = if (jerseyContrast(ink, design.fabric) < 4.5) jerseyPrintOutline(design.fabric) else border
                if (cutline != null) {
                    lettering.style = Paint.Style.STROKE
                    lettering.strokeWidth = (height * if (scale > .12f) .012f else .0045f).coerceAtLeast(1f)
                    lettering.color = cutline
                    draw()
                }
                lettering.style = Paint.Style.FILL
                lettering.color = ink
                draw()
            }
            if (!compact && front && width >= 76f) {
                mark("COURTQUEST", .328f, .103f, .71f, design.nameInk, true, design.side)
                mark("PRO", .371f, .052f, .4f, design.side, false, null)
            } else if (!front && !compact) {
                mark(jersey.name.uppercase(Locale.ROOT), .239f, .108f, .65f, design.nameInk, true, null)
            }
            jersey.number?.let {
                val baseline = when {
                    compact -> .532f
                    !front -> .503f
                    template == JerseyTemplate.FASTBREAK -> .565f
                    else -> .548f
                }
                val scale = if (front) .215f else .267f
                mark(it, baseline, scale, if (front) .46f else .56f, design.numberInk, false,
                    if (compact) null else design.side)
            }
        }
    }
}
