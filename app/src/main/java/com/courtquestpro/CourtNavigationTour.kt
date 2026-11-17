package com.courtquestpro

import androidx.compose.foundation.background
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
    onBack: () -> Unit
) {
    val stops = listOf(
        Triple("TRAINING", "Find your drills", "Open Training to explore drills and start a practice session."),
        Triple("PROGRESS", "See your improvement", "Follow your training history and watch your progress build."),
        Triple("COURT", "Your basketball home", "Return to Court whenever you want your main dashboard."),
        Triple("ACHIEVEMENTS", "Celebrate every milestone", "Find your rewards and accomplishments as you train."),
        Triple("PLAYER", "Make it yours", "Manage your player details and customize your jersey.")
    )
    val current = stops[step.coerceIn(stops.indices)]
    Box(
        Modifier.fillMaxSize().background(Color(0xE9000000)).testTag("navigation-tour"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            Modifier.fillMaxWidth().padding(24.dp)
                .background(Color(0xFF171311), RoundedCornerShape(24.dp))
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
            Text("${step + 1} / ${stops.size}", color = CourtQuestDesign.textSecondary)
            Spacer(Modifier.height(18.dp))
            Button(onClick = onNext, modifier = Modifier.fillMaxWidth().height(52.dp)
                .testTag("tour-next"), colors = ButtonDefaults.buttonColors(
                containerColor = CourtQuestDesign.accentVivid, contentColor = Color.Black
            )) { Text(if (step == stops.lastIndex) "FINISH TOUR" else "NEXT", fontWeight = FontWeight.Bold) }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onBack, enabled = step > 0) { Text("Back") }
                TextButton(onClick = onSkip) { Text("Skip tour") }
            }
        }
    }
}
