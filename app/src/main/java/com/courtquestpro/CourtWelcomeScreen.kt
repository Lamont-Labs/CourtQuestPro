package com.courtquestpro

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Configuration
import android.view.accessibility.AccessibilityManager
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

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
    val reducedMotion = remember { !ValueAnimator.areAnimatorsEnabled() }
    val touchExploration = remember(context) {
        (context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager)?.isTouchExplorationEnabled == true
    }
    val landscape = androidx.compose.ui.platform.LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val spin = rememberInfiniteTransition(label = "Basketball idle")
    val idleRotation by if (reducedMotion) remember { mutableFloatStateOf(0f) } else spin.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(7000, easing = LinearEasing), RepeatMode.Restart),
        label = "Ball rotation"
    )
    val pulse by if (reducedMotion) remember { mutableFloatStateOf(1f) } else spin.animateFloat(
        initialValue = .48f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1050), RepeatMode.Reverse),
        label = "Swipe prompt"
    )
    fun shoot() {
        if (entering) return
        entering = true
        scope.launch {
            if (!reducedMotion) progress.animateTo(1f, tween(1320, easing = LinearEasing))
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
                modifier = Modifier.fillMaxWidth(.9f)
                    .heightIn(min = if (landscape) 64.dp else 104.dp, max = if (landscape) 96.dp else 148.dp)
                    .testTag("welcome-signature-wordmark"),
                contentScale = ContentScale.Fit
            )
            Spacer(Modifier.weight(if (landscape) .15f else .5f))
            Box(
                Modifier.fillMaxWidth().weight(if (landscape) 3.2f else 2.5f)
                    .semantics {
                        contentDescription = "Photorealistic basketball. Swipe upward or activate to shoot and enter CourtQuest."
                        role = Role.Button
                    }
                    .pointerInput(entering) {
                        var upward = 0f
                        detectDragGestures(
                            onDragStart = { upward = 0f },
                            onDragEnd = { if (upward > size.height * .09f) shoot() },
                            onDragCancel = { upward = 0f },
                            onDrag = { change, drag ->
                                change.consume()
                                upward -= drag.y
                            }
                        )
                    }
                    .clickable(
                        enabled = !entering,
                        onClickLabel = if (touchExploration) "Tap the basketball to enter CourtQuest" else "Shoot to enter CourtQuest",
                        role = Role.Button
                    ) { shoot() }
                    .testTag("welcome-swipe-ball"),
                contentAlignment = Alignment.Center
            ) {
                val p = progress.value
                val approach = (p / .66f).coerceIn(0f, 1f)
                val descent = ((p - .66f) / .34f).coerceIn(0f, 1f)
                val ballY = if (p < .66f) {
                    .73f - .30f * (1f - (1f - approach) * (1f - approach))
                } else {
                    .43f + .23f * (descent * descent)
                }
                val ballScale = if (p < .66f) 1f - .69f * approach else .31f - .07f * descent
                val hoopReveal = ((p - .04f) / .42f).coerceIn(0f, 1f)
                val netProgress = ((p - .64f) / .36f).coerceIn(0f, 1f)
                val netPulse = if (netProgress > 0f) {
                    sin(netProgress.toDouble() * PI * 4.0).toFloat() * (1f - netProgress)
                } else 0f
                val hoopWidth = minOf(maxWidth * .96f, maxHeight * .56f * 1.607f)
                val boardHeight = hoopWidth / 1.607f
                val boardTop = maxHeight * .035f
                val density = androidx.compose.ui.platform.LocalDensity.current
                val stageHeightPx = with(density) { maxHeight.toPx() }
                val stageWidthPx = with(density) { maxWidth.toPx() }
                val ballSize = minOf(maxWidth * .63f, maxHeight * .54f)
                Canvas(Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val stageLight = .18f + hoopReveal * .5f
                    drawCircle(
                        Brush.radialGradient(
                            listOf(Color(0xFFFF6A18).copy(alpha = stageLight), Color.Transparent),
                            center = Offset(w * .5f, h * .34f),
                            radius = w * .72f
                        ),
                        radius = w * .72f,
                        center = Offset(w * .5f, h * .34f)
                    )
                    val floorY = h * .82f
                    drawLine(
                        Color(0xFFFF6A18).copy(alpha = .14f + hoopReveal * .16f),
                        Offset(w * .04f, floorY), Offset(w * .96f, floorY), 1.5.dp.toPx()
                    )
                    drawOval(
                        Brush.radialGradient(listOf(Color.Black.copy(alpha = .72f), Color.Transparent)),
                        topLeft = Offset(w * (.5f - .24f * ballScale), floorY - h * .025f),
                        size = Size(w * .48f * ballScale, h * .045f)
                    )
                    if (hoopReveal > 0f) {
                        drawCircle(
                            Color(0xFFFF5B0A).copy(alpha = hoopReveal * .32f),
                            radius = w * (.17f + hoopReveal * .08f),
                            center = Offset(w * .5f, h * .29f),
                            style = Stroke(w * .006f)
                        )
                    }
                }
                Image(
                    painter = painterResource(R.drawable.cq_welcome_backboard_rim),
                    contentDescription = null,
                    modifier = Modifier.align(Alignment.TopCenter).width(hoopWidth).height(boardHeight)
                        .offset(y = boardTop)
                        .graphicsLayer {
                            alpha = hoopReveal
                            translationY = (1f - hoopReveal) * -24.dp.toPx()
                        },
                    contentScale = ContentScale.FillBounds
                )
                val netWidth = hoopWidth * .33f
                Image(
                    painter = painterResource(R.drawable.cq_welcome_net),
                    contentDescription = null,
                    modifier = Modifier.align(Alignment.TopCenter)
                        .width(netWidth).height(netWidth * 1.10f)
                        .offset(y = boardTop + boardHeight * .67f)
                        .graphicsLayer {
                            alpha = hoopReveal
                            scaleY = 1f + netPulse * .20f
                            translationY = netProgress * stageHeightPx * .012f
                            transformOrigin = TransformOrigin(.5f, 0f)
                        },
                    contentScale = ContentScale.FillBounds
                )
                Image(
                    painter = painterResource(R.drawable.cq_welcome_ball),
                    contentDescription = null,
                    modifier = Modifier.align(Alignment.Center).size(ballSize)
                        .graphicsLayer {
                            translationY = (ballY - .5f) * stageHeightPx
                            translationX = sin(p.toDouble() * PI * 1.15).toFloat() * stageWidthPx * .035f
                            scaleX = ballScale
                            scaleY = ballScale
                            rotationZ = idleRotation + p * 630f
                        },
                    contentScale = ContentScale.Fit
                )
                if (!entering) {
                    Text(
                        if (touchExploration) "TAP THE BALL TO ENTER" else "SWIPE TO ENTER",
                        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp)
                            .testTag("welcome-swipe-prompt"),
                        color = CourtQuestDesign.accentVivid.copy(alpha = pulse),
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp,
                        fontSize = 16.sp
                    )
                }
            }
            Spacer(Modifier.weight(if (landscape) .08f else .25f))
            if (!returningPlayer) {
                TextButton(
                    onClick = onGuest,
                    modifier = Modifier.testTag("welcome-continue-guest")
                ) { Text("CONTINUE AS GUEST", color = CourtQuestDesign.textSecondary) }
            }
        }
    }
}
