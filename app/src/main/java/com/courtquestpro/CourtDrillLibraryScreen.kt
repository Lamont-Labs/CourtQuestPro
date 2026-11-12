package com.courtquestpro

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun DrillLibraryScreen(
    save: SaveState, swapSlotId: String?, onNew: () -> Unit, onEdit: (String) -> Unit,
    onArchive: (String) -> Unit, onFavorite: (String) -> Unit, onPractice: (String) -> Unit,
    onUse: (String) -> Unit, onResume: () -> Unit, onHome: () -> Unit, selectedId: String?, onSelect: (String?) -> Unit, onMissionBuilder: () -> Unit
) {
    var query by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableStateOf("All") }
    var playerCount by rememberSaveable { mutableIntStateOf(1) }
    var category by rememberSaveable(swapSlotId) { mutableStateOf(
        swapSlotId?.let { id -> save.slots.firstOrNull { it.id == id }?.drillId }
            ?.let { id -> TrainingLibrary.catalog(save).firstOrNull { it.id == id }?.category } ?: "All skills") }
    var categoryMenu by remember { mutableStateOf(false) }
    val catalog = TrainingLibrary.catalog(save)
    val selected = catalog.firstOrNull { it.id == selectedId }
    val favorites = save.favoriteDrillIds.orEmpty()
    if (selected != null) {
        BackHandler { onSelect(null) }
        DrillLibraryDetail(selected, save, swapSlotId, onBack = { onSelect(null) },
            onFavorite = { onFavorite(selected.id) }, onPractice = { onPractice(selected.id) },
            onUse = { onUse(selected.id) }, onEdit = { onEdit(selected.id) },
            onArchive = { onArchive(selected.id); onSelect(null) }, onResume = onResume)
        return
    }
    val available = TrainingLibrary.byPlayerCount(TrainingLibrary.available(save), playerCount)
    val shown = available.filter { drill ->
        (category == "All skills" || drill.category == category) &&
            (filter != "Mine" || drill.id.startsWith("custom_")) &&
            (filter != "Favorites" || drill.id in favorites) &&
            (query.isBlank() || listOf(drill.title, drill.category, drill.instructions).any { it.contains(query.trim(), true) })
    }
    val suggestions = TrainingLibrary.suggestions(save)
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { AppHeader(if (swapSlotId == null) "Train your way." else "Make the swap.",
            if (swapSlotId == null) "Find a drill. Make it yours. Put in the work." else "Choose a suitable drill for your untouched daily slot.") }
        item {
            Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                CourtQuestPrimaryButton(onNew, Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Add, null, Modifier.size(20.dp)); Spacer(Modifier.width(8.dp)); Text("Create custom drill")
                }
                if (swapSlotId == null) OutlinedButton(onMissionBuilder, Modifier.fillMaxWidth().heightIn(min = 52.dp)) { Text("Build a Mission") }
                if (save.practiceSession != null) {
                    OutlinedButton(onResume, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) {
                        Icon(Icons.Default.PlayArrow, null); Spacer(Modifier.width(8.dp)); Text("Resume practice")
                    }
                    Text(catalog.firstOrNull { it.id == save.practiceSession?.drillId }?.title ?: "Your saved practice",
                        style = MaterialTheme.typography.bodySmall, color = CourtQuestDesign.textSecondary)
                }
                OutlinedTextField(query, { query = it }, label = { Text("Search drills") }, singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Search, null) }, modifier = Modifier.fillMaxWidth().testTag("library-search"), shape = RoundedCornerShape(12.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("All", "Mine", "Favorites").forEach { value ->
                        FilterChip(filter == value, { filter = value }, label = { Text(value) }, modifier = Modifier.heightIn(min = 48.dp))
                    }
                }
                Text("Players available", color = CourtQuestDesign.textPrimary,
                    style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(1 to "Solo", 2 to "2 players", 3 to "3+ players").forEach { (count, label) ->
                        FilterChip(
                            selected = playerCount == count,
                            onClick = { playerCount = count },
                            label = { Text(label) },
                            modifier = Modifier.heightIn(min = 48.dp).testTag("library-players-$count")
                        )
                    }
                }
                Box {
                    OutlinedButton({ categoryMenu = true }, modifier = Modifier.heightIn(min = 48.dp)) {
                        Icon(Icons.Default.FilterList, null); Spacer(Modifier.width(8.dp)); Text(category)
                    }
                    DropdownMenu(categoryMenu, { categoryMenu = false }) {
                        (listOf("All skills") + TrainingLibrary.categories).forEach { value ->
                            DropdownMenuItem(text = { Text(value) }, onClick = { category = value; categoryMenu = false })
                        }
                    }
                }
            }
        }
        if (query.isBlank() && filter == "All" && category == "All skills" && swapSlotId == null) {
            item { Box(Modifier.padding(horizontal = 20.dp)) { CourtHeading("Suggested for you") } }
            item {
                LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(suggestions.filter { it.drill.minimumPlayers <= playerCount }, key = { it.drill.id }) { suggestion ->
                        CourtQuestPremiumSurface(Modifier.width(264.dp).clickable { onSelect(suggestion.drill.id) }, PaddingValues(20.dp)) {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                CourtEyebrow(suggestion.drill.category)
                                Text(suggestion.drill.title, color = CourtQuestDesign.textPrimary, style = MaterialTheme.typography.titleLarge)
                                Text(suggestion.reason, color = CourtQuestDesign.textSecondary, style = MaterialTheme.typography.bodyMedium)
                                Text("${suggestion.drill.level} · ${suggestion.drill.minutes} min", color = CourtQuestDesign.accent, style = MaterialTheme.typography.labelMedium)
                                TextButton({ onSelect(suggestion.drill.id) }, modifier = Modifier.heightIn(min = 48.dp)) { Text("View drill") }
                            }
                        }
                    }
                }
            }
        }
        item { Box(Modifier.padding(horizontal = 20.dp)) { CourtHeading("${shown.size} ${if (filter == "Mine") "custom " else ""}drills") } }
        if (shown.isEmpty()) item {
            Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(if (filter == "Mine" && query.isBlank()) "Your drills start here." else "Try a different search.",
                    style = MaterialTheme.typography.headlineMedium, color = CourtQuestDesign.textPrimary)
                Text(if (filter == "Favorites") "Tap the star on a drill to keep it here." else "Clear a filter or create a drill with your own instructions and target.",
                    style = MaterialTheme.typography.bodyMedium, color = CourtQuestDesign.textSecondary)
                TextButton({ query = ""; category = "All skills"; filter = "All"; playerCount = 3 }, modifier = Modifier.heightIn(min = 48.dp)) { Text("Show all drills") }
            }
        }
        items(shown, key = { it.id }) { drill ->
            CourtQuestPremiumSurface(Modifier.padding(horizontal = 20.dp).fillMaxWidth(), PaddingValues(18.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CourtEyebrow(if (drill.id.startsWith("custom_")) "YOUR DRILL · ${drill.category}" else drill.category, Modifier.weight(1f))
                        IconButton({ onFavorite(drill.id) }) {
                            Icon(if (drill.id in favorites) Icons.Default.Star else Icons.Default.StarBorder,
                                if (drill.id in favorites) "Remove ${drill.title} from favorites" else "Favorite ${drill.title}", tint = CourtQuestDesign.accent)
                        }
                    }
                    Text(drill.title, style = MaterialTheme.typography.titleLarge, color = CourtQuestDesign.textPrimary)
                    Text("${TrainingLibrary.playerRequirement(drill)} · ${drill.level} · ${drill.minutes} min · ${DrillTargets.minimum(drill)} successful actions",
                        style = MaterialTheme.typography.bodySmall, color = CourtQuestDesign.textSecondary)
                    OutlinedButton({ onSelect(drill.id) }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                        Text("View ${drill.title}", Modifier.weight(1f)); Icon(Icons.AutoMirrored.Filled.ArrowForward, null, Modifier.size(18.dp))
                    }
                }
            }
        }
        item { TextButton(onHome, Modifier.padding(horizontal = 20.dp).heightIn(min = 48.dp)) { Text("Back to Court") } }
    }
}

