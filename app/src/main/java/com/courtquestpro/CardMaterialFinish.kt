package com.courtquestpro

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.layout.fillMaxSize

/** Finite touch response, never a perpetual shimmer or sensor listener. Platform motion scale applies. */
@Composable
internal fun CardMaterialFinish(style:PlayerCardStyle,interactive:Boolean) {
    var pressed by remember{mutableStateOf(false)}
    val light by animateFloatAsState(if(pressed).16f else .035f,tween(180),label="Card reflection")
    val touch=if(interactive)Modifier.pointerInput(style){
        detectTapGestures(onPress={pressed=true;try{tryAwaitRelease()}finally{pressed=false}})
    }else Modifier
    Canvas(Modifier.fillMaxSize().then(touch)) {
        val w=size.width;val h=size.height
        val colors=when(style) {
            PlayerCardStyle.HOLOGRAPHIC->listOf(Color.Cyan,Color(0xFFB197FF),Color.White,Color(0xFF9AEBDB))
            PlayerCardStyle.CLASSIC->listOf(Color(0xFFB98A41),Color(0xFFFFE8B8),Color.White)
            PlayerCardStyle.BATTLE->listOf(Color(0xFFFF5B0A),Color.White,Color(0xFF7B3214))
            PlayerCardStyle.OBSIDIAN->listOf(Color(0xFFB8C7DC),Color.White,Color(0xFF434C5A))
        }
        val gradient=Brush.linearGradient(colors.map{it.copy(alpha=light)},Offset.Zero,Offset(w,h*.6f))
        // Reflective light stays on the dimensional rails, not on the uniform or player lettering.
        drawPath(Path().apply{moveTo(w*.025f,h*.02f);lineTo(w*.12f,h*.045f);lineTo(w*.07f,h*.79f);lineTo(w*.025f,h*.84f);close()},gradient)
        drawPath(Path().apply{moveTo(w*.91f,h*.10f);lineTo(w*.98f,h*.04f);lineTo(w*.98f,h*.84f);lineTo(w*.93f,h*.78f);close()},gradient)
    }
}
