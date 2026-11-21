package com.courtquestpro

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import android.content.Context
import android.view.accessibility.AccessibilityManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * The welcome shot is deliberately a deterministic animation rather than a physics game.
 * A swipe (or accessible tap) always scores and then follows the existing entry callback.
 * The legacy first-run guest route remains available.
 */
@Composable
internal fun CourtWelcomeScreen(
    modifier: Modifier = Modifier,
    returningPlayer: Boolean = false,
    onGetStarted: () -> Unit,
    onGuest: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val progress = remember { Animatable(0f) }
    var entering by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val reducedMotion = remember(context) { (context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager)?.isTouchExplorationEnabled == true }
    val spin = rememberInfiniteTransition(label = "Basketball idle")
    val idleRotation by spin.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(7000), RepeatMode.Restart),
        label = "Ball rotation"
    )
    val pulse by spin.animateFloat(
        initialValue = .48f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1050), RepeatMode.Reverse),
        label = "Swipe prompt"
    )
    fun shoot() {
        if (entering) return
        entering = true
        scope.launch {
            if (!reducedMotion) progress.animateTo(1f, tween(1150))
            onGetStarted()
        }
    }
    Box(
        modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Color(0xFF050505), Color(0xFF110A06), Color(0xFF050505)))
        ).testTag("court-welcome-screen")
    ) {
        Column(
            Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 24.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(R.drawable.courtquest_signature_wordmark),
                contentDescription = "CourtQuest Pro",
                modifier = Modifier.fillMaxWidth(.92f).height(170.dp)
                    .testTag("welcome-signature-wordmark"),
                contentScale = ContentScale.Fit
            )
            Spacer(Modifier.weight(.5f))
            BoxWithConstraints(
                Modifier.fillMaxWidth().weight(2.5f)
                    .semantics { contentDescription = "Basketball. Swipe upward or tap to enter CourtQuest." }
                    .pointerInput(entering) {
                        var upward = 0f
                        detectDragGestures(
                            onDragStart = { upward = 0f },
                            onDragEnd = { if (upward > 36f) shoot() },
                            onDragCancel = { upward = 0f },
                            onDrag = { change, drag ->
                                change.consume()
                                upward -= drag.y
                            }
                        )
                    }
                    .clickable(enabled = !entering, onClickLabel = "Shoot to enter CourtQuest") { shoot() }
                    .testTag("welcome-swipe-ball"),
                contentAlignment = Alignment.Center
            ) {
                val p = progress.value
                val ballY = if (p < .72f) -.62f * (p / .72f) else -.62f + .32f * ((p - .72f) / .28f)
                Canvas(Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val hoopAlpha = ((p - .12f) / .48f).coerceIn(0f, 1f)
                    if (hoopAlpha > 0f) {
                        val boardY = h * .19f
                        drawRect(Color(0xFFE7E8EA).copy(alpha = hoopAlpha * .85f),
                            Offset(w * .22f, boardY), Size(w * .56f, h * .18f),
                            style = Stroke(width = 5.dp.toPx()))
                        drawRect(Color.White.copy(alpha = hoopAlpha * .75f),
                            Offset(w * .40f, boardY + h * .06f), Size(w * .20f, h * .08f),
                            style = Stroke(width = 3.dp.toPx()))
                        val rimY = boardY + h * .20f
                        drawLine(Color(0xFFFF6518).copy(alpha = hoopAlpha),
                            Offset(w * .35f, rimY), Offset(w * .65f, rimY),
                            8.dp.toPx(), StrokeCap.Round)
                        for (i in 0..5) {
                            val x = w * (.36f + i * .056f)
                            drawLine(Color.White.copy(alpha = hoopAlpha * .75f),
                                Offset(x, rimY), Offset(w * (.42f + i * .032f), rimY + h * .13f),
                                1.5.dp.toPx())
                        }
                    }
                    val radius = (w * .265f * (1f - p * .62f)).coerceAtLeast(8.dp.toPx())
                    val center = Offset(w * .5f, h * (.60f + ballY))
                    drawCircle(Brush.radialGradient(
                        listOf(Color(0xFFFFB15B), Color(0xFFE66A19), Color(0xFF7D2C0A)),
                        center = center - Offset(radius * .35f, radius * .38f),
                        radius = radius * 2.1f
                    ), radius, center)
                    val seam = Color(0xFF31180E)
                    val sw = (radius * .055f).coerceAtLeast(1.5f)
                    drawLine(seam, center - Offset(radius, 0f), center + Offset(radius, 0f), sw)
                    drawLine(seam, center - Offset(0f, radius), center + Offset(0f, radius), sw)
                    val left = Path().apply {
                        moveTo(center.x - radius * .68f, center.y - radius * .72f)
                        cubicTo(center.x + radius * .12f, center.y - radius * .25f,
                            center.x + radius * .12f, center.y + radius * .25f,
                            center.x - radius * .68f, center.y + radius * .72f)
                    }
                    val right = Path().apply {
                        moveTo(center.x + radius * .68f, center.y - radius * .72f)
                        cubicTo(center.x - radius * .12f, center.y - radius * .25f,
                            center.x - radius * .12f, center.y + radius * .25f,
                            center.x + radius * .68f, center.y + radius * .72f)
                    }
                    drawPath(left, seam, style = Stroke(sw))
                    drawPath(right, seam, style = Stroke(sw))
                }
                if (!entering) {
                    Text(
                        "SWIPE TO ENTER",
                        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp)
                            .testTag("welcome-swipe-prompt"),
                        color = CourtQuestDesign.accentVivid.copy(alpha = pulse),
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp,
                        fontSize = 16.sp
                    )
                }
            }
            Spacer(Modifier.weight(.25f))
            if (!returningPlayer) {
                TextButton(
                    onClick = onGuest,
                    modifier = Modifier.testTag("welcome-continue-guest")
                ) { Text("CONTINUE AS GUEST", color = CourtQuestDesign.textSecondary) }
            }
        }
    }
}
