package com.courtquestpro

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
internal fun PlayerCardStudio(save: SaveState, onSaveJersey: (String,String,JerseyDesign)->Boolean, onBack: () -> Unit) {
    val context = LocalContext.current
    val initial = remember { PlayerCardSelection.identity(context) }
    var style by rememberSaveable { mutableStateOf(initial.style) }
    var discard by rememberSaveable { mutableStateOf(false) }
    var menu by remember { mutableStateOf(false) }
    var reading by rememberSaveable { mutableStateOf(false) }
    var editingUniform by rememberSaveable { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }
    var saveStatus by remember { mutableStateOf("idle") }
    var error by remember { mutableStateOf(false) }
    val identity = initial.copy(style = style)
    val jersey = save.playerJersey
    val record = remember(save) { PlayerCardRecord.from(save) }
    val dirty = style != initial.style
    val scope = rememberCoroutineScope()
    fun close() { if (!saving) { if (dirty) discard=true else onBack() } }
    fun commit() {
        if (saving) return
        saving=true;saveStatus="saving"
        scope.launch {
            try {
                val success=withContext(Dispatchers.IO) { PlayerCardSelection.persist(context,identity) }
                if (success) {
                    PlayerCardSelection.notifySaved()
                    saveStatus="saved";saving=false;onBack();saveStatus="back-requested"
                } else {saveStatus="failed";saving=false;error=true}
            } catch (cancelled:kotlinx.coroutines.CancellationException) {
                throw cancelled
            } catch (_:Exception) {
                saveStatus="failed";saving=false;error=true
            }
        }
    }
    if(editingUniform) {
        BackHandler { editingUniform=false }
        CourtJerseyStudio(jersey,onSave=onSaveJersey,onBack={editingUniform=false})
        return
    }
    BackHandler { close() }
    if(reading) PlayerRecordDialog(save.playerName,record){reading=false}
    if(discard) AlertDialog(onDismissRequest={discard=false},title={Text("Save card changes?")},
        confirmButton={TextButton({discard=false;commit()}){Text("Save & exit")}},
        dismissButton={Column {
            TextButton(onBack,Modifier.testTag("player-card-discard")){Text("Discard card edits")}
            TextButton({discard=false}){Text("Keep editing")}
        }})
    BoxWithConstraints(Modifier.fillMaxSize().background(CourtQuestDesign.background).testTag("player-card-studio-screen")) {
        Spacer(Modifier.size(1.dp).testTag("player-card-save-state-$saveStatus"))
        val wide=maxWidth>maxHeight && maxWidth>=540.dp
        val large=LocalDensity.current.fontScale>1.4f
        val stageHeight=(maxHeight*(if(large) .42f else .55f)).coerceIn(120.dp,540.dp)
        Column(Modifier.fillMaxSize().padding(horizontal=10.dp)) {
            Row(Modifier.fillMaxWidth().heightIn(min=52.dp),verticalAlignment=Alignment.CenterVertically) {
                IconButton({close()},Modifier.testTag("player-card-close")) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack,"Close Card Studio",tint=CourtQuestDesign.textPrimary)
                }
                Column(Modifier.weight(1f)) {
                    Text(if(large)"Studio" else "Card Studio",style=MaterialTheme.typography.titleLarge,
                        color=CourtQuestDesign.textPrimary,maxLines=1,overflow=TextOverflow.Ellipsis,
                        modifier=Modifier.semantics{heading()})
                    if(dirty) Text("Unsaved",style=MaterialTheme.typography.labelSmall,color=CourtQuestDesign.accent,
                        modifier=Modifier.testTag("card-unsaved"))
                }
                TextButton({commit()},enabled=!saving,modifier=Modifier.heightIn(min=48.dp).testTag("player-card-save")) {
                    Text(if(saving)"Saving…" else "Save",modifier=Modifier.testTag(if(saving)"player-card-saving" else "player-card-save-label"))
                }
                Box {
                    IconButton({menu=true},Modifier.testTag("card-menu")) {
                        Icon(Icons.Default.MoreVert,"Card options",tint=CourtQuestDesign.textPrimary)
                    }
                    DropdownMenu(menu,{menu=false}) {
                        DropdownMenuItem(text={Text("Read player statistics")},onClick={menu=false;reading=true})
                        DropdownMenuItem(text={Text("Open Jersey Studio")},onClick={menu=false;editingUniform=true})
                        DropdownMenuItem(text={Text("Restore default edition")},onClick={
                            menu=false;style=PlayerCardIdentity().style
                        },modifier=Modifier.testTag("card-reset-all"))
                    }
                }
            }
            val preview:@Composable (Modifier)->Unit={ mod ->
                BoxWithConstraints(mod,contentAlignment=Alignment.Center) {
                    val width=minOf(maxWidth,maxHeight*.69f)
                    CourtPlayerCard(jersey,save.playerName,style,Modifier.width(width),record=record)
                }
            }
            val inspector:@Composable (Modifier)->Unit={ mod ->
                Column(mod) {
                    Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
                        Text("CARD EDITION", color=CourtQuestDesign.textPrimary, style=MaterialTheme.typography.titleSmall)
                        Spacer(Modifier.weight(1f))
                        TextButton({editingUniform=true},Modifier.heightIn(min=48.dp).testTag("card-live-jersey")) {
                            Text("Customize jersey")
                        }
                        Text("Tap card to flip", color=CourtQuestDesign.textSecondary,
                            style=MaterialTheme.typography.labelSmall)
                    }
                    key(style) {
                        Column(Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState()).testTag("player-card-inspector")
                            .padding(vertical=6.dp),verticalArrangement=Arrangement.spacedBy(5.dp)) {
                            Choices("Card material",PlayerCardStyle.entries,style,{it.label},{"player-card-style-${it.name.lowercase()}"},
                                swatch={it.metal.toArgb()},preview={CardStyleOptionPreview(jersey,save.playerName,it)}) { style=it }
                        }
                    }
                }
            }
            if(wide) Row(Modifier.fillMaxWidth().weight(1f),horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                preview(Modifier.weight(.46f).fillMaxHeight());inspector(Modifier.weight(.54f).fillMaxHeight())
            } else {
                preview(Modifier.fillMaxWidth().height(stageHeight))
                inspector(Modifier.fillMaxWidth().weight(1f))
            }
            if(error) Text("Save failed. Your edits are still here.",color=MaterialTheme.colorScheme.error,
                modifier=Modifier.semantics{liveRegion=LiveRegionMode.Polite}.testTag("player-card-save-error"))
        }
    }
}
