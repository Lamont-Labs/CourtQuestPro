package com.courtquestpro

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp

/** Persisted player uniform; independent of the CourtQuest brand palette. */
data class PlayerJersey(
    val number: String = "23",
    val primary: Long = 0xFF141820,
    val secondary: Long = 0xFFFF650E,
    val lettering: Long = 0xFFFFFFFF
) {
    fun sanitized() = copy(number = number.trim().take(2).takeIf {
        it == "00" || (it?.toIntOrNull() in 0..99)
    } ?: "23")
}

internal val jerseyColors = listOf(
    "Black" to 0xFF141820L, "Orange" to 0xFFFF650EL,
    "White" to 0xFFFFFFFFL, "Blue" to 0xFF2563EBL,
    "Red" to 0xFFDC2626L, "Green" to 0xFF16A34AL,
    "Purple" to 0xFF7C3AEDL, "Gold" to 0xFFFBBF24L,
    "Silver" to 0xFF94A3B8L
)

@Composable
internal fun PlayerJerseyArt(jersey: PlayerJersey, modifier: Modifier = Modifier) {
    val uniform = jersey.sanitized()
    val base = Color(uniform.primary.toInt())
    val trim = Color(uniform.secondary.toInt())
    val ink = Color(uniform.lettering.toInt())
    Canvas(modifier) {
        val w=size.width; val h=size.height
        val outline=Path().apply {
            moveTo(w*.28f,h*.07f);lineTo(w*.39f,h*.04f)
            quadraticTo(w*.5f,h*.16f,w*.61f,h*.04f)
            lineTo(w*.72f,h*.07f);lineTo(w*.97f,h*.25f)
            lineTo(w*.80f,h*.49f);lineTo(w*.73f,h*.43f)
            lineTo(w*.76f,h*.94f);lineTo(w*.24f,h*.94f)
            lineTo(w*.27f,h*.43f);lineTo(w*.20f,h*.49f)
            lineTo(w*.03f,h*.25f);close()
        }
        drawPath(outline, Brush.linearGradient(listOf(base.copy(alpha=.82f),base,base.copy(alpha=.72f))))
        drawPath(outline,trim,style=Stroke(2.5.dp.toPx()))
        drawLine(trim,Offset(w*.29f,h*.10f),Offset(w*.23f,h*.45f),2.dp.toPx())
        drawLine(trim,Offset(w*.71f,h*.10f),Offset(w*.77f,h*.45f),2.dp.toPx())
        drawLine(trim,Offset(w*.27f,h*.86f),Offset(w*.73f,h*.86f),1.5.dp.toPx())
        drawLine(Color.White.copy(alpha=.24f),Offset(w*.34f,h*.18f),Offset(w*.32f,h*.80f),1.dp.toPx())
        val paint=android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            color=ink.toArgb(); textAlign=android.graphics.Paint.Align.CENTER
            typeface=android.graphics.Typeface.create("sans-serif-condensed",android.graphics.Typeface.BOLD)
            textSize=h*.39f; setShadowLayer(2.dp.toPx(),0f,2.dp.toPx(),android.graphics.Color.BLACK)
        }
        drawContext.canvas.nativeCanvas.drawText(uniform.number,w*.5f,h*.65f,paint)
    }
}
