package com.courtquestpro

import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat
import java.util.Locale

internal val PlayerCardStyle.metal: Color get() = when (this) {
    PlayerCardStyle.HOLOGRAPHIC -> Color(0xFFA9EFFA)
    PlayerCardStyle.CLASSIC -> Color(0xFFF2D39C)
    PlayerCardStyle.BATTLE -> Color(0xFFFF9B58)
    PlayerCardStyle.OBSIDIAN -> Color(0xFFFFD36C)
    PlayerCardStyle.PLAYBOOK -> Color(0xFF58AFFF)
}

@Composable
internal fun CourtPlayerCard(
    jersey: PlayerJersey, playerName: String, style: PlayerCardStyle, modifier: Modifier = Modifier,
    thumbnail: Boolean = false, record: PlayerCardRecord? = null
) {
    val context = LocalContext.current
    val display = remember(context) { ResourcesCompat.getFont(context, R.font.barlow_condensed_extra_bold) }
    val body = remember(context) { ResourcesCompat.getFont(context, R.font.barlow_semibold) }
    val name = playerName.ifBlank { "Your player" }
    var back by rememberSaveable { mutableStateOf(false) }
    var reading by rememberSaveable { mutableStateOf(false) }
    val flipEnabled = record != null && !thumbnail
    val targetBack = flipEnabled && back
    val rotation by animateFloatAsState(if (targetBack) 180f else 0f, tween(440, easing = FastOutSlowInEasing), label = "Card turn")
    val reverse = rotation > 90f
    val frame = RoundedCornerShape(if (style == PlayerCardStyle.CLASSIC) 5.dp else 10.dp)
    val flipLabel = if (targetBack) "Show card front" else "Show player record"
    val description = if (reverse && record != null) "${style.label}. $name. Player record. ${record.description}"
        else "${style.label} jersey collectible card. $name. " +
            "${jersey.design.template.label} saved uniform. " +
            (jersey.number?.let { "Jersey number $it." } ?: "Jersey number not set.")
    val interaction = if (flipEnabled) Modifier.clickable(onClickLabel = flipLabel, role = Role.Button) { back = !back } else Modifier
    if (reading && record != null) PlayerRecordDialog(name, record) { reading = false }
    Box(modifier.aspectRatio(PLAYER_CARD_ASPECT_RATIO).then(interaction).semantics(mergeDescendants = true) {
        contentDescription = description
        if (flipEnabled) {
            stateDescription = if (targetBack) "Card back" else "Card front"
            customActions = listOf(CustomAccessibilityAction("Read player statistics") { reading = true; true })
        }
    }.testTag(if (thumbnail) "card-sample" else "player-card-preview")) {
        Box(Modifier.fillMaxSize().graphicsLayer {
            rotationY = if (reverse) rotation - 180f else rotation
            cameraDistance = 24 * density
        }.clip(frame).background(Color(0xFF090C11)).border(1.dp,style.metal.copy(alpha=.8f),frame)) {
            if (!reverse) CardSurface(style,CardArtworkFace.FRONT,thumbnail)
            // User artwork supplies the card-specific frame; the saved uniform and identity stay native.
            if (!reverse) BoxWithConstraints(Modifier.fillMaxSize()) {
                Canvas(Modifier.fillMaxSize()) {
                    val w=size.width;val h=size.height
                    val placement=style.heroPlacement
                    val shadowY=h*(placement.top+placement.height*.98f)
                    val centerX=w*(.5f+placement.x)
                    val shadowWidth=w*placement.width*.72f
                    drawOval(
                        Brush.radialGradient(
                            listOf(Color.Black.copy(alpha=.52f),Color.Transparent),
                            center=Offset(centerX,shadowY),radius=shadowWidth*.5f
                        ),
                        topLeft=Offset(centerX-shadowWidth*.5f,shadowY-h*.018f),
                        size=Size(shadowWidth,h*.036f)
                    )
                }
                val placement=style.heroPlacement
                CourtJerseyArtwork(
                    jersey,
                    Modifier.width(maxWidth * placement.width).height(maxHeight * placement.height)
                        .align(Alignment.TopCenter)
                        .offset(x = maxWidth * placement.x, y = maxHeight * placement.top),
                    view = JerseyView.FRONT,
                    describe = true,
                    compactMark = false
                )
                Canvas(Modifier.fillMaxSize()) {
                    drawArtworkIdentity(style,name,jersey.number,display,body)
                }
                CardMaterialFinish(style,false)
            }
            if (reverse && record != null) CardReverse(name,jersey.number,style,record,display,body)
        }
    }
}

