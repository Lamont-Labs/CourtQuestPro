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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    val tabs = listOf(PlayerTab.TRAIN, PlayerTab.PROGRESS, PlayerTab.COURT, PlayerTab.QUESTS, PlayerTab.ME)
    val tab = tabs[step.coerceIn(tabs.indices)]
    Box(Modifier.fillMaxSize().testTag("navigation-tour"), contentAlignment = Alignment.TopCenter) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(top = 12.dp)
                .background(Color(0xF7191513), RoundedCornerShape(18.dp))
                .border(1.dp, CourtQuestDesign.accentVivid.copy(alpha = .5f), RoundedCornerShape(18.dp))
                .padding(horizontal = 18.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween) {
                Text("EXPLORE COURTQUEST", color = CourtQuestDesign.accentVivid,
                    fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
                Text("${step + 1} / ${stops.size}", color = CourtQuestDesign.textSecondary, fontSize = 11.sp)
            }
            Spacer(Modifier.height(8.dp))
            Text(current.second, color = CourtQuestDesign.textPrimary,
                style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(current.third, color = CourtQuestDesign.textSecondary,
                style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(8.dp))
            Text("Tap ${tab.label} in the highlighted dock below", color = CourtQuestDesign.accentVivid,
                style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onSkip, modifier = Modifier.testTag("tour-skip")) {
                    Text("Skip")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = onBack, enabled = step > 0) { Text("Back") }
                    Button(onClick = onNext, modifier = Modifier.testTag("tour-next"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CourtQuestDesign.accentVivid, contentColor = Color.Black
                        )) { Text(if (step == stops.lastIndex) "Finish" else "Next") }
                }
            }
        }
    }
}
