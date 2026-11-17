package com.courtquestpro

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Optional, locally persisted navigation introduction; never blocks normal app access. */
@Composable
internal fun CourtNavigationTour(
    step: Int,
    onNext: () -> Unit,
    onSkip: () -> Unit,
    onBack: () -> Unit,
) {
    val stops = listOf(
        Triple("TRAINING", "Find your drills", "Open Training to explore drills and start a practice session."),
        Triple("PROGRESS", "See your improvement", "Follow your training history and watch your progress build."),
        Triple("COURT", "Your basketball home", "Return to Court whenever you want your main dashboard."),
        Triple("QUESTS", "Build your momentum", "Explore challenges that make your practice more engaging."),
        Triple("PLAYER", "Make it yours", "Manage your player details and customize your jersey.")
    )
    val current = stops[step.coerceIn(stops.indices)]
    Column(
        Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag("navigation-tour")
            .background(Color(0xF7191513), RoundedCornerShape(14.dp))
            .border(1.dp, CourtQuestDesign.accentVivid.copy(alpha = .55f), RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("${step + 1}/${stops.size}", color = CourtQuestDesign.accentVivid,
                fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text(current.second, modifier = Modifier.weight(1f).testTag("tour-current-step")
                .semantics {
                    liveRegion = LiveRegionMode.Polite
                    contentDescription = "Step ${step + 1} of ${stops.size}: ${current.second}. ${current.third}"
                }, color = CourtQuestDesign.textPrimary, fontSize = 14.sp,
                fontWeight = FontWeight.Bold, maxLines = 1)
            TextButton(onClick = onSkip, contentPadding = PaddingValues(horizontal = 5.dp),
                modifier = Modifier.heightIn(min = 40.dp).testTag("tour-skip")) {
                Text("Skip", fontSize = 12.sp)
            }
        }
        Text(current.third, color = CourtQuestDesign.textSecondary,
            fontSize = 11.sp, lineHeight = 14.sp, maxLines = 2)
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.weight(1f).clearAndSetSemantics { },
                horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                stops.indices.forEach { index ->
                    Box(Modifier.weight(1f).height(3.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(if (index <= step) CourtQuestDesign.accentVivid else CourtQuestDesign.border))
                }
            }
            TextButton(onClick = onBack, enabled = step > 0,
                modifier = Modifier.heightIn(min = 40.dp).testTag("tour-back")) {
                Text("Back", fontSize = 12.sp)
            }
            Button(onClick = onNext,
                modifier = Modifier.heightIn(min = 40.dp).testTag("tour-next"),
                contentPadding = PaddingValues(horizontal = 14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CourtQuestDesign.accentVivid, contentColor = Color.Black
                )) {
                Text(if (step == stops.lastIndex) "Done" else "Next", fontSize = 12.sp)
            }
        }
    }
}
