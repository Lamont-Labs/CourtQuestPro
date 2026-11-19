package com.courtquestpro

import android.content.res.Resources
import android.graphics.Bitmap
import android.util.LruCache
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import java.io.DataInputStream
import java.util.zip.InflaterInputStream

internal data class JerseyRaster(
    val template: JerseyTemplate,
    val view: JerseyView,
    val compact: Boolean,
    val inks: List<Int>
) {
    val resource: Int
        get() = when (template) {
            JerseyTemplate.ARENA -> if (view == JerseyView.FRONT) R.raw.jersey_arena_front else R.raw.jersey_arena_back
            JerseyTemplate.CLASSIC -> if (view == JerseyView.FRONT) R.raw.jersey_classic_front else R.raw.jersey_classic_back
            JerseyTemplate.FASTBREAK -> if (view == JerseyView.FRONT) R.raw.jersey_fastbreak_front else R.raw.jersey_fastbreak_back
            JerseyTemplate.RETRO -> if (view == JerseyView.FRONT) R.raw.jersey_retro_front else R.raw.jersey_retro_back
        }
}

/** Alpha, textile-region index and photographed luminance stay independent of the chosen inks. */
internal class JerseyPhotoMaterial private constructor(
    private val width: Int,
    private val height: Int,
    private val samples: ByteArray
) {
    private suspend fun draw(raster: JerseyRaster): ImageBitmap {
        val midtones = if (raster.template == JerseyTemplate.ARENA) arenaMidtones else standardMidtones
        val tint = Array(raster.inks.size) { zone ->
            IntArray(256) { light -> arenaRelight(raster.inks[zone], midtones[zone], light) }
        }
        val step = if (raster.compact) 3 else 1
        val columns = (width + step - 1) / step
        val rows = (height + step - 1) / step
        val pixels = IntArray(columns * rows)
        val job = currentCoroutineContext()
        for (row in 0 until rows) {
            job.ensureActive()
            for (column in 0 until columns) {
                val index = (row * step * width + column * step) * 3
                val opacity = samples[index + 2].toInt() and 255
                if (opacity != 0) {
                    val light = samples[index].toInt() and 255
                    val material = samples[index + 1].toInt() and 255
                    pixels[row * columns + column] = (opacity shl 24) or tint[material][light]
                }
            }
        }
        return Bitmap.createBitmap(pixels, columns, rows, Bitmap.Config.ARGB_8888).asImageBitmap()
    }

    companion object {
        private val arenaMidtones = intArrayOf(34, 225, 128, 142, 127)
        private val standardMidtones = IntArray(5) { 128 }
        private val atlases = object : LruCache<Int, JerseyPhotoMaterial>(6 * 1024 * 1024) {
            override fun sizeOf(key: Int, value: JerseyPhotoMaterial) = value.samples.size
        }
        private val renders = object : LruCache<JerseyRaster, ImageBitmap>(8 * 1024 * 1024) {
            override fun sizeOf(key: JerseyRaster, value: ImageBitmap) = value.width * value.height * 4
        }

        suspend fun render(resources: Resources, raster: JerseyRaster): ImageBitmap {
            renders.get(raster)?.let { return it }
            val atlas = atlases.get(raster.resource) ?: load(resources, raster.resource).also {
                atlases.put(raster.resource, it)
            }
            return atlas.draw(raster).also { renders.put(raster, it) }
        }

        private fun load(resources: Resources, resource: Int): JerseyPhotoMaterial =
            DataInputStream(resources.openRawResource(resource).buffered()).use { source ->
                val width = source.readUnsignedShort()
                val height = source.readUnsignedShort()
                require(width in 1..550 && height in 1..900)
                val samples = ByteArray(width * height * 3)
                DataInputStream(InflaterInputStream(source)).use { material -> material.readFully(samples) }
                JerseyPhotoMaterial(width, height, samples)
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
