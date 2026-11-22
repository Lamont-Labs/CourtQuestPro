package com.courtquestpro

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun CustomDrillEditor(editing: Drill?, initialCategory: String, initialLevel: String,
    onSave: (DrillInput) -> String?, onBack: () -> Unit) {
    var title by rememberSaveable(editing?.id) { mutableStateOf(editing?.title ?: "") }
    var category by rememberSaveable(editing?.id) { mutableStateOf(editing?.category ?: initialCategory) }
    var level by rememberSaveable(editing?.id) { mutableStateOf(editing?.level ?: initialLevel) }
    var minutes by rememberSaveable(editing?.id) { mutableStateOf((editing?.minutes ?: 6).toString()) }
    var goal by rememberSaveable(editing?.id) { mutableStateOf((editing?.let { DrillTargets.minimum(it) } ?: 10).toString()) }
    var instructions by rememberSaveable(editing?.id) { mutableStateOf(editing?.instructions ?: "") }
    var error by remember { mutableStateOf<String?>(null) }
    var confirmBack by remember { mutableStateOf(false) }
    val focus = LocalFocusManager.current
    val input = DrillInput(title, category, level, minutes.toIntOrNull() ?: 0, goal.toIntOrNull() ?: 0, instructions)
    val initial = DrillInput(editing?.title ?: "", editing?.category ?: initialCategory, editing?.level ?: initialLevel,
        editing?.minutes ?: 6, editing?.let { DrillTargets.minimum(it) } ?: 10, editing?.instructions ?: "")
    fun back() { if (input != initial) confirmBack = true else onBack() }
    BackHandler { back() }
    if (confirmBack) AlertDialog(onDismissRequest = { confirmBack = false }, title = { Text("Leave this edit?") },
        text = { Text("Your unsaved changes will be discarded.") },
        confirmButton = { TextButton(onBack) { Text("Discard changes") } },
        dismissButton = { TextButton({ confirmBack = false }) { Text("Keep editing") } })
    Column(Modifier.fillMaxSize().imePadding()) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            AppHeader(if (editing == null) "Build your drill." else "Refine your drill.", "Your instructions. Your target. Your library.")
            Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                TextButton({ back() }, modifier = Modifier.heightIn(min = 48.dp)) { Text("Back to library") }
                OutlinedTextField(title, { title = it.take(60); error = null }, label = { Text("Drill name") },
                    modifier = Modifier.fillMaxWidth().testTag("custom-title"), singleLine = true)
                CourtEyebrow("SKILL")
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TrainingLibrary.categories.forEach { value -> FilterChip(category == value, { category = value },
                        label = { Text(value) }, modifier = Modifier.heightIn(min = 48.dp)) }
                }
                CourtEyebrow("LEVEL")
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TrainingLibrary.levels.forEach { value -> FilterChip(level == value, { level = value },
                        label = { Text(value) }, modifier = Modifier.heightIn(min = 48.dp)) }
                }
                OutlinedTextField(minutes, { minutes = it.take(3); error = null }, label = { Text("Planned minutes") },
                    supportingText = { Text("1–60 minutes for your training clock.") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth().testTag("custom-minutes"), singleLine = true)
                OutlinedTextField(goal, { goal = it.take(4); error = null }, label = { Text("Target successful actions") },
                    supportingText = { Text("1–500 made shots, clean repetitions, accurate passes or secured rebounds.") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth().testTag("custom-goal"), singleLine = true)
                OutlinedTextField(instructions, { instructions = it.take(1500); error = null }, label = { Text("Instructions") },
                    modifier = Modifier.fillMaxWidth().testTag("custom-instructions"), minLines = 4,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done), keyboardActions = KeyboardActions(onDone = { focus.clearFocus() }))
                Text("Describe the setup, movement and what counts as a successful action. Keep the target practical and the space clear.",
                    style = MaterialTheme.typography.bodySmall, color = CourtQuestDesign.textSecondary)
                if (editing != null) Text("Saved results and today's queued drills keep their original targets. This edit applies when you choose the drill again.",
                    style = MaterialTheme.typography.bodySmall, color = CourtQuestDesign.textSecondary)
                Spacer(Modifier.height(16.dp))
            }
        }
        Surface(color = CourtQuestDesign.surface) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }) }
                CourtQuestPrimaryButton({
                    focus.clearFocus()
                    error = TrainingLibrary.validation(input)
                    if (error == null) error = onSave(input)
                }, Modifier.fillMaxWidth()) { Text("Save drill") }
            }
        }
    }
}
