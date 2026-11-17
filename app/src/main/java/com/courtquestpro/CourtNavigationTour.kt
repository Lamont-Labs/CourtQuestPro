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
    Box(
        Modifier.fillMaxSize().background(Color(0xD9000000)).testTag("navigation-tour"),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(top = 42.dp)
                .background(Color(0xFF171311), RoundedCornerShape(24.dp))
                .border(1.dp, CourtQuestDesign.accentVivid.copy(alpha = .45f), RoundedCornerShape(24.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("COURTQUEST PRO  /  QUICK TOUR", color = CourtQuestDesign.accent,
                fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.3.sp)
            Spacer(Modifier.height(18.dp))
            Text(current.first, color = CourtQuestDesign.accentVivid,
                fontSize = 14.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
            Spacer(Modifier.height(12.dp))
            Text(current.second, color = CourtQuestDesign.textPrimary,
                style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
            Spacer(Modifier.height(10.dp))
            Text(current.third, color = CourtQuestDesign.textSecondary,
                style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
            Spacer(Modifier.height(18.dp))
            Text("Tap the highlighted navigation icon below to open this section.",
                color = CourtQuestDesign.accentVivid, textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(12.dp))
            Text("${step + 1} / ${stops.size}", color = CourtQuestDesign.textSecondary)
            Spacer(Modifier.height(18.dp))
            Button(onClick = onNext, modifier = Modifier.fillMaxWidth().height(52.dp)
                .testTag("tour-next"), colors = ButtonDefaults.buttonColors(
                containerColor = CourtQuestDesign.accentVivid, contentColor = Color.Black
            )) { Text(if (step == stops.lastIndex) "FINISH TOUR" else "NEXT WITHOUT OPENING", fontWeight = FontWeight.Bold) }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onBack, enabled = step > 0) { Text("Back") }
                TextButton(onClick = onSkip) { Text("Skip tour") }
            }
        }
        Column(Modifier.align(Alignment.BottomCenter), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("TAP ${tab.label.uppercase()} TO EXPLORE", color = CourtQuestDesign.accentVivid,
                fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Spacer(Modifier.height(8.dp))
            CourtQuestBottomDock(selected = tab, jerseyNumber = jerseyNumber, onSelect = onDestination, spotlightTab = tab)
        }
    }
}
