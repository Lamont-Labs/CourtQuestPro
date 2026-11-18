package com.courtquestpro

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
@OptIn(ExperimentalLayoutApi::class)
internal fun CareerScreen(
    save: SaveState,
    onHome: () -> Unit,
    initiallyShowGames: Boolean = false,
    initiallyShowTrainingHistory: Boolean = false,
    onMissionReceipt: (String) -> Unit = {}
) {
    val drills = TrainingLibrary.catalog(save)
    var showGames by rememberSaveable(initiallyShowGames) { mutableStateOf(initiallyShowGames) }
    var section by rememberSaveable { mutableStateOf("Overview") }
    var showAllSessions by rememberSaveable(initiallyShowTrainingHistory) { mutableStateOf(initiallyShowTrainingHistory) }
    var showAllGames by rememberSaveable { mutableStateOf(false) }
    val recorded = save.drillResults.orEmpty()
    val valid = recorded.filter { it.attempted > 0 && it.completed in 0..it.attempted }
    val totalAttempts = valid.sumOf { it.attempted }
    val totalSuccessful = valid.sumOf { it.completed }
    val trainingRate = if (totalAttempts > 0) "${totalSuccessful * 100 / totalAttempts}%" else "—"
    val practicedDrills = valid.map { it.drillId }.distinct().size
    val recordCount = if (showGames) save.matches.size else recorded.size
    val recordedDays = if (showGames) save.matches.map { it.date }.distinct().size
        else valid.map { it.date }.distinct().size
    if (showAllGames) {
        BackHandler { showAllGames = false }
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item { AppHeader("Game history", "${save.matches.size} recorded games") }
            item {
                TextButton(onClick = { showAllGames = false },
                    modifier = Modifier.padding(horizontal = 20.dp).heightIn(min = 48.dp)) { Text("Back to Progress") }
            }
            items(save.matches) { match ->
                RecordedGameCard(match, Modifier.padding(horizontal = 20.dp))
            }
        }
        return
    }
    if (showAllSessions) {
        BackHandler {
            if (initiallyShowTrainingHistory) onHome() else showAllSessions = false
        }
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item { AppHeader("Training history", "${recorded.size} saved drill results") }
            item {
                TextButton(onClick = {
                    if (initiallyShowTrainingHistory) onHome() else showAllSessions = false
                },
                    modifier = Modifier.padding(horizontal = 20.dp).heightIn(min = 48.dp)) { Text("Back to Progress") }
            }
            items(recorded.asReversed()) { result ->
                TrainingSessionCard(result, drills, recorded, Modifier.padding(horizontal = 20.dp))
            }
        }
        return
    }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 12.dp)) {
        item { AppHeader("Progress", "${rankFor(save.totalXp)} · ${save.playerName.ifBlank { "Player development" }}") }
        item {
            CourtProgressSectionPicker(section) { section = it }
        }
        item {
            Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                if (section == "Overview") CourtProgressActivity(valid)
                if (section == "Overview") CourtQuestPremiumSurface(
                    Modifier.fillMaxWidth(), PaddingValues(16.dp), highlighted = true
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CourtEyebrow("PLAYER RECORD", Modifier.weight(1f))
                            Text(rankFor(save.totalXp).uppercase(), color = CourtQuestDesign.accent,
                                style = MaterialTheme.typography.labelSmall)
                        }
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            maxItemsInEachRow = if (LocalDensity.current.fontScale > 1.3f) 1 else 3) {
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                                Text(save.totalXp.toString(), color = CourtQuestDesign.accent,
                                    style = MaterialTheme.typography.displayMedium)
                                CourtEyebrow("TOTAL XP", color = CourtQuestDesign.textSecondary)
                            }
                            if (LocalDensity.current.fontScale <= 1.3f) {
                                Box(Modifier.width(1.dp).height(54.dp).background(CourtQuestDesign.border))
                            }
                            Column(Modifier.weight(1f).padding(start = 2.dp).testTag("progress-record-sessions"),
                                verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(recordCount.toString(), color = CourtQuestDesign.textPrimary,
                                    style = MaterialTheme.typography.headlineLarge)
                                Text(if (showGames) "GAMES" else "SESSIONS",
                                    color = CourtQuestDesign.textSecondary, style = MaterialTheme.typography.labelSmall)
                                Text("$recordedDays ${if (showGames) "game" else "training"} days",
                                    color = CourtQuestDesign.textSecondary, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        CourtRule()
                        Text("XP tracks participation. Results are self-recorded.",
                            color = CourtQuestDesign.textSecondary, style = MaterialTheme.typography.bodySmall)
                    }
                }
                if (section == "Overview") {
                    CourtQuestPremiumSurface(Modifier.fillMaxWidth(), PaddingValues(16.dp)) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            CourtEyebrow("TRAINING PERFORMANCE")
                            Text("Your recorded baseline", style = MaterialTheme.typography.titleLarge,
                                color = CourtQuestDesign.textPrimary)
                            CourtMetricStrip(listOf(
                                "Successful" to totalSuccessful.toString(),
                                "Attempts" to totalAttempts.toString(),
                                "Success rate" to trainingRate
                            ))
                            if (totalAttempts > 0) {
                                LinearProgressIndicator(
                                    progress = { totalSuccessful.toFloat() / totalAttempts },
                                    modifier = Modifier.fillMaxWidth().height(7.dp),
                                    color = CourtQuestDesign.accentVivid,
                                    trackColor = CourtQuestDesign.surface
                                )
                                Text("$practicedDrills distinct drills practiced across ${valid.size} valid saved results.",
                                    color = CourtQuestDesign.textSecondary,
                                    style = MaterialTheme.typography.bodySmall)
                            } else {
                                Text("Record your first drill to establish a training baseline.",
                                    color = CourtQuestDesign.textSecondary,
                                    style = MaterialTheme.typography.bodyMedium)
                            }
                            Text("Success rate combines self-recorded attempts across drills with different targets; use same-drill history for fairer comparisons.",
                                color = CourtQuestDesign.textSecondary,
                                style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                if (section == "History" && save.missionReceipts.orEmpty().isNotEmpty() && !showGames) {
                    CourtHeading("Completed Missions")
                    save.missionReceipts.orEmpty().asReversed().forEach { receipt ->
                        val version = save.missionVersions.orEmpty().firstOrNull { it.id == receipt.missionVersionId }
                        CourtQuestPremiumSurface(Modifier.fillMaxWidth()) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(version?.title ?: "Saved Mission", style = MaterialTheme.typography.titleLarge, color = CourtQuestDesign.textPrimary)
                                Text("Version ${version?.version ?: "?"} · ${receipt.resultIds.size} recorded drills · Self-recorded",
                                    style = MaterialTheme.typography.bodySmall, color = CourtQuestDesign.textSecondary)
                                TextButton({ onMissionReceipt(receipt.id) }, Modifier.heightIn(min = 48.dp)) { Text("View Mission receipt") }
                            }
                        }
                    }
                }
                if (section == "History") {
                CourtRule()
                CourtQuestSegmentRail(
                    labels = listOf("Training", "Games"),
                    selected = if (showGames) "Games" else "Training",
                    onSelect = { showGames = it == "Games" },
                    modifier = Modifier.fillMaxWidth()
                )
                if (showGames) {
                    CourtHeading("Game history")
                    if (save.matches.isEmpty()) {
                        CourtQuestPremiumSurface(Modifier.fillMaxWidth(), PaddingValues(18.dp)) {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Icon(Icons.Default.SportsBasketball, null, tint = CourtQuestDesign.accent, modifier = Modifier.size(32.dp))
                                Text("Your first match awaits.", color = CourtQuestDesign.textPrimary, style = MaterialTheme.typography.headlineMedium)
                                Text("Your players, final scores, and match XP will appear here after a game.",
                                    color = CourtQuestDesign.textSecondary, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                    save.matches.take(3).forEach { match -> RecordedGameCard(match) }
                    if (save.matches.size > 3) {
                        TextButton(onClick = { showAllGames = true }, modifier = Modifier.heightIn(min = 48.dp)) {
                            Text("View all ${save.matches.size} games")
                        }
                    }
                } else if (recorded.isEmpty()) {
                    CourtQuestPremiumSurface(Modifier.fillMaxWidth(), PaddingValues(18.dp)) {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Icon(Icons.Default.Insights, null, tint = CourtQuestDesign.accent, modifier = Modifier.size(36.dp))
                            Text("Start your baseline.", color = CourtQuestDesign.textPrimary, style = MaterialTheme.typography.headlineLarge)
                            Text("Complete a drill and record your attempts. Your history and same-drill comparisons will grow here.",
                                color = CourtQuestDesign.textSecondary, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                } else {
                    CourtHeading("Recorded sessions")
                    val sessionHistory = recorded.asReversed()
                    sessionHistory.take(3).forEach { result -> TrainingSessionCard(result, drills, recorded) }
                    if (sessionHistory.size > 3) {
                        TextButton(onClick = { showAllSessions = true }, modifier = Modifier.heightIn(min = 48.dp)) {
                            Text("View all ${sessionHistory.size} sessions")
                        }
                    }
                    }
                    if (section == "Overview") {
                    CourtRule()
                    CourtHeading("Training activity")
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatCard("CURRENT STREAK", "${save.currentStreak} ${if (save.currentStreak == 1) "day" else "days"}", Modifier.weight(1f))
                        StatCard("LONGEST STREAK", "${save.longestStreak} ${if (save.longestStreak == 1) "day" else "days"}", Modifier.weight(1f))
                    }
                    }
                    if (section == "Skills") {
                    CourtRule()
                    CourtHeading("By skill")
                    listOf("Shooting", "Ball Handling", "Finishing", "Defense", "Rebounding", "Passing").forEach { category ->
                        val results = valid.filter { result -> result.drillSnapshot?.category == category || drills.any { it.id == result.drillId && it.category == category } }
                        val attempts = results.sumOf { it.attempted }
                        val successful = results.sumOf { it.completed }
                        Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                            Text(category, color = CourtQuestDesign.textPrimary, style = MaterialTheme.typography.titleMedium)
                            if (attempts > 0) {
                                LinearProgressIndicator(progress = { (successful.toFloat() / attempts).coerceIn(0f, 1f) },
                                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                                    color = CourtQuestDesign.accentVivid, trackColor = CourtQuestDesign.elevatedSurface)
                                Text("$successful successful / $attempts attempts · ${results.size} sessions",
                                    color = CourtQuestDesign.textSecondary, style = MaterialTheme.typography.bodySmall)
                            } else Text("Your first result is ahead of you.", color = CourtQuestDesign.textSecondary,
                                style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    CourtRule()
                    CourtHeading("Same drill. New session.")
                    recorded.map { it.drillId }.distinct().forEach { id ->
                        val drillResults = valid.filter { it.drillId == id }
                        val best = drillResults.maxByOrNull { it.completed }
                        val trend = DrillTrends.calculate(id, recorded)
                        CourtQuestPremiumSurface(Modifier.fillMaxWidth()) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(drills.firstOrNull { it.id == id }?.title ?: id,
                                    color = CourtQuestDesign.textPrimary, style = MaterialTheme.typography.titleMedium)
                                if (best != null) {
                                    Text("Best · ${best.completed} successful / ${best.attempted} attempts",
                                        color = CourtQuestDesign.accent, style = MaterialTheme.typography.bodyMedium)
                                }
                                if (trend != null) {
                                    Text("${(trend.firstRate * 100).toInt()}% → ${(trend.latestRate * 100).toInt()}%",
                                        color = CourtQuestDesign.textPrimary, style = MaterialTheme.typography.headlineLarge)
                                    Text("First → latest · ${trend.sessions} valid sessions",
                                        color = CourtQuestDesign.textSecondary, style = MaterialTheme.typography.bodySmall)
                                } else Text("Record this drill again to establish a comparison.",
                                    color = CourtQuestDesign.textSecondary, style = MaterialTheme.typography.bodySmall)
                                Text("${drillResults.sumOf { it.attempted }} recorded attempts",
                                    color = CourtQuestDesign.textSecondary, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                    Text("Compare sessions for the same drill. Targets and conditions can differ. XP, streaks, and training days describe activity.",
                        color = CourtQuestDesign.textSecondary, style = MaterialTheme.typography.bodySmall)
                    }
                }
                TextButton(onClick = onHome, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("Back to Court") }
            }
        }
    }
}

private fun windowDateLabel(date: LocalDate): String = date.format(DateTimeFormatter.ofPattern("MMM d"))

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun CourtProgressActivity(results: List<DrillResult>) {
    val window = courtActivityWindow(results, LocalDate.now())
    val count = window.sumOf { it.count }
    CourtQuestPremiumSurface(Modifier.fillMaxWidth().testTag("progress-activity"), PaddingValues(16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                maxItemsInEachRow = if (LocalDensity.current.fontScale > 1.3f) 1 else 2) {
                CourtEyebrow("ACTIVITY", Modifier.weight(1f).testTag("progress-activity-heading"))
                Text("LAST 14 DAYS", color = CourtQuestDesign.textSecondary,
                    style = MaterialTheme.typography.labelSmall)
            }
            CourtActivityChart(results)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(windowDateLabel(LocalDate.now().minusDays(13)),
                    color = CourtQuestDesign.textSecondary, style = MaterialTheme.typography.labelSmall)
                Text("TODAY", color = CourtQuestDesign.accent, style = MaterialTheme.typography.labelSmall)
            }
            Text(if (count == 0) "No recorded activity in the last 14 days."
                else "$count ${if (count == 1) "result" else "results"} · ${window.count { it.count > 0 }} active ${if (window.count { it.count > 0 } == 1) "day" else "days"}",
                color = CourtQuestDesign.textSecondary, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun CourtActivityChart(results: List<DrillResult>) {
    val window = courtActivityWindow(results, LocalDate.now())
    val daily = window.map { it.count }
    val maximum = (daily.maxOrNull() ?: 0).coerceAtLeast(1)
    Canvas(Modifier.fillMaxWidth().height(132.dp).semantics {
        contentDescription = if (daily.sum() == 0) "No recorded training activity in the last 14 days"
        else "Recorded drills per day, ${window.first().date} to ${window.last().date}. " +
            window.joinToString(". ") { "${it.date}: ${it.count}" }
    }) {
        val baseline = size.height - 10.dp.toPx()
        val left = 3.dp.toPx()
        val gap = 5.dp.toPx()
        val barWidth = (size.width - left * 2 - gap * (daily.size - 1)) / daily.size
        listOf(0f, .5f, 1f).forEach { level ->
            val y = baseline - (baseline - 8.dp.toPx()) * level
            drawLine(Color(0xFF292A2C), Offset(left, y), Offset(size.width - left, y), 1.dp.toPx())
        }
        daily.forEachIndexed { index, count ->
            if (count > 0) {
                val height = (baseline - 8.dp.toPx()) * count / maximum
                val x = left + index * (barWidth + gap)
                drawRoundRect(
                    brush = Brush.verticalGradient(listOf(CourtQuestDesign.accent, CourtQuestDesign.accentVivid)),
                    topLeft = Offset(x, baseline - height),
                    size = androidx.compose.ui.geometry.Size(barWidth, height),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(barWidth / 2, barWidth / 2)
                )
            }
        }
    }
}

@Composable
private fun TrainingSessionCard(result: DrillResult, catalog: List<Drill>, history: List<DrillResult>, modifier: Modifier = Modifier) {
    val kind = when {
        result.missionSessionId?.startsWith("mission-session_") == true -> "Mission"
        result.practice -> "Extra practice"
        else -> "Training"
    }
    val comparable = catalog.firstOrNull { it.id == result.drillId }
    val rate = if (result.attempted > 0 && result.completed in 0..result.attempted)
        result.completed.toFloat() / result.attempted else null
    // Only compare earlier results for the same drill and attempt count.
    // This avoids presenting changes in drill difficulty or volume as improvement.
    val position = history.indexOfLast { it.slotId == result.slotId }
    val previous = if (position > 0) history.subList(0, position).lastOrNull {
        it.drillId == result.drillId && it.attempted == result.attempted &&
            it.attempted > 0 && it.completed in 0..it.attempted
    } else null
    CourtQuestPremiumSurface(modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            CourtEyebrow("${result.date} · $kind")
            Text(result.drillSnapshot?.title ?: comparable?.title ?: "Saved drill",
                color = CourtQuestDesign.textPrimary, style = MaterialTheme.typography.titleMedium)
            CourtMetricStrip(listOf(
                "Successful" to result.completed.toString(),
                "Attempts" to result.attempted.toString(),
                "Success rate" to (rate?.let { "${(it * 100).toInt()}%" } ?: "—")
            ))
            if (rate != null) {
                LinearProgressIndicator(
                    progress = { rate },
                    modifier = Modifier.fillMaxWidth().height(6.dp),
                    color = CourtQuestDesign.accentVivid,
                    trackColor = CourtQuestDesign.surface
                )
            }
            if (previous != null && rate != null) {
                val difference = result.completed - previous.completed
                val label = when {
                    difference > 0 -> "+$difference successful vs previous same-volume session"
                    difference < 0 -> "$difference successful vs previous same-volume session"
                    else -> "Matched previous same-volume session"
                }
                Text(label, color = if (difference > 0) CourtQuestDesign.accentVivid
                    else CourtQuestDesign.textSecondary,
                    style = MaterialTheme.typography.bodySmall)
            }
            val target = result.drillSnapshot?.target ?: comparable?.target
            if (!target.isNullOrBlank()) {
                Text(target, color = CourtQuestDesign.textSecondary,
                    style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
