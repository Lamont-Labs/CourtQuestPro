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
                    val depth=style.heroDepth
                    val centerX=w*(.5f+placement.x)
                    val centerY=h*(placement.top+placement.height*.52f)
                    val radius=w*placement.width*.62f
                    drawCircle(
                        Brush.radialGradient(
                            listOf(style.metal.copy(alpha=depth.ambient),Color.Transparent),
                            center=Offset(centerX,centerY),radius=radius
                        ),radius=radius,center=Offset(centerX,centerY)
                    )
                    val shadowY=h*(placement.top+placement.height*.96f)
                    val shadowWidth=w*placement.width*depth.width
                    val shadowHeight=h*depth.height
                    // Elliptical falloff: normalize the gradient to the actual shadow bounds.
                    for (i in 12 downTo 1) {
                        val scale=i/12f
                        val sw=shadowWidth*scale
                        val sh=shadowHeight*scale
                        drawOval(
                            Color.Black.copy(alpha=depth.opacity/12f),
                            topLeft=Offset(centerX+w*depth.offset-sw*.5f,shadowY-sh*.5f),
                            size=Size(sw,sh)
                        )
                    }
                }
                val placement=style.heroPlacement
                if (style == PlayerCardStyle.CLASSIC) LeatherMount(Modifier.fillMaxSize())
                if (style == PlayerCardStyle.BATTLE) ComicPrintField(Modifier.fillMaxSize())
                if (style == PlayerCardStyle.OBSIDIAN) ArenaSpotlight(Modifier.fillMaxSize())
                CardGarmentHero(jersey, style,
                    Modifier.width(maxWidth * placement.width).height(maxHeight * placement.height)
                        .align(Alignment.TopCenter)
                        .offset(x = maxWidth * placement.x, y = maxHeight * placement.top))
                if (style == PlayerCardStyle.HOLOGRAPHIC) ChromeForeground(Modifier.fillMaxSize())
                Canvas(Modifier.fillMaxSize()) {
                    drawArtworkIdentity(style,name,jersey.number,display,body)
                }
                CardMaterialFinish(style,false)
            }
            if (reverse && record != null) CardReverse(name,jersey.number,style,record,display,body)
        }
    }
}

// One live garment path for every card consumer, including studio and thumbnails.
@Composable
private fun CardGarmentHero(jersey: PlayerJersey, style: PlayerCardStyle, modifier: Modifier) {
    val pose = when (style) {
        PlayerCardStyle.HOLOGRAPHIC -> Triple(-3f, -9f, -2f)
        PlayerCardStyle.CLASSIC -> Triple(1f, 4f, 0f)
        PlayerCardStyle.BATTLE -> Triple(-2f, -5f, -3f)
        PlayerCardStyle.OBSIDIAN -> Triple(-4f, 7f, 1f)
        PlayerCardStyle.PLAYBOOK -> Triple(0f, 0f, 0f)
    }
    Box(modifier.graphicsLayer {
        rotationX = pose.first
        rotationY = pose.second
        rotationZ = pose.third
        cameraDistance = 24 * density
    }) {
        // Offset silhouettes reuse the exact saved garment shape, not a generic jersey outline.
        if (style != PlayerCardStyle.PLAYBOOK) {
            CourtJerseyArtwork(jersey, Modifier.fillMaxSize().graphicsLayer {
                translationX = size.width * .018f
                translationY = size.height * .022f
                alpha = if (style == PlayerCardStyle.BATTLE) .72f else .36f
                if (style == PlayerCardStyle.BATTLE) {
                    // Darken the registered duplicate to create a printed edge.
                    shadowElevation = 0f
                }
            }, view = JerseyView.FRONT, describe = false, compactMark = false)
        }
        CourtJerseyArtwork(jersey, Modifier.fillMaxSize(),
            view = JerseyView.FRONT, describe = true, compactMark = false)
    }
}

