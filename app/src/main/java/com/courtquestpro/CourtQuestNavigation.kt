package com.courtquestpro

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color as AndroidColor
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val NAV_ATLAS_FILE = "courtquest_navigation_atlas.png"
private const val DEFAULT_JERSEY_NUMBER = 23
@Volatile private var cachedNavigationAtlas: ImageBitmap? = null
private val navigationAtlasLock = Any()

internal fun displayedJerseyNumber(value: Int?): Int = value?.coerceIn(0, 99) ?: DEFAULT_JERSEY_NUMBER

@Composable
internal fun CourtQuestNavigation(
    modifier: Modifier = Modifier,
    selected: PlayerTab,
    jerseyNumber: Int?,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onSelect: (PlayerTab) -> Unit
) {
    val atlas = rememberNavigationAtlas()
    BackHandler(enabled = expanded) { onExpandedChange(false) }

    Box(modifier.fillMaxSize().testTag("edge-navigation-host")) {
        if (expanded) {
            Box(
                Modifier.fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.04f))
                    .clickable(role = Role.Button, onClick = { onExpandedChange(false) })
                    .semantics { contentDescription = "Close navigation" }
                    .testTag("nav-outside-dismiss")
            )
        }
        AnimatedVisibility(
            visible = expanded,
            modifier = Modifier.align(Alignment.CenterStart).zIndex(1f)
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Vertical)),
            enter = slideInHorizontally(tween(200)) { -it } + fadeIn(tween(200)),
            exit = slideOutHorizontally(tween(200)) { -it } + fadeOut(tween(200))
        ) {
            Box(
                Modifier.width(108.dp).wrapContentHeight()
                    .pointerInput(expanded) {
                        var dragged = 0f
                        detectHorizontalDragGestures(
                            onHorizontalDrag = { change, delta ->
                                dragged += delta
                                if (dragged < -48.dp.toPx()) {
                                    onExpandedChange(false)
                                    dragged = 0f
                                }
                                change.consume()
                            },
                            onDragEnd = { dragged = 0f },
                            onDragCancel = { dragged = 0f }
                        )
                    }
                    .testTag("player-navigation-drawer")
            ) {
                val railShape = RoundedCornerShape(topEnd = 26.dp, bottomEnd = 26.dp)
                CourtQuestPremiumSurface(
                    Modifier.width(84.dp).wrapContentHeight()
                        .shadow(18.dp, railShape, spotColor = CourtQuestDesign.accentVivid.copy(alpha = 0.52f))
                        .clip(railShape)
                        .border(1.25.dp, CourtQuestDesign.accent.copy(alpha = 0.74f), railShape),
                    contentPadding = PaddingValues(horizontal = 3.dp, vertical = 6.dp),
                    blurred = true
                ) {
                    LazyColumn(
                        Modifier.fillMaxWidth().selectableGroup(),
                        contentPadding = PaddingValues(vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterVertically)
                    ) {
                        items(PlayerTab.entries, key = { it.route }) { tab ->
                            NavigationArtCard(
                                tab = tab,
                                selected = selected == tab,
                                jerseyNumber = displayedJerseyNumber(jerseyNumber),
                                atlas = atlas,
                                onClick = { onSelect(tab) }
                            )
                        }
                    }
                }
                Box(
                    Modifier.align(Alignment.CenterEnd).width(48.dp).height(72.dp)
                        .zIndex(2f)
                        .semantics { contentDescription = "Close navigation" }
                        .clickable(role = Role.Button, onClick = { onExpandedChange(false) })
                        .testTag("nav-close-handle"),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Box(
                        Modifier.width(23.dp).height(60.dp)
                            .shadow(14.dp, RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp),
                                spotColor = CourtQuestDesign.accentVivid)
                            .clip(RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp))
                            .background(Brush.verticalGradient(listOf(
                                CourtQuestDesign.accent, CourtQuestDesign.accentVivid, Color(0xFFE84D00)
                            )))
                            .border(1.dp, Color.White.copy(alpha = 0.5f),
                                RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.ChevronLeft, null, tint = CourtQuestDesign.iconForeground)
                    }
                }
            }
        }
        if (!expanded) {
            Box(
                Modifier.align(Alignment.CenterStart).width(48.dp).height(96.dp)
                    .zIndex(1f)
                    .semantics { contentDescription = "Open navigation" }
                    .clickable(role = Role.Button, onClick = { onExpandedChange(true) })
                    .testTag("nav-open-handle"),
                contentAlignment = Alignment.CenterStart
            ) {
                Box(
                    Modifier.width(27.dp).height(78.dp)
                        .shadow(16.dp, RoundedCornerShape(topEnd = 18.dp, bottomEnd = 18.dp),
                            spotColor = CourtQuestDesign.accentVivid.copy(alpha = 0.82f))
                        .clip(RoundedCornerShape(topEnd = 18.dp, bottomEnd = 18.dp))
                        .background(Brush.verticalGradient(listOf(
                            Color(0xFFFFA45F), CourtQuestDesign.accentVivid, Color(0xFFE94B00)
                        )))
                        .border(1.dp, Color.White.copy(alpha = 0.62f),
                            RoundedCornerShape(topEnd = 18.dp, bottomEnd = 18.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.ChevronRight, null, tint = CourtQuestDesign.iconForeground)
                }
            }
        }
    }
}

