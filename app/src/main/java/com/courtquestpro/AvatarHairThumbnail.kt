package com.courtquestpro

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

private object AvatarHairThumbnailCache {
    private val images=object:LruCache<AvatarHair,Bitmap>(1*1024*1024) {
        override fun sizeOf(key:AvatarHair,value:Bitmap)=value.allocationByteCount
    }
    fun cached(hair:AvatarHair)=images.get(hair)
    suspend fun load(context:android.content.Context,hair:AvatarHair)=withContext(Dispatchers.IO) {
        images.get(hair)?:context.assets.open(
            "player_cards/appearance-thumbnails/hair-${hair.name.lowercase(Locale.ROOT)}.webp"
        ).use { requireNotNull(BitmapFactory.decodeStream(it)).apply { prepareToDraw() } }
            .also { images.put(hair,it) }
    }
}

@Composable
internal fun AvatarHairThumbnail(hair:AvatarHair) {
    val context=LocalContext.current
    var bitmap by remember(hair) { mutableStateOf(AvatarHairThumbnailCache.cached(hair)) }
    LaunchedEffect(hair) { bitmap=AvatarHairThumbnailCache.load(context,hair) }
    bitmap?.let { Image(it.asImageBitmap(),null,Modifier.fillMaxSize(),contentScale=ContentScale.Fit) }
}
