package com.courtquestpro

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

/**
 * Uses bundled, offline WebP artwork when present. Missing assets are not errors:
 * the existing court and form diagrams remain available.
 */
@Composable
internal fun DrillArtworkReference(drill: Drill): Boolean {
    val filename = when (drill.id) {
        "b1" -> "crossover_dribble.webp"
        "s2" -> "catch_and_shoot.webp"
        "f1" -> "right_hand_layup.webp"
        "d1" -> "defensive_slide.webp"
        else -> null
    } ?: return false

    val context = LocalContext.current
    val image = remember(context, filename) {
        try {
            context.assets.open("drill_art/$filename").use { input ->
                BitmapFactory.decodeStream(input)?.asImageBitmap()
            }
        } catch (_: java.io.IOException) {
            null
        }
    } ?: return false

    CourtQuestPremiumSurface(Modifier.fillMaxWidth(), PaddingValues(12.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("ILLUSTRATED DRILL GUIDE",
                color = CourtQuestDesign.accentVivid,
                style = MaterialTheme.typography.labelMedium)
            Image(
                bitmap = image,
                contentDescription = "Illustrated coaching reference for ${drill.title}",
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.FillWidth
            )
            Text("Visual coaching concept. Follow the verified written instructions for technique.",
                color = CourtQuestDesign.textSecondary,
                style = MaterialTheme.typography.bodySmall)
        }
    }
    return true
}
