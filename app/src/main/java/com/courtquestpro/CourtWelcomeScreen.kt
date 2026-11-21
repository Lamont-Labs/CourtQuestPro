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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

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
            if (!reducedMotion) {
                progress.animateTo(1f, tween(1320, easing = LinearEasing))
                delay(100)
            }
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
            var stageSize by remember { mutableStateOf(IntSize.Zero) }
            var measuredBoard by remember { mutableStateOf(WelcomeShotRect(0f, 0f, 0f, 0f)) }
            val density = LocalDensity.current
            val boardArt = ImageBitmap.imageResource(R.drawable.cq_welcome_backboard_rim)
            BoxWithConstraints(
                Modifier.fillMaxWidth().weight(if (landscape) 3.2f else 2.5f)
                    .onSizeChanged { stageSize = it }
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
                val hoopWidth = minOf(maxWidth * .96f, maxHeight * .56f * 1.607f)
                val boardHeight = hoopWidth / 1.607f
                val boardTop = maxHeight * .035f
                val stageHeightPx = stageSize.height.takeIf { it > 0 }?.toFloat()
                    ?: with(density) { maxHeight.toPx() }
                val stageWidthPx = stageSize.width.takeIf { it > 0 }?.toFloat()
                    ?: with(density) { maxWidth.toPx() }
                val boardWidthPx = with(density) { hoopWidth.toPx() }
                val boardHeightPx = with(density) { boardHeight.toPx() }
                val boardTopPx = with(density) { boardTop.toPx() }
                val board = measuredBoard.takeIf { it.width > 0f && it.height > 0f }
                    ?: WelcomeShotRect((stageWidthPx - boardWidthPx) * .5f, boardTopPx, boardWidthPx, boardHeightPx)
                val ballSize = minOf(maxWidth * .63f, maxHeight * .54f)
                val ballSizePx = with(density) { ballSize.toPx() }
                val netWidth = hoopWidth * .31f
                val netHeight = netWidth * 1.10f
                val netHeightPx = with(density) { netHeight.toPx() }
                val frame = WelcomeShotGeometry.frame(
                    p, WelcomeShotLayout(stageWidthPx, stageHeightPx, board, ballSizePx, netHeightPx)
                )
                Canvas(Modifier.fillMaxSize()) {
                    val w = size.width
                    val stageLight = .14f + frame.hoopReveal * .48f
                    drawCircle(
                        Brush.radialGradient(
                            listOf(Color(0xFFFF6A18).copy(alpha = stageLight), Color.Transparent),
                            center = Offset(frame.rimCenterX, frame.rimCenterY),
                            radius = w * .72f
                        ),
                        radius = w * .72f,
                        center = Offset(frame.rimCenterX, frame.rimCenterY)
                    )
                    drawLine(
                        Color(0xFFFF6A18).copy(alpha = .14f + frame.hoopReveal * .16f),
                        Offset(w * .04f, frame.floorY), Offset(w * .96f, frame.floorY), 1.5.dp.toPx()
                    )
                    drawOval(
                        Brush.radialGradient(listOf(Color.Black.copy(alpha = frame.shadowAlpha), Color.Transparent)),
                        topLeft = Offset(frame.ballCenterX - ballSizePx * .27f * frame.shadowScale,
                            frame.floorY - ballSizePx * .035f),
                        size = Size(ballSizePx * .54f * frame.shadowScale, ballSizePx * .07f)
                    )
                    drawCircle(Color(0xFFFF5B0A).copy(alpha = frame.hoopReveal * .28f),
                        radius = frame.rimOpeningWidthPx * .72f,
                        center = Offset(frame.rimCenterX, frame.rimCenterY), style = Stroke(w * .006f))
                }
                Image(
                    painter = painterResource(R.drawable.cq_welcome_backboard_rim),
                    contentDescription = null,
                    modifier = Modifier.align(Alignment.TopCenter).width(hoopWidth).height(boardHeight)
                        .offset(y = boardTop)
                        .onGloballyPositioned { coordinates ->
                            val position = coordinates.positionInParent()
                            measuredBoard = WelcomeShotRect(
                                position.x, position.y,
                                coordinates.size.width.toFloat(), coordinates.size.height.toFloat()
                            )
                        }
                        .graphicsLayer { alpha = frame.hoopReveal },
                    contentScale = ContentScale.FillBounds
                )
                if (frame.behindNet) {
                    WelcomeShotBall(frame, p, idleRotation, ballSize, stageWidthPx, stageHeightPx)
                }
                val netTopPx = frame.rimCenterY - netHeightPx * .02f
                val netProgress = ((p - .67f) / .20f).coerceIn(0f, 1f)
                Image(
                    painter = painterResource(R.drawable.cq_welcome_net),
                    contentDescription = null,
                    modifier = Modifier.align(Alignment.TopCenter)
                        .width(netWidth).height(netHeight)
                        .offset(y = with(density) { netTopPx.toDp() })
                        .graphicsLayer {
                            alpha = frame.hoopReveal * if (frame.behindNet) 1f else ((p - .50f) / .17f).coerceIn(0f, 1f)
                            scaleY = frame.netScaleY
                            translationY = frame.netTranslationY
                            transformOrigin = TransformOrigin(.5f, 0f)
                        },
                    contentScale = ContentScale.FillBounds
                )
                if (!frame.behindNet) {
                    WelcomeShotBall(frame, p, idleRotation, ballSize, stageWidthPx, stageHeightPx)
                }
                Canvas(
                    Modifier.align(Alignment.TopCenter).width(hoopWidth).height(boardHeight)
                        .offset(y = boardTop).graphicsLayer { alpha = frame.hoopReveal }
                        .testTag("welcome-front-rim-overlay")
                ) {
                    val frontArc = Path().apply {
                        moveTo(size.width * .342f, size.height * .709f)
                        cubicTo(size.width * .355f, size.height * .744f, size.width * .420f, size.height * .771f,
                            size.width * .500f, size.height * .782f)
                        cubicTo(size.width * .580f, size.height * .771f, size.width * .645f, size.height * .744f,
                            size.width * .660f, size.height * .709f)
                        lineTo(size.width * .661f, size.height * .750f)
                        cubicTo(size.width * .635f, size.height * .778f, size.width * .575f, size.height * .797f,
                            size.width * .500f, size.height * .802f)
                        cubicTo(size.width * .425f, size.height * .797f, size.width * .365f, size.height * .778f,
                            size.width * .341f, size.height * .750f)
                        close()
                    }
                    clipPath(frontArc) {
                        drawImage(
                            image = boardArt,
                            srcOffset = IntOffset.Zero,
                            srcSize = IntSize(boardArt.width, boardArt.height),
                            dstOffset = IntOffset.Zero,
                            dstSize = IntSize(size.width.roundToInt(), size.height.roundToInt())
                        )
                    }
                }
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

@Composable
private fun BoxScope.WelcomeShotBall(
    frame: WelcomeShotFrame,
    progress: Float,
    idleRotation: Float,
    ballSize: androidx.compose.ui.unit.Dp,
    stageWidthPx: Float,
    stageHeightPx: Float
) {
    Image(
        painter = painterResource(R.drawable.cq_welcome_ball),
        contentDescription = null,
        modifier = Modifier.align(Alignment.Center).size(ballSize)
            .semantics { stateDescription = "Shot progress ${(progress * 100).roundToInt()} percent" }
            .graphicsLayer {
                translationX = frame.ballCenterX - stageWidthPx * .5f
                translationY = frame.ballCenterY - stageHeightPx * .5f
                scaleX = frame.ballScale
                scaleY = frame.ballScale
                alpha = frame.ballAlpha
                rotationZ = idleRotation + progress * 720f
            }.testTag("welcome-shot-ball"),
        contentScale = ContentScale.Fit
    )
}
