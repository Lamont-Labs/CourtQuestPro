package com.courtquestpro

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/** A contextual hint in Scaffold's reserved bottom area, never an overlay on content. */
@Composable
internal fun CourtNavigationTour(step: Int, onNext: () -> Unit, onSkip: () -> Unit, onBack: () -> Unit) {
    val index = step.coerceIn(SoloNavigationTour.tabs.indices)
    val tab = SoloNavigationTour.tabs[index]
    val cue = SoloNavigationTour.cues[index]
    val shape = RoundedCornerShape(14.dp)
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp)
            .testTag("navigation-tour")
            .background(CourtQuestDesign.elevatedSurface, shape)
            .border(1.dp, CourtQuestDesign.accentVivid.copy(alpha = .35f), shape)
            .padding(start = 10.dp, end = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (index > 0) IconButton(onBack, Modifier.size(48.dp).testTag("tour-back")) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = CourtQuestDesign.textSecondary)
        }
        Column(Modifier.weight(1f).padding(vertical = 7.dp).testTag("tour-current-step")
            .semantics {
                liveRegion = LiveRegionMode.Polite
                contentDescription = "Step ${index + 1} of 5: ${tab.label}. $cue"
            }) {
            Text("${index + 1}/5 · ${tab.label}", color = CourtQuestDesign.accent,
                style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            Text(cue, color = CourtQuestDesign.textPrimary,
                style = MaterialTheme.typography.bodySmall)
        }
        IconButton(onNext, Modifier.size(48.dp).testTag("tour-next")) {
            Icon(if (index == 4) Icons.Default.Check else Icons.AutoMirrored.Filled.ArrowForward,
                if (index == 4) "Done" else "Next", tint = CourtQuestDesign.accentVivid)
        }
        IconButton(onSkip, Modifier.size(48.dp).testTag("tour-skip")) {
            Icon(Icons.Default.Close, "Skip", tint = CourtQuestDesign.textSecondary)
        }
    }
}
