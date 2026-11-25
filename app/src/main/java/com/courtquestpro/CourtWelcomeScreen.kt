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
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.Font
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
            Text("WELCOME TO", color = Color.White, fontWeight = FontWeight.Black,
                fontFamily = FontFamily(Font(R.font.barlow_condensed_extra_bold, FontWeight.ExtraBold)),
                fontSize = if (maxHeight < 360.dp) 20.sp else minOf(42f, maxWidth.value * .115f).sp,
                letterSpacing = 1.8.sp,
                style = TextStyle(shadow = Shadow(Color.Black.copy(alpha = .8f), Offset(0f, 2f), 5f)),
                maxLines = 1, modifier = Modifier.align(Alignment.TopCenter)
                    .padding(top = if (maxHeight < 360.dp) 0.dp else maxHeight * .17f)
                    .testTag("welcome-heading"))
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
                        fontSize = if (ballSize < 80.dp) 10.sp else 18.sp, maxLines = 1, softWrap = false,
                        style = TextStyle(shadow = Shadow(Color.Black.copy(alpha = .65f), Offset(0f, 1f), 2f)))
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
