package com.courtquestpro

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Drill-specific text cues paired with the bundled illustration, never a generic pose drawing. */
@Composable
internal fun DrillFormReference(drill: Drill) {
    val coaching = DrillCoachingLibrary.forDrill(drill) ?: return
    val context = LocalContext.current.applicationContext
    val artwork by produceState<DrillArtworkManifestEntry?>(null, context, drill.id) {
        value = withContext(Dispatchers.IO) { drillArtworkEntry(context, drill.id) }
    }
    val hasComparison = artwork?.comparison?.included == true
    var showCorrection by rememberSaveable(drill.id) { mutableStateOf(false) }

    CourtQuestPremiumSurface(Modifier.fillMaxWidth(), androidx.compose.foundation.layout.PaddingValues(16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("DRILL-SPECIFIC COACHING",
                color = CourtQuestDesign.accentVivid,
                style = MaterialTheme.typography.labelMedium)

            if (hasComparison) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = !showCorrection,
                        onClick = { showCorrection = false },
                        label = { Text("Technique cues") },
                        modifier = Modifier.heightIn(min = 48.dp)
                    )
                    FilterChip(
                        selected = showCorrection,
                        onClick = { showCorrection = true },
                        label = { Text("Common mistake") },
                        modifier = Modifier.heightIn(min = 48.dp)
                    )
                }
            }

            if (showCorrection && hasComparison) {
                Text("COMMON MISTAKE & FIX",
                    color = CourtQuestDesign.accentVivid,
                    style = MaterialTheme.typography.labelMedium)
                artwork?.comparison?.note?.let {
                    Text(it, color = CourtQuestDesign.textPrimary,
                        style = MaterialTheme.typography.bodyLarge)
                }
                CoachingCue("CORRECTION", coaching.correction)
            } else {
                CoachingCue("1 · SETUP", coaching.setup)
                CoachingCue("2 · MOVEMENT", coaching.movement)
                CoachingCue("3 · TECHNIQUE CHECK", coaching.checkpoint)
                if (!hasComparison) {
                    CoachingCue("CORRECTION", coaching.correction)
                    Text(
                        artwork?.comparison?.reason
                            ?: "No safe, drill-specific comparison image is available; follow the written correction cue.",
                        color = CourtQuestDesign.textSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
private fun CoachingCue(label: String, text: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label,
            color = CourtQuestDesign.accent,
            style = MaterialTheme.typography.labelSmall)
        Text(text,
            color = CourtQuestDesign.textPrimary,
            style = MaterialTheme.typography.bodyMedium)
    }
}