private data class HeroPlacement(val width:Float,val height:Float,val x:Float,val top:Float)
private val PlayerCardStyle.heroPlacement:HeroPlacement get()=when(this) {
    PlayerCardStyle.HOLOGRAPHIC->HeroPlacement(.40f,.39f,-.20f,.40f)
    PlayerCardStyle.CLASSIC->HeroPlacement(.56f,.60f,-.08f,.18f)
    PlayerCardStyle.BATTLE->HeroPlacement(.48f,.49f,.02f,.32f)
    PlayerCardStyle.OBSIDIAN->HeroPlacement(.46f,.49f,.13f,.30f)
    PlayerCardStyle.PLAYBOOK->HeroPlacement(.53f,.53f,.04f,.25f)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawArtworkIdentity(
    style:PlayerCardStyle,name:String,number:String?,display:Typeface?,body:Typeface?
) {
    val w=size.width;val h=size.height
    drawRect(
        Brush.verticalGradient(
            listOf(Color.Transparent,Color.Black.copy(alpha=.20f),Color.Black.copy(alpha=.34f)),
            startY=h*.885f,endY=h
        ),
        topLeft=Offset(0f,h*.885f),size=Size(w,h*.115f)
    )
    if (style == PlayerCardStyle.OBSIDIAN) {
        drawRect(Color(0xFF090C11).copy(alpha=.94f),
            topLeft=Offset(w*.07f,h*.906f),size=Size(w*.86f,h*.084f))
    }
    drawContext.canvas.nativeCanvas.apply {
        printLine(name.uppercase(Locale.ROOT),w*.5f,h*.946f,w*.072f,w*.82f,Color.White,display)
        val identity=number?.let{"#$it  ·  ${style.edition}"}?:style.edition
        printLine(identity.uppercase(Locale.ROOT),w*.5f,h*.978f,w*.031f,w*.82f,style.metal,body)
    }
}

private fun android.graphics.Canvas.printLine(value:String,x:Float,y:Float,size:Float,max:Float,
    color:Color,font:Typeface?,align:Paint.Align=Paint.Align.CENTER) {
    val p=Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
        typeface=font; textSize=size; this.color=color.toArgb(); textAlign=align
    }
    val measured=p.measureText(value)
    if(measured>max) p.textSize*=max/measured
    drawText(value,x,y,p)
}

