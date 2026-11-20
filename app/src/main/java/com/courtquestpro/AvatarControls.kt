package com.courtquestpro

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

@Composable
internal fun AvatarControls(value:AvatarAppearance,part:String,inspect:(AvatarInspection)->Unit,
    change:(AvatarAppearance)->Unit) {
    when(part) {
        "Hair" -> {
            Choices("Hair",AvatarHair.entries,value.hair,{it.label},{"avatar-hair-${it.name.lowercase()}"}){inspect(AvatarInspection.HEAD);change(value.copy(hair=it))}
            if(value.hair!=AvatarHair.BALD) Choices("Hair color",HairInk.entries,value.hairInk,{it.label},{"avatar-ink-${it.name.lowercase()}"},{it.color}) {
                inspect(AvatarInspection.HEAD);change(value.copy(hairInk=it))
            }
            Choices("Headwear",AvatarHeadwear.entries,value.headwear,{it.label},{"avatar-headwear-${it.name.lowercase()}"},{it.ink}){
                inspect(AvatarInspection.HEAD);change(value.copy(headwear=it))}
        }
        "Body" -> {
            Choices("Build",AvatarBuild.entries,value.build,{it.label},{"avatar-build-${it.name.lowercase()}"}){inspect(AvatarInspection.NONE);change(value.copy(build=it))}
            Choices("Skin tone",AvatarSkin.entries,value.skin,{it.label},{"avatar-skin-${it.name.lowercase()}"},{it.color}){
                inspect(AvatarInspection.NONE);change(value.copy(skin=it))}
        }
        else -> {
            Choices("Footwear model",AvatarFootwear.entries,value.footwear,{it.label},{"avatar-footwear-${it.name.lowercase()}"},
                preview={FootwearThumbnail(it)}){
                inspect(AvatarInspection.SHOES);change(value.copy(footwear=it))}
            Choices("Shoe color",AvatarShoes.entries,value.shoes,{it.label},{"avatar-shoes-${it.name.lowercase()}"},{it.color}){
                inspect(AvatarInspection.SHOES);change(value.copy(shoes=it))}
            Choices("Accessories",AvatarGear.entries,value.gear,{it.label},{"avatar-gear-${it.name.lowercase()}"}){inspect(AvatarInspection.NONE);change(value.copy(gear=it))}
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun <T> Choices(title:String,values:List<T>,selected:T,label:(T)->String,tag:(T)->String,
    swatch:((T)->Int?)?=null,preview:(@Composable (T)->Unit)?=null,select:(T)->Unit) {
    Text(title,style=MaterialTheme.typography.labelMedium,color=CourtQuestDesign.textSecondary,
        modifier=Modifier.padding(start=4.dp,top=4.dp).semantics{heading()})
    FlowRow(Modifier.fillMaxWidth().selectableGroup(),horizontalArrangement=Arrangement.spacedBy(6.dp),
        verticalArrangement=Arrangement.spacedBy(3.dp)) {
        values.forEach { choice ->
            val active=choice==selected
            val shape=RoundedCornerShape(9.dp)
            Row(Modifier.heightIn(min=48.dp).clip(shape)
                .selectable(active,role=Role.RadioButton,onClick={select(choice)}).testTag(tag(choice)).padding(vertical=3.dp)
                .background(if(active) CourtQuestDesign.warmSurface else CourtQuestDesign.surface,shape)
                .border(1.dp,if(active) CourtQuestDesign.accent else CourtQuestDesign.border,shape)
                .padding(horizontal=11.dp,vertical=7.dp),verticalAlignment=Alignment.CenterVertically,
                horizontalArrangement=Arrangement.spacedBy(6.dp)) {
                preview?.invoke(choice)
                swatch?.invoke(choice)?.let { Box(Modifier.size(15.dp).background(Color(it),CircleShape)
                    .border(1.dp,Color.White.copy(alpha=.48f),CircleShape)) }
                Text(label(choice),style=MaterialTheme.typography.labelLarge,color=CourtQuestDesign.textPrimary)
                if(active) Icon(Icons.Default.CheckCircle,null,Modifier.size(15.dp),tint=CourtQuestDesign.accent)
            }
        }
    }
}
