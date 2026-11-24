package com.courtquestpro

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun CourtWelcomeScreen(
    modifier: Modifier = Modifier,
    returningPlayer: Boolean = false,
    onGetStarted: () -> Unit,
    onGuest: () -> Unit
) {
    var entering by remember { mutableStateOf(false) }
    Box(modifier.fillMaxSize().background(Color.Black).testTag("court-welcome-screen")) {
        Image(painterResource(R.drawable.cq_welcome_opening_court), null,
            Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        BoxWithConstraints(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 20.dp, vertical = 16.dp)) {
            val ballSize = minOf(maxWidth * .36f, maxHeight * .27f, 144.dp).coerceAtLeast(48.dp)
            Column(Modifier.align(Alignment.BottomCenter),
                horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.size(ballSize).clip(CircleShape)
                    .clickable(enabled = !entering, role = Role.Button, onClickLabel = "Enter CourtQuest") {
                        entering = true
                        onGetStarted()
                    }.testTag("welcome-enter-ball"), contentAlignment = Alignment.Center) {
                    Image(painterResource(R.drawable.cq_welcome_ball), null,
                        Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
                    Text("ENTER", color = Color.White, fontWeight = FontWeight.Black,
                        fontSize = 18.sp, modifier = Modifier.background(Color.Black.copy(alpha = .65f), CircleShape)
                            .padding(horizontal = 8.dp, vertical = 4.dp))
                }
                if (!returningPlayer) {
                    TextButton(onClick = onGuest, modifier = Modifier.testTag("welcome-continue-guest")) {
                        Text("CONTINUE AS GUEST", color = Color.White)
                    }
                }
            }
        }
    }
}
