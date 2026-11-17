package com.courtquestpro

import androidx.compose.foundation.Image
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.draw.clip
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
            Spacer(Modifier.height(16.dp))
            Image(
                painter = painterResource(R.drawable.courtquest_signature_wordmark),
                contentDescription = "CourtQuest Pro",
                modifier = Modifier.fillMaxWidth(0.77f).height(92.dp).testTag("welcome-signature-wordmark"),
                contentScale = ContentScale.Fit
            )
            Spacer(Modifier.height(24.dp))
            Box(
                Modifier.fillMaxWidth().weight(1f)
                    .clip(RoundedCornerShape(26.dp))
                    .background(CourtQuestDesign.surface)
            ) {
                Image(
                    painter = painterResource(R.drawable.court_arena_player),
                    contentDescription = "Basketball player entering the court",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    Modifier.fillMaxSize().background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color(0x33050505), Color(0xED050505)),
                            startY = 130f
                        )
                    )
                )
                Column(
                    Modifier.align(Alignment.BottomStart).padding(22.dp)
                ) {
                    Text(
                        "WELCOME TO THE COURT",
                        color = CourtQuestDesign.accent,
                        fontSize = 12.sp,
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "YOUR GAME.\nYOUR NEXT LEVEL.",
                        color = CourtQuestDesign.textPrimary,
                        fontFamily = CourtDisplay,
                        fontWeight = FontWeight.Black,
                        fontSize = 30.sp,
                        lineHeight = 34.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Train with purpose. Track every rep.",
                        color = CourtQuestDesign.textPrimary.copy(alpha = 0.88f),
                        fontSize = 14.sp
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
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
            Spacer(Modifier.height(12.dp))
            Text(
                "Offline training · Progress saved on this device",
                color = CourtQuestDesign.textSecondary,
                fontSize = 12.sp,
                lineHeight = 17.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
