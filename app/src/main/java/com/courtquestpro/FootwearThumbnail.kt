package com.courtquestpro

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

private val footwearThumbs=object:LruCache<AvatarFootwear,Bitmap>(2*1024*1024) {
    override fun sizeOf(key:AvatarFootwear,value:Bitmap)=value.allocationByteCount
}
@Composable
internal fun FootwearThumbnail(model:AvatarFootwear) {
    val context=LocalContext.current
    var bitmap by remember(model){mutableStateOf(footwearThumbs.get(model))}
    LaunchedEffect(model) {
        bitmap=withContext(Dispatchers.IO) {
            footwearThumbs.get(model)?:context.assets.open("player_cards/footwear-${model.name.lowercase(Locale.ROOT)}.webp").use{
                requireNotNull(BitmapFactory.decodeStream(it))
            }.also{footwearThumbs.put(model,it)}
        }
    }
    bitmap?.let{Image(it.asImageBitmap(),null,Modifier.size(54.dp))}
}
