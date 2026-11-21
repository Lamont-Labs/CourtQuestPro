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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
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
            // Keep both faces registered on one plane and the live saved jersey aligned across editions.
            BoxWithConstraints(Modifier.fillMaxSize().drawWithContent { if (!reverse) drawContent() }) {
                Canvas(Modifier.fillMaxSize()) {
                    val w=size.width; val h=size.height
                    drawRect(Brush.verticalGradient(listOf(Color.Black.copy(alpha=.52f),Color.Transparent),endY=h*.17f))
                    drawOval(Brush.radialGradient(listOf(Color.Black.copy(alpha=.78f),Color.Transparent),
                        center=Offset(w*.5f,h*.846f),radius=w*.29f),
                        topLeft=Offset(w*.18f,h*.815f),size=Size(w*.64f,h*.065f))
                    drawEditionSpotlight(style)
                    // Sports-card framing: double metallic keyline, edition masthead,
                    // and lower corner numbering remain legible at thumbnail size.
                    val metal=style.metal
                    drawRoundRect(metal.copy(alpha=.92f),
                        topLeft=Offset(w*.026f,h*.018f),size=Size(w*.948f,h*.964f),
                        cornerRadius=androidx.compose.ui.geometry.CornerRadius(w*.028f),
                        style=Stroke(width=w*.007f))
                    drawRoundRect(metal.copy(alpha=.42f),
                        topLeft=Offset(w*.043f,h*.032f),size=Size(w*.914f,h*.936f),
                        cornerRadius=androidx.compose.ui.geometry.CornerRadius(w*.018f),
                        style=Stroke(width=w*.0025f))
                    drawRect(Brush.verticalGradient(listOf(Color(0xF00A1019),Color(0xB0091019),Color.Transparent),
                        startY=h*.025f,endY=h*.17f),topLeft=Offset(w*.052f,h*.048f),size=Size(w*.896f,h*.13f))
                    drawContext.canvas.nativeCanvas.apply {
                        printLine("COURTQUEST  /  BASKETBALL",w*.085f,h*.093f,w*.036f,w*.62f,
                            Color.White,body,Paint.Align.LEFT)
                        printLine(style.label.uppercase(Locale.ROOT),w*.915f,h*.094f,w*.032f,w*.30f,
                            metal,display,Paint.Align.RIGHT)
                    }
                    drawLine(metal.copy(alpha=.76f),Offset(w*.075f,h*.116f),
                        Offset(w*.925f,h*.116f),w*.0025f)
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
                    drawEditionNameplate(style,name,jersey.number,display,body)
                }
                CardMaterialFinish(style,false)
            }
            if (reverse && record != null) CardReverse(name,jersey.number,style,record,display,body)
        }
    }
}

private data class HeroPlacement(val width:Float,val height:Float,val x:Float,val top:Float)
private val PlayerCardStyle.heroPlacement:HeroPlacement get()=when(this) {
    PlayerCardStyle.HOLOGRAPHIC->HeroPlacement(.945f,.780f,0f,.068f)
    PlayerCardStyle.CLASSIC->HeroPlacement(.865f,.748f,-.008f,.086f)
    PlayerCardStyle.BATTLE->HeroPlacement(.985f,.800f,-.014f,.040f)
    PlayerCardStyle.OBSIDIAN->HeroPlacement(.885f,.770f,.030f,.073f)
}

