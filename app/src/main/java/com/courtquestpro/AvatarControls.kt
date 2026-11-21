package com.courtquestpro

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
internal fun AvatarControls(
    value:AvatarAppearance,
    part:String,
    identity:PlayerCardIdentity,
    jersey:PlayerJersey,
    inspect:(AvatarInspection)->Unit,
    change:(AvatarAppearance)->Unit
) {
    when(part) {
        "Hair" -> {
            Choices("Hairstyle",AvatarHair.entries,value.hair,{it.label},{"avatar-hair-${it.name.lowercase()}"},
                preview={AvatarHairThumbnail(it)}) {
                inspect(AvatarInspection.HEAD);change(value.copy(hair=it))
            }
            if(value.hair!=AvatarHair.BALD) Choices("Hair color",HairInk.entries,value.hairInk,{it.label},
                {"avatar-ink-${it.name.lowercase()}"},{it.color}) {
                inspect(AvatarInspection.HEAD);change(value.copy(hairInk=it))
            }
            Choices("Headwear",AvatarHeadwear.entries,value.headwear,{it.label},{"avatar-headwear-${it.name.lowercase()}"},
                preview={ option ->
                    AthleteOptionPreview(jersey,identity.copy(avatar=value.copy(headwear=option)),AvatarInspection.HEAD)
                }) {
                inspect(AvatarInspection.HEAD);change(value.copy(headwear=it))
            }
        }
        "Body" -> {
            Choices("Build",AvatarBuild.entries,value.build,{it.label},{"avatar-build-${it.name.lowercase()}"},
                preview={ option ->
                    AthleteOptionPreview(jersey,identity.copy(avatar=value.copy(build=option)),AvatarInspection.NONE)
                }) {
                inspect(AvatarInspection.NONE);change(value.copy(build=it))
            }
            Choices("Skin tone",AvatarSkin.entries,value.skin,{it.label},{"avatar-skin-${it.name.lowercase()}"},
                swatch={it.color},preview={ option ->
                    AthleteOptionPreview(jersey,identity.copy(avatar=value.copy(skin=option)),AvatarInspection.NONE)
                }) {
                inspect(AvatarInspection.NONE);change(value.copy(skin=it))
            }
        }
        else -> {
            Choices("Footwear model",AvatarFootwear.entries,value.footwear,{it.label},{"avatar-footwear-${it.name.lowercase()}"},
                preview={FootwearThumbnail(it)}) {
                inspect(AvatarInspection.SHOES);change(value.copy(footwear=it))
            }
            Choices("Shoe color",AvatarShoes.entries,value.shoes,{it.label},{"avatar-shoes-${it.name.lowercase()}"},{it.color}) {
                inspect(AvatarInspection.SHOES);change(value.copy(shoes=it))
            }
            Choices("Accessories",AvatarGear.entries,value.gear,{it.label},{"avatar-gear-${it.name.lowercase()}"},
                preview={AccessoryThumbnail(it)}) {
                inspect(AvatarInspection.NONE);change(value.copy(gear=it))
            }
        }
    }
}

@Composable
internal fun <T> Choices(
    title:String,
    values:List<T>,
    selected:T,
    label:(T)->String,
    tag:(T)->String,
    swatch:((T)->Int?)?=null,
    preview:(@Composable (T)->Unit)?=null,
    select:(T)->Unit
) {
    val railTag=values.firstOrNull()?.let { "${tag(it).substringBeforeLast('-')}-rail" }
    Column(verticalArrangement=Arrangement.spacedBy(4.dp)) {
        Row(Modifier.fillMaxWidth().padding(start=4.dp,end=5.dp,top=3.dp),
            verticalAlignment=Alignment.CenterVertically) {
            Text(title,style=MaterialTheme.typography.labelMedium,color=CourtQuestDesign.textSecondary,
                modifier=Modifier.semantics{heading()})
            Spacer(Modifier.weight(1f))
            Text("Swipe options",style=MaterialTheme.typography.labelSmall,color=CourtQuestDesign.textSecondary)
        }
        LazyRow(
            Modifier.fillMaxWidth().heightIn(min=92.dp).selectableGroup()
                .then(railTag?.let { Modifier.testTag(it) } ?: Modifier),
            horizontalArrangement=Arrangement.spacedBy(8.dp),
            contentPadding=PaddingValues(horizontal=3.dp,vertical=2.dp)
        ) {
            items(values,key={tag(it)}) { choice ->
                val active=choice==selected
                val shape=RoundedCornerShape(11.dp)
                Column(
                    Modifier.widthIn(min=78.dp,max=106.dp).heightIn(min=84.dp)
                        .clip(shape)
                        .background(if(active) CourtQuestDesign.warmSurface else CourtQuestDesign.surface,shape)
                        .border(if(active)1.5.dp else 1.dp,
                            if(active)CourtQuestDesign.accent else CourtQuestDesign.border,shape)
                        .selectable(active,role=Role.RadioButton,onClick={select(choice)})
                        .testTag(tag(choice))
                        .padding(horizontal=5.dp,vertical=4.dp),
                    horizontalAlignment=Alignment.CenterHorizontally,
                    verticalArrangement=Arrangement.spacedBy(3.dp)
                ) {
                    Box(Modifier.fillMaxWidth().height(62.dp).clearAndSetSemantics{},
                        contentAlignment=Alignment.Center) {
                        if(preview!=null) preview.invoke(choice)
                        else swatch?.invoke(choice)?.let { color ->
                            Box(Modifier.size(30.dp).background(Color(color),CircleShape)
                                .border(1.dp,Color.White.copy(alpha=.55f),CircleShape))
                        }
                        swatch?.invoke(choice)?.let { color ->
                            Box(Modifier.align(Alignment.BottomEnd).padding(2.dp).size(13.dp)
                                .background(Color(color),CircleShape).border(1.dp,Color.White.copy(alpha=.7f),CircleShape))
                        }
                        if(active) Icon(Icons.Default.CheckCircle,"Selected",Modifier.align(Alignment.TopEnd).size(17.dp),
                            tint=CourtQuestDesign.accent)
                    }
                    Text(label(choice),style=MaterialTheme.typography.labelSmall,
                        color=if(active)CourtQuestDesign.textPrimary else CourtQuestDesign.textSecondary,
                        maxLines=2,overflow=TextOverflow.Ellipsis,textAlign=TextAlign.Center)
                }
            }
        }
    }
}

