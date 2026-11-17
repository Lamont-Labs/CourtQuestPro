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
            Brush.verticalGradient(listOf(Color(0xFF050505), Color(0xFF100B08), Color(0xFF050505)))
        ).testTag("court-welcome-screen")
    ) {
        Column(
            Modifier.fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 28.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.weight(0.85f))
            Text(
                "WELCOME TO",
                color = CourtQuestDesign.textSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 3.sp
            )
            Spacer(Modifier.height(26.dp))
            Image(
                painter = painterResource(R.drawable.courtquest_signature_wordmark),
                contentDescription = "CourtQuest Pro",
                modifier = Modifier.fillMaxWidth(0.94f).height(136.dp)
                    .testTag("welcome-signature-wordmark"),
                contentScale = ContentScale.Fit
            )
            Spacer(Modifier.height(24.dp))
            Box(
                Modifier.width(58.dp).height(3.dp)
                    .background(CourtQuestDesign.accentVivid, RoundedCornerShape(3.dp))
            )
            Spacer(Modifier.height(24.dp))
            Text(
                "YOUR GAME. YOUR NEXT LEVEL.",
                color = CourtQuestDesign.textPrimary,
                fontFamily = CourtDisplay,
                fontWeight = FontWeight.Black,
                fontSize = 19.sp,
                textAlign = TextAlign.Center,
                letterSpacing = 0.6.sp
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "Train with purpose. Build your skills.\nSee how far you can go.",
                color = CourtQuestDesign.textSecondary,
                fontSize = 15.sp,
                lineHeight = 23.sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.weight(1.15f))
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