/** Edition-specific lighting and framing give the saved jersey distinct collectible compositions. */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawEditionSpotlight(style:PlayerCardStyle) {
    val w=size.width;val h=size.height
    val center=Offset(w*when(style){PlayerCardStyle.OBSIDIAN->.56f;else->.5f},h*.43f)
    val radius=w*when(style){PlayerCardStyle.CLASSIC->.42f;PlayerCardStyle.BATTLE->.52f;else->.46f}
    val glow=when(style) {
        PlayerCardStyle.HOLOGRAPHIC->Color(0xFF75DDF2)
        PlayerCardStyle.CLASSIC->Color(0xFFE8B764)
        PlayerCardStyle.BATTLE->Color(0xFFFF5B0A)
        PlayerCardStyle.OBSIDIAN->Color(0xFF8BA8D0)
    }
    drawCircle(Brush.radialGradient(listOf(glow.copy(alpha=.23f),glow.copy(alpha=.06f),Color.Transparent),
        center=center,radius=radius),radius=radius,center=center)
    when(style) {
        PlayerCardStyle.HOLOGRAPHIC -> {
            drawLine(Color.Cyan.copy(alpha=.24f),Offset(w*.13f,h*.28f),Offset(w*.37f,h*.12f),w*.006f)
            drawLine(Color(0xFFB197FF).copy(alpha=.30f),Offset(w*.88f,h*.30f),Offset(w*.67f,h*.14f),w*.005f)
        }
        PlayerCardStyle.CLASSIC -> {
            drawArc(Color(0xFFF2D39C).copy(alpha=.24f),180f,180f,false,
                Offset(w*.14f,h*.27f),Size(w*.72f,h*.60f),style=Stroke(w*.004f))
            drawLine(Color(0xFFFFE8B8).copy(alpha=.18f),Offset(w*.19f,h*.34f),Offset(w*.81f,h*.34f),w*.003f)
        }
        PlayerCardStyle.BATTLE -> {
            drawPath(Path().apply {
                moveTo(0f,h*.23f);lineTo(w*.13f,h*.19f);lineTo(w*.04f,h*.40f);lineTo(0f,h*.43f);close()
            },Brush.linearGradient(listOf(glow.copy(alpha=.48f),Color.Transparent)))
            drawPath(Path().apply {
                moveTo(w,h*.29f);lineTo(w*.88f,h*.24f);lineTo(w*.97f,h*.47f);lineTo(w,h*.49f);close()
            },Brush.linearGradient(listOf(Color.Transparent,glow.copy(alpha=.42f))))
            drawLine(glow.copy(alpha=.22f),Offset(w*.02f,h*.58f),Offset(w*.19f,h*.46f),w*.009f)
        }
        PlayerCardStyle.OBSIDIAN -> {
            drawLine(Color.White.copy(alpha=.14f),Offset(w*.83f,h*.22f),Offset(w*.96f,h*.79f),w*.003f)
            drawLine(glow.copy(alpha=.28f),Offset(w*.08f,h*.73f),Offset(w*.18f,h*.44f),w*.004f)
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawEditionNameplate(
    style:PlayerCardStyle,name:String,number:String?,display:Typeface?,body:Typeface?
) {
    val w=size.width;val h=size.height
    val left=w*.065f;val right=w*.935f;val top=h*.846f;val bottom=h*.978f
    val plate=Path()
    when(style) {
        PlayerCardStyle.HOLOGRAPHIC -> plate.apply {
            moveTo(left,top+h*.014f);lineTo(w*.12f,top);lineTo(w*.88f,top);lineTo(right,top+h*.014f)
            lineTo(right,bottom-h*.018f);lineTo(w*.87f,bottom);lineTo(w*.13f,bottom);lineTo(left,bottom-h*.018f);close()
        }
        PlayerCardStyle.CLASSIC -> plate.apply {
            moveTo(left+12,bottom);lineTo(left,bottom-12);lineTo(left,top+12);lineTo(left+12,top)
            lineTo(right-12,top);lineTo(right,top+12);lineTo(right,bottom-12);lineTo(right-12,bottom);close()
        }
        PlayerCardStyle.BATTLE -> plate.apply {
            moveTo(left,top+h*.035f);lineTo(w*.17f,top);lineTo(right,top);lineTo(right-h*.024f,bottom)
            lineTo(w*.11f,bottom);lineTo(left,bottom-h*.018f);close()
        }
        PlayerCardStyle.OBSIDIAN -> plate.apply {
            moveTo(left,top);lineTo(right-h*.012f,top);lineTo(right,bottom-h*.02f)
            lineTo(w*.78f,bottom);lineTo(left,bottom);close()
        }
    }
    val fill=when(style) {
        PlayerCardStyle.CLASSIC->Color(0xFFF1E4C7)
        PlayerCardStyle.BATTLE->Color(0xF00A0C11)
        PlayerCardStyle.HOLOGRAPHIC->Color(0xEE09141D)
        PlayerCardStyle.OBSIDIAN->Color(0xF0070A10)
    }
    drawPath(plate,fill)
    drawPath(plate,style.metal.copy(alpha=.9f),style=Stroke(w*.0035f))
    when(style) {
        PlayerCardStyle.HOLOGRAPHIC -> {
            drawLine(Color.Cyan.copy(alpha=.88f),Offset(w*.14f,top+h*.021f),Offset(w*.86f,top+h*.021f),w*.003f)
            drawLine(Color(0xFFB197FF).copy(alpha=.75f),Offset(w*.15f,bottom-h*.015f),Offset(w*.85f,bottom-h*.015f),w*.002f)
        }
        PlayerCardStyle.CLASSIC -> {
            drawRoundRect(Color(0xFFD4B477),Offset(w*.095f,top+h*.012f),Size(w*.81f,h*.105f),
                cornerRadius=androidx.compose.ui.geometry.CornerRadius(5.dp.toPx()),style=Stroke(w*.002f))
            drawCircle(Color(0xFFD0A953),w*.026f,Offset(w*.875f,(top+bottom)*.51f))
            drawCircle(Color(0xFF5D421D),w*.012f,Offset(w*.875f,(top+bottom)*.51f))
        }
        PlayerCardStyle.BATTLE -> {
            drawPath(Path().apply {
                moveTo(left,top+h*.035f);lineTo(w*.35f,top+h*.035f);lineTo(w*.31f,top+h*.050f);lineTo(left,top+h*.050f);close()
            },Color(0xFFFF5B0A))
            drawLine(Color(0xFFFF5B0A),Offset(w*.78f,bottom-h*.013f),Offset(right,bottom-h*.013f),w*.004f)
        }
        PlayerCardStyle.OBSIDIAN -> {
            drawLine(Color.White.copy(alpha=.56f),Offset(w*.77f,top+h*.012f),Offset(w*.77f,bottom-h*.012f),w*.002f)
            drawLine(style.metal.copy(alpha=.36f),Offset(w*.10f,bottom-h*.014f),Offset(w*.74f,bottom-h*.014f),w*.002f)
        }
    }
    val nameColor=if(style==PlayerCardStyle.CLASSIC) Color(0xFF241A12) else Color.White
    val infoColor=if(style==PlayerCardStyle.CLASSIC) Color(0xFF79521F) else style.metal
    val nameLeft=style==PlayerCardStyle.BATTLE || style==PlayerCardStyle.OBSIDIAN
    val x=if(nameLeft) w*.15f else w*.5f
    val align=if(nameLeft) Paint.Align.LEFT else Paint.Align.CENTER
    val maxName=if(style==PlayerCardStyle.OBSIDIAN) w*.58f else if(nameLeft) w*.70f else w*.78f
    drawContext.canvas.nativeCanvas.apply {
        printLine(name.uppercase(Locale.ROOT),x,h*.921f,w*.090f,maxName,nameColor,display,align)
        val info=when(style) {
            PlayerCardStyle.CLASSIC -> number?.let{"COURT LEGENDS  ·  #$it"} ?: "COURT LEGENDS"
            PlayerCardStyle.BATTLE -> number?.let{"#$it  /  BATTLE SERIES"} ?: "BATTLE SERIES"
            PlayerCardStyle.OBSIDIAN -> number?.let{"NO. $it  ·  OBSIDIAN CHROME"} ?: "OBSIDIAN CHROME"
            PlayerCardStyle.HOLOGRAPHIC -> number?.let{"Nº $it   /   HOLOGRAPHIC ELITE"} ?: "HOLOGRAPHIC ELITE"
        }
        val infoAlign=if(style==PlayerCardStyle.OBSIDIAN) Paint.Align.LEFT else Paint.Align.CENTER
        val infoX=if(style==PlayerCardStyle.OBSIDIAN) w*.15f else w*.5f
        printLine(info.uppercase(Locale.ROOT),infoX,h*.957f,w*.036f,w*.72f,infoColor,body,infoAlign)
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
        drawLine(ink.copy(alpha=.78f),Offset(w*.095f,h*.175f),Offset(w*.905f,h*.175f),w*.005f)
        with(drawContext.canvas.nativeCanvas) {
            printLine("COURTQUEST   /   PLAYER RECORD",w*.10f,h*.083f,w*.043f,w*.81f,ink,body,Paint.Align.LEFT)
            printLine(name.uppercase(Locale.ROOT),w*.095f,h*.147f,w*.093f,w*.66f,Color.White,display,Paint.Align.LEFT)
            number?.let { printLine("#$it",w*.905f,h*.146f,w*.077f,w*.16f,ink,display,Paint.Align.RIGHT) }
            var y=.222f
            fun section(heading:String, metrics:List<CardMetric>) {
                val sectionTop=y-.027f
                val sectionBottom=sectionTop+.054f+metrics.size*.049f+.012f
                val panel=Paint(Paint.ANTI_ALIAS_FLAG).apply{color=ink.copy(alpha=.065f).toArgb()}
                drawRoundRect(RectF(w*.065f,h*sectionTop,w*.935f,h*sectionBottom),w*.022f,h*.014f,panel)
                val edge=Paint(Paint.ANTI_ALIAS_FLAG).apply{color=ink.copy(alpha=.52f).toArgb();strokeWidth=w*.003f}
                drawLine(w*.078f,h*(sectionTop+.014f),w*.078f,h*(sectionBottom-.014f),edge)
                printLine(heading,w*.105f,h*y,w*.035f,w*.74f,ink,body,Paint.Align.LEFT)
                y+=.047f
                metrics.forEach { metric ->
                    printLine(metric.label,w*.105f,h*y,w*.047f,w*.49f,Color(0xFFE6E7E6),body,Paint.Align.LEFT)
                    printLine(metric.value ?: "—",w*.90f,h*y,w*.057f,w*.28f,Color.White,display,Paint.Align.RIGHT)
                    y+=.049f
                }
                y+=.014f
            }
            section("TRAINING",record.training)
            section("GAME LOG",record.games)
            section("PROGRESSION",record.progress)
            printLine(if(record.hasRecordedActivity) "SAVED PLAYER RECORD" else "NO SAVED TRAINING OR GAME DATA",
                w*.90f,h*.941f,w*.034f,w*.72f,ink,body,Paint.Align.RIGHT)
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