@Composable
private fun AthleteOptionPreview(jersey:PlayerJersey,identity:PlayerCardIdentity,inspection:AvatarInspection) {
    Box(Modifier.width(48.dp).height(66.dp).clip(RoundedCornerShape(8.dp))
        .background(Color(0xFF101216)),contentAlignment=Alignment.Center) {
        CourtCardAthlete(jersey,identity,Modifier.fillMaxSize().padding(1.dp),inspection,showTestTags=false)
    }
}

@Composable
internal fun CardStyleOptionPreview(
    jersey:PlayerJersey,
    playerName:String,
    style:PlayerCardStyle
) {
    CourtPlayerCard(jersey,playerName,style,Modifier.height(60.dp),thumbnail=true)
}

@Composable
internal fun CardPoseOptionPreview(jersey:PlayerJersey,identity:PlayerCardIdentity,pose:PlayerCardPose) {
    AthleteOptionPreview(jersey,identity.copy(pose=pose),AvatarInspection.NONE)
}

@Composable
internal fun CardFaceOptionPreview(jersey:PlayerJersey,identity:PlayerCardIdentity,face:PlayerCardFace) {
    AthleteOptionPreview(jersey,identity.copy(face=face),AvatarInspection.HEAD)
}

@Composable
private fun AccessoryThumbnail(gear:AvatarGear) {
    Canvas(Modifier.size(42.dp,52.dp)) {
        val w=size.width;val h=size.height
        drawLine(Color(0xFFB87A50),androidx.compose.ui.geometry.Offset(w*.32f,h*.12f),
            androidx.compose.ui.geometry.Offset(w*.68f,h*.9f),strokeWidth=w*.22f,
            cap=androidx.compose.ui.graphics.StrokeCap.Round)
        drawLine(Color(0x66FFFFFF),androidx.compose.ui.geometry.Offset(w*.29f,h*.12f),
            androidx.compose.ui.geometry.Offset(w*.65f,h*.88f),strokeWidth=1.dp.toPx())
        if(gear in listOf(AvatarGear.SLEEVE,AvatarGear.BOTH,AvatarGear.FULL))
            drawLine(Color(0xFF30353B),androidx.compose.ui.geometry.Offset(w*.37f,h*.28f),
                androidx.compose.ui.geometry.Offset(w*.53f,h*.60f),strokeWidth=w*.25f,
                cap=androidx.compose.ui.graphics.StrokeCap.Round)
        if(gear in listOf(AvatarGear.WRISTS,AvatarGear.BOTH,AvatarGear.FULL))
            drawLine(Color(0xFFE5E0D5),androidx.compose.ui.geometry.Offset(w*.60f,h*.68f),
                androidx.compose.ui.geometry.Offset(w*.75f,h*.78f),strokeWidth=w*.27f,
                cap=androidx.compose.ui.graphics.StrokeCap.Round)
        if(gear in listOf(AvatarGear.KNEES,AvatarGear.FULL)) {
            drawRoundRect(Color(0xFF343A41),topLeft=androidx.compose.ui.geometry.Offset(w*.08f,h*.39f),
                size=androidx.compose.ui.geometry.Size(w*.30f,h*.24f),cornerRadius=androidx.compose.ui.geometry.CornerRadius(4.dp.toPx()))
            drawRoundRect(Color(0xFF343A41),topLeft=androidx.compose.ui.geometry.Offset(w*.62f,h*.12f),
                size=androidx.compose.ui.geometry.Size(w*.30f,h*.24f),cornerRadius=androidx.compose.ui.geometry.CornerRadius(4.dp.toPx()))
        }
        if(gear==AvatarGear.NONE)
            drawCircle(CourtQuestDesign.accent, radius=w*.06f,center=androidx.compose.ui.geometry.Offset(w*.83f,h*.16f))
    }
}