@Composable
private fun DrillLibraryDetail(drill: Drill, save: SaveState, swapSlotId: String?, onBack: () -> Unit,
    onFavorite: () -> Unit, onPractice: () -> Unit, onUse: () -> Unit, onEdit: () -> Unit,
    onArchive: () -> Unit, onResume: () -> Unit) {
    var confirmArchive by remember { mutableStateOf(false) }
    val active = save.practiceSession
    val samePractice = active?.drillId == drill.id
    val canPractice = active == null || samePractice
    val canUse = TrainingLibrary.swappableSlot(save, drill, swapSlotId) != null
    if (confirmArchive) AlertDialog(onDismissRequest = { confirmArchive = false }, title = { Text("Archive this drill?") },
        text = { Text("It will leave your library. Saved results and queued drills keep their original instructions and targets.") },
        confirmButton = { TextButton({ confirmArchive = false; onArchive() }) { Text("Archive drill") } },
        dismissButton = { TextButton({ confirmArchive = false }) { Text("Keep drill") } })
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        item { AppHeader(drill.title, "${drill.category} · ${drill.level} · ${drill.minutes} min") }
        item {
            Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                TextButton(onBack, modifier = Modifier.heightIn(min = 48.dp)) { Text("Back to library") }
                CourtQuestPremiumSurface(Modifier.fillMaxWidth(), PaddingValues(20.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        CourtEyebrow("YOUR TARGET")
                        Text(drill.target, color = CourtQuestDesign.accent, style = MaterialTheme.typography.titleLarge)
                    }
                }
                CourtHeading("The drill guide")
                Text(drill.instructions, color = CourtQuestDesign.textSecondary, style = MaterialTheme.typography.bodyLarge)
                CourtQuestPrimaryButton(onPractice, Modifier.fillMaxWidth(), canPractice) { Text(if (samePractice) "Resume practice" else "Start extra practice") }
                Text("Extra practice saves your results in Progress. Daily workout XP and streaks stay with your daily workout.",
                    style = MaterialTheme.typography.bodySmall, color = CourtQuestDesign.textSecondary)
                if (!canPractice) {
                    Text("You have another practice in progress. Resume it before starting a different drill.", color = CourtQuestDesign.textSecondary,
                        style = MaterialTheme.typography.bodyMedium)
                    OutlinedButton(onResume, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("Resume current practice") }
                }
                OutlinedButton(onUse, enabled = canUse, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) { Text("Use in daily workout") }
                if (!canUse) Text("A swap needs an untouched ${drill.category.lowercase()} slot and a drill at or below your selected level.",
                    color = CourtQuestDesign.textSecondary, style = MaterialTheme.typography.bodySmall)
                TextButton(onFavorite, modifier = Modifier.heightIn(min = 48.dp)) {
                    Icon(if (drill.id in save.favoriteDrillIds.orEmpty()) Icons.Default.Star else Icons.Default.StarBorder, null)
                    Spacer(Modifier.width(8.dp)); Text(if (drill.id in save.favoriteDrillIds.orEmpty()) "Remove favorite" else "Save to favorites")
                }
                if (save.customDrills.orEmpty().any { it.drill.id == drill.id && !it.archived }) {
                    CourtRule()
                    OutlinedButton(onEdit, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("Edit custom drill") }
                    TextButton({ confirmArchive = true }, modifier = Modifier.heightIn(min = 48.dp)) { Text("Archive drill") }
                }
            }
        }
    }
}
