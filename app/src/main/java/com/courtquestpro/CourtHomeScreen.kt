package com.courtquestpro

import androidx.compose.foundation.background
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun HomeScreen(
    save: SaveState, onSpin: () -> Unit, onStartChamp: () -> Unit, onCareer: () -> Unit,
    onSaveName: (String) -> Unit, onWeeklyPlan: (String) -> Unit,
    onTrainingDay: (Int) -> Unit, onActivity: (Int, String) -> Unit,
    onLibrary: () -> Unit, onShuffle: () -> Unit, onSwap: (String) -> Unit, onResumePractice: () -> Unit, onMissionBuilder: () -> Unit, onResumeMission: () -> Unit
) {
    var showWeeklyEditor by remember { mutableStateOf(false) }
    var newPlayerName by remember { mutableStateOf("") }
    val today = LocalDate.now()
    val drills = TrainingLibrary.catalog(save)
    val resumeMatch = save.hasResumableMatch(today.toString())
    val done = save.slots.count { it.complete }
    val resumable = save.slots.any { slot ->
        !slot.complete && save.drillDrafts.orEmpty().any {
            it.date == save.activeDate && it.slotId == slot.id && it.drillId == slot.drillId
        }
    }
    val weekStart = today.with(java.time.DayOfWeek.MONDAY)
    val weekEnd = weekStart.plusDays(6)
    val trainedThisWeek = save.drillResults.orEmpty().filter {
        it.attempted > 0 && it.completed in 0..it.attempted &&
            runCatching { LocalDate.parse(it.date) in weekStart..weekEnd }.getOrDefault(false)
    }.map { it.date }.distinct().size
    val plannedDays = save.trainingDays?.filter { it in 1..7 }?.distinct() ?: listOf(1, 3, 5)
    val dayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val todayActivity = save.weeklyActivities.orEmpty()
        .firstOrNull { it.startsWith("${today.dayOfWeek.value}:") }?.substringAfter(":")
    val minutes = save.slots.sumOf { slot -> drills.firstOrNull { it.id == slot.drillId }?.minutes ?: 0 }

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 12.dp)) {
        item {
            AppHeader(save.playerName.takeIf { it.isNotBlank() }?.let { "$it’s court." } ?: "Your court.",
                today.format(DateTimeFormatter.ofPattern("EEEE, MMMM d")))
        }
        item {
            Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                val activeMission = MissionTraining.active(save)
                if (activeMission != null) {
                    val version = MissionTraining.version(save, activeMission)
                    val next = MissionTraining.currentIndex(save, activeMission)
                    CourtQuestPremiumSurface(Modifier.fillMaxWidth(), PaddingValues(16.dp), highlighted = true) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            CourtEyebrow("MISSION IN PROGRESS")
                            Text(version?.title ?: "Your Mission", style = MaterialTheme.typography.headlineLarge, color = CourtQuestDesign.textPrimary)
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text((next ?: 0).toString().padStart(2, '0'), style = MaterialTheme.typography.displaySmall,
                                    color = CourtQuestDesign.accent)
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text("DRILLS RECORDED", style = MaterialTheme.typography.labelSmall, color = CourtQuestDesign.textSecondary)
                                    Text("of ${version?.steps?.size ?: 0} · Version ${version?.version ?: "?"}",
                                        style = MaterialTheme.typography.bodyMedium, color = CourtQuestDesign.textPrimary)
                                }
                            }
                            CourtQuestPrimaryButton(onResumeMission, Modifier.fillMaxWidth()) { Text("Resume Mission") }
                        }
                    }
                } else {
                CourtQuestPremiumSurface(Modifier.fillMaxWidth(), PaddingValues(0.dp), highlighted = true) {
                  Box(Modifier.fillMaxWidth()) {
                    Icon(painterResource(R.drawable.courtquest_half_court), contentDescription = null,
                        tint = CourtQuestDesign.accent.copy(alpha = 0.10f),
                        modifier = Modifier.size(160.dp).align(Alignment.TopEnd).offset(x = 62.dp, y = 18.dp))
                    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            CourtEyebrow("DAILY TRAINING", Modifier.weight(1f))
                            CourtStatus(if (done == 6) "Complete" else if (resumable) "In progress" else "Ready")
                        }
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(done.toString().padStart(2, '0'), style = MaterialTheme.typography.displaySmall,
                                color = CourtQuestDesign.accent)
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(when {
                                    done == 6 -> "Work put in."
                                    resumable -> "Back to work."
                                    else -> "Build your game."
                                }, style = MaterialTheme.typography.headlineMedium, color = CourtQuestDesign.textPrimary)
                                Text("OF ${save.slots.size} DRILLS  ·  $minutes MIN",
                                    style = MaterialTheme.typography.labelSmall, color = CourtQuestDesign.textSecondary)
                            }
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.semantics { contentDescription = "$done of 6 drills completed" }) {
                            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                save.slots.forEach { slot ->
                                    Box(Modifier.weight(1f).height(5.dp).clip(CircleShape)
                                        .background(if (slot.complete) CourtQuestDesign.accentVivid else CourtQuestDesign.border))
                                }
                            }
                            Text("$done of 6 drills completed", style = MaterialTheme.typography.labelMedium,
                                color = CourtQuestDesign.textSecondary)
                        }
                        CourtQuestPrimaryButton(onClick = onSpin, modifier = Modifier.fillMaxWidth()) {
                            Text(if (done == 6 && resumeMatch) "Resume 1-on-1" else if (done == 6) "Play 1-on-1" else if (resumable) "Resume session" else "Start training",
                                modifier = Modifier.weight(1f))
                            Icon(Icons.Default.ArrowForward, null, Modifier.size(20.dp))
                        }
                    }
                  }
                }
                }
                if (resumeMatch && done != 6) {
                    OutlinedButton(onClick = onStartChamp, modifier = Modifier.fillMaxWidth().heightIn(min = 72.dp),
                        shape = RoundedCornerShape(16.dp), contentPadding = PaddingValues(16.dp)) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Resume 1-on-1", style = MaterialTheme.typography.titleMedium)
                            Text("${save.matchPlayer} ${save.playerScore} · ${save.matchOpponent} ${save.opponentScore}",
                                color = CourtQuestDesign.textSecondary, style = MaterialTheme.typography.bodySmall)
                        }
                        Icon(Icons.Default.ArrowForward, null, Modifier.size(20.dp))
                    }
                }
                CourtQuestPremiumSurface(Modifier.fillMaxWidth(), PaddingValues(horizontal = 14.dp, vertical = 12.dp)) {
                    CourtMetricStrip(listOf("Total XP" to save.totalXp.toString(), "Day streak" to save.currentStreak.toString(),
                        "Days this week" to "$trainedThisWeek / 3"))
                }
                CourtRule()
                if (save.practiceSession != null) {
                    OutlinedButton(onResumePractice, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) { Text("Resume extra practice") }
                }
                CourtQuestPremiumSurface(Modifier.fillMaxWidth(), PaddingValues(14.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        CourtEyebrow("YOUR GAME PLAN")
                        Text("A session built around you.", style = MaterialTheme.typography.headlineMedium, color = CourtQuestDesign.textPrimary)
                        Text("Choose your drills, set their order, and save a Mission you can return to.", style = MaterialTheme.typography.bodyMedium, color = CourtQuestDesign.textSecondary)
                        OutlinedButton(onMissionBuilder, Modifier.fillMaxWidth().heightIn(min = 52.dp)) { Text("Build a Mission") }
                    }
                }
                CourtHeading("Your session", "Progress", onCareer)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onLibrary, modifier = Modifier.heightIn(min = 48.dp)) { Text("Drill library") }
                    TextButton(onShuffle, enabled = save.slots.any { !TrainingLibrary.locked(save, it) },
                        modifier = Modifier.heightIn(min = 48.dp)) { Text("Shuffle remaining") }
                }
                Text("Completed and started drills stay protected.", style = MaterialTheme.typography.bodySmall,
                    color = CourtQuestDesign.textSecondary)
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    save.slots.forEachIndexed { index, slot ->
                        val drill = drills.firstOrNull { it.id == slot.drillId }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text((index + 1).toString().padStart(2, '0'), color = CourtQuestDesign.accent,
                                style = MaterialTheme.typography.headlineSmall, modifier = Modifier.width(28.dp))
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(drill?.title ?: slot.drillId, style = MaterialTheme.typography.titleMedium,
                                    color = CourtQuestDesign.textPrimary)
                                Text("${drill?.category.orEmpty()} · ${drill?.minutes ?: 0} min",
                                    style = MaterialTheme.typography.bodySmall, color = CourtQuestDesign.textSecondary)
                            }
                            Icon(if (slot.complete) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                if (slot.complete) "Completed" else "Available",
                                tint = if (slot.complete) CourtQuestDesign.accent else CourtQuestDesign.textSecondary,
                                modifier = Modifier.size(20.dp))
                            if (!TrainingLibrary.locked(save, slot)) {
                                TextButton({ onSwap(slot.id) }, modifier = Modifier.heightIn(min = 48.dp)
                                    .semantics { contentDescription = "Swap ${drill?.title ?: "daily drill"}" }) { Text("Swap") }
                            }
                        }
                    }
                }
                CourtRule()
                CourtHeading("This week", if (showWeeklyEditor) "Done" else "Edit", { showWeeklyEditor = !showWeeklyEditor })
                CourtQuestPremiumSurface(Modifier.fillMaxWidth(), PaddingValues(18.dp)) {
                    Column(Modifier.animateContentSize(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text(when {
                            todayActivity == "Game" -> "Game day"
                            todayActivity == "Practice" -> "Team practice today"
                            today.dayOfWeek.value in plannedDays -> "A day to build your game"
                            else -> "Make room for recovery"
                        }, style = MaterialTheme.typography.titleLarge, color = CourtQuestDesign.textPrimary)
                        Text("${save.weeklyPlan} focus · ${plannedDays.size} planned training days",
                            color = CourtQuestDesign.textSecondary, style = MaterialTheme.typography.bodyMedium)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            dayLabels.forEachIndexed { index, label ->
                                val day = index + 1
                                val activity = save.weeklyActivities.orEmpty().firstOrNull { it.startsWith("$day:") }?.substringAfter(":")
                                Surface(shape = RoundedCornerShape(12.dp), color = if (day in plannedDays) CourtQuestDesign.warmSurface else CourtQuestDesign.elevatedSurface) {
                                    Column(Modifier.padding(horizontal = 10.dp, vertical = 9.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(label, color = if (day == today.dayOfWeek.value) CourtQuestDesign.accent else CourtQuestDesign.textPrimary,
                                            style = MaterialTheme.typography.labelMedium, maxLines = 1)
                                        Text(activity ?: if (day in plannedDays) "Train" else "Rest",
                                            color = CourtQuestDesign.textSecondary, style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }
                        if (showWeeklyEditor) {
                            CourtRule()
                            CourtEyebrow("TRAINING FOCUS")
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("Balanced", "Shooting", "Ball Handling", "Finishing").forEach { plan ->
                                    FilterChip(selected = save.weeklyPlan == plan, onClick = { onWeeklyPlan(plan) },
                                        modifier = Modifier.heightIn(min = 48.dp), label = { Text(plan) })
                                }
                            }
                            CourtEyebrow("TRAINING DAYS")
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                dayLabels.forEachIndexed { index, label ->
                                    FilterChip(selected = index + 1 in plannedDays, onClick = { onTrainingDay(index + 1) },
                                        modifier = Modifier.heightIn(min = 48.dp), label = { Text(label, maxLines = 1) })
                                }
                            }
                            CourtEyebrow("TEAM PRACTICES & GAMES")
                            dayLabels.forEachIndexed { index, label ->
                                Column {
                                    Text(label, color = CourtQuestDesign.textPrimary, style = MaterialTheme.typography.labelLarge)
                                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        val activity = save.weeklyActivities.orEmpty().firstOrNull { it.startsWith("${index + 1}:") }?.substringAfter(":")
                                        listOf("Practice", "Game").forEach { kind ->
                                            FilterChip(selected = activity == kind, onClick = { onActivity(index + 1, kind) },
                                                modifier = Modifier.heightIn(min = 48.dp), label = { Text(kind) })
                                        }
                                    }
                                }
                            }
                            Text("Tap a selected activity to clear it. Your choices repeat each week.",
                                color = CourtQuestDesign.textSecondary, style = MaterialTheme.typography.bodySmall)
                        }
                        Text(if (trainedThisWeek >= 3) "Your three-day training goal is recorded."
                            else "Record a drill on ${3 - trainedThisWeek} more day${if (3 - trainedThisWeek == 1) "" else "s"} to reach this week's goal.",
                            color = CourtQuestDesign.textSecondary, style = MaterialTheme.typography.bodySmall)
                    }
                }
                CourtHeading("Take it to the court")
                OutlinedButton(onClick = onStartChamp, modifier = Modifier.fillMaxWidth().heightIn(min = 76.dp),
                    shape = RoundedCornerShape(18.dp), contentPadding = PaddingValues(18.dp)) {
                    Icon(Icons.Default.SportsBasketball, null, Modifier.size(28.dp))
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(if (resumeMatch) "Resume 1-on-1" else "Play 1-on-1", style = MaterialTheme.typography.titleMedium)
                        Text(if (resumeMatch) "${save.matchPlayer} ${save.playerScore} · ${save.matchOpponent} ${save.opponentScore}" else "First to 11 · Live scorekeeping", style = MaterialTheme.typography.bodySmall,
                            color = CourtQuestDesign.textSecondary)
                    }
                    Icon(Icons.Default.ChevronRight, null)
                }
                if (save.playerName.isBlank()) {
                    CourtHeading("Make it yours")
                    OutlinedTextField(newPlayerName, { newPlayerName = it.take(40) }, label = { Text("Player name") },
                        singleLine = true, modifier = Modifier.fillMaxWidth())
                    CourtQuestPrimaryButton({ onSaveName(newPlayerName) }, Modifier.fillMaxWidth(), newPlayerName.isNotBlank()) {
                        Text("Save player name")
                    }
                }
            }
        }
    }
}
