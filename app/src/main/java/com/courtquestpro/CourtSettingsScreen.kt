package com.courtquestpro

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp

@Composable
internal fun SettingsScreen(playerName: String, difficulty: String, onDifficulty: (String) -> Unit, onSaveName: (String) -> Unit) {
    var name by remember(playerName) { mutableStateOf(playerName) }
    var savedNotice by remember { mutableStateOf(false) }
    val keyboard = LocalSoftwareKeyboardController.current
    val saveName = {
        if (name.isNotBlank()) {
            onSaveName(name.trim())
            savedNotice = true
            keyboard?.hide()
        }
    }
    Column(Modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState())) {
        AppHeader("Make it yours.", "Your player profile and training preferences.")
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)) {
            CourtEyebrow("PLAYER IDENTITY")
            OutlinedTextField(value = name, onValueChange = { name = it.take(40); savedNotice = false },
                label = { Text("Player name") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { saveName() }))
            CourtQuestPrimaryButton(onClick = saveName, enabled = name.isNotBlank() && name.trim() != playerName,
                modifier = Modifier.fillMaxWidth()) { Text("Save name") }
            if (savedNotice) Text("Player name saved", color = CourtQuestDesign.accent,
                style = MaterialTheme.typography.bodyMedium, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
            CourtRule()
            CourtHeading("Find your level")
            Text("Choose the challenge that fits your current game. This applies when a new daily workout is created.",
                color = CourtQuestDesign.textSecondary, style = MaterialTheme.typography.bodyMedium)
            listOf("Beginner", "Intermediate", "Advanced", "Expert", "Elite").forEachIndexed { index, level ->
                val selected = level == difficulty
                val shape = RoundedCornerShape(16.dp)
                Row(Modifier.fillMaxWidth().clip(shape)
                    .background(if (selected) CourtQuestDesign.warmSurface else CourtQuestDesign.surface)
                    .border(1.dp, if (selected) CourtQuestDesign.accent else CourtQuestDesign.border, shape)
                    .selectable(selected, role = Role.RadioButton, onClick = { onDifficulty(level) }).padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text((index + 1).toString().padStart(2, '0'), color = CourtQuestDesign.accent,
                        style = MaterialTheme.typography.headlineSmall)
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(level, color = CourtQuestDesign.textPrimary, style = MaterialTheme.typography.titleMedium)
                        Text(difficultyDescription(level), color = CourtQuestDesign.textSecondary,
                            style = MaterialTheme.typography.bodySmall)
                    }
                    RadioButton(selected, onClick = null)
                }
            }
            CourtRule()
            Text("Your training and player history are saved on this phone. Video demonstrations use the internet.",
                color = CourtQuestDesign.textSecondary, style = MaterialTheme.typography.bodySmall)
        }
    }
}