@Composable
private fun NavigationArtCard(
    tab: PlayerTab,
    selected: Boolean,
    jerseyNumber: Int,
    atlas: ImageBitmap?,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(17.dp)
    val label = tab.label
    Box(
        Modifier.fillMaxWidth().heightIn(min = 76.dp)
            .clip(shape)
            .background(if (selected) CourtQuestDesign.warmSurface.copy(alpha = 0.76f) else Color(0xB20B0C0E))
            .border(
                if (selected) 1.7.dp else 1.dp,
                if (selected) CourtQuestDesign.accentVivid else Color.White.copy(alpha = 0.26f),
                shape
            )
            .selectable(selected = selected, role = Role.Tab, onClick = onClick)
            .semantics { contentDescription = label }
            .testTag("nav-${tab.route}")
    ) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 2.dp, vertical = 3.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(Modifier.size(56.dp), contentAlignment = Alignment.Center) {
                if (atlas != null) NavigationSpriteArtwork(atlas, tab.ordinal, Modifier.fillMaxSize())
                if (tab == PlayerTab.ME) {
                    Text(
                        jerseyNumber.toString(),
                        Modifier.align(Alignment.Center).offset(y = (-6).dp),
                        color = CourtQuestDesign.accent,
                        fontFamily = CourtDisplay,
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        lineHeight = 14.sp,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
            }
            Text(
                tab.label,
                color = if (selected) CourtQuestDesign.textPrimary else Color(0xFFF5F1EC),
                fontFamily = CourtDisplay,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                lineHeight = 14.sp,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
    }
}

@Composable
internal fun CourtQuestJerseyArtwork(
    jerseyNumber: Int?,
    modifier: Modifier = Modifier
) {
    val atlas = rememberNavigationAtlas()
    Box(modifier, contentAlignment = Alignment.Center) {
        if (atlas != null) NavigationSpriteArtwork(atlas, PlayerTab.ME.ordinal, Modifier.fillMaxSize())
        Text(
            displayedJerseyNumber(jerseyNumber).toString(),
            Modifier.align(Alignment.Center).offset(y = (-8).dp),
            color = CourtQuestDesign.accent,
            fontFamily = CourtDisplay,
            fontWeight = FontWeight.Black,
            fontSize = 19.sp,
            lineHeight = 21.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun NavigationSpriteArtwork(atlas: ImageBitmap, cell: Int, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val cellLeft = atlas.width * cell / 5
        val cellRight = atlas.width * (cell + 1) / 5
        val cellSize = minOf(cellRight - cellLeft, 434)
        val cellTop = ((atlas.height - cellSize) / 2).coerceAtLeast(0)
        drawImage(
            image = atlas,
            srcOffset = IntOffset(cellLeft + ((cellRight - cellLeft - cellSize) / 2), cellTop),
            srcSize = IntSize(cellSize, cellSize),
            dstOffset = IntOffset.Zero,
            dstSize = IntSize(size.width.toInt().coerceAtLeast(1), size.height.toInt().coerceAtLeast(1))
        )
    }
}

@Composable
private fun rememberNavigationAtlas(): ImageBitmap? {
    val context = LocalContext.current.applicationContext
    var atlas by remember(context) { mutableStateOf(cachedNavigationAtlas) }
    LaunchedEffect(context) {
        val loaded = withContext(Dispatchers.IO) {
            runCatching {
                cachedNavigationAtlas ?: synchronized(navigationAtlasLock) {
                    cachedNavigationAtlas ?: context.assets.open(NAV_ATLAS_FILE).use { stream ->
                        BitmapFactory.decodeStream(stream)?.let(::removeNavigationTileBackdrops)?.asImageBitmap()
                    }.also { cachedNavigationAtlas = it }
                }
            }.getOrNull()
        }
        atlas = loaded
    }
    return atlas
}

/**
 * The approved atlas is RGB artwork with opaque black tile backgrounds.
 * Make near-black pixels transparent without replacing the illustrations.
 * Original packaged asset is left untouched. Processed once off the UI thread.
 */
private fun removeNavigationTileBackdrops(source: Bitmap): Bitmap {
    val width = source.width
    val height = source.height
    val pixels = IntArray(width * height)
    source.getPixels(pixels, 0, width, 0, 0, width, height)
    val cellWidth = width / 5
    for (cell in 0 until 5) {
        val left = cell * cellWidth
        val right = if (cell == 4) width else (cell + 1) * cellWidth
        for (y in 0 until height) {
            for (x in left until right) {
                val index = y * width + x
                val color = pixels[index]
                val red = AndroidColor.red(color)
                val green = AndroidColor.green(color)
                val blue = AndroidColor.blue(color)
                // The source has a nearly-black matte. Preserve bright art and
                // emissive glow while feathering the matte out to transparency.
                val strongest = maxOf(red, green, blue)
                val alpha = when {
                    strongest <= 12 -> 0
                    strongest >= 62 -> 255
                    else -> ((strongest - 12) * 255 / 50).coerceIn(0, 255)
                }
                pixels[index] = AndroidColor.argb(
                    AndroidColor.alpha(color) * alpha / 255, red, green, blue
                )
            }
        }
    }
    return Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888)
}
