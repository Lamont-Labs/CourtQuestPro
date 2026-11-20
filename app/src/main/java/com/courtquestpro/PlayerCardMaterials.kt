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

internal data class PlayerCardPlate(val images:List<Bitmap>,val head:Bitmap?,val clearedHeadBase:Bitmap?) {
    val bytes get()=images.sumOf{it.allocationByteCount}+(head?.allocationByteCount?:0)+(clearedHeadBase?.allocationByteCount?:0)
}
private data class PlayerCardBase(val images:List<Bitmap>) {
    val bytes get()=images.sumOf{it.allocationByteCount}
}

/** Bounded decoded-plate cache; decoding is serialized and never on the UI thread. */
internal object PlayerCardMaterials {
    private data class Key(val pose:PlayerCardPose,val sample:Int,val template:Int)
    private data class HeadKey(val pose:PlayerCardPose,val sample:Int,val hair:AvatarHair)
    private data class ClearKey(val pose:PlayerCardPose,val sample:Int)
    private val bases = object : LruCache<Key,PlayerCardBase>(32*1024*1024) {
        override fun sizeOf(key:Key,value:PlayerCardBase)=value.bytes
    }
    private val heads = object : LruCache<HeadKey,Bitmap>(8*1024*1024) {
        override fun sizeOf(key:HeadKey,value:Bitmap)=value.allocationByteCount
    }
    private val clearedBases = object : LruCache<ClearKey,Bitmap>(8*1024*1024) {
        override fun sizeOf(key:ClearKey,value:Bitmap)=value.allocationByteCount
    }
    private val lock = Mutex()
    @Volatile var largestPlateBytes:Int=0
        private set
    val cacheBytes get()=bases.size()+heads.size()+clearedBases.size()
    val headCacheBytes get()=heads.size()
    val clearedBaseCacheBytes get()=clearedBases.size()
    fun cached(pose:PlayerCardPose,sample:Int,template:Int,hair:AvatarHair):PlayerCardPlate? {
        val base=bases.get(Key(pose,sample,template))?:return null
        if(hair==AvatarHair.ORIGINAL)return PlayerCardPlate(base.images,null,null)
        val head=heads.get(HeadKey(pose,sample,hair))?:return null
        val clear=clearedBases.get(ClearKey(pose,sample))?:return null
        return PlayerCardPlate(base.images,head,clear)
    }
    suspend fun load(assets: AssetManager, pose: PlayerCardPose, sample: Int,template:Int,hair:AvatarHair): PlayerCardPlate =
        withContext(Dispatchers.IO) {
            lock.withLock {
                val key=Key(pose,sample,template)
                val base=bases.get(key)?:decodeBase(assets,key).also{bases.put(key,it)}
                val head=if(hair==AvatarHair.ORIGINAL)null else {
                    val headKey=HeadKey(pose,sample,hair)
                    heads.get(headKey)?:decodeHead(assets,headKey).also{heads.put(headKey,it)}
                }
                val clear=if(hair==AvatarHair.ORIGINAL)null else {
                    val clearKey=ClearKey(pose,sample)
                    clearedBases.get(clearKey)?:decodeClearedHead(assets,clearKey).also{clearedBases.put(clearKey,it)}
                }
                PlayerCardPlate(base.images,head,clear).also{largestPlateBytes=maxOf(largestPlateBytes,it.bytes)}
            }
        }

    private fun decodeBase(assets:AssetManager,key:Key):PlayerCardBase {
        val path = "player_cards/${key.pose.name.lowercase(Locale.ROOT)}.pcmat"
        return DataInputStream(assets.open(path).buffered()).use { input ->
            require(input.readInt() == 0x43515032)
            require(input.readUnsignedShort() == 1024 && input.readUnsignedShort() == 1024)
            val count = input.readUnsignedByte()
            require(count == 11)
            val options = BitmapFactory.Options().apply { inSampleSize = key.sample; inScaled = false }
            val unusedOptions=BitmapFactory.Options().apply {inSampleSize=4;inScaled=false}
            val images = List(count) {index->
                val length = input.readInt()
                require(length in 1..5_000_000)
                val bytes = ByteArray(length).also(input::readFully)
                // Only the worn template needs detail-resolution maps. Re-key on template changes.
                val detail=if(index in 2..9 && (index-2)/2!=key.template)unusedOptions else options
                requireNotNull(BitmapFactory.decodeByteArray(bytes, 0, length, detail)).apply { prepareToDraw() }
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
            val footwear=DataInputStream(assets.open("player_cards/${key.pose.name.lowercase(Locale.ROOT)}-footwear.pcmat").buffered()).use { extra ->
                require(extra.readInt()==0x43515032)
                require(extra.readUnsignedShort()==1024 && extra.readUnsignedShort()==1024 && extra.readUnsignedByte()==4)
                List(4) {
                    val size=extra.readInt();require(size in 1..5_000_000)
                    val data=ByteArray(size).also(extra::readFully)
                    requireNotNull(BitmapFactory.decodeByteArray(data,0,size,options)).apply {prepareToDraw()}
                }.also {require(extra.read()==-1)}
            }
            PlayerCardBase(images+avatar+footwear)
        }
    }

    private fun decodeHead(assets:AssetManager,key:HeadKey):Bitmap {
        val name="${key.pose.name.lowercase(Locale.ROOT)}-${key.hair.name.lowercase(Locale.ROOT)}.webp"
        val options=BitmapFactory.Options().apply{inSampleSize=key.sample;inScaled=false}
        return assets.open("player_cards/heads/$name").use {
            requireNotNull(BitmapFactory.decodeStream(it,null,options)).apply{prepareToDraw()}
        }
    }

    private fun decodeClearedHead(assets:AssetManager,key:ClearKey):Bitmap {
        val name="cleared-${key.pose.name.lowercase(Locale.ROOT)}.webp"
        val options=BitmapFactory.Options().apply{inSampleSize=key.sample;inScaled=false}
        return assets.open("player_cards/heads/$name").use {
            requireNotNull(BitmapFactory.decodeStream(it,null,options)).apply{prepareToDraw()}
        }
    }
}