// Championship presentation: overhead beams and a restrained golden halo.
@Composable
private fun ArenaSpotlight(modifier: Modifier) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val center = Offset(w*.62f,h*.53f)
        drawCircle(Brush.radialGradient(
            listOf(Color(0xFFFFD36C).copy(alpha=.25f),Color(0xFFB77B24).copy(alpha=.10f),Color.Transparent),
            center=center,radius=w*.43f),radius=w*.43f,center=center)
        for (i in 0..2) {
            val x = w*(.38f+i*.19f)
            val beam = Path().apply {
                moveTo(x-w*.018f,h*.24f)
                lineTo(x+w*.018f,h*.24f)
                lineTo(w*(.80f+i*.035f),h*.79f)
                lineTo(w*(.32f+i*.035f),h*.79f)
                close()
            }
            drawPath(beam,Brush.verticalGradient(
                listOf(Color(0xFFFFE5A4).copy(alpha=.15f),Color(0xFFFFC45A).copy(alpha=.045f),Color.Transparent),
                startY=h*.24f,endY=h*.79f))
        }
        for (i in 0..23) {
            val x=w*(.28f+((i*37)%61)/100f)
            val y=h*(.34f+((i*19)%43)/100f)
            drawCircle(Color(0xFFFFDA86).copy(alpha=.12f+(i%3)*.045f),
                radius=w*(.0015f+(i%4)*.0007f),center=Offset(x,y))
        }
    }
}

// Screen-print registration and halftone falloff behind the live jersey.
@Composable
private fun ComicPrintField(modifier: Modifier) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val center = Offset(w*.52f,h*.55f)
        for (row in 0..18) {
            for (column in 0..18) {
                val x = w*(.23f+column*.032f+(row%2)*.016f)
                val y = h*(.30f+row*.029f)
                val distance = kotlin.math.sqrt(
                    ((x-center.x)/w)*((x-center.x)/w)+((y-center.y)/h)*((y-center.y)/h))
                val strength = (1f-distance/.36f).coerceIn(0f,1f)
                if (strength > 0f) drawCircle(Color(0xFFFFB36B).copy(alpha=.24f*strength),
                    radius=w*(.002f+.005f*strength),center=Offset(x,y))
            }
        }
        for (i in 0..6) {
            val y = h*(.34f+i*.065f)
            drawLine(Color(0xFF101014).copy(alpha=.72f),Offset(w*.18f,y),
                Offset(w*.35f,y+h*.035f),strokeWidth=w*.009f)
            drawLine(Color(0xFFFFB36B).copy(alpha=.64f),Offset(w*.18f,y-h*.006f),
                Offset(w*.35f,y+h*.029f),strokeWidth=w*.003f)
        }
    }
}

// Recessed memorabilia mount: warm light, dark inner bevel and stitched perimeter.
@Composable
private fun LeatherMount(modifier: Modifier) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val left = w*.115f
        val top = h*.165f
        val mw = w*.61f
        val mh = h*.63f
        val corner = androidx.compose.ui.geometry.CornerRadius(w*.035f)
        drawRoundRect(Color.Black.copy(alpha=.48f), Offset(left+w*.012f,top+h*.009f),
            Size(mw,mh),corner)
        drawRoundRect(Brush.verticalGradient(
            listOf(Color(0xFF170E08).copy(alpha=.82f),Color(0xFF4C2915).copy(alpha=.52f),
                Color(0xFF130B07).copy(alpha=.86f)),startY=top,endY=top+mh),
            Offset(left,top),Size(mw,mh),corner)
        drawRoundRect(Brush.radialGradient(
            listOf(Color(0xFFFFC17A).copy(alpha=.23f),Color.Transparent),
            center=Offset(left+mw*.45f,top+mh*.24f),radius=mh*.65f),
            Offset(left,top),Size(mw,mh),corner)
        drawRoundRect(Color(0xFFC48A52).copy(alpha=.62f),Offset(left,top),Size(mw,mh),corner,
            style=androidx.compose.ui.graphics.drawscope.Stroke(width=w*.004f))
        val inset=w*.014f
        drawRoundRect(Color(0xFFF1C18B).copy(alpha=.48f),
            Offset(left+inset,top+inset),Size(mw-2*inset,mh-2*inset),corner,
            style=androidx.compose.ui.graphics.drawscope.Stroke(width=w*.002f,
                pathEffect=PathEffect.dashPathEffect(floatArrayOf(w*.012f,w*.009f))))
    }
}

