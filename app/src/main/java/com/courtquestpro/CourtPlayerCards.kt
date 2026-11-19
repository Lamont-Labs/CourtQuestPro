package com.courtquestpro

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat
import java.util.Locale
import kotlin.math.sin

/** A collectible graphic with a complete accessible identity; all metrics stay below the card. */
@Composable
internal fun CourtPlayerCard(
    jersey: PlayerJersey,
    playerName: String,
    style: PlayerCardStyle,
    modifier: Modifier = Modifier,
    pose: PlayerCardPose = PlayerCardPose.STANDING,
    face: PlayerCardFace = PlayerCardFace.SHADOW,
    thumbnail: Boolean = false
) {
    val identity = PlayerCardIdentity(style, pose, face)
    val context = LocalContext.current
    val display = remember(context) { ResourcesCompat.getFont(context, R.font.barlow_condensed_extra_bold) }
    val body = remember(context) { ResourcesCompat.getFont(context, R.font.barlow_bold) }
    val classic = style == PlayerCardStyle.CLASSIC
    val frame = if (classic) RoundedCornerShape(9.dp) else RoundedCornerShape(20.dp)
    val accent = when (style) {
        PlayerCardStyle.HOLOGRAPHIC -> Color(0xFF9DF1FF)
        PlayerCardStyle.CLASSIC -> Color(0xFFE4C486)
        PlayerCardStyle.BATTLE -> Color(0xFFFF7A24)
    }
    val name = playerName.ifBlank { "YOUR PLAYER" }.uppercase(Locale.ROOT)
    val description = "${style.label} player card. ${playerName.ifBlank { "Player name not set" }}. " +
        "${pose.label} pose. ${face.label} identity. ${jersey.design.template.label} saved uniform. " +
        (jersey.number?.let { "Jersey number $it." } ?: "Jersey number not set.")
    Box(modifier.aspectRatio(.69f).clip(frame).background(Color(0xFF080B12))
        .border(if (thumbnail) 1.dp else 1.5.dp, Brush.linearGradient(listOf(accent, Color.White.copy(alpha=.8f),
            accent.copy(alpha=.25f), accent)), frame)
        .semantics(mergeDescendants = true) { contentDescription = description }
        .testTag("player-card-preview")) {
        Canvas(Modifier.fillMaxSize()) {
            val w=size.width; val h=size.height
            when(style) {
                PlayerCardStyle.HOLOGRAPHIC -> {
                    drawRect(Brush.linearGradient(listOf(Color(0xFF060E25),Color(0xFF262056),Color(0xFF103D4A),Color(0xFF080A17))))
                    val colors = listOf(Color(0xFF67E6FF),Color(0xFFCA8CFF),Color(0xFFDAE8FB))
                    repeat(13) { i ->
                        val x=w*(i-4)*.12f
                        val path=Path().apply { moveTo(x,0f);lineTo(x+w*.34f,0f);lineTo(x+w*.95f,h);lineTo(x+w*.84f,h);close() }
                        drawPath(path,Brush.linearGradient(listOf(colors[i%3].copy(alpha=.02f), colors[i%3].copy(alpha=.16f),Color.Transparent)))
                    }
                    drawCircle(Brush.radialGradient(listOf(Color(0xFF7BE3FF).copy(alpha=.24f),Color.Transparent)),
                        radius=w*.85f,center=Offset(w*.5f,h*.43f))
                    repeat(3) { i ->
                        val inset=w*(.035f+i*.012f)
                        drawRoundRect(accent.copy(alpha=.38f-i*.09f),Offset(inset,inset),Size(w-inset*2,h-inset*2),
                            androidx.compose.ui.geometry.CornerRadius(w*.05f),style=Stroke(w*.0025f))
                    }
                    repeat(30) { i ->
                        val x=(sin(i*42.7)*.5+.5).toFloat()*w
                        val y=(sin(i*18.1)*.5+.5).toFloat()*h
                        drawCircle(Color.White.copy(alpha=.3f),w*.0017f,Offset(x,y))
                    }
                }
                PlayerCardStyle.CLASSIC -> {
                    drawRect(Color(0xFF211E18))
                    repeat(56) { i -> drawLine(Color(0xFFDBC7A0).copy(alpha=.03f),
                        Offset(0f,h*i/56),Offset(w,h*i/56),w*.002f) }
                    drawRect(Brush.verticalGradient(listOf(Color(0xFF8B6838),Color(0xFFEAD6A8),Color(0xFF665038))),
                        Offset(w*.035f,h*.025f),Size(w*.93f,h*.95f))
                    drawRect(Color(0xFF101D21),Offset(w*.07f,h*.052f),Size(w*.86f,h*.896f))
                    drawRect(Brush.verticalGradient(listOf(Color(0xFF294449),Color(0xFF0C171A),Color(0xFF383021))),
                        Offset(w*.09f,h*.14f),Size(w*.82f,h*.66f))
                    repeat(5) { i ->
                        drawLine(accent.copy(alpha=.10f),Offset(w*.11f,h*(.25f+i*.09f)),Offset(w*.89f,h*(.25f+i*.09f)),w*.003f)
                    }
                    drawOval(accent.copy(alpha=.16f),Offset(w*.16f,h*.20f),Size(w*.68f,h*.56f),style=Stroke(w*.003f))
                    val ribbon=Path().apply { moveTo(w*.04f,h*.79f);lineTo(w*.96f,h*.77f);lineTo(w*.96f,h*.91f);lineTo(w*.04f,h*.93f);close() }
                    drawPath(ribbon,Brush.verticalGradient(listOf(Color(0xFF9A733D),Color(0xFFE8D2A0),Color(0xFFAA854B))))
                    drawPath(ribbon,Color(0xFFF0DBB2),style=Stroke(w*.004f))
                }
                PlayerCardStyle.BATTLE -> {
                    drawRect(Brush.linearGradient(listOf(Color(0xFF050506),Color(0xFF17100C),Color(0xFF50210E),Color.Black)))
                    drawCircle(Brush.radialGradient(listOf(Color(0xFFFF5B0A).copy(alpha=.45f),Color.Transparent)),
                        w*.9f,Offset(w*.68f,h*.38f))
                    repeat(11) { i ->
                        val x=w*(i*.16f-.5f)
                        val path=Path().apply { moveTo(x,h);lineTo(x+w*.5f,0f);lineTo(x+w*.54f,0f);lineTo(x+w*.035f,h);close() }
                        drawPath(path,Color(0xFFFF7A24).copy(alpha=if(i%3==0) .25f else .05f))
                    }
                    val cuts=Path().apply {
                        moveTo(w*.04f,h*.23f);lineTo(w*.04f,h*.06f);lineTo(w*.25f,h*.025f)
                        moveTo(w*.75f,h*.975f);lineTo(w*.96f,h*.94f);lineTo(w*.96f,h*.77f)
                    }
                    drawPath(cuts,accent,style=Stroke(w*.014f))
                    repeat(22) { i ->
                        val x=(sin(i*10.7)*.5+.5).toFloat()*w
                        val y=h*(.3f+(sin(i*7.1)*.5+.5).toFloat()*.5f)
                        drawLine(accent.copy(alpha=.45f),Offset(x,y),Offset(x+w*.015f,y-h*.02f),w*.002f)
                    }
                }
            }
        }
        // Pose compositions remain full-body; each edition has a different art window.
        CourtCardAthlete(jersey, identity, Modifier.fillMaxWidth(if(classic) .86f else .96f)
            .fillMaxHeight(if(classic) .69f else .74f)
            .align(Alignment.TopCenter).padding(top=if(classic) 35.dp else 25.dp))
        Canvas(Modifier.fillMaxSize()) {
            val w=size.width; val h=size.height
            if (!classic) drawRect(Brush.verticalGradient(listOf(Color.Transparent,Color(0xEF06080E))),
                topLeft=Offset(0f,h*.68f),size=Size(w,h*.32f))
            drawContext.canvas.nativeCanvas.apply {
                fun title(value:String,x:Float,y:Float,cap:Float,color:Color,font:Typeface?,max:Float,align:Paint.Align=Paint.Align.CENTER) {
                    val paint=Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface=font;textSize=w*cap;this.color=color.toArgb();textAlign=align }
                    if(paint.measureText(value)>w*max) paint.textSize*=w*max/paint.measureText(value)
                    drawText(value,w*x,h*y,paint)
                }
                title(style.edition,.5f,.115f,if(classic) .032f else .029f,accent,body,.78f)
                title(name,.5f,if(classic) .852f else .84f,.100f,if(classic) Color(0xFF182023) else Color.White,
                    if(classic) Typeface.create(Typeface.SERIF,Typeface.BOLD) else display,.80f)
                val number=jersey.number?.let{"NO. $it"} ?: "YOUR IDENTITY"
                title(number,.5f,if(classic) .892f else .885f,.035f,if(classic) Color(0xFF40301F) else accent,body,.75f)
                title(when(style) {
                    PlayerCardStyle.HOLOGRAPHIC -> "HOLOGRAPHIC ELITE"
                    PlayerCardStyle.CLASSIC -> "COURT LEGENDS"
                    PlayerCardStyle.BATTLE -> "B A T T L E   S E R I E S"
                },.5f,.948f,.031f,accent,display,.80f)
            }
        }
        Image(painterResource(R.drawable.courtquest_signature_wordmark),null,
            Modifier.align(Alignment.TopCenter).fillMaxWidth(.38f).fillMaxHeight(.072f).padding(top=9.dp))
    }
}
