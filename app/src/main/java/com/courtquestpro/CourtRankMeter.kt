package com.courtquestpro

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

internal data class CourtRankWindow(val minimum: Int, val next: Int?) {
    fun fraction(xp: Int): Float = next?.let { ((xp - minimum).toFloat() / (it - minimum)).coerceIn(0f, 1f) } ?: 1f
}

/** Mirrors the existing participation rank thresholds; does not award or change XP. */
internal fun courtRankWindow(xp: Int): CourtRankWindow {
    val thresholds = listOf(0, 300, 800, 1500, 2500, 4000, 6000)
    return CourtRankWindow(thresholds.last { it <= xp.coerceAtLeast(0) }, thresholds.firstOrNull { it > xp })
}

@Composable
internal fun CourtRankMeter(xp: Int, modifier: Modifier = Modifier) {
    val window = courtRankWindow(xp)
    val caption = window.next?.let { "${it - xp.coerceAtLeast(0)} XP to ${rankFor(it)}" } ?: "Legend · Highest participation rank"
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(5.dp)) {
        LinearProgressIndicator(progress = { window.fraction(xp) },
            modifier = Modifier.fillMaxWidth().height(4.dp).semantics { contentDescription = caption },
            color = CourtQuestDesign.accentVivid, trackColor = CourtQuestDesign.border)
        Text(caption, color = CourtQuestDesign.textSecondary, style = MaterialTheme.typography.labelSmall)
    }
}