// Sparse foreground facets overlap the garment edges without obscuring its number.
@Composable
private fun ChromeForeground(modifier: Modifier) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val facets = listOf(
            listOf(Offset(.35f,.27f), Offset(.43f,.31f), Offset(.38f,.43f)),
            listOf(Offset(.83f,.40f), Offset(.76f,.48f), Offset(.87f,.53f)),
            listOf(Offset(.46f,.59f), Offset(.57f,.61f), Offset(.49f,.66f))
        )
        facets.forEachIndexed { index, points ->
            val p = points.map { Offset(it.x*w,it.y*h) }
            val shape = Path().apply {
                moveTo(p[0].x,p[0].y)
                lineTo(p[1].x,p[1].y)
                lineTo(p[2].x,p[2].y)
                close()
            }
            drawPath(shape, Brush.linearGradient(
                listOf(Color(0xFFB8F5FF).copy(alpha=.32f), Color(0xFF7776FF).copy(alpha=.08f), Color.Transparent),
                start=p[0],end=p[2]))
            drawLine(Color(0xFFD9F9FF).copy(alpha=.70f),p[0],p[1],strokeWidth=w*.0025f)
            drawLine((if(index==1) Color(0xFFBD9AFF) else Color(0xFF62DFFF)).copy(alpha=.45f),
                p[1],p[2],strokeWidth=w*.0018f)
        }
    }
}

// Edition-specific lighting, kept behind the live uniform and above the original artwork.
private data class HeroDepth(val ambient:Float,val width:Float,val height:Float,val opacity:Float,val offset:Float)
private val PlayerCardStyle.heroDepth:HeroDepth get()=when(this) {
    PlayerCardStyle.HOLOGRAPHIC->HeroDepth(.22f,.84f,.038f,.56f,.012f)
    PlayerCardStyle.CLASSIC->HeroDepth(.13f,.92f,.055f,.78f,.018f)
    PlayerCardStyle.BATTLE->HeroDepth(.10f,.94f,.055f,.82f,.022f)
    PlayerCardStyle.OBSIDIAN->HeroDepth(.18f,.86f,.038f,.72f,.012f)
    PlayerCardStyle.PLAYBOOK->HeroDepth(.065f,.88f,.040f,.66f,.008f)
}

private data class HeroPlacement(val width:Float,val height:Float,val x:Float,val top:Float)
private val PlayerCardStyle.heroPlacement:HeroPlacement get()=when(this) {
    // Keep the embedded mid-left wordmark clear; use the upper central field.
    PlayerCardStyle.HOLOGRAPHIC->HeroPlacement(.48f,.43f,.13f,.19f)
    PlayerCardStyle.CLASSIC->HeroPlacement(.56f,.60f,-.08f,.18f)
    PlayerCardStyle.BATTLE->HeroPlacement(.48f,.49f,.02f,.32f)
    PlayerCardStyle.OBSIDIAN->HeroPlacement(.49f,.54f,.12f,.31f)
    PlayerCardStyle.PLAYBOOK->HeroPlacement(.48f,.48f,0f,.31f)
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
        printLine(name.uppercase(Locale.ROOT),w*.5f,h*(if(style==PlayerCardStyle.PLAYBOOK) .932f else .946f),w*.072f,w*.82f,Color.White,display)
        val identity=number?.let{"#$it  ·  ${style.edition}"}?:style.edition
        printLine(identity.uppercase(Locale.ROOT),w*.5f,h*(if(style==PlayerCardStyle.PLAYBOOK) .963f else .978f),w*.031f,w*.82f,style.metal,body)
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
