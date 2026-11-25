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

internal enum class CardArtworkFace(val suffix: String) {
    FRONT("front"), BACK("back")
}

internal const val PLAYER_CARD_ASPECT_RATIO=1060f/1484f

private data class CardArtworkKey(
    val style: PlayerCardStyle,
    val face: CardArtworkFace,
    val thumbnail: Boolean
)

internal object CardSurfaceCache {
    private val images=object:LruCache<CardArtworkKey,Bitmap>(40*1024*1024) {
        override fun sizeOf(key:CardArtworkKey,value:Bitmap)=value.allocationByteCount
    }
    fun cached(style:PlayerCardStyle,face:CardArtworkFace,thumbnail:Boolean)=
        images.get(CardArtworkKey(style,face,thumbnail))

    suspend fun load(
        assets:AssetManager,
        style:PlayerCardStyle,
        face:CardArtworkFace,
        thumbnail:Boolean
    ):Bitmap=withContext(Dispatchers.IO) {
        val key=CardArtworkKey(style,face,thumbnail)
        images.get(key)?:assets.open(
            "player_cards/owner_editions/${style.artworkId}-${face.suffix}.webp"
        ).use { stream ->
            val options=BitmapFactory.Options().apply { inSampleSize=if(thumbnail)4 else 1 }
            requireNotNull(BitmapFactory.decodeStream(stream,null,options)) {
                "Could not decode ${style.label} ${face.suffix} artwork"
            }.apply {prepareToDraw()}
        }.also {images.put(key,it)}
    }
}

@Composable
internal fun CardSurface(
    style:PlayerCardStyle,
    face:CardArtworkFace=CardArtworkFace.FRONT,
    thumbnail:Boolean=false
) {
    val context=LocalContext.current
    var bitmap by remember(style,face,thumbnail) {
        mutableStateOf(CardSurfaceCache.cached(style,face,thumbnail))
    }
    LaunchedEffect(style,face,thumbnail) {
        try {
            bitmap=CardSurfaceCache.load(context.assets,style,face,thumbnail)
            // Warm the opposite face while the current face is visible.
            if(!thumbnail) {
                val opposite=if(face==CardArtworkFace.FRONT) CardArtworkFace.BACK else CardArtworkFace.FRONT
                CardSurfaceCache.load(context.assets,style,opposite,false)
            }
        }
        catch(error:IOException) {
            throw IOException("Missing bundled ${style.label} ${face.suffix} artwork",error)
        }
    }
    val tag=if(face==CardArtworkFace.FRONT) "card-surface-${style.name.lowercase(Locale.ROOT)}-ready"
        else "card-back-surface-${style.name.lowercase(Locale.ROOT)}-ready"
    val preview=remember(style,face,thumbnail) {
        if(thumbnail) null else CardSurfaceCache.cached(style,face,true)
    }
    val displayed=bitmap ?: preview
    displayed?.let {Image(it.asImageBitmap(),null,Modifier.fillMaxSize()
        .then(if(bitmap!=null) Modifier.testTag(tag) else Modifier),contentScale=ContentScale.FillBounds)}
}