@Composable
private fun CardReverse(name:String,number:String?,style:PlayerCardStyle,record:PlayerCardRecord,display:Typeface?,body:Typeface?) {
    val panel=style.recordPanel
    Box(Modifier.fillMaxSize()) {
        CardSurface(style,CardArtworkFace.BACK)
        Canvas(Modifier.fillMaxSize().testTag("player-card-back")) {
            val w=size.width;val h=size.height
            val ink=style.metal
            val panelWidth=panel.right-panel.left
            val panelHeight=panel.bottom-panel.top
            val panelRect=RectF(w*panel.left,h*panel.top,w*panel.right,h*panel.bottom)
            val nativeCanvas=drawContext.canvas.nativeCanvas
            nativeCanvas.drawRoundRect(panelRect,w*.018f,h*.012f,
                Paint(Paint.ANTI_ALIAS_FLAG).apply { color=Color(0xFF080A0D).copy(alpha=.84f).toArgb() })
            val panelStroke=Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color=ink.copy(alpha=.28f).toArgb()
                this.style=Paint.Style.STROKE
                strokeWidth=w*.002f
            }
            nativeCanvas.drawRoundRect(panelRect,w*.018f,h*.012f,panelStroke)
            with(drawContext.canvas.nativeCanvas) {
                printLine("PLAYER RECORD",w*(panel.left+.035f),
                    h*(panel.top+panelHeight*.08f),w*.034f,w*(panel.right-panel.left)*.90f,ink,body,Paint.Align.LEFT)
                printLine(name.uppercase(Locale.ROOT),w*(panel.left+.035f),
                    h*(panel.top+panelHeight*.19f),w*.074f,w*panelWidth*(if(number.isNullOrBlank()) .90f else .65f),Color.White,display,Paint.Align.LEFT)
                number?.takeIf { it.isNotBlank() }?.let {
                    printLine("#$it",w*(panel.right-.035f),h*(panel.top+panelHeight*.19f),w*.060f,
                        w*(panel.right-panel.left)*.20f,ink,display,Paint.Align.RIGHT)
                }
                val sections=listOf(
                    "TRAINING" to record.training,
                    "GAME LOG" to record.games,
                    "PROGRESSION" to record.progress
                )
                val rowCount=sections.sumOf { it.second.size }.coerceAtLeast(1)
                val bodyTop=panel.top+panelHeight*.29f
                val bodyBottom=panel.bottom-panelHeight*.10f
                val step=(bodyBottom-bodyTop)/(rowCount+sections.size*1.25f)
                val rulePaint=Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color=ink.copy(alpha=.24f).toArgb(); strokeWidth=w*.0015f
                }
                val rowFont=minOf(w*.043f,h*step*.72f)
                val headingFont=minOf(w*.035f,h*step*.70f)
                var y=bodyTop
                sections.forEach { (heading,metrics) ->
                    drawLine(w*(panel.left+.045f),h*(y-step*.78f),w*(panel.right-.04f),h*(y-step*.78f),rulePaint)
                    printLine(heading,w*(panel.left+.045f),h*y,headingFont,
                        w*(panel.right-panel.left)*.86f,ink,body,Paint.Align.LEFT)
                    y+=step
                    metrics.forEach { metric ->
                        printLine(metric.label,w*(panel.left+.045f),h*y,rowFont,
                            w*panelWidth*.54f,Color(0xFFE8E8E8),body,Paint.Align.LEFT)
                        printLine(metric.value ?: "—",w*(panel.right-.04f),h*y,rowFont,
                            w*panelWidth*.28f,Color.White,display,Paint.Align.RIGHT)
                        y+=step
                    }
                    y+=step*.25f
                }
                printLine(
                    if(record.hasRecordedActivity) "SAVED PLAYER RECORD" else "NO SAVED TRAINING OR GAME DATA",
                    w*(panel.right-.04f),h*(panel.bottom-panelHeight*.035f),w*.025f,
                    w*(panel.right-panel.left)*.88f,ink,body,Paint.Align.RIGHT
                )
            }
        }
    }
}

private data class RecordPanel(val left:Float,val right:Float,val top:Float,val bottom:Float)
private val PlayerCardStyle.recordPanel:RecordPanel get()=when(this) {
    PlayerCardStyle.PLAYBOOK->RecordPanel(.10f,.90f,.18f,.81f)
    PlayerCardStyle.CLASSIC->RecordPanel(.075f,.925f,.105f,.88f)
    PlayerCardStyle.OBSIDIAN->RecordPanel(.09f,.91f,.12f,.735f)
    PlayerCardStyle.HOLOGRAPHIC->RecordPanel(.14f,.92f,.205f,.84f)
    PlayerCardStyle.BATTLE->RecordPanel(.085f,.915f,.17f,.845f)
}

@Composable
internal fun PlayerRecordDialog(name:String,record:PlayerCardRecord,onDismiss:()->Unit) {
    AlertDialog(onDismissRequest=onDismiss,title={Text(name.ifBlank{"Player record"})},text={
        Column(Modifier.verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(6.dp)) {
            for ((heading,items) in listOf("Training" to record.training,"Game log" to record.games,"Progression" to record.progress)) {
                Text(heading,color=CourtQuestDesign.accent,modifier=Modifier.semantics { heading() })
                items.forEach { metric -> Text("${metric.label}  ·  ${metric.value ?: "No record"}") }
            }
        }
    },confirmButton={TextButton(onDismiss){Text("Done")}})
}
