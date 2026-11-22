package com.courtquestpro

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ColorMatrixColorFilter
import android.graphics.RectF
import android.util.LruCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.DataInputStream
import java.util.Locale

/** Lossless RGB masks hold the photographed diffuse light of three independently dyed yarns. */
internal data class TextileLayer(val image: Bitmap, val bounds: RectF)
internal data class JerseyPlate(val width: Int, val height: Int, val layers: List<TextileLayer>) {
    val byteCount = layers.sumOf { it.image.allocationByteCount }
}

internal object JerseyMaterials {
    private data class Key(val style: JerseyTemplate, val side: JerseyView, val sample: Int)
    private val cache = object : LruCache<Key, JerseyPlate>(24 * 1024 * 1024) {
        override fun sizeOf(key: Key, value: JerseyPlate) = value.byteCount
    }
    private val decoder = Mutex()

    fun cached(style: JerseyTemplate, side: JerseyView, sample: Int) = cache.get(Key(style, side, sample))

    suspend fun load(assets: AssetManager, style: JerseyTemplate, side: JerseyView, sample: Int): JerseyPlate =
        withContext(Dispatchers.IO) {
            // One decode at a time bounds transient memory and coalesces requests from multiple surfaces.
            decoder.withLock {
                val key = Key(style, side, sample)
                cache.get(key) ?: decode(assets, key).also { cache.put(key, it) }
            }
        }

    private fun decode(assets: AssetManager, key: Key): JerseyPlate {
        val file = "jerseys/${key.style.name.lowercase(Locale.ROOT)}_${key.side.name.lowercase(Locale.ROOT)}.jmat"
        return DataInputStream(assets.open(file).buffered()).use { input ->
            require(input.readInt() == 0x43514A31)
            val width = input.readUnsignedShort()
            val height = input.readUnsignedShort()
            require(width in 1..2048 && height in 1..2048)
            val options = BitmapFactory.Options().apply { inSampleSize = key.sample; inScaled = false }
            val layers = List(3) {
                val x = input.readUnsignedShort(); val y = input.readUnsignedShort()
                val w = input.readUnsignedShort(); val h = input.readUnsignedShort()
                require(w > 0 && h > 0 && x + w <= width && y + h <= height)
                val length = input.readInt(); require(length in 1..4_194_304)
                val bytes = ByteArray(length).also(input::readFully)
                val bitmap = requireNotNull(BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options))
                bitmap.prepareToDraw()
                TextileLayer(bitmap, RectF(x.toFloat(), y.toFloat(), (x + w).toFloat(), (y + h).toFloat()))
            }
            require(input.read() == -1)
            JerseyPlate(width, height, layers)
        }
    }
}

/** Maps each source channel to one user-chosen color. All five dyes change without bitmap work. */
internal fun textileDyes(a: Int, b: Int, c: Int = 0): ColorMatrixColorFilter {
    fun component(color: Int, shift: Int) = ((color ushr shift) and 255) / 255f
    val matrix = FloatArray(20)
    for (row in 0..2) {
        val shift = 16 - 8 * row
        matrix[row * 5] = component(a, shift)
        matrix[row * 5 + 1] = component(b, shift)
        matrix[row * 5 + 2] = component(c, shift)
    }
    matrix[18] = 1f
    return ColorMatrixColorFilter(matrix)
}
