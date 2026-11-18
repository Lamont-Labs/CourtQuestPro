package com.courtquestpro

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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

/** Full-width premium guidance panel in Scaffold's reserved area; no content obstruction. */
@Composable
internal fun CourtNavigationTour(step: Int, onNext: () -> Unit, onSkip: () -> Unit, onBack: () -> Unit) {
    val index = step.coerceIn(SoloNavigationTour.tabs.indices)
    val tab = SoloNavigationTour.tabs[index]
    val cue = SoloNavigationTour.cues[index]
    val shape = RoundedCornerShape(20.dp)
    Column(
        Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 5.dp)
            .testTag("navigation-tour")
            .background(Color(0xD91A1A1D), shape)
            .border(1.dp, CourtQuestDesign.accentVivid.copy(alpha = .65f), shape)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("FIELD GUIDE", color = CourtQuestDesign.accent,
                style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f))
            Text("${index + 1} / ${SoloNavigationTour.tabs.size}",
                color = CourtQuestDesign.textSecondary, style = MaterialTheme.typography.labelLarge)
            IconButton(onClick = onSkip, modifier = Modifier.size(40.dp).testTag("tour-skip")) {
                Icon(Icons.Default.Close, "Skip walkthrough", tint = CourtQuestDesign.textSecondary)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            SoloNavigationTour.tabs.indices.forEach { i ->
                Box(Modifier.weight(1f).height(4.dp)
                    .background(if (i <= index) CourtQuestDesign.accentVivid else CourtQuestDesign.border,
                        RoundedCornerShape(6.dp)))
            }
        }
        Column(Modifier.fillMaxWidth().testTag("tour-current-step")
            .semantics {
                liveRegion = LiveRegionMode.Polite
                contentDescription = "Step ${index + 1} of ${SoloNavigationTour.tabs.size}: ${tab.label}. $cue"
            }, verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(tab.label, color = CourtQuestDesign.textPrimary,
                style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
            Text(cue, color = CourtQuestDesign.textSecondary,
                style = MaterialTheme.typography.bodyMedium)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically) {
            if (index > 0) {
                OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f).heightIn(min = 46.dp).testTag("tour-back")) {
                    Text("Back", maxLines = 1, softWrap = false)
                }
            } else {
                TextButton(onClick = onSkip, modifier = Modifier.weight(1f).heightIn(min = 46.dp)) {
                    Text("Skip tour")
                }
            }
            Button(onClick = onNext, modifier = Modifier.weight(1.5f).heightIn(min = 46.dp).testTag("tour-next"),
                colors = ButtonDefaults.buttonColors(containerColor = CourtQuestDesign.accentVivid)) {
                Text(if (index == SoloNavigationTour.tabs.lastIndex) "Finish" else "Next",
                    fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(6.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, null)
            }
        }
    }
}
