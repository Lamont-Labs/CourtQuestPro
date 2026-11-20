package com.courtquestpro

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.util.LruCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.DataInputStream
import java.util.Locale
import java.util.zip.ZipInputStream

internal data class RegisteredPatch(val bitmap: Bitmap, val bounds: RectF)
internal data class PlayerCardPlate(
    val images: List<Bitmap>, val hair: RegisteredPatch, val shoe: RegisteredPatch
) {
    val bytes get() = images.sumOf { it.allocationByteCount } + hair.bitmap.allocationByteCount + shoe.bitmap.allocationByteCount
}

internal object PlayerCardMaterials {
    private data class BaseKey(val pose: PlayerCardPose, val sample: Int, val template: Int)
    private data class PartKey(val pose: PlayerCardPose, val sample: Int, val name: String)
    private class Bundle(val entries: Map<String, ByteArray>, val bounds: JSONObject) {
        fun patch(name: String, sample: Int): RegisteredPatch {
            val bytes = entries.getValue("$name.webp")
            val box = bounds.getJSONArray(name)
            return RegisteredPatch(decode(bytes, sample), RectF(box.getInt(0).toFloat(), box.getInt(1).toFloat(),
                box.getInt(2).toFloat(), box.getInt(3).toFloat()))
        }
    }
    private val bundles = mutableMapOf<PlayerCardPose, Bundle>()
    private val bases = object : LruCache<BaseKey, List<Bitmap>>(32 * 1024 * 1024) {
        override fun sizeOf(key: BaseKey, value: List<Bitmap>) = value.sumOf { it.allocationByteCount }
    }
    private val patches = object : LruCache<PartKey, RegisteredPatch>(12 * 1024 * 1024) {
        override fun sizeOf(key: PartKey, value: RegisteredPatch) = value.bitmap.allocationByteCount
    }
    private val lock = Mutex()
    @Volatile var largestPlateBytes = 0
        private set
    val cacheBytes get() = bases.size() + patches.size()
    val patchCacheBytes get() = patches.size()

    private fun hairKey(pose: PlayerCardPose, sample: Int, hair: AvatarHair) =
        PartKey(pose, sample, "hair-${hair.name.lowercase(Locale.ROOT)}")
    private fun shoeKey(pose: PlayerCardPose, sample: Int, shoe: AvatarFootwear) =
        PartKey(pose, sample, "shoe-${shoe.name.lowercase(Locale.ROOT)}")

    fun cached(pose: PlayerCardPose, sample: Int, template: Int, hair: AvatarHair, shoe: AvatarFootwear): PlayerCardPlate? {
        val base = bases.get(BaseKey(pose, sample, template)) ?: return null
        val hairPatch = patches.get(hairKey(pose, sample, hair)) ?: return null
        val shoePatch = patches.get(shoeKey(pose, sample, shoe)) ?: return null
        return PlayerCardPlate(base, hairPatch, shoePatch)
    }
    suspend fun load(assets: AssetManager, pose: PlayerCardPose, sample: Int, template: Int, hair: AvatarHair, shoe: AvatarFootwear) =
        withContext(Dispatchers.IO) {
            lock.withLock {
                val bundle = bundles.getOrPut(pose) {
                    val entries = mutableMapOf<String, ByteArray>()
                    ZipInputStream(assets.open("player_cards/anatomy/${pose.name.lowercase(Locale.ROOT)}.zip").buffered()).use { zip ->
                        while (true) {
                            val entry = zip.nextEntry ?: break
                            entries[entry.name] = zip.readBytes()
                        }
                    }
                    Bundle(entries, JSONObject(entries.getValue("bounds.json").decodeToString()))
                }
                fun part(key: PartKey) = patches.get(key) ?: bundle.patch(key.name, sample).also { patches.put(key, it) }
                val key = BaseKey(pose, sample, template)
                val base = bases.get(key) ?: decodeBase(assets, key, bundle).also { bases.put(key, it) }
                PlayerCardPlate(base, part(hairKey(pose, sample, hair)), part(shoeKey(pose, sample, shoe))).also {
                    largestPlateBytes = maxOf(largestPlateBytes, it.bytes)
                }
            }
        }

    private fun decode(bytes: ByteArray, sample: Int): Bitmap = requireNotNull(
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, BitmapFactory.Options().apply {
            inSampleSize = sample
            inScaled = false
        })
    )

    private fun decodeBase(assets: AssetManager, key: BaseKey, bundle: Bundle): List<Bitmap> {
        val origin = "player_cards/${key.pose.name.lowercase(Locale.ROOT)}.pcmat"
        val layers = DataInputStream(assets.open(origin).buffered()).use { input ->
            require(input.readInt() == 0x43515032 && input.readUnsignedShort() == 1024 &&
                input.readUnsignedShort() == 1024 && input.readUnsignedByte() == 11)
            MutableList(11) { i ->
                val length = input.readInt()
                require(length in 1..5_000_000)
                val bytes = ByteArray(length).also(input::readFully)
                if (i == 1 || (i in 2..9 && (i-2)/2 != key.template)) Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
                else decode(bytes, key.sample)
            }.also { require(input.read() == -1) }
        }
        val body = layers[0].copy(Bitmap.Config.ARGB_8888, true)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        fun patch(canvas: Canvas, name: String, mode: PorterDuff.Mode? = null) {
            val part = bundle.patch(name, key.sample)
            val dest = RectF(part.bounds).apply {
                left /= key.sample; right /= key.sample; top /= key.sample; bottom /= key.sample
            }
            paint.xfermode = mode?.let(::PorterDuffXfermode)
            canvas.drawBitmap(part.bitmap, null, dest, paint)
            paint.xfermode = null
        }
        Canvas(body).apply { patch(this, "erase", PorterDuff.Mode.DST_OUT); patch(this, "scalp") }
        fun surface(name: String): Bitmap {
            val result = Bitmap.createBitmap(body.width, body.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(result)
            patch(canvas, name)
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
            canvas.drawBitmap(body, null, RectF(0f, 0f, result.width.toFloat(), result.height.toFloat()), paint)
            paint.xfermode = null
            return result
        }
        layers[0] = body
        layers[1] = surface("face")
        layers.add(surface("skin"))
        return layers
    }
}
