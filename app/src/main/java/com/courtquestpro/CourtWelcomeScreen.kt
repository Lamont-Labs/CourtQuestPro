package com.courtquestpro

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Branded entry on every fresh app launch; never implies account authentication. */
@Composable
internal fun CourtWelcomeScreen(
    modifier: Modifier = Modifier,
    returningPlayer: Boolean = false,
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
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    Modifier.widthIn(max = 420.dp).fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.courtquest_signature_wordmark),
                        contentDescription = "CourtQuest Pro",
                        modifier = Modifier.fillMaxWidth(0.9f).height(122.dp)
                            .testTag("welcome-signature-wordmark"),
                        contentScale = ContentScale.Fit
                    )
                    Box(
                        Modifier.width(58.dp).height(3.dp)
                            .background(CourtQuestDesign.accentVivid, RoundedCornerShape(3.dp))
                    )
                    Text(
                        "YOUR GAME. YOUR NEXT LEVEL.",
                        color = CourtQuestDesign.textPrimary,
                        fontFamily = CourtDisplay,
                        fontWeight = FontWeight.Black,
                        fontSize = 19.sp,
                        textAlign = TextAlign.Center,
                        letterSpacing = 0.6.sp
                    )
                    Text(
                        "Build a practice routine, record your drills, and track your progress.",
                        color = CourtQuestDesign.textSecondary,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        "Offline training · Progress saved on this device",
                        color = CourtQuestDesign.textSecondary,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Column(
                Modifier.widthIn(max = 420.dp).fillMaxWidth().padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onGetStarted,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 58.dp)
                        .testTag("welcome-get-started"),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CourtQuestDesign.accentVivid,
                        contentColor = CourtQuestDesign.iconForeground
                    )
                ) {
                    Text(if (returningPlayer) "ENTER COURT" else "GET STARTED",
                        fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
                }
                if (!returningPlayer) {
                    OutlinedButton(
                        onClick = onGuest,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
                            .testTag("welcome-continue-guest"),
                        shape = RoundedCornerShape(18.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CourtQuestDesign.chipBorder)
                    ) {
                        Text("CONTINUE AS GUEST", color = CourtQuestDesign.textPrimary,
                            fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}
