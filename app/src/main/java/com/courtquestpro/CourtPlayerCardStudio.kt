package com.courtquestpro

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
internal fun PlayerCardStudio(jersey: PlayerJersey, playerName: String, onBack: () -> Unit) {
    val context = LocalContext.current
    val initial = remember { PlayerCardSelection.identity(context) }
    var style by rememberSaveable { mutableStateOf(initial.style) }
    var pose by rememberSaveable { mutableStateOf(initial.pose) }
    var face by rememberSaveable { mutableStateOf(initial.face) }
    var avatar by rememberSaveable(stateSaver=AvatarAppearance.Saver) {mutableStateOf(initial.avatar)}
    var avatarPart by rememberSaveable {mutableStateOf("Hair")}
    var inspection by rememberSaveable {mutableStateOf(AvatarInspection.HEAD)}
    var tool by rememberSaveable { mutableStateOf("Edition") }
    var discard by rememberSaveable { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var saving by remember { mutableStateOf(false) }
    val identity = PlayerCardIdentity(style, pose, face, avatar)
    val dirty = identity != initial
    val scope = rememberCoroutineScope()
    val close = { if (dirty) discard = true else onBack() }
    fun selectEdition(choice: PlayerCardStyle) {
        if(saving)return
        style = choice
    }
    fun saveAndExit() {
        if (saving) return
        saving = true
        scope.launch {
            val success = withContext(Dispatchers.IO) { PlayerCardSelection.write(context, identity) }
            saving = false
            if (success) onBack() else error = "Card could not be saved. Your previous card is safe. Try again."
        }
    }
    BackHandler { if (!saving) close() }
    if (discard) AlertDialog(onDismissRequest = { discard = false },
        title = { Text("Save this player card?") },
        text = { Text("Your uniform and training data are unchanged. Save your new card identity or discard these card edits.") },
        confirmButton = { TextButton({ discard = false; saveAndExit() }) { Text("Save & exit") } },
        dismissButton = { Column {
            TextButton(onBack, Modifier.testTag("player-card-discard")) { Text("Discard card edits") }
            TextButton({ discard = false }) { Text("Keep editing") }
        } })
    BoxWithConstraints(Modifier.fillMaxSize().background(CourtQuestDesign.background).testTag("player-card-studio-screen")) {
        val largeText = LocalDensity.current.fontScale > 1.4f
        val wide = maxWidth > maxHeight && maxWidth >= 600.dp
        val stageFraction=if(tool=="Avatar") {if(largeText) .28f else .41f} else {if(largeText) .40f else .49f}
        val stageHeight = (maxHeight * stageFraction).coerceIn(120.dp, 410.dp)
        Column(Modifier.fillMaxSize().padding(horizontal = 12.dp)) {
            Row(Modifier.fillMaxWidth().heightIn(min = 56.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton({ if (!saving) close() }, Modifier.testTag("player-card-close")) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Close Player Card Studio", tint = CourtQuestDesign.textPrimary)
                }
                Text(if(largeText)"Studio"else"Card Studio", style = MaterialTheme.typography.headlineMedium,
                    color = CourtQuestDesign.textPrimary, modifier = Modifier.weight(1f).semantics { heading() })
                TextButton({tool=if(tool=="Avatar")"Edition" else "Avatar"},Modifier.heightIn(min=48.dp)
                    .testTag("player-card-avatar")) {Text(if(tool=="Avatar")"Cards" else "Avatar")}
            }
            val preview: @Composable (Modifier) -> Unit = { mod ->
                Box(mod, contentAlignment = Alignment.Center) {
                    if(tool=="Avatar" && inspection!=AvatarInspection.NONE) {
                        Row(Modifier.fillMaxSize(),horizontalArrangement=Arrangement.spacedBy(8.dp),
                            verticalAlignment=Alignment.CenterVertically) {
                            BoxWithConstraints(Modifier.weight(.43f).fillMaxHeight(),contentAlignment=Alignment.Center) {
                                val cardWidth=minOf(maxWidth,maxHeight*.69f)
                                CourtPlayerCard(jersey,playerName,style,Modifier.width(cardWidth),pose,face,appearance=avatar)
                            }
                            Column(Modifier.weight(.57f).fillMaxHeight()) {
                                Text(if(inspection==AvatarInspection.HEAD)"Hair detail"else"Shoe detail",
                                    style=MaterialTheme.typography.labelLarge,color=CourtQuestDesign.textSecondary)
                                CourtCardAthlete(jersey,identity,Modifier.fillMaxWidth().weight(1f)
                                    .clip(RoundedCornerShape(16.dp)).background(CourtQuestDesign.surface)
                                    .testTag("avatar-inspection"),inspection)
                            }
                        }
                    } else Crossfade(identity, animationSpec = tween(160), label = "Card identity",
                        modifier = Modifier.fillMaxSize()) { choice ->
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CourtPlayerCard(jersey,playerName,choice.style,Modifier.fillMaxHeight().padding(4.dp),
                                choice.pose,choice.face,appearance=choice.avatar,interactive=true)
                        }
                    }
                }
            }
            val inspector: @Composable (Modifier) -> Unit = { mod ->
                Column(mod) {
                    if(tool=="Avatar") CourtQuestSegmentRail(listOf("Hair","Body","Gear"),avatarPart,{
                        avatarPart=it;inspection=when(it){"Hair"->AvatarInspection.HEAD;"Gear"->AvatarInspection.SHOES;else->AvatarInspection.NONE}
                    },tagKeys=listOf("avatar-hair","avatar-body","avatar-gear"),reflowAtLargeText=false)
                    else CourtQuestSegmentRail(listOf("Edition","Pose","Face"),tool,{ tool=it },
                        tagKeys=listOf("card-edition","card-pose","card-face"),reflowAtLargeText=false)
                    key(tool,avatarPart) {
                        Column(Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState())
                            .testTag("player-card-inspector").padding(vertical=8.dp),
                            verticalArrangement=Arrangement.spacedBy(8.dp)) {
                            when(tool) {
                                "Avatar" -> AvatarControls(avatar,avatarPart,{inspection=it}){if(!saving)avatar=it}
                                "Edition" -> {
                                    if (largeText) {
                                        PlayerCardStyle.entries.forEach { choice ->
                                            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                                                .background(CourtQuestDesign.surface)
                                                .border(1.dp,if(style==choice) CourtQuestDesign.accent else CourtQuestDesign.border,RoundedCornerShape(12.dp))
                                                .selectable(style==choice,role=Role.RadioButton,onClick={selectEdition(choice)})
                                                .testTag("player-card-style-${choice.name.lowercase()}").padding(8.dp),
                                                verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                                                CourtPlayerCard(jersey,playerName,choice,Modifier.width(62.dp),
                                                    pose,face,thumbnail=true,appearance=avatar)
                                                Text(choice.label,Modifier.weight(1f),color=CourtQuestDesign.textPrimary,style=MaterialTheme.typography.titleSmall)
                                                if(style==choice) Icon(Icons.Default.CheckCircle,"Selected",tint=CourtQuestDesign.accent)
                                            }
                                        }
                                    } else Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).selectableGroup(),
                                        horizontalArrangement=Arrangement.spacedBy(10.dp)) {
                                        PlayerCardStyle.entries.forEach { choice ->
                                            Column(Modifier.width(112.dp).clip(RoundedCornerShape(10.dp))
                                                .border(if(style==choice) 2.dp else 1.dp,
                                                    if(style==choice) CourtQuestDesign.accent else CourtQuestDesign.border,RoundedCornerShape(10.dp))
                                                .selectable(style==choice,role=Role.RadioButton,onClick={selectEdition(choice)})
                                                .testTag("player-card-style-${choice.name.lowercase()}").padding(5.dp),
                                                horizontalAlignment=Alignment.CenterHorizontally) {
                                                CourtPlayerCard(jersey,playerName,choice,Modifier.fillMaxWidth(),
                                                    pose,face,thumbnail=true,appearance=avatar)
                                                Spacer(Modifier.height(5.dp))
                                                Text(choice.label,color=CourtQuestDesign.textPrimary,style=MaterialTheme.typography.labelMedium)
                                            }
                                        }
                                    }
                                }
                                "Pose" -> CardChoices(PlayerCardPose.entries,pose,{it.label},{"player-card-pose-${it.name.lowercase()}"}) { if(!saving)pose=it }
                                "Face" -> {
                                    CardChoices(PlayerCardFace.entries,face,{it.label},{"player-card-face-${it.name.lowercase()}"}) { if(!saving)face=it }
                                    Text("All identities are anonymous original illustrations. No photo or real-person likeness.",
                                        color=CourtQuestDesign.textSecondary,style=MaterialTheme.typography.bodySmall)
                                }
                            }
                            Text("Live uniform · ${jersey.design.template.label}${jersey.number?.let { " · #$it" }.orEmpty()}",
                                color=CourtQuestDesign.textSecondary,style=MaterialTheme.typography.bodySmall,
                                modifier=Modifier.testTag("card-live-jersey"))
                        }
                    }
                }
            }
            if(wide) Row(Modifier.weight(1f),horizontalArrangement=Arrangement.spacedBy(14.dp)) {
                preview(Modifier.weight(1f).fillMaxHeight())
                inspector(Modifier.weight(1f).fillMaxHeight())
            } else {
                preview(Modifier.fillMaxWidth().height(stageHeight))
                inspector(Modifier.fillMaxWidth().weight(1f))
            }
            error?.let { Text(it,color=MaterialTheme.colorScheme.error,modifier=Modifier.semantics { liveRegion=LiveRegionMode.Polite }) }
            CourtQuestPrimaryButton({ saveAndExit() },Modifier.fillMaxWidth().padding(vertical=8.dp).testTag("player-card-save"),
                enabled=!saving) { Text(if(saving) "Saving…" else "Save & Exit") }
        }
    }
}

@Composable
private fun <T> CardChoices(values: List<T>, selected: T, label:(T)->String, tag:(T)->String, select:(T)->Unit) {
    Column(Modifier.fillMaxWidth().selectableGroup(),verticalArrangement=Arrangement.spacedBy(8.dp)) {
        values.forEach { value ->
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                .background(if(value==selected) CourtQuestDesign.warmSurface else CourtQuestDesign.surface)
                .border(1.dp,if(value==selected) CourtQuestDesign.accent else CourtQuestDesign.border,RoundedCornerShape(12.dp))
                .selectable(value==selected,role=Role.RadioButton,onClick={select(value)}).testTag(tag(value))
                .heightIn(min=52.dp).padding(horizontal=14.dp,vertical=10.dp),
                verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                Text(label(value),Modifier.weight(1f),color=CourtQuestDesign.textPrimary,style=MaterialTheme.typography.titleMedium)
                if(value==selected) Icon(Icons.Default.CheckCircle,"Selected",tint=CourtQuestDesign.accent)
            }
        }
    }
}
