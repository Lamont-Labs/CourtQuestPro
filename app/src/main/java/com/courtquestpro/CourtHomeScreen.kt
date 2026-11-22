package com.courtquestpro

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.time.LocalDate

@OptIn(ExperimentalLayoutApi::class)
@Suppress("UNUSED_PARAMETER")
@Composable
internal fun HomeScreen(
    save: SaveState, onSpin: () -> Unit, onStartChamp: () -> Unit, onCareer: () -> Unit,
    onSaveName: (String) -> Unit, onWeeklyPlan: (String) -> Unit,
    onTrainingDay: (Int) -> Unit, onActivity: (Int, String) -> Unit,
    onLibrary: () -> Unit, onShuffle: () -> Unit, onSwap: (String) -> Unit, onResumePractice: () -> Unit, onMissionBuilder: () -> Unit, onResumeMission: () -> Unit,
    onQuests: () -> Unit = {}, onDrills: () -> Unit = onLibrary
) {
    var showWeeklyEditor by remember { mutableStateOf(false) }
    var newPlayerName by remember { mutableStateOf("") }
    val today = LocalDate.now()
    val drills = TrainingLibrary.catalog(save)
    val activeMission = MissionTraining.active(save)
    val resumeMatch = save.hasResumableMatch(today.toString())
    val done = save.slots.count { it.complete }
    val allDailyDrillsDone = save.slots.isNotEmpty() && done == save.slots.size
    val nextSlot = nextDailySlot(save)?.let(save.slots::get)
    val nextDrill = nextSlot?.let { slot -> drills.firstOrNull { it.id == slot.drillId } }
    val resumable = nextSlot != null && nextDrill != null &&
        recoverDailyDraft(save, nextSlot, nextDrill) != null
    val missionVersion = activeMission?.let { MissionTraining.version(save, it) }
    val missionIndex = activeMission?.let { MissionTraining.currentIndex(save, it) }
    val weekStart = today.with(java.time.DayOfWeek.MONDAY)
    val weekEnd = weekStart.plusDays(6)
    val trainedThisWeek = save.drillResults.orEmpty().filter {
        it.attempted > 0 && it.completed in 0..it.attempted &&
            runCatching { LocalDate.parse(it.date) in weekStart..weekEnd }.getOrDefault(false)
    }.map { it.date }.distinct().size
    val plannedDays = save.trainingDays?.filter { it in 1..7 }?.distinct() ?: listOf(1, 3, 5)
    val dayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val primaryLabel = when {
        activeMission != null -> "Resume Mission"
        allDailyDrillsDone && resumeMatch -> "Resume 1-on-1"
        allDailyDrillsDone -> "Play 1-on-1"
        resumable -> "Resume session"
        else -> "Start training"
    }
    val primaryAction = if (activeMission != null) onResumeMission else onSpin

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, top = 10.dp, end = 16.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            CourtArenaHero(
                R.drawable.court_arena_player,
                Modifier.fillMaxWidth().heightIn(min = 160.dp).testTag("court-player-hero"),
                imageAlignment = Alignment.TopCenter
            ) {
                Column(
                    Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(painterResource(R.drawable.courtquest_signature_wordmark), "CourtQuest Pro",
                            Modifier.width(130.dp).height(36.dp))
                        Spacer(Modifier.weight(1f))
                        Surface(
                            color = CourtQuestDesign.background.copy(alpha = .68f),
                            shape = RoundedCornerShape(50),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp, CourtQuestDesign.accent.copy(alpha = .42f)
                            )
                        ) {
                            Text(
                                "XP · ${rankFor(save.totalXp).uppercase()}",
                                Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
                                color = CourtQuestDesign.accent,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        CourtJerseyArtwork(save.playerJersey, Modifier.size(66.dp).testTag("court-player-jersey"),
                            describe = true)
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(
                                save.playerName.ifBlank { "Your next session" },
                                color = CourtQuestDesign.textPrimary,
                                style = MaterialTheme.typography.headlineLarge,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.semantics { heading() }
                            )
                            Text("${save.difficulty} · ${save.weeklyPlan} focus",
                                color = CourtQuestDesign.textSecondary, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
        item {
            CourtQuestPremiumSurface(
                Modifier.fillMaxWidth().testTag("court-focus-card"),
                PaddingValues(14.dp),
                highlighted = true
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            CourtEyebrow(
                                when {
                                    activeMission != null -> "MISSION READY"
                                    allDailyDrillsDone -> "TODAY COMPLETE"
                                    resumable -> "SESSION PAUSED"
                                    else -> "TODAY'S PLAN"
                                }
                            )
                            Text(
                                when {
                                    activeMission != null -> missionVersion?.title ?: "Your Mission"
                                    allDailyDrillsDone -> "Take your work to the court."
                                    resumable -> nextDrill?.title ?: "Pick up where you left off."
                                    else -> nextDrill?.title ?: "Your next drill is ready."
                                },
                                color = CourtQuestDesign.textPrimary,
                                style = MaterialTheme.typography.titleLarge,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            val detail = when {
                                missionVersion != null && missionIndex != null ->
                                    "Drill ${missionIndex + 1} of ${missionVersion.steps.size}"
                                activeMission != null -> "Continue your saved Mission"
                                allDailyDrillsDone -> "$done of ${save.slots.size} drills recorded"
                                nextDrill != null -> "${nextDrill.category} · ${nextDrill.minutes} min · $done of ${save.slots.size} recorded"
                                else -> "$done of ${save.slots.size} drills recorded"
                            }
                            Text(detail, color = CourtQuestDesign.textSecondary,
                                style = MaterialTheme.typography.bodySmall)
                        }
                        Icon(
                            if (allDailyDrillsDone) Icons.Default.CheckCircle else Icons.Default.Bolt,
                            null,
                            tint = CourtQuestDesign.accentVivid,
                            modifier = Modifier.size(25.dp)
                        )
                    }
                    CourtQuestPrimaryButton(
                        primaryAction,
                        Modifier.fillMaxWidth().heightIn(min = 52.dp).testTag("court-todays-focus")
                    ) {
                        Text(primaryLabel, Modifier.weight(1f), style = MaterialTheme.typography.labelLarge)
                        Icon(Icons.Default.ArrowForward, null, Modifier.size(20.dp))
                    }
                }
            }
        }
        if (save.practiceSession != null) item {
            OutlinedButton(
                onClick = onResumePractice,
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.PlayArrow, null)
                Spacer(Modifier.width(8.dp))
                Text("Resume extra practice", Modifier.weight(1f))
                Icon(Icons.Default.ChevronRight, null)
            }
        }
        item {
            CourtQuestPremiumSurface(
                Modifier.fillMaxWidth(),
                PaddingValues(horizontal = 14.dp, vertical = 12.dp)
            ) {
                CourtMetricStrip(
                    listOf(
                        "Participation XP" to save.totalXp.toString(),
                        "Day streak" to save.currentStreak.toString(),
                        "Days this week" to if (plannedDays.isEmpty()) trainedThisWeek.toString()
                        else "$trainedThisWeek / ${plannedDays.size}"
                    )
                )
            }
        }
        save.drillResults.orEmpty().lastOrNull {
            it.attempted > 0 && it.completed in 0..it.attempted
        }?.let { latest ->
            item {
                val title = latest.drillSnapshot?.title
                    ?: drills.firstOrNull { it.id == latest.drillId }?.title
                    ?: "Saved drill"
                CourtQuestPremiumSurface(
                    Modifier.fillMaxWidth().clickable(onClick = onCareer)
                        .testTag("court-latest-result"),
                    PaddingValues(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Default.Insights, null, tint = CourtQuestDesign.accentVivid)
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            CourtEyebrow("LATEST SAVED RESULT")
                            Text(title, color = CourtQuestDesign.textPrimary,
                                style = MaterialTheme.typography.titleMedium, maxLines = 1,
                                overflow = TextOverflow.Ellipsis)
                            Text("${latest.completed} / ${latest.attempted} successful · ${latest.completed * 100 / latest.attempted}% success",
                                color = CourtQuestDesign.textSecondary,
                                style = MaterialTheme.typography.bodySmall)
                            Text("Self-recorded · Tap to view Progress",
                                color = CourtQuestDesign.textSecondary,
                                style = MaterialTheme.typography.labelSmall)
                        }
                        Icon(Icons.Default.ChevronRight, null, tint = CourtQuestDesign.textSecondary)
                    }
                }
            }
        }
        item {
            FlowRow(
                Modifier.fillMaxWidth(),
                maxItemsInEachRow = if (LocalDensity.current.fontScale > 1.3f) 1 else 2,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onDrills,
                    modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("court-shortcut-drills"),
                    contentPadding = PaddingValues(horizontal = 10.dp)
                ) {
                    Icon(Icons.Default.Search, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "Find a drill",
                        modifier = Modifier.semantics { contentDescription = "Drills" }
                    )
                }
                OutlinedButton(
                    onClick = onStartChamp,
                    modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp)
                ) {
                    Icon(Icons.Default.SportsBasketball, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(if (resumeMatch) "Resume 1-on-1" else "Play 1-on-1")
                }
            }
        }
        item {
            CourtHeading("Today's session", "Shuffle", {
                if (save.slots.any { !TrainingLibrary.locked(save, it) }) onShuffle()
            })
            Text(
                "Recorded and started drills stay protected.",
                Modifier.padding(top = 2.dp),
                color = CourtQuestDesign.textSecondary,
                style = MaterialTheme.typography.bodySmall
            )
        }
        items(save.slots.size) { index ->
            val slot = save.slots[index]
            val drill = drills.firstOrNull { it.id == slot.drillId }
            val locked = TrainingLibrary.locked(save, slot)
            CourtQuestPremiumSurface(
                Modifier.fillMaxWidth().testTag("court-session-slot-${index + 1}"),
                PaddingValues(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Row(
                    Modifier.fillMaxWidth().heightIn(min = 48.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(11.dp)
                ) {
                    Text(
                        (index + 1).toString().padStart(2, '0'),
                        color = CourtQuestDesign.accent,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            drill?.title ?: slot.drillId,
                            style = MaterialTheme.typography.titleMedium,
                            color = CourtQuestDesign.textPrimary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            "${drill?.category.orEmpty()} · ${drill?.minutes ?: 0} min",
                            style = MaterialTheme.typography.bodySmall,
                            color = CourtQuestDesign.textSecondary
                        )
                    }
                    if (slot.complete) {
                        Icon(Icons.Default.CheckCircle, "Completed", tint = CourtQuestDesign.accent)
                    } else if (locked) {
                        Icon(Icons.Default.PauseCircle, "Started, progress saved", tint = CourtQuestDesign.accent)
                    } else {
                        TextButton(
                            onClick = { onSwap(slot.id) },
                            modifier = Modifier.heightIn(min = 48.dp)
                                .semantics { contentDescription = "Swap ${drill?.title ?: "daily drill"}" }
                        ) { Text("Swap") }
                    }
                }
            }
        }
        item {
            CourtQuestPremiumSurface(Modifier.fillMaxWidth(), PaddingValues(16.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            CourtEyebrow("BUILD YOUR OWN ROUTINE")
                            Text("A session built around you.", color = CourtQuestDesign.textPrimary,
                                style = MaterialTheme.typography.titleLarge)
                        }
                        Icon(Icons.Default.Tune, null, tint = CourtQuestDesign.accent)
                    }
                    Text(
                        "Choose drills, set their order, and save a Mission you can return to.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CourtQuestDesign.textSecondary
                    )
                    OutlinedButton(
                        onClick = onMissionBuilder,
                        Modifier.fillMaxWidth().heightIn(min = 48.dp)
                    ) { Text("Build a Mission") }
                }
            }
        }
        item {
            CourtHeading("Your week", if (showWeeklyEditor) "Done" else "Edit", {
                showWeeklyEditor = !showWeeklyEditor
            })
            CourtQuestPremiumSurface(Modifier.fillMaxWidth(), PaddingValues(16.dp)) {
                Column(Modifier.animateContentSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        if (trainedThisWeek >= 3) "Your three-day training goal is recorded."
                        else "A little consistency adds up.",
                        style = MaterialTheme.typography.titleMedium,
                        color = CourtQuestDesign.textPrimary
                    )
                    Text(
                        "${save.weeklyPlan} focus · ${plannedDays.size} planned training days",
                        color = CourtQuestDesign.textSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (LocalDensity.current.fontScale > 1.3f) {
                        FlowRow(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            maxItemsInEachRow = 4
                        ) {
                            dayLabels.forEachIndexed { index, label ->
                                val day = index + 1
                                CourtWeekDay(label, day == today.dayOfWeek.value, day in plannedDays,
                                    Modifier.widthIn(min = 58.dp).heightIn(min = 48.dp))
                            }
                        }
                    } else {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            dayLabels.forEachIndexed { index, label ->
                                val day = index + 1
                                CourtWeekDay(label, day == today.dayOfWeek.value, day in plannedDays,
                                    Modifier.weight(1f).heightIn(min = 48.dp))
                            }
                        }
                    }
                    if (showWeeklyEditor) {
                        CourtRule()
                        CourtEyebrow("TRAINING FOCUS")
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Balanced", "Shooting", "Ball Handling", "Finishing").forEach { plan ->
                                FilterChip(
                                    selected = save.weeklyPlan == plan,
                                    onClick = { onWeeklyPlan(plan) },
                                    modifier = Modifier.heightIn(min = 48.dp),
                                    label = { Text(plan) }
                                )
                            }
                        }
                        CourtEyebrow("TRAINING DAYS")
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            dayLabels.forEachIndexed { index, label ->
                                FilterChip(
                                    selected = index + 1 in plannedDays,
                                    onClick = { onTrainingDay(index + 1) },
                                    modifier = Modifier.heightIn(min = 48.dp),
                                    label = { Text(label, maxLines = 1) }
                                )
                            }
                        }
                    }
                }
            }
        }
        if (save.playerName.isBlank()) item {
            CourtQuestPremiumSurface(Modifier.fillMaxWidth(), PaddingValues(16.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    CourtEyebrow("PLAYER PROFILE")
                    Text("Make this court yours.", color = CourtQuestDesign.textPrimary,
                        style = MaterialTheme.typography.titleLarge)
                    OutlinedTextField(
                        newPlayerName,
                        { newPlayerName = it.take(40) },
                        label = { Text("Player name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    CourtQuestPrimaryButton(
                        { onSaveName(newPlayerName) },
                        Modifier.fillMaxWidth(),
                        newPlayerName.isNotBlank()
                    ) { Text("Save player name") }
                }
            }
        }
    }
}

@Composable
private fun CourtWeekDay(label: String, isToday: Boolean, planned: Boolean, modifier: Modifier = Modifier) {
    Surface(
        modifier,
        shape = RoundedCornerShape(11.dp),
        color = if (planned) CourtQuestDesign.warmSurface else CourtQuestDesign.elevatedSurface,
        border = if (isToday) androidx.compose.foundation.BorderStroke(
            1.dp, CourtQuestDesign.accentVivid.copy(alpha = .75f)
        ) else null
    ) {
        Column(
            Modifier.padding(vertical = 7.dp, horizontal = 3.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(label, color = if (isToday) CourtQuestDesign.accent
                else CourtQuestDesign.textPrimary,
                style = MaterialTheme.typography.labelMedium, maxLines = 1)
            Text(if (planned) "Train" else "Rest",
                color = CourtQuestDesign.textSecondary,
                style = MaterialTheme.typography.labelSmall)
        }
    }
}
