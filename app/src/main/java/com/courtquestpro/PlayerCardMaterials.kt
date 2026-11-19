package com.courtquestpro

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.DataInputStream
import java.util.Locale

internal data class PlayerCardPlate(val images: List<Bitmap>) {
    val bytes get() = images.sumOf { it.allocationByteCount }
}

/** Bounded decoded-plate cache; decoding is serialized and never on the UI thread. */
internal object PlayerCardMaterials {
    private data class Key(val pose: PlayerCardPose, val sample: Int)
    private val cache = object : LruCache<Key, PlayerCardPlate>(48 * 1024 * 1024) {
        override fun sizeOf(key: Key, value: PlayerCardPlate) = value.bytes
    }
    private val lock = Mutex()
    fun cached(pose: PlayerCardPose, sample: Int) = cache.get(Key(pose, sample))
    suspend fun load(assets: AssetManager, pose: PlayerCardPose, sample: Int): PlayerCardPlate =
        withContext(Dispatchers.IO) {
            lock.withLock {
                val key = Key(pose, sample)
                cache.get(key) ?: decode(assets, key).also { cache.put(key, it) }
            }
        }

    private fun decode(assets: AssetManager, key: Key): PlayerCardPlate {
        val path = "player_cards/${key.pose.name.lowercase(Locale.ROOT)}.pcmat"
        return DataInputStream(assets.open(path).buffered()).use { input ->
            require(input.readInt() == 0x43515032)
            require(input.readUnsignedShort() == 1024 && input.readUnsignedShort() == 1024)
            val count = input.readUnsignedByte()
            require(count == 11)
            val options = BitmapFactory.Options().apply { inSampleSize = key.sample; inScaled = false }
            val images = List(count) {
                val length = input.readInt()
                require(length in 1..5_000_000)
                val bytes = ByteArray(length).also(input::readFully)
                requireNotNull(BitmapFactory.decodeByteArray(bytes, 0, length, options)).apply { prepareToDraw() }
            }
            require(input.read() == -1)
            val avatar=DataInputStream(assets.open("player_cards/${key.pose.name.lowercase(Locale.ROOT)}-avatar.pcmat").buffered()).use { extra ->
                require(extra.readInt()==0x43515032)
                require(extra.readUnsignedShort()==1024 && extra.readUnsignedShort()==1024 && extra.readUnsignedByte()==2)
                List(2) {
                    val size=extra.readInt();require(size in 1..5_000_000)
                    val data=ByteArray(size).also(extra::readFully)
                    requireNotNull(BitmapFactory.decodeByteArray(data,0,size,options)).apply {prepareToDraw()}
                }.also { require(extra.read()==-1) }
            }
            PlayerCardPlate(images+avatar)
        }
    }
}
