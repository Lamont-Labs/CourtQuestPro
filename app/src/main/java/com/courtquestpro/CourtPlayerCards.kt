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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat
import java.util.Locale

/** Original collector materials + original live athlete + native accessible player lettering. */
@Composable
internal fun CourtPlayerCard(
    jersey:PlayerJersey,playerName:String,style:PlayerCardStyle,modifier:Modifier=Modifier,
    pose:PlayerCardPose=PlayerCardPose.STANDING,face:PlayerCardFace=PlayerCardFace.SHADOW,
    thumbnail:Boolean=false,appearance:AvatarAppearance=AvatarAppearance(),interactive:Boolean=false
) {
    val context=LocalContext.current
    val display=remember(context){ResourcesCompat.getFont(context,R.font.barlow_condensed_extra_bold)}
    val body=remember(context){ResourcesCompat.getFont(context,R.font.barlow_bold)}
    val classic=style==PlayerCardStyle.CLASSIC
    val frame=RoundedCornerShape(if(classic) 5.dp else 10.dp)
    val accent=when(style) {
        PlayerCardStyle.HOLOGRAPHIC->Color(0xFFB9F1FF)
        PlayerCardStyle.CLASSIC->Color(0xFFE6C690)
        PlayerCardStyle.BATTLE->Color(0xFFFF8D45)
        PlayerCardStyle.OBSIDIAN->Color(0xFFD0DAEA)
    }
    val name=playerName.ifBlank{"YOUR PLAYER"}.uppercase(Locale.ROOT)
    val identity=PlayerCardIdentity(style,pose,face,appearance)
    val description="${style.label} player card. ${playerName.ifBlank{"Player name not set"}}. "+
        "${pose.label} pose. ${face.label} identity. ${jersey.design.template.label} saved uniform. "+
        (jersey.number?.let{"Jersey number $it."}?:"Jersey number not set.")+" ${appearance.description}."
    BoxWithConstraints(modifier.aspectRatio(.69f).clip(frame).background(Color(0xFF080A0E))
        .border(if(thumbnail) .5.dp else 1.dp,accent.copy(alpha=.6f),frame)
        .semantics(mergeDescendants=true){contentDescription=description}.testTag("player-card-preview")) {
        CardSurface(style)
        // The full body stays inside the material aperture; frames never cover the live jersey.
        CourtCardAthlete(jersey,identity,Modifier.fillMaxWidth(if(classic) .73f else .81f)
            .fillMaxHeight(.58f).align(Alignment.Center))
        Canvas(Modifier.fillMaxSize()) {
            val w=size.width;val h=size.height
            // Gentle contact shadow, not an opaque footer obscuring the shoes.
            drawOval(Brush.radialGradient(listOf(Color.Black.copy(alpha=.6f),Color.Transparent),
                center=Offset(w*.5f,h*.795f),radius=w*.28f),
                topLeft=Offset(w*.22f,h*.77f),size=Size(w*.56f,h*.05f))
            drawContext.canvas.nativeCanvas.apply {
                fun title(value:String,y:Float,cap:Float,color:Color,font:Typeface?,max:Float) {
                    val p=Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        typeface=font;textSize=w*cap;this.color=color.toArgb();textAlign=Paint.Align.CENTER
                        if(!classic)setShadowLayer(w*.004f,0f,w*.002f,android.graphics.Color.BLACK)
                    }
                    if(p.measureText(value)>w*max)p.textSize*=w*max/p.measureText(value)
                    drawText(value,w*.5f,h*y,p)
                }
                title(style.label.uppercase(Locale.ROOT),if(style==PlayerCardStyle.OBSIDIAN).122f else .194f,.035f,accent,body,.66f)
                title(name,.903f,.093f,if(classic)Color(0xFF281C13)else Color.White,
                    if(classic)Typeface.create(Typeface.SERIF,Typeface.BOLD)else display,.65f)
                title(jersey.number?.let{"Nº $it  /  PLAYER EDITION"}?:"PLAYER EDITION",
                    .939f,.024f,if(classic)Color(0xFF493723)else accent,body,.61f)
            }
        }
        Image(painterResource(R.drawable.courtquest_signature_wordmark),null,
            Modifier.align(Alignment.TopCenter).offset(y=maxHeight*(if(style==PlayerCardStyle.OBSIDIAN).055f else .115f))
                .fillMaxWidth(.33f).height(maxHeight*(if(style==PlayerCardStyle.OBSIDIAN).04f else .054f)))
        CardMaterialFinish(style,interactive)
    }
}
