package com.courtquestpro

import android.content.res.AssetManager
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
import androidx.compose.ui.platform.testTag
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Locale

internal object CardSurfaceCache {
    private val images=object:LruCache<PlayerCardStyle,Bitmap>(12*1024*1024) {
        override fun sizeOf(key:PlayerCardStyle,value:Bitmap)=value.allocationByteCount
    }
    fun cached(style:PlayerCardStyle)=images.get(style)
    suspend fun load(assets:AssetManager,style:PlayerCardStyle):Bitmap=withContext(Dispatchers.IO) {
        images.get(style)?:assets.open("player_cards/surface-${style.name.lowercase(Locale.ROOT)}.webp").use {
            requireNotNull(BitmapFactory.decodeStream(it)).apply {prepareToDraw()}
        }.also {images.put(style,it)}
    }
}

@Composable
internal fun CardSurface(style:PlayerCardStyle) {
    val context=LocalContext.current
    var bitmap by remember(style) {mutableStateOf(CardSurfaceCache.cached(style))}
    LaunchedEffect(style) {
        try { bitmap=CardSurfaceCache.load(context.assets,style) }
        catch(error:IOException) {throw IOException("Missing bundled collectible material ${style.name}",error)}
    }
    bitmap?.let {Image(it.asImageBitmap(),null,Modifier.fillMaxSize()
        .testTag("card-surface-${style.name.lowercase()}-ready"),contentScale=ContentScale.FillBounds)}
}
