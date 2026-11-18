package com.courtquestpro

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
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
    var showFilters by rememberSaveable { mutableStateOf(false) }
    var levelFilter by rememberSaveable { mutableStateOf("All levels") }
    var durationFilter by rememberSaveable { mutableStateOf("Any length") }
    var expandedCategory by rememberSaveable { mutableStateOf<String?>(null) }
    var showAllCategories by rememberSaveable { mutableStateOf(false) }
    val largeText = LocalDensity.current.fontScale > 1.3f
    val catalog = TrainingLibrary.catalog(save)
    val browseState = rememberLazyListState()
    var lastRandomId by rememberSaveable { mutableStateOf<String?>(null) }
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
        DrillDiscovery.matches(drill, query)
    }
    val validResults = save.drillResults.orEmpty().filter {
        it.attempted > 0 && it.completed in 0..it.attempted
    }
    val practicePriority = TrainingLibrary.categories.mapNotNull { skill ->
        val skillResults = validResults.filter { result ->
            result.drillSnapshot?.category == skill ||
                catalog.any { it.id == result.drillId && it.category == skill }
        }
        val attempts = skillResults.sumOf { it.attempted }
        if (attempts >= 20) Triple(skill, skillResults.sumOf { it.completed }, attempts) else null
    }.minByOrNull { it.second.toFloat() / it.third }
    val suggestions = TrainingLibrary.suggestions(save)
    LazyColumn(Modifier.fillMaxSize(), state = browseState, contentPadding = PaddingValues(bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(if (swapSlotId == null) "Train" else "Make the swap.",
                    color = CourtQuestDesign.textPrimary, style = MaterialTheme.typography.headlineMedium)
                Text(
                    if (swapSlotId == null) "${shown.size} drills ready to explore"
                    else "Choose a matching drill for your untouched slot.",
                    color = CourtQuestDesign.textSecondary, style = MaterialTheme.typography.bodySmall
                )
            }
        }
        if (swapSlotId == null) item {
            CourtQuestSegmentRail(
                labels = listOf("My Plan", "Library", "Custom"),
                tagKeys = listOf("today", "library", "custom"),
                selected = if (section == "Today") "My Plan" else section,
                onSelect = { tab ->
                    if (tab == "My Plan") onSectionChange("Today")
                    else {
                        onSectionChange(tab)
                        filter = "All"
                        category = "All skills"
                        levelFilter = "All levels"
                        durationFilter = "Any length"
                        expandedCategory = null
                        showAllCategories = true
                        query = ""
                    }
                },
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }
        if (section == "Today" && swapSlotId == null) item {
            Column(Modifier.padding(horizontal = 20.dp).testTag("train-plan-content"),
                verticalArrangement = Arrangement.spacedBy(12.dp)) {
                val done = save.slots.count { it.complete }
                val inProgressSlot = save.slots.firstOrNull { slot ->
                    !slot.complete && save.drillDrafts.orEmpty().any {
                        it.date == save.activeDate && it.slotId == slot.id && it.drillId == slot.drillId
                    }
                }
                val nextSlot = inProgressSlot ?: save.slots.firstOrNull { !TrainingLibrary.locked(save, it) }
                val nextDrill = nextSlot?.let { slot -> catalog.firstOrNull { it.id == slot.drillId } }
                val activeMission = MissionTraining.active(save)
                val startLabel = when {
                    activeMission != null -> "Resume Mission"
                    save.slots.isNotEmpty() && save.slots.all { it.complete } -> "Play 1-on-1"
                    resumablePlan -> "Resume session"
                    else -> "Start training"
                }
                @Composable fun PlanCounter(modifier: Modifier) {
                    Column(modifier.testTag("train-plan-summary"),
                        verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        CourtEyebrow("YOUR SAVED PLAN")
                        Text("$done of ${save.slots.size} drills recorded",
                            color = CourtQuestDesign.textPrimary, style = MaterialTheme.typography.titleLarge)
                    }
                }
                @Composable fun StartAction() {
                    CourtQuestPrimaryButton(onStartTraining,
                        Modifier.fillMaxWidth().heightIn(min = 52.dp).testTag("train-start-training")) {
                        Text(startLabel, Modifier.weight(1f))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, null)
                    }
                }
                CourtQuestPremiumSurface(
                    Modifier.fillMaxWidth().testTag("train-arena-hero"),
                    PaddingValues(14.dp),
                    highlighted = true
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (largeText) {
                            StartAction()
                            PlanCounter(Modifier.fillMaxWidth())
                            Text(save.difficulty.uppercase(),
                                color = CourtQuestDesign.accent, style = MaterialTheme.typography.labelSmall)
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                PlanCounter(Modifier.weight(1f))
                                Text(save.difficulty.uppercase(),
                                    color = CourtQuestDesign.accent, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        if (nextDrill != null) {
                            Row(verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                                Icon(Icons.Default.SportsBasketball, null,
                                    tint = CourtQuestDesign.accentVivid, modifier = Modifier.size(20.dp))
                                Text(
                                    "NEXT  ${nextDrill.title} · ${nextDrill.minutes} min",
                                    color = CourtQuestDesign.textSecondary,
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 2,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            }
                        }
                        if (!largeText) StartAction()
                        save.slots.forEachIndexed { index, slot ->
                            val drill = catalog.firstOrNull { it.id == slot.drillId }
                            Row(
                                Modifier.fillMaxWidth().heightIn(min = 48.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text((index + 1).toString().padStart(2, '0'),
                                    color = CourtQuestDesign.accent, style = MaterialTheme.typography.titleLarge)
                                Column(Modifier.weight(1f)) {
                                    Text(drill?.title ?: "Saved drill", color = CourtQuestDesign.textPrimary,
                                        style = MaterialTheme.typography.titleMedium, maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                                    Text("${drill?.category ?: "Training"} · ${drill?.minutes ?: 0} min",
                                        color = CourtQuestDesign.textSecondary, style = MaterialTheme.typography.bodySmall)
                                }
                                if (slot.complete) Icon(Icons.Default.CheckCircle, "Recorded",
                                    tint = CourtQuestDesign.accent)
                            }
                        }
                    }
                }
            }
        }
        item {
            Column(Modifier.padding(horizontal = 20.dp)
                .then(if (section == "Library") Modifier.testTag("train-library-content") else Modifier),
                verticalArrangement = Arrangement.spacedBy(11.dp)) {
                if (section != "Today") {
                if (section == "Library" && swapSlotId == null && practicePriority != null) {
                    CourtQuestPremiumSurface(Modifier.fillMaxWidth(), PaddingValues(14.dp)) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            CourtEyebrow("YOUR PRACTICE FOCUS")
                            Text(practicePriority.first, color = CourtQuestDesign.textPrimary,
                                style = MaterialTheme.typography.titleLarge)
                            Text("${practicePriority.second * 100 / practicePriority.third}% success across ${practicePriority.third} recorded attempts",
                                color = CourtQuestDesign.textSecondary,
                                style = MaterialTheme.typography.bodySmall)
                            OutlinedButton(
                                onClick = {
                                    category = practicePriority.first
                                    query = ""
                                    levelFilter = "All levels"
                                    durationFilter = "Any length"
                                    filter = "All"
                                    expandedCategory = practicePriority.first
                                    showAllCategories = true
                                },
                                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                                    .testTag("library-practice-focus")
                            ) {
                                Icon(Icons.Default.Tune, null, Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Explore ${practicePriority.first} drills")
                            }
                        }
                    }
                }
                OutlinedTextField(query, { query = it }, label = { Text("Search drills") }, singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    trailingIcon = { if (query.isNotBlank()) IconButton(onClick = { query = "" }) { Icon(Icons.Default.Close, "Clear search") } },
                    modifier = Modifier.fillMaxWidth().testTag("library-search"), shape = RoundedCornerShape(14.dp))
                if (section == "Custom" && swapSlotId == null) {
                    CourtQuestPrimaryButton(onNew, Modifier.fillMaxWidth().testTag("library-create")) {
                        Icon(Icons.Default.Add, null, Modifier.size(19.dp)); Spacer(Modifier.width(8.dp))
                        Text("Create custom drill", Modifier.weight(1f), style = MaterialTheme.typography.labelLarge)
                        Icon(Icons.Default.ArrowForward, null, Modifier.size(18.dp))
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
                    OutlinedButton({ showFilters = !showFilters },
                        modifier = Modifier.heightIn(min = 48.dp).testTag("library-toggle-filters")) {
                        Icon(Icons.Default.Tune, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(5.dp))
                        Text(if (showFilters) "Less filters" else "Filters",
                            style = MaterialTheme.typography.labelMedium)
                    }
                    if (swapSlotId == null && shown.isNotEmpty()) {
                        OutlinedButton(
                            onClick = {
                                val candidates = if (shown.size > 1) shown.filterNot { it.id == lastRandomId } else shown
                                val pick = candidates.random()
                                lastRandomId = pick.id
                                onSelect(pick.id)
                            },
                            modifier = Modifier.heightIn(min = 48.dp).testTag("library-random-drill"),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Shuffle, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(5.dp))
                            Text("Random drill", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                    if (showFilters) {
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
        }
        if (section == "Library" && query.isBlank() && filter == "All" && category == "All skills" &&
            swapSlotId == null && suggestions.isNotEmpty()) {
            item {
                val featured = suggestions.first()
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                CourtQuestPremiumSurface(
                    Modifier.padding(horizontal = 20.dp).fillMaxWidth()
                        .clickable(
                            role = Role.Button,
                            onClickLabel = "View recommended drill: ${featured.drill.title}"
                        ) { onSelect(featured.drill.id) }
                        .testTag("library-featured-drill"),
                    PaddingValues(16.dp), highlighted = true
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        Surface(color = CourtQuestDesign.surface, shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.size(68.dp)) {
                            DrillArtworkThumbnail(featured.drill, Modifier.fillMaxSize())
                        }
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            CourtEyebrow("RECOMMENDED · ${featured.drill.category}")
                            Text(featured.drill.title, style = MaterialTheme.typography.titleLarge,
                                color = CourtQuestDesign.textPrimary)
                            Text("${featured.drill.level} · ${featured.drill.minutes} min",
                                style = MaterialTheme.typography.labelMedium, color = CourtQuestDesign.accent)
                            Text(featured.reason,
                                style = MaterialTheme.typography.bodySmall,
                                color = CourtQuestDesign.textSecondary,
                                maxLines = 4,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.testTag("library-recommendation-reason"))
                        }
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, "View recommended drill",
                            tint = CourtQuestDesign.accent)
                    }
                }
                    if (suggestions.size > 1) {
                        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            CourtEyebrow("MORE PICKS FOR YOU")
                            suggestions.drop(1).take(3).forEach { suggestion ->
                                CourtQuestPremiumSurface(
                                    Modifier.fillMaxWidth()
                                        .clickable(
                                            role = Role.Button,
                                            onClickLabel = "View ${suggestion.drill.title} drill details"
                                        ) { onSelect(suggestion.drill.id) }
                                        .testTag("library-alternative-${suggestion.drill.id}"),
                                    PaddingValues(horizontal = 12.dp, vertical = 10.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        if (!largeText) {
                                            DrillArtworkThumbnail(suggestion.drill, Modifier.size(52.dp))
                                        }
                                        Column(Modifier.weight(1f),
                                            verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                            Text(suggestion.drill.title,
                                                color = CourtQuestDesign.textPrimary,
                                                style = MaterialTheme.typography.titleMedium,
                                                maxLines = if (largeText) 3 else 2,
                                                overflow = TextOverflow.Ellipsis)
                                            Text("${suggestion.drill.category} · ${suggestion.drill.minutes} min",
                                                color = CourtQuestDesign.accent,
                                                style = MaterialTheme.typography.labelSmall)
                                            Text(suggestion.reason,
                                                color = CourtQuestDesign.textSecondary,
                                                style = MaterialTheme.typography.bodySmall,
                                                maxLines = if (largeText) 4 else 2,
                                                overflow = TextOverflow.Ellipsis)
                                        }
                                        Icon(Icons.AutoMirrored.Filled.ArrowForward,
                                            "View ${suggestion.drill.title}",
                                            tint = CourtQuestDesign.accent)
                                    }
                                }
                            }
                        }
                    }
                    Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Text("Browse drills", Modifier.weight(1f),
                            style = MaterialTheme.typography.headlineMedium,
                            color = CourtQuestDesign.textPrimary)
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
                    Text("Browse drills", Modifier.weight(1f),
                        style = MaterialTheme.typography.headlineMedium,
                        color = CourtQuestDesign.textPrimary)
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
            if (grouped) item(key = "library-browse-mode") {
                Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Choose a skill to reveal its drills", color = CourtQuestDesign.textSecondary,
                        style = MaterialTheme.typography.labelMedium)
                    TextButton(
                        onClick = {
                            showAllCategories = !showAllCategories
                            if (showAllCategories) expandedCategory = null
                        },
                        modifier = Modifier.testTag("library-toggle-all-categories")
                    ) {
                        Text(if (showAllCategories) "Collapse all" else "Show all drills")
                        Spacer(Modifier.width(4.dp))
                        Icon(if (showAllCategories) Icons.Default.UnfoldLess else Icons.Default.UnfoldMore,
                            null, Modifier.size(18.dp))
                    }
                }
            }
            groups.forEach { (skill, groupDrills) ->
                if (grouped) item(key = "skill-$skill") {
                    CourtQuestPremiumSurface(Modifier.padding(horizontal = 20.dp).fillMaxWidth()
                        .clickable { expandedCategory = if (expandedCategory == skill && !showAllCategories) null else skill; showAllCategories = false }
                        .testTag("library-category-$skill"), PaddingValues(16.dp),
                        highlighted = showAllCategories || expandedCategory == skill) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.SportsBasketball, null, tint = CourtQuestDesign.accentVivid)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(skill, color = CourtQuestDesign.textPrimary, style = MaterialTheme.typography.titleMedium)
                                Text("${groupDrills.size} drills", color = CourtQuestDesign.textSecondary,
                                    style = MaterialTheme.typography.bodySmall)
                            }
                            Icon(if (showAllCategories || expandedCategory == skill) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                "Toggle $skill", tint = CourtQuestDesign.accent)
                        }
                    }
                }
                if (!grouped || showAllCategories || expandedCategory == skill) {
                items(groupDrills, key = { it.id }) { drill ->
            Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp)
                .clickable { onSelect(drill.id) }
                .semantics { contentDescription = "View ${drill.title}" }
                .heightIn(min = 72.dp).testTag("library-drill-${drill.id}"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (!largeText) Surface(color = CourtQuestDesign.surface, shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.size(52.dp)) {
                    DrillArtworkThumbnail(drill, Modifier.fillMaxSize())
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(drill.title, color = CourtQuestDesign.textPrimary,
                        style = MaterialTheme.typography.titleMedium, maxLines = if (largeText) 4 else 2,
                        overflow = TextOverflow.Ellipsis)
                    Text("${drill.category} · ${drill.level} · ${drill.minutes} min",
                        color = CourtQuestDesign.textSecondary,
                        style = MaterialTheme.typography.bodySmall, maxLines = if (largeText) 3 else 1,
                        overflow = TextOverflow.Ellipsis)
                }
                IconButton({ onFavorite(drill.id) },
                    modifier = Modifier.testTag("library-favorite-${drill.id}")) {
                    Icon(if (drill.id in favorites) Icons.Default.Star else Icons.Default.StarBorder,
                        if (drill.id in favorites) "Remove ${drill.title} from favorites"
                        else "Favorite ${drill.title}", tint = CourtQuestDesign.accent)
                }
                IconButton(
                    onClick = { if (swapSlotId != null) onUse(drill.id) else onPractice(drill.id) },
                    modifier = Modifier.testTag("library-quick-practice-${drill.id}")
                ) {
                    Icon(
                        if (swapSlotId != null) Icons.Default.SwapHoriz else Icons.Default.PlayArrow,
                        if (swapSlotId != null) "Use ${drill.title} for this training slot"
                        else "Practice ${drill.title} now",
                        tint = CourtQuestDesign.accentVivid
                    )
                }
            }
            HorizontalDivider(Modifier.padding(start = if (largeText) 20.dp else 84.dp, end = 20.dp),
                color = CourtQuestDesign.surface)
        }
                }
            }
        }
        if (section == "Library" && swapSlotId == null) item {
            CourtQuestPrimaryButton(
                onNew,
                Modifier.padding(horizontal = 20.dp).fillMaxWidth().testTag("library-create")
            ) {
                Icon(Icons.Default.Add, null, Modifier.size(19.dp))
                Spacer(Modifier.width(8.dp))
                Text("Create custom drill", Modifier.weight(1f))
                Icon(Icons.Default.ArrowForward, null, Modifier.size(18.dp))
            }
        }
        if (section == "Library" && swapSlotId == null) item {
            OutlinedButton(
                onClick = onMissionBuilder,
                Modifier.padding(horizontal = 20.dp).fillMaxWidth().heightIn(min = 48.dp)
                    .testTag("library-build-mission")
            ) {
                Icon(Icons.Default.Tune, null, Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Build a Mission", Modifier.weight(1f))
                Icon(Icons.Default.ChevronRight, null, Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun DrillLibraryDetail(drill: Drill, save: SaveState, swapSlotId: String?, onBack: () -> Unit,
    onFavorite: () -> Unit, onPractice: () -> Unit, onUse: () -> Unit, onEdit: () -> Unit,
    onArchive: () -> Unit, onResume: () -> Unit) {
    var showCourtDiagram by rememberSaveable(drill.id) { mutableStateOf(false) }
    var showFormReference by rememberSaveable(drill.id) { mutableStateOf(false) }
    var showTechnique by rememberSaveable(drill.id) { mutableStateOf(true) }
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
        item {
            Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                TextButton(onBack, modifier = Modifier.heightIn(min = 48.dp)) { Text("Back to library") }
                CourtEyebrow("${drill.category.uppercase()} · ${drill.level.uppercase()} · ${drill.minutes} MIN")
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(drill.title, Modifier.weight(1f),
                        style = MaterialTheme.typography.headlineMedium,
                        color = CourtQuestDesign.textPrimary)
                    IconButton(onClick = onFavorite,
                        modifier = Modifier.testTag("drill-detail-favorite")) {
                        val favorite = drill.id in save.favoriteDrillIds.orEmpty()
                        Icon(if (favorite) Icons.Default.Star else Icons.Default.StarBorder,
                            if (favorite) "Remove ${drill.title} from favorites"
                            else "Save ${drill.title} to favorites",
                            tint = CourtQuestDesign.accentVivid)
                    }
                }
                CourtQuestPremiumSurface(
                    Modifier.fillMaxWidth().testTag("drill-detail-hero"),
                    PaddingValues(14.dp),
                    highlighted = true
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(
                            Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            CourtEyebrow("DRILL PLAYBOOK")
                            Text("${drill.minutes} MIN", color = CourtQuestDesign.accentVivid,
                                style = MaterialTheme.typography.labelLarge)
                        }
                        DrillArtworkReference(drill)
                        HorizontalDivider(color = CourtQuestDesign.surface)
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            CourtEyebrow("YOUR TARGET")
                            Text(drill.target, color = CourtQuestDesign.textPrimary,
                                style = MaterialTheme.typography.titleLarge)
                            DrillCoachingLibrary.forDrill(drill)?.let {
                                CourtEyebrow("FORM CHECKPOINT")
                                Text(it.checkpoint, color = CourtQuestDesign.textSecondary,
                                    style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                        CourtQuestPrimaryButton(
                            onPractice,
                            Modifier.fillMaxWidth().heightIn(min = 52.dp)
                                .testTag("drill-start-practice"),
                            canPractice
                        ) {
                            Icon(Icons.Default.PlayArrow, null, Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(if (samePractice) "Resume practice" else "Start extra practice",
                                Modifier.weight(1f))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, Modifier.size(18.dp))
                        }
                    }
                }
                Text("Extra practice saves results to Progress. Daily workout rewards are earned in your daily workout.",
                    style = MaterialTheme.typography.bodySmall, color = CourtQuestDesign.textSecondary)
                if (!canPractice) {
                    Text("You have another practice in progress. Resume it before starting a different drill.", color = CourtQuestDesign.textSecondary,
                        style = MaterialTheme.typography.bodyMedium)
                    OutlinedButton(onResume, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("Resume current practice") }
                }
                val hasFormGuide = DrillCoachingLibrary.forDrill(drill) != null
                val allGuidanceOpen = showCourtDiagram && showTechnique && showCoaching &&
                    (!hasFormGuide || showFormReference)
                CourtEyebrow("VISUAL COACHING REFERENCES")
                OutlinedButton(
                    onClick = {
                        val expand = !allGuidanceOpen
                        showCourtDiagram = expand
                        showFormReference = expand && hasFormGuide
                        showTechnique = expand
                        showCoaching = expand
                    },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                        .testTag("drill-toggle-all-guidance")
                ) {
                    Icon(if (allGuidanceOpen) Icons.Default.UnfoldLess else Icons.Default.UnfoldMore, null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (allGuidanceOpen) "Collapse coaching guides" else "Show all coaching guides")
                }
                OutlinedButton(onClick = { showCourtDiagram = !showCourtDiagram }, modifier = Modifier.fillMaxWidth().testTag("drill-toggle-court")) {
                    Text("Court positioning", modifier = Modifier.weight(1f))
                    Icon(if (showCourtDiagram) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null)
                }
                if (showCourtDiagram) {
                    DrillCourtReference(drill)
                }
                if (DrillCoachingLibrary.forDrill(drill) != null) {
                    OutlinedButton(onClick = { showFormReference = !showFormReference }, modifier = Modifier.fillMaxWidth().testTag("drill-toggle-form")) {
                        Text("Movement and form", modifier = Modifier.weight(1f))
                        Icon(if (showFormReference) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null)
                    }
                    if (showFormReference) {
                        DrillFormReference(drill)
                    }
                }
                CourtEyebrow("TECHNIQUE & COACHING")
                Text("Start with the steps below. Open court positioning, movement reference, or coaching corrections whenever you need them.",
                    color = CourtQuestDesign.textSecondary, style = MaterialTheme.typography.bodySmall)
                OutlinedButton(onClick = { showTechnique = !showTechnique }, modifier = Modifier.fillMaxWidth().testTag("drill-toggle-technique")) {
                    Text("Step-by-step technique", modifier = Modifier.weight(1f))
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
                    Text("Coaching tips and corrections", modifier = Modifier.weight(1f))
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
                        Text("Target: ${drill.target}",
                            color = CourtQuestDesign.accent, style = MaterialTheme.typography.labelLarge)
                    }
                }
                }
                OutlinedButton(onUse, enabled = canUse, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) { Text("Use in daily workout") }
                if (!canUse) Text("A swap needs an untouched ${drill.category.lowercase()} slot and a drill at or below your selected level.",
                    color = CourtQuestDesign.textSecondary, style = MaterialTheme.typography.bodySmall)
                if (save.customDrills.orEmpty().any { it.drill.id == drill.id && !it.archived }) {
                    CourtRule()
                    OutlinedButton(onEdit, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("Edit custom drill") }
                    TextButton({ confirmArchive = true }, modifier = Modifier.heightIn(min = 48.dp)) { Text("Archive drill") }
                }
            }
        }
    }
}
