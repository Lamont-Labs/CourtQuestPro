package com.courtquestpro

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** First-launch entry. Account authentication is deliberately not implied or simulated. */
@Composable
internal fun CourtWelcomeScreen(
    modifier: Modifier = Modifier,
    onGetStarted: () -> Unit,
    onGuest: () -> Unit
) {
    Box(
        modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Color(0xFF050505), Color(0xFF15100D), Color(0xFF050505)))
        ).testTag("court-welcome-screen")
    ) {
        Column(
            Modifier.fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 28.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.weight(1f))
            Text(
                "COURTQUEST",
                color = CourtQuestDesign.textPrimary,
                fontFamily = CourtDisplay,
                fontSize = 43.sp,
                lineHeight = 49.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )
            Text(
                "PRO",
                color = CourtQuestDesign.accentVivid,
                fontFamily = CourtDisplay,
                fontSize = 26.sp,
                letterSpacing = 8.sp,
                fontWeight = FontWeight.Black
            )
            Spacer(Modifier.height(38.dp))
            Text(
                "WELCOME TO\nCOURTQUEST PRO",
                color = CourtQuestDesign.textPrimary,
                fontFamily = CourtDisplay,
                fontWeight = FontWeight.Black,
                fontSize = 32.sp,
                lineHeight = 38.sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(14.dp))
            Text(
                "Your court. Your progress. Your game.",
                color = CourtQuestDesign.textSecondary,
                fontSize = 16.sp,
                lineHeight = 23.sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.weight(1f))
            Button(
                onClick = onGetStarted,
                modifier = Modifier.fillMaxWidth().height(58.dp).testTag("welcome-get-started"),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CourtQuestDesign.accentVivid,
                    contentColor = Color(0xFF160A04)
                )
            ) {
                Text("GET STARTED", fontWeight = FontWeight.Black, letterSpacing = 1.sp)
            }
            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick = onGuest,
                modifier = Modifier.fillMaxWidth().height(56.dp).testTag("welcome-continue-guest"),
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CourtQuestDesign.chipBorder)
            ) {
                Text("CONTINUE AS GUEST", color = CourtQuestDesign.textPrimary, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(16.dp))
            Text(
                "Play offline. Your progress stays on this device.",
                color = CourtQuestDesign.textSecondary,
                fontSize = 12.sp,
                lineHeight = 17.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
