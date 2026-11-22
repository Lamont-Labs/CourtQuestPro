package com.courtquestpro

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import java.time.Instant
import kotlin.random.Random

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun MissionBuilderScreen(save: SaveState, onChange: () -> Unit, onStart: (String) -> Unit,
    onResume: () -> Unit, onHome: () -> Unit, backLabel: String = "Back to Court") {
    val draft = MissionBuilder.ensureDraft(save)
    val saved = MissionBuilder.savedVersion(save)
    var query by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf("") }
    var generationError by rememberSaveable { mutableStateOf(false) }
    var showOptions by rememberSaveable { mutableStateOf(false) }
    var replaceVersion by remember { mutableStateOf<String?>(null) }
    var confirmNew by remember { mutableStateOf(false) }
    fun edit(action: () -> Unit) { action(); error = ""; generationError = false; onChange() }
    val versions = save.missionVersions.orEmpty().filter { it.source in listOf("PERSONAL", "RECOMMENDED") }.asReversed()
    if (replaceVersion != null || confirmNew) AlertDialog(onDismissRequest = { replaceVersion = null; confirmNew = false },
        title = { Text("Replace the builder draft?") }, text = { Text("Your saved Mission versions and training results stay available. Unsaved changes in this builder will be replaced.") },
        confirmButton = { TextButton({
            if (confirmNew) MissionBuilder.newDraft(save) else replaceVersion?.let { MissionBuilder.open(save, it) }
            replaceVersion = null; confirmNew = false; error = ""; onChange()
        }) { Text("Replace draft") } }, dismissButton = { TextButton({ replaceVersion = null; confirmNew = false }) { Text("Keep editing") } })
    LazyColumn(Modifier.fillMaxSize().imePadding(), contentPadding = PaddingValues(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { AppHeader("Build your Mission.", "Your drills. Your order. Your next session.") }
        item {
            Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                TextButton(onHome, Modifier.heightIn(min = 48.dp)) { Text(backLabel) }
                if (MissionTraining.active(save) != null) {
                    CourtQuestPrimaryButton(onResume, Modifier.fillMaxWidth()) { Text("Resume Mission") }
                    Text("Your active session keeps its saved version while you edit this draft.", style = MaterialTheme.typography.bodySmall, color = CourtQuestDesign.textSecondary)
                }
                OutlinedTextField(draft.title, { text -> edit { MissionBuilder.rename(save, text) } },
                    label = { Text("Mission name") }, singleLine = true, modifier = Modifier.fillMaxWidth().testTag("mission-title"), shape = RoundedCornerShape(12.dp))
                CourtQuestPremiumSurface(Modifier.fillMaxWidth(), PaddingValues(20.dp), highlighted = true) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        CourtEyebrow("YOUR LINEUP")
                        CourtMetricStrip(listOf("Drills" to draft.steps.size.toString(), "Planned minutes" to draft.steps.sumOf { it.drill.minutes }.toString()))
                        Text(if (draft.steps.isEmpty()) "Add your first drill below, or generate a Mission that fits your setup."
                            else "Move drills into your preferred order. Keep a drill to protect it when generating again.",
                            style = MaterialTheme.typography.bodyMedium, color = CourtQuestDesign.textSecondary)
                        CourtQuestPrimaryButton({
                            generationError = false
                            val version = MissionBuilder.publish(save, Instant.now().toString())
                            error = if (version == null) "Name your Mission and choose 1–12 different drills before saving." else ""
                            onChange()
                        }, Modifier.fillMaxWidth()) { Icon(Icons.Default.Save, null); Spacer(Modifier.width(8.dp)); Text("Save Mission") }
                        if (saved != null) {
                            Text("Saved on this device", color = CourtQuestDesign.accent, style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
                            Text("Version ${saved.version} · Instructions and targets preserved", style = MaterialTheme.typography.bodySmall, color = CourtQuestDesign.textSecondary)
                            OutlinedButton({ onStart(saved.id) }, Modifier.fillMaxWidth().heightIn(min = 56.dp)) { Text("Start Mission") }
                        } else Text("Save a version to start training. Builder changes are kept automatically.", style = MaterialTheme.typography.bodySmall, color = CourtQuestDesign.textSecondary)
                        if (error.isNotBlank() && !generationError) Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
                    }
                }
            }
        }
        items(draft.steps, key = { it.id }) { step ->
            val index = draft.steps.indexOfFirst { it.id == step.id }
            CourtQuestPremiumSurface(Modifier.padding(horizontal = 20.dp).fillMaxWidth(), PaddingValues(18.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    CourtEyebrow("${index + 1} / ${draft.steps.size} · ${step.drill.category}")
                    Text(step.drill.title, style = MaterialTheme.typography.titleLarge, color = CourtQuestDesign.textPrimary)
                    Text("${step.drill.minutes} min · ${step.measurement.minimumSuccesses} successful actions", style = MaterialTheme.typography.bodySmall, color = CourtQuestDesign.textSecondary)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton({ edit { MissionBuilder.move(save, step.id, -1) } }, enabled = index > 0) {
                            Icon(Icons.Default.KeyboardArrowUp, "Move ${step.drill.title} up")
                        }
                        IconButton({ edit { MissionBuilder.move(save, step.id, 1) } }, enabled = index < draft.steps.lastIndex) {
                            Icon(Icons.Default.KeyboardArrowDown, "Move ${step.drill.title} down")
                        }
                        FilterChip(step.required, { edit { MissionBuilder.toggleRequired(save, step.id) } },
                            label = { Text(if (step.required) "Kept" else "Keep") }, modifier = Modifier.heightIn(min = 48.dp))
                        IconButton({ edit { MissionBuilder.remove(save, step.id) } }) { Icon(Icons.Default.Close, "Remove ${step.drill.title}") }
                    }
                }
            }
        }
        item {
            Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                CourtRule()
                CourtHeading("Make it fit your day")
                OutlinedButton({ showOptions = !showOptions }, Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                    Text(if (showOptions) "Hide generator options" else "Generator options")
                }
                if (showOptions) {
                    val inputs = draft.constraints
                    MissionChoice("Skill", inputs.skill, listOf("Any skill") + TrainingLibrary.categories) { value -> edit { MissionBuilder.constraints(save, inputs.copy(skill = value)) } }
                    MissionChoice("Time budget", "${inputs.minutes} min", listOf("5 min", "15 min", "30 min", "60 min")) { value -> edit { MissionBuilder.constraints(save, inputs.copy(minutes = value.substringBefore(' ').toInt())) } }
                    MissionChoice("Available space", inputs.space, MissionBuilder.spaces) { value -> edit { MissionBuilder.constraints(save, inputs.copy(space = value)) } }
                    MissionChoice("Maximum level", inputs.maxLevel, TrainingLibrary.levels) { value -> edit { MissionBuilder.constraints(save, inputs.copy(maxLevel = value)) } }
                    Text("Available equipment", color = CourtQuestDesign.textPrimary, style = MaterialTheme.typography.titleMedium)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MissionBuilder.equipmentOptions.forEach { option ->
                            FilterChip(option in inputs.equipment, { edit {
                                MissionBuilder.constraints(save, inputs.copy(equipment = if (option in inputs.equipment) inputs.equipment - option else inputs.equipment + option))
                            } }, label = { Text(option) }, modifier = Modifier.heightIn(min = 48.dp))
                        }
                    }
                    Text("Custom drills can be added manually. Generation uses drills with known equipment and space requirements.", color = CourtQuestDesign.textSecondary, style = MaterialTheme.typography.bodySmall)
                }
                OutlinedButton({
                    val result = MissionBuilder.generateIntoDraft(save, draft.constraints, Random.nextLong())
                    generationError = true; error = result.error ?: ""; onChange()
                }, Modifier.fillMaxWidth().heightIn(min = 56.dp)) { Icon(Icons.Default.Shuffle, null); Spacer(Modifier.width(8.dp)); Text("Generate Mission") }
                if (error.isNotBlank() && generationError) Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
                Text("Next generation: ${draft.constraints.skill} · ${draft.constraints.minutes} min · ${draft.constraints.space.lowercase()}. Generation replaces unpinned drills; kept drills remain required.", color = CourtQuestDesign.textSecondary, style = MaterialTheme.typography.bodySmall)
                if (draft.generation != null) Text(
                    if (draft.constraints.copy(equipment = draft.constraints.equipment.sorted()) == draft.generation.inputs)
                        "This lineup fits your generator settings. Review it before you train."
                    else "Generator settings changed. Generate again to apply them to your lineup.",
                    color = CourtQuestDesign.textSecondary, style = MaterialTheme.typography.bodySmall)
                CourtRule()
                CourtHeading("Add drills")
                OutlinedTextField(query, { query = it }, label = { Text("Find a drill for this Mission") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("mission-search"), shape = RoundedCornerShape(12.dp), leadingIcon = { Icon(Icons.Default.Search, null) })
            }
        }
        val candidates = TrainingLibrary.available(save).filter { it.id !in draft.steps.map { s -> s.drill.id } &&
            (query.isBlank() || "${it.title} ${it.category}".contains(query.trim(), true)) }
        if (candidates.isEmpty()) item { Text("No drills match. Try another search or remove a drill from your lineup.", Modifier.padding(horizontal = 20.dp), color = CourtQuestDesign.textSecondary) }
        items(candidates, key = { "pick_${it.id}" }) { drill ->
            Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(drill.title, style = MaterialTheme.typography.titleMedium, color = CourtQuestDesign.textPrimary)
                Text("${drill.category} · ${drill.level} · ${drill.minutes} min", style = MaterialTheme.typography.bodySmall, color = CourtQuestDesign.textSecondary)
                OutlinedButton({ edit { MissionBuilder.add(save, drill.id) } }, enabled = draft.steps.size < 12,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("Add ${drill.title}") }
            }
        }
        item {
            Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                CourtRule(); CourtHeading("Saved Missions")
                if (versions.isEmpty()) Text("Save your first Mission to reuse its exact lineup.", color = CourtQuestDesign.textSecondary)
                OutlinedButton({ confirmNew = true }, Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("New Mission") }
            }
        }
        items(versions, key = { "version_${it.id}" }) { version ->
            CourtQuestPremiumSurface(Modifier.padding(horizontal = 20.dp).fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(version.title, style = MaterialTheme.typography.titleMedium, color = CourtQuestDesign.textPrimary)
                    Text("Version ${version.version} · ${version.steps.size} drills · ${version.steps.sumOf { it.drill.minutes }} planned min", color = CourtQuestDesign.textSecondary, style = MaterialTheme.typography.bodySmall)
                    TextButton({ replaceVersion = version.id }, Modifier.heightIn(min = 48.dp)) { Text("Open ${version.title} version ${version.version}") }
                }
            }
        }
    }
}

@Composable
private fun MissionChoice(label: String, value: String, choices: List<String>, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        Text(label, style = MaterialTheme.typography.labelLarge, color = CourtQuestDesign.textSecondary)
        Box {
            OutlinedButton({ expanded = true }, Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text(value, Modifier.weight(1f)); Icon(Icons.Default.ExpandMore, null) }
            DropdownMenu(expanded, { expanded = false }) { choices.forEach { choice ->
                DropdownMenuItem(text = { Text(choice) }, onClick = { expanded = false; onSelect(choice) })
            } }
        }
    }
}
