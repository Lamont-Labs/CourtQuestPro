package com.courtquestpro

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PathMeasure
import androidx.annotation.RawRes
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.core.content.res.ResourcesCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.DataInputStream
import java.util.Locale
import java.util.zip.InflaterInputStream
import kotlin.math.roundToInt

/** Matte photograph with photographed lighting and an independent textile material per pixel. */
private class ArenaJerseyMaterial private constructor(
    private val width: Int,
    private val height: Int,
    private val samples: ByteArray
) {
    fun render(inks: List<Int>): ImageBitmap {
        val tint = Array(inks.size) { zone ->
            IntArray(256) { light -> arenaRelight(inks[zone], linearExposures[zone], light) }
        }
        val pixels = IntArray(width * height)
        for (i in pixels.indices) {
            val offset = i * 3
            val opacity = samples[offset + 2].toInt() and 255
            if (opacity > 0) {
                val light = samples[offset].toInt() and 255
                val material = samples[offset + 1].toInt() and 255
                pixels[i] = (opacity shl 24) or tint[material][light]
            }
        }
        return Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888).asImageBitmap()
    }

    companion object {
        // Target ink is mapped to the midtone of its region in the photographed material.
        private val linearExposures = intArrayOf(34, 225, 128, 142, 127)
        private val atlasCache = arrayOfNulls<ArenaJerseyMaterial>(JerseyView.entries.size)

        @Synchronized
        fun load(resources: Resources, side: JerseyView): ArenaJerseyMaterial {
            atlasCache[side.ordinal]?.let { return it }
            @RawRes val resource = if (side == JerseyView.FRONT) R.raw.jersey_arena_front else R.raw.jersey_arena_back
            return DataInputStream(resources.openRawResource(resource).buffered()).use { source ->
                val width = source.readUnsignedShort()
                val height = source.readUnsignedShort()
                require(width in 1..460 && height in 1..840)
                val samples = ByteArray(width * height * 3)
                DataInputStream(InflaterInputStream(source)).use { material -> material.readFully(samples) }
                ArenaJerseyMaterial(width, height, samples).also { atlasCache[side.ordinal] = it }
            }
        }
    }
}

internal fun arenaRelight(ink: Int, midtone: Int, light: Int): Int {
    fun channel(shift: Int): Int {
        val base = (ink shr shift) and 255
        return if (light <= midtone) base * light / midtone
        else base + (255 - base) * (light - midtone) / (255 - midtone)
    }
    return (channel(16) shl 16) or (channel(8) shl 8) or channel(0)
}

@Composable
internal fun CourtArenaJerseyArtwork(jersey: PlayerJersey, modifier: Modifier, side: JerseyView) {
    val context = LocalContext.current
    val design = jersey.design
    val colors = listOf(design.fabric, design.secondary, design.side, design.collar, design.armhole)
    val image by produceState<ImageBitmap?>(null, context, side, colors) {
        value = withContext(Dispatchers.Default) {
            ArenaJerseyMaterial.load(context.resources, side).render(colors)
        }
    }
    val lettering = remember(context) {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = ResourcesCompat.getFont(context, R.font.barlow_condensed_extra_bold)
            strokeJoin = Paint.Join.ROUND
        }
    }
    Canvas(modifier) {
        val art = image ?: return@Canvas
        val factor = minOf(size.width / art.width, size.height / art.height)
        if (factor <= 0f) return@Canvas
        val width = (art.width * factor).roundToInt().coerceAtLeast(1)
        val height = (art.height * factor).roundToInt().coerceAtLeast(1)
        val x = ((size.width - width) / 2).roundToInt()
        val y = ((size.height - height) / 2).roundToInt()
        drawImage(art, dstOffset = IntOffset(x, y), dstSize = IntSize(width, height))

        drawIntoCanvas { target ->
            val canvas = target.nativeCanvas
            val center = Offset(x + width * .5f, y.toFloat())
            val front = side == JerseyView.FRONT
            fun mark(text: String, fraction: Float, scale: Float, limit: Float, ink: Int, arched: Boolean, border: Int?) {
                if (text.isBlank()) return
                lettering.textAlign = Paint.Align.LEFT
                lettering.textSize = height * scale
                val measured = lettering.measureText(text)
                if (measured > width * limit) lettering.textSize *= width * limit / measured
                val baseline = center.y + height * fraction
                val path = if (arched) Path().apply {
                    moveTo(x.toFloat(), baseline + height * .010f)
                    quadTo(center.x, baseline - height * .038f, x + width.toFloat(), baseline + height * .010f)
                } else null
                val length = path?.let { PathMeasure(it, false).length } ?: width.toFloat()
                val margin = (length - lettering.measureText(text)) / 2
                fun draw(dy: Float = 0f) {
                    if (path != null) canvas.drawTextOnPath(text, path, margin, dy, lettering)
                    else canvas.drawText(text, x + margin, baseline + dy, lettering)
                }
                if (border != null) {
                    lettering.style = Paint.Style.STROKE
                    lettering.strokeWidth = (height * if (scale > .12f) .012f else .0045f).coerceAtLeast(1f)
                    lettering.color = border
                    draw()
                }
                lettering.style = Paint.Style.FILL
                lettering.color = ink
                draw()
            }
            if (front && width >= 76f) {
                mark("COURTQUEST", .328f, .103f, .71f, design.nameInk, true, design.side)
                mark("PRO", .371f, .052f, .4f, design.side, false, null)
            } else if (!front) {
                mark(jersey.name.uppercase(Locale.ROOT), .239f, .108f, .65f, design.nameInk, true, null)
            }
            jersey.number?.let { mark(it, if (front) .548f else .503f, if (front) .215f else .267f,
                if (front) .46f else .56f, design.numberInk, false, design.side) }
        }
    }
}
