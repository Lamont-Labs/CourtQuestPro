package com.courtquestpro

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

@Composable
internal fun AvatarControls(value:AvatarAppearance,change:(AvatarAppearance)->Unit) {
    var part by rememberSaveable {mutableStateOf("Hair")}
    Text("Your athlete",style=MaterialTheme.typography.titleMedium,color=CourtQuestDesign.textPrimary,
        modifier=Modifier.semantics{heading()})
    CourtQuestSegmentRail(listOf("Hair","Body","Gear"),part,{part=it},
        tagKeys=listOf("avatar-hair","avatar-body","avatar-gear"),reflowAtLargeText=false)
    when(part) {
        "Hair" -> {
            Choices("Hair",AvatarHair.entries,value.hair,{it.label},{"avatar-hair-${it.name.lowercase()}"}){change(value.copy(hair=it))}
            Choices("Headwear",AvatarHeadwear.entries,value.headwear,{it.label},{"avatar-headwear-${it.name.lowercase()}"},{it.ink}){
                change(value.copy(headwear=it))}
        }
        "Body" -> {
            Choices("Build",AvatarBuild.entries,value.build,{it.label},{"avatar-build-${it.name.lowercase()}"}){change(value.copy(build=it))}
            Text("Illustrated proportions only. This does not change your player data or training.",
                style=MaterialTheme.typography.bodySmall,color=CourtQuestDesign.textSecondary)
            Choices("Skin tone",AvatarSkin.entries,value.skin,{it.label},{"avatar-skin-${it.name.lowercase()}"},{it.color}){
                change(value.copy(skin=it))}
        }
        else -> {
            Choices("Shoes",AvatarShoes.entries,value.shoes,{it.label},{"avatar-shoes-${it.name.lowercase()}"},{it.color}){
                change(value.copy(shoes=it))}
            Choices("Accessories",AvatarGear.entries,value.gear,{it.label},{"avatar-gear-${it.name.lowercase()}"}){change(value.copy(gear=it))}
        }
    }
}

@Composable
private fun <T> Choices(title:String,values:List<T>,selected:T,label:(T)->String,tag:(T)->String,
    swatch:((T)->Int?)?=null,select:(T)->Unit) {
    Text(title,style=MaterialTheme.typography.titleSmall,color=CourtQuestDesign.textSecondary,
        modifier=Modifier.padding(top=8.dp).semantics{heading()})
    Column(Modifier.fillMaxWidth().selectableGroup()) {
        values.forEach { choice ->
            Row(Modifier.fillMaxWidth().heightIn(min=52.dp)
                .background(if(selected==choice)CourtQuestDesign.warmSurface else CourtQuestDesign.surface)
                .selectable(selected==choice,role=Role.RadioButton,onClick={select(choice)})
                .testTag(tag(choice)).padding(horizontal=12.dp,vertical=10.dp),
                verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(10.dp)) {
                swatch?.invoke(choice)?.let {Box(Modifier.size(25.dp).background(Color(it),CircleShape)
                    .border(1.dp,CourtQuestDesign.controlOutline,CircleShape))}
                Text(label(choice),Modifier.weight(1f),color=CourtQuestDesign.textPrimary,style=MaterialTheme.typography.bodyLarge)
                if(selected==choice)Icon(Icons.Default.CheckCircle,"Selected",tint=CourtQuestDesign.accent)
            }
            HorizontalDivider(color=CourtQuestDesign.border)
        }
    }
}
