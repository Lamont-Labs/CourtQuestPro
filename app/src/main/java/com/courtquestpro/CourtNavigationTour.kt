package com.courtquestpro

import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
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
    val tips = listOf(
        "Start with a drill that matches today's goal, then record your attempts to track consistency.",
        "Review completed sessions to spot patterns and choose what to practice next.",
        "Use Court as your starting point when returning to the app.",
        "Check Quests when you want a specific challenge to work toward.",
        "Keep your player details and jersey up to date so the app feels like yours."
    )
    val currentTip = tips[step.coerceIn(tips.indices)]
    val current = stops[step.coerceIn(stops.indices)]
    val tabs = listOf(PlayerTab.TRAIN, PlayerTab.PROGRESS, PlayerTab.COURT, PlayerTab.QUESTS, PlayerTab.ME)
    val tab = tabs[step.coerceIn(tabs.indices)]
    val tourScroll = rememberScrollState()
    LaunchedEffect(step) { tourScroll.scrollTo(0) }
    Box(Modifier.fillMaxSize().testTag("navigation-tour"), contentAlignment = Alignment.TopCenter) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(top = 12.dp, bottom = 12.dp)
                .background(Color(0xF7191513), RoundedCornerShape(18.dp))
                .border(1.dp, CourtQuestDesign.accentVivid.copy(alpha = .5f), RoundedCornerShape(18.dp))
                .verticalScroll(tourScroll)
                .padding(horizontal = 18.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween) {
                Text("EXPLORE COURTQUEST", color = CourtQuestDesign.accentVivid,
                    fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
                Text("${step + 1} / ${stops.size}", color = CourtQuestDesign.textSecondary, fontSize = 11.sp)
            }
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth().semantics {
                contentDescription = "Walkthrough progress: step ${step + 1} of ${stops.size}"
            }, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                stops.indices.forEach { index ->
                    Box(
                        Modifier.weight(1f).height(3.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (index <= step) CourtQuestDesign.accentVivid else CourtQuestDesign.border)
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
            Row(Modifier.testTag("tour-current-step").semantics {
                liveRegion = LiveRegionMode.Polite
                contentDescription = "Step ${step + 1} of ${stops.size}: ${current.second}. ${current.third}"
            }, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    Modifier.size(44.dp).clip(RoundedCornerShape(13.dp))
                        .background(CourtQuestDesign.accentVivid.copy(alpha = .13f))
                        .border(1.dp, CourtQuestDesign.accentVivid.copy(alpha = .55f), RoundedCornerShape(13.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text((step + 1).toString().padStart(2, '0'), color = CourtQuestDesign.accentVivid,
                        style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
                Column(Modifier.weight(1f)) {
                    Text(current.first, color = CourtQuestDesign.accentVivid,
                        style = MaterialTheme.typography.labelSmall, letterSpacing = 1.sp)
                    Text(current.second, color = CourtQuestDesign.textPrimary,
                        style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(current.third, color = CourtQuestDesign.textSecondary,
                style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(8.dp))
            Text("QUICK TIP", color = CourtQuestDesign.accentVivid,
                style = MaterialTheme.typography.labelSmall, letterSpacing = .7.sp)
            Text(currentTip, color = CourtQuestDesign.textPrimary,
                style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(10.dp))
            Text("EXPLORE THIS SECTION", color = CourtQuestDesign.accentVivid,
                style = MaterialTheme.typography.labelSmall, letterSpacing = .7.sp)
            Text("You're viewing ${tab.label}. Use Next to continue, or choose another dock tab to explore.",
                color = CourtQuestDesign.textPrimary, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onSkip, modifier = Modifier.testTag("tour-skip")) {
                    Text("Skip tour")
                }
            }
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onBack,
                    enabled = step > 0,
                    modifier = Modifier.weight(1f)
                ) { Text("Back") }
                Button(
                    onClick = onNext,
                    modifier = Modifier.weight(1f).testTag("tour-next"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CourtQuestDesign.accentVivid,
                        contentColor = Color.Black
                    )
                ) { Text(if (step == stops.lastIndex) "Finish tour" else "Next") }
            }

        }
    }
}
