package com.courtquestpro

import android.graphics.Paint
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
import androidx.compose.ui.draw.drawWithContent
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
    PlayerCardStyle.OBSIDIAN -> Color(0xFFC5D4EC)
}

@Composable
internal fun CourtPlayerCard(
    jersey: PlayerJersey, playerName: String, style: PlayerCardStyle, modifier: Modifier = Modifier,
    pose: PlayerCardPose = PlayerCardPose.STANDING, face: PlayerCardFace = PlayerCardFace.SHADOW,
    thumbnail: Boolean = false, appearance: AvatarAppearance = AvatarAppearance(), record: PlayerCardRecord? = null
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
        else "${style.label} player card. $name. ${pose.label} pose. ${face.label} identity. " +
            "${jersey.design.template.label} saved uniform. " +
            (jersey.number?.let { "Jersey number $it." } ?: "Jersey number not set.") + " ${appearance.description}."
    val interaction = if (flipEnabled) Modifier.clickable(onClickLabel = flipLabel, role = Role.Button) { back = !back } else Modifier
    if (reading && record != null) PlayerRecordDialog(name, record) { reading = false }
    Box(modifier.aspectRatio(.69f).then(interaction).semantics(mergeDescendants = true) {
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
            CardSurface(style)
            // Keep both faces registered on one plane. Decoded athlete art stays warm through a turn.
            BoxWithConstraints(Modifier.fillMaxSize().drawWithContent { if (!reverse) drawContent() }) {
                Canvas(Modifier.fillMaxSize()) {
                    val w=size.width; val h=size.height
                    drawRect(Brush.verticalGradient(listOf(Color.Black.copy(alpha=.52f),Color.Transparent),endY=h*.17f))
                    drawOval(Brush.radialGradient(listOf(Color.Black.copy(alpha=.78f),Color.Transparent),
                        center=Offset(w*.5f,h*.846f),radius=w*.29f),
                        topLeft=Offset(w*.18f,h*.815f),size=Size(w*.64f,h*.065f))
                }
                CourtCardAthlete(jersey,PlayerCardIdentity(style,pose,face,appearance),
                    Modifier.fillMaxWidth(.945f).fillMaxHeight(.780f).align(Alignment.TopCenter)
                        .offset(y=maxHeight*.068f))
                Canvas(Modifier.fillMaxSize()) {
                    val w=size.width; val h=size.height
                    drawRect(Brush.verticalGradient(listOf(Color(0x1005070C),Color(0xF0080A10),Color(0xFF090B10)),
                        startY=h*.84f,endY=h*.91f), topLeft=Offset(w*.065f,h*.843f),size=Size(w*.87f,h*.132f))
                    drawLine(style.metal,Offset(w*.09f,h*.853f),Offset(w*.91f,h*.853f),w*.004f)
                    drawContext.canvas.nativeCanvas.apply {
                        printLine(style.edition,w*.5f,h*.072f,w*.044f,w*.75f,style.metal,body)
                        printLine(name.uppercase(Locale.ROOT),w*.5f,h*.919f,w*.092f,w*.79f,Color.White,display)
                        printLine(jersey.number?.let { "Nº $it   /   ${style.label.uppercase(Locale.ROOT)}" }
                            ?: style.label.uppercase(Locale.ROOT),w*.5f,h*.954f,w*.039f,w*.76f,style.metal,body)
                    }
                }
                CardMaterialFinish(style,false)
            }
            if (reverse && record != null) CardReverse(name,jersey.number,style,record,display,body)
        }
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
    Canvas(Modifier.fillMaxSize().testTag("player-card-back")) {
        val w=size.width; val h=size.height
        val ink=style.metal
        val ground=when(style) {
            PlayerCardStyle.CLASSIC -> Color(0xFF34271D)
            PlayerCardStyle.BATTLE -> Color(0xFF2C1812)
            PlayerCardStyle.OBSIDIAN -> Color(0xFF1B2330)
            PlayerCardStyle.HOLOGRAPHIC -> Color(0xFF142D3C)
        }
        drawRect(Brush.linearGradient(listOf(ground,Color(0xFF090C12),ground)),Offset(w*.05f,h*.038f),Size(w*.90f,h*.924f))
        drawLine(ink,Offset(w*.095f,h*.175f),Offset(w*.905f,h*.175f),w*.005f)
        with(drawContext.canvas.nativeCanvas) {
            printLine("COURTQUEST   /   PLAYER RECORD",w*.10f,h*.083f,w*.043f,w*.81f,ink,body,Paint.Align.LEFT)
            printLine(name.uppercase(Locale.ROOT),w*.095f,h*.147f,w*.093f,w*.66f,Color.White,display,Paint.Align.LEFT)
            number?.let { printLine("#$it",w*.905f,h*.146f,w*.077f,w*.16f,ink,display,Paint.Align.RIGHT) }
            var y=.222f
            fun section(heading:String, metrics:List<CardMetric>) {
                printLine(heading,w*.10f,h*y,w*.038f,w*.74f,ink,body,Paint.Align.LEFT)
                y+=.047f
                metrics.forEach { metric ->
                    printLine(metric.label,w*.10f,h*y,w*.051f,w*.49f,Color(0xFFE6E7E6),body,Paint.Align.LEFT)
                    printLine(metric.value ?: "—",w*.90f,h*y,w*.060f,w*.28f,Color.White,display,Paint.Align.RIGHT)
                    y+=.049f
                }
                y+=.014f
            }
            section("TRAINING",record.training)
            section("GAME LOG",record.games)
            section("PROGRESSION",record.progress)
            printLine(if ((record.training+record.games+record.progress).any{it.value==null}) "—  NO RECORD" else style.edition,
                w*.90f,h*.939f,w*.035f,w*.68f,ink,body,Paint.Align.RIGHT)
        }
    }
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
