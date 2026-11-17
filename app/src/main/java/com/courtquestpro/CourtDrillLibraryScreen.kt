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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun DrillLibraryScreen(
    save: SaveState, swapSlotId: String?, onNew: () -> Unit, onEdit: (String) -> Unit,
    onArchive: (String) -> Unit, onFavorite: (String) -> Unit, onPractice: (String) -> Unit,
    onUse: (String) -> Unit, onResume: () -> Unit, onHome: () -> Unit, selectedId: String?, onSelect: (String?) -> Unit, onMissionBuilder: () -> Unit,
    onStartTraining: () -> Unit = onHome,
    selectedSection: String = "Today",
    onSectionChange: (String) -> Unit = {}
) {
    var query by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableStateOf("All") }
    val section = if (swapSlotId == null) selectedSection else "Library"
    var category by rememberSaveable(swapSlotId) { mutableStateOf(
        swapSlotId?.let { id -> save.slots.firstOrNull { it.id == id }?.drillId }
            ?.let { id -> TrainingLibrary.catalog(save).firstOrNull { it.id == id }?.category } ?: "All skills") }
    var categoryMenu by remember { mutableStateOf(false) }
    var levelFilter by rememberSaveable { mutableStateOf("All levels") }
    var durationFilter by rememberSaveable { mutableStateOf("Any length") }
    var expandedCategory by rememberSaveable { mutableStateOf<String?>(null) }
    val catalog = TrainingLibrary.catalog(save)
    val selected = catalog.firstOrNull { it.id == selectedId }
    val favorites = save.favoriteDrillIds.orEmpty()
    val resumablePlan = save.slots.any { slot ->
        !slot.complete && save.drillDrafts.orEmpty().any {
            it.date == save.activeDate && it.slotId == slot.id && it.drillId == slot.drillId
        }
    }
    if (selected != null) {
        BackHandler { onSelect(null) }
        DrillLibraryDetail(selected, save, swapSlotId, onBack = { onSelect(null) },
            onFavorite = { onFavorite(selected.id) }, onPractice = { onPractice(selected.id) },
            onUse = { onUse(selected.id) }, onEdit = { onEdit(selected.id) },
            onArchive = { onArchive(selected.id); onSelect(null) }, onResume = onResume)
        return
    }
    val available = TrainingLibrary.available(save)
    val shown = available.filter { drill ->
        (section != "Custom" || drill.id.startsWith("custom_")) &&
        (category == "All skills" || drill.category == category) &&
        (levelFilter == "All levels" || drill.level == levelFilter) &&
        (durationFilter == "Any length" || when (durationFilter) {
            "Under 10 min" -> drill.minutes < 10
            "10–20 min" -> drill.minutes in 10..20
            else -> drill.minutes > 20
        }) &&
            (filter != "Mine" || drill.id.startsWith("custom_")) &&
            (filter != "Favorites" || drill.id in favorites) &&
            (query.isBlank() || listOf(drill.title, drill.category, drill.level, drill.instructions, drill.target).any { it.contains(query.trim(), true) })
    }
    val suggestions = TrainingLibrary.suggestions(save)
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item { AppHeader(if (swapSlotId == null) "Train" else "Make the swap.",
            if (swapSlotId == null) "${shown.size} drills in view · Find your next rep."
            else "Choose a suitable drill for your untouched daily slot.", compact = true) }
        if (swapSlotId == null) item {
            CourtArenaHero(R.drawable.court_arena_hoop,
                Modifier.padding(horizontal = 16.dp).fillMaxWidth()
                    .height(210.dp * LocalDensity.current.fontScale).testTag("train-arena-hero"),
                imageAlignment = Alignment.TopEnd) {
                Column(Modifier.align(Alignment.BottomStart).padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    CourtEyebrow("TRAINING FLOOR")
                    Text("Make the next rep count.", color = CourtQuestDesign.textPrimary,
                        style = MaterialTheme.typography.headlineMedium)
                    Text("${save.difficulty} · ${save.weeklyPlan} focus", color = CourtQuestDesign.textSecondary,
                        style = MaterialTheme.typography.bodyMedium)
                    CourtQuestPrimaryButton(onStartTraining, Modifier.fillMaxWidth().testTag("train-start-training")) {
                        Text(when {
                            MissionTraining.active(save) != null -> "Resume Mission"
                            save.slots.isNotEmpty() && save.slots.all { it.complete } -> "Play 1-on-1"
                            resumablePlan -> "Resume session"
                            else -> "Start training"
                        }, Modifier.weight(1f))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, null)
                    }
                }
            }
        }
        if (swapSlotId == null) item {
            CourtQuestSegmentRail(
                labels = listOf("My Plan", "Library", "Custom"),
                tagKeys = listOf("today", "library", "custom"),
                selected = if (section == "Today") "My Plan" else section,
                onSelect = { tab ->
                    if (tab == "My Plan") onSectionChange("Today")
                    else { onSectionChange(tab); filter = "All"; category = "All skills"; query = "" }
                },
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }
        if (section == "Today" && swapSlotId == null) item {
            Column(Modifier.padding(horizontal = 20.dp).testTag("train-plan-content"),
                verticalArrangement = Arrangement.spacedBy(12.dp)) {
                CourtQuestPremiumSurface(Modifier.fillMaxWidth(), PaddingValues(18.dp), highlighted = true) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        CourtEyebrow("YOUR SAVED PLAN")
                        Text("${save.slots.count { it.complete }} of ${save.slots.size} drills recorded",
                            color = CourtQuestDesign.textPrimary, style = MaterialTheme.typography.headlineMedium)
                        OutlinedButton(onNew, Modifier.fillMaxWidth().heightIn(min = 52.dp).testTag("library-create")) {
                            Icon(Icons.Default.Add, null, Modifier.size(19.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Create custom drill")
                        }
                        save.slots.forEachIndexed { index, slot ->
                            val drill = catalog.firstOrNull { it.id == slot.drillId }
                            Row(Modifier.fillMaxWidth().heightIn(min = 48.dp), verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text((index + 1).toString().padStart(2, '0'), color = CourtQuestDesign.accent,
                                    style = MaterialTheme.typography.titleLarge)
                                Column(Modifier.weight(1f)) {
                                    Text(drill?.title ?: "Saved drill", color = CourtQuestDesign.textPrimary,
                                        style = MaterialTheme.typography.titleMedium)
                                    Text("${drill?.category ?: "Training"} · ${drill?.minutes ?: 0} min",
                                        color = CourtQuestDesign.textSecondary, style = MaterialTheme.typography.bodySmall)
                                }
                                if (slot.complete) Icon(Icons.Default.CheckCircle, "Recorded",
                                    tint = CourtQuestDesign.accent)
                            }
                        }
                    }
                }
                OutlinedButton(onClick = onHome, Modifier.fillMaxWidth().heightIn(min = 52.dp)) {
                    Text("Back to Court")
                }
            }
        }
        item {
            Column(Modifier.padding(horizontal = 20.dp)
                .then(if (section == "Library") Modifier.testTag("train-library-content") else Modifier),
                verticalArrangement = Arrangement.spacedBy(11.dp)) {
                if (section != "Today") {
                OutlinedTextField(query, { query = it }, label = { Text("Search drills, skills or targets") }, singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    trailingIcon = { if (query.isNotBlank()) IconButton(onClick = { query = "" }) { Icon(Icons.Default.Close, "Clear search") } },
                    modifier = Modifier.fillMaxWidth().testTag("library-search"), shape = RoundedCornerShape(14.dp))
                FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    maxItemsInEachRow = if (LocalDensity.current.fontScale > 1.3f) 1 else 2) {
                    CourtQuestPrimaryButton(onNew, Modifier.weight(1f).testTag("library-create")) {
                        Icon(Icons.Default.Add, null, Modifier.size(19.dp)); Spacer(Modifier.width(6.dp))
                        Text("Create custom drill", Modifier.weight(1f), style = MaterialTheme.typography.labelLarge,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                    if (swapSlotId == null && section == "Library") {
                        OutlinedButton(onMissionBuilder, Modifier.weight(1f).heightIn(min = 52.dp).testTag("library-build-mission")) {
                            Text("Build a Mission", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
                if (save.practiceSession != null) {
                    OutlinedButton(onResume, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) {
                        Icon(Icons.Default.PlayArrow, null); Spacer(Modifier.width(8.dp)); Text("Resume practice")
                    }
                    Text(catalog.firstOrNull { it.id == save.practiceSession?.drillId }?.title ?: "Your saved practice",
                        style = MaterialTheme.typography.bodySmall, color = CourtQuestDesign.textSecondary)
                }
                if (section == "Library" && (query.isNotBlank() || category != "All skills" ||
                    levelFilter != "All levels" || durationFilter != "Any length" || filter != "All")) {
                    Row(Modifier.fillMaxWidth().testTag("library-active-filters"),
                        verticalAlignment = Alignment.CenterVertically) {
                        Text("${shown.size} matching drills", modifier = Modifier.weight(1f),
                            color = CourtQuestDesign.textSecondary,
                            style = MaterialTheme.typography.labelMedium)
                        TextButton(onClick = {
                            query = ""
                            category = "All skills"
                            levelFilter = "All levels"
                            durationFilter = "Any length"
                            filter = "All"
                            expandedCategory = null
                        }, modifier = Modifier.testTag("library-clear-filters")) {
                            Icon(Icons.Default.Close, null, Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Clear filters")
                        }
                    }
                }
                if (section == "Library") FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Box {
                        OutlinedButton({ categoryMenu = true }, modifier = Modifier.heightIn(min = 48.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)) {
                            Icon(Icons.Default.FilterList, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(category, style = MaterialTheme.typography.labelMedium)
                        }
                        DropdownMenu(categoryMenu, { categoryMenu = false }) {
                            (listOf("All skills") + TrainingLibrary.categories).forEach { value ->
                                DropdownMenuItem(text = { Text(value) }, onClick = { category = value; categoryMenu = false })
                            }
                        }
                    }
                    listOf("Level" to levelFilter, "Duration" to durationFilter).forEach { (kind, current) ->
                        var open by remember { mutableStateOf(false) }
                        Box {
                            OutlinedButton({ open = true }, modifier = Modifier.heightIn(min = 48.dp)) {
                                Icon(Icons.Default.Tune, null, Modifier.size(16.dp))
                                Spacer(Modifier.width(5.dp))
                                Text(current, style = MaterialTheme.typography.labelMedium)
                            }
                            DropdownMenu(open, { open = false }) {
                                val choices = if (kind == "Level") listOf("All levels") + TrainingLibrary.levels
                                    else listOf("Any length", "Under 10 min", "10–20 min", "Over 20 min")
                                choices.forEach { value ->
                                    DropdownMenuItem(text = { Text(value) }, onClick = {
                                        if (kind == "Level") levelFilter = value else durationFilter = value
                                        open = false
                                    })
                                }
                            }
                        }
                    }
                    listOf("All", "Mine", "Favorites").forEach { value ->
                        FilterChip(filter == value, { filter = value },
                            label = { Text(value, style = MaterialTheme.typography.labelMedium) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CourtQuestDesign.surface,
                                selectedLabelColor = CourtQuestDesign.accentVivid),
                            modifier = Modifier.defaultMinSize(minWidth = 48.dp).heightIn(min = 48.dp))
                    }
                }
                }
            }
        }
        if (section == "Library" && query.isBlank() && filter == "All" && category == "All skills" &&
            swapSlotId == null && suggestions.isNotEmpty()) {
            item {
                val featured = suggestions.first()
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                CourtQuestPremiumSurface(
                    Modifier.padding(horizontal = 20.dp).fillMaxWidth()
                        .clickable { onSelect(featured.drill.id) }
                        .testTag("library-featured-drill"),
                    PaddingValues(16.dp), highlighted = true
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        Surface(color = CourtQuestDesign.surface, shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.size(68.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.SportsBasketball, null,
                                    tint = CourtQuestDesign.accentVivid, modifier = Modifier.size(32.dp))
                            }
                        }
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            CourtEyebrow("RECOMMENDED · ${featured.drill.category}")
                            Text(featured.drill.title, style = MaterialTheme.typography.titleLarge,
                                color = CourtQuestDesign.textPrimary)
                            Text(featured.reason, style = MaterialTheme.typography.bodySmall,
                                color = CourtQuestDesign.textSecondary, maxLines = 2)
                            Text("${featured.drill.level} · ${featured.drill.minutes} min",
                                style = MaterialTheme.typography.labelMedium, color = CourtQuestDesign.accent)
                        }
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, "View recommended drill",
                            tint = CourtQuestDesign.accent)
                    }
                }
                    Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        CourtHeading("Browse drills")
                        Spacer(Modifier.weight(1f))
                        Text("${shown.size} drills", color = CourtQuestDesign.textSecondary,
                            style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
        if (section != "Today" && shown.isEmpty()) item {
            Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(if ((section == "Custom" || filter == "Mine") && query.isBlank()) "Your drills start here." else "Try a different search.",
                    style = MaterialTheme.typography.headlineMedium, color = CourtQuestDesign.textPrimary)
                Text(if (filter == "Favorites") "Tap the star on a drill to keep it here." else "Clear a filter or create a drill with your own instructions and target.",
                    style = MaterialTheme.typography.bodyMedium, color = CourtQuestDesign.textSecondary)
                TextButton({ query = ""; category = "All skills"; filter = "All"; levelFilter = "All levels"; durationFilter = "Any length"; onSectionChange("Library") }, modifier = Modifier.heightIn(min = 48.dp)) { Text("Show all drills") }
            }
        }
        if (section != "Today" && shown.isNotEmpty() &&
            !(section == "Library" && query.isBlank() && filter == "All" &&
                category == "All skills" && swapSlotId == null && suggestions.isNotEmpty())) {
            item {
                Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    CourtHeading("Browse drills")
                    Spacer(Modifier.weight(1f))
                    Text("${shown.size} drills", color = CourtQuestDesign.textSecondary,
                        style = MaterialTheme.typography.labelMedium)
                }
            }
        }
        if (section != "Today") {
            val grouped = section == "Library" && category == "All skills" && query.isBlank() && filter == "All"
            val groups = if (grouped) TrainingLibrary.categories.map { skill ->
                skill to shown.filter { it.category == skill }
            }.filter { it.second.isNotEmpty() } else listOf("Results" to shown)
            groups.forEach { (skill, groupDrills) ->
                if (grouped) item(key = "skill-$skill") {
                    CourtQuestPremiumSurface(Modifier.padding(horizontal = 20.dp).fillMaxWidth()
                        .clickable { expandedCategory = if (expandedCategory == skill) null else skill }
                        .testTag("library-category-$skill"), PaddingValues(16.dp),
                        highlighted = expandedCategory == skill) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.SportsBasketball, null, tint = CourtQuestDesign.accentVivid)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(skill, color = CourtQuestDesign.textPrimary, style = MaterialTheme.typography.titleMedium)
                                Text("${groupDrills.size} drills", color = CourtQuestDesign.textSecondary,
                                    style = MaterialTheme.typography.bodySmall)
                            }
                            Icon(if (expandedCategory == skill) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                "Toggle $skill", tint = CourtQuestDesign.accent)
                        }
                    }
                }
                if (!grouped || expandedCategory == skill) {
                items(groupDrills, key = { it.id }) { drill ->
            Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp)
                .clickable { onSelect(drill.id) }
                .heightIn(min = 72.dp).testTag("library-drill-${drill.id}"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(color = CourtQuestDesign.surface, shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.size(52.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.SportsBasketball, null,
                            tint = CourtQuestDesign.accentVivid, modifier = Modifier.size(23.dp))
                    }
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(drill.title, color = CourtQuestDesign.textPrimary,
                        style = MaterialTheme.typography.titleMedium, maxLines = 2)
                    Text("${drill.category} · ${drill.level} · ${drill.minutes} min",
                        color = CourtQuestDesign.textSecondary,
                        style = MaterialTheme.typography.bodySmall, maxLines = 1)
                }
                IconButton({ onFavorite(drill.id) },
                    modifier = Modifier.testTag("library-favorite-${drill.id}")) {
                    Icon(if (drill.id in favorites) Icons.Default.Star else Icons.Default.StarBorder,
                        if (drill.id in favorites) "Remove ${drill.title} from favorites"
                        else "Favorite ${drill.title}", tint = CourtQuestDesign.accent)
                }
                Icon(Icons.Default.ChevronRight, "View ${drill.title}",
                    tint = CourtQuestDesign.textSecondary, modifier = Modifier.size(18.dp))
            }
            HorizontalDivider(Modifier.padding(start = 84.dp, end = 20.dp),
                color = CourtQuestDesign.surface)
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
    var showCourtDiagram by rememberSaveable(drill.id) { mutableStateOf(false) }
    var showFormReference by rememberSaveable(drill.id) { mutableStateOf(false) }
    var showTechnique by rememberSaveable(drill.id) { mutableStateOf(false) }
    var showCoaching by rememberSaveable(drill.id) { mutableStateOf(false) }
    var confirmArchive by remember { mutableStateOf(false) }
    val active = save.practiceSession
    val samePractice = active?.drillId == drill.id
    val canPractice = active == null || samePractice
    val canUse = TrainingLibrary.swappableSlot(save, drill, swapSlotId) != null
    if (confirmArchive) AlertDialog(onDismissRequest = { confirmArchive = false }, title = { Text("Archive this drill?") },
        text = { Text("It will leave your library. Saved results and queued drills keep their original instructions and targets.") },
        confirmButton = { TextButton({ confirmArchive = false; onArchive() }) { Text("Archive drill") } },
        dismissButton = { TextButton({ confirmArchive = false }) { Text("Keep drill") } })
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 12.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
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
                CourtEyebrow("READY TO PRACTICE")
                CourtQuestPrimaryButton(onPractice, Modifier.fillMaxWidth(), canPractice) { Text(if (samePractice) "Resume practice" else "Start extra practice") }
                Text("Extra practice saves your results in Progress. Daily workout XP and streaks stay with your daily workout.",
                    style = MaterialTheme.typography.bodySmall, color = CourtQuestDesign.textSecondary)
                if (!canPractice) {
                    Text("You have another practice in progress. Resume it before starting a different drill.", color = CourtQuestDesign.textSecondary,
                        style = MaterialTheme.typography.bodyMedium)
                    OutlinedButton(onResume, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("Resume current practice") }
                }
                CourtEyebrow("DRILL DEMONSTRATION")
                Text("Study the movement, then open a court diagram or form guide when you need more detail.",
                    color = CourtQuestDesign.textSecondary, style = MaterialTheme.typography.bodyMedium)
                DrillArtworkReference(drill)
                OutlinedButton(onClick = { showCourtDiagram = !showCourtDiagram }, modifier = Modifier.fillMaxWidth().testTag("drill-toggle-court")) {
                    Text("Court positioning · placement and movement", modifier = Modifier.weight(1f))
                    Icon(if (showCourtDiagram) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null)
                }
                if (showCourtDiagram) {
                    DrillCourtReference(drill)
                }
                if (DrillCoachingLibrary.forDrill(drill) != null) {
                    OutlinedButton(onClick = { showFormReference = !showFormReference }, modifier = Modifier.fillMaxWidth().testTag("drill-toggle-form")) {
                        Text("Movement and form · technique checkpoints", modifier = Modifier.weight(1f))
                        Icon(if (showFormReference) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null)
                    }
                    if (showFormReference) {
                        DrillFormReference(drill)
                    }
                }
                CourtEyebrow("TECHNIQUE & COACHING")
                OutlinedButton(onClick = { showTechnique = !showTechnique }, modifier = Modifier.fillMaxWidth().testTag("drill-toggle-technique")) {
                    Text("Step-by-step technique · drill instructions", modifier = Modifier.weight(1f))
                    Icon(if (showTechnique) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null)
                }
                if (showTechnique) {
                CourtQuestPremiumSurface(Modifier.fillMaxWidth(), PaddingValues(18.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Default.MenuBook, null, tint = CourtQuestDesign.accentVivid)
                            Text("Step-by-step technique", color = CourtQuestDesign.textPrimary,
                                style = MaterialTheme.typography.titleMedium)
                        }
                        val steps = drill.instructions.split(Regex("(?<=[.!?])\\s+|\\n+"))
                            .map { it.trim() }.filter { it.isNotBlank() }
                        steps.forEachIndexed { index, step ->
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.Top) {
                                Surface(color = CourtQuestDesign.surface,
                                    shape = RoundedCornerShape(10.dp)) {
                                    Text((index + 1).toString().padStart(2, '0'),
                                        modifier = Modifier.padding(horizontal = 11.dp, vertical = 8.dp),
                                        color = CourtQuestDesign.accentVivid,
                                        style = MaterialTheme.typography.labelLarge)
                                }
                                Text(step, Modifier.weight(1f), color = CourtQuestDesign.textPrimary,
                                    style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                }
                }
                OutlinedButton(onClick = { showCoaching = !showCoaching }, modifier = Modifier.fillMaxWidth().testTag("drill-toggle-coaching")) {
                    Text("Coaching tips · mistakes and corrections", modifier = Modifier.weight(1f))
                    Icon(if (showCoaching) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null)
                }
                if (showCoaching) {
                CourtQuestPremiumSurface(Modifier.fillMaxWidth(), PaddingValues(18.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Default.TipsAndUpdates, null, tint = CourtQuestDesign.accentVivid)
                            Text("Coaching focus", color = CourtQuestDesign.textPrimary,
                                style = MaterialTheme.typography.titleMedium)
                        }
                        val coaching = DrillCoachingLibrary.forDrill(drill)
                        if (coaching != null) {
                            listOf(
                                "SETUP" to coaching.setup,
                                "MOVEMENT" to coaching.movement,
                                "TECHNIQUE CHECK" to coaching.checkpoint,
                                "COMMON MISTAKE & FIX" to coaching.correction
                            ).forEach { (heading, guidance) ->
                                CourtEyebrow(heading)
                                Text(guidance, color = CourtQuestDesign.textPrimary,
                                    style = MaterialTheme.typography.bodyMedium)
                            }
                        } else {
                            Text("Use your written instructions as the primary guide. Practice with control and stop if the movement feels unsafe.",
                                color = CourtQuestDesign.textSecondary,
                                style = MaterialTheme.typography.bodyMedium)
                        }
                        Text("Target: ${drill.minimumSuccesses} successful actions · ${drill.minutes} min",
                            color = CourtQuestDesign.accent, style = MaterialTheme.typography.labelLarge)
                    }
                }
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
