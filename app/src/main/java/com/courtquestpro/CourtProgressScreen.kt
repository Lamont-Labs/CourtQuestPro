package com.courtquestpro

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.dp

@Composable
internal fun CareerScreen(save: SaveState, onHome: () -> Unit, initiallyShowGames: Boolean = false, onMissionReceipt: (String) -> Unit = {}) {
    val drills = TrainingLibrary.catalog(save)
    var showGames by rememberSaveable(initiallyShowGames) { mutableStateOf(initiallyShowGames) }
    var section by rememberSaveable { mutableStateOf("Overview") }
    var showAllSessions by rememberSaveable { mutableStateOf(false) }
    var showAllGames by rememberSaveable { mutableStateOf(false) }
    val recorded = save.drillResults.orEmpty()
    val valid = recorded.filter { it.attempted > 0 && it.completed in 0..it.attempted }
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
        BackHandler { showAllSessions = false }
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item { AppHeader("Training history", "${recorded.size} recorded sessions") }
            item {
                TextButton(onClick = { showAllSessions = false },
                    modifier = Modifier.padding(horizontal = 20.dp).heightIn(min = 48.dp)) { Text("Back to Progress") }
            }
            items(recorded.asReversed()) { result ->
                TrainingSessionCard(result, drills, Modifier.padding(horizontal = 20.dp))
            }
        }
        return
    }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 12.dp)) {
        item { AppHeader("Progress", "${rankFor(save.totalXp)} · ${save.playerName.ifBlank { "Player development" }}") }
        item { Row(Modifier.padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Overview", "Skills", "History").forEach { tab ->
                FilterChip(section == tab, { section = tab }, label = { Text(tab) }, modifier = Modifier.heightIn(min = 48.dp))
            }
        } }
        item {
            Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                if (section == "Overview") CourtQuestPremiumSurface(Modifier.fillMaxWidth(), PaddingValues(16.dp), highlighted = true) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        CourtEyebrow("YOUR TRAINING RECORD")
                        CourtMetricStrip(listOf("Total XP" to save.totalXp.toString(),
                            (if (showGames) "Games" else "Sessions") to (if (showGames) save.matches.size else recorded.size).toString(),
                            (if (showGames) "Game days" else "Training days") to
                                (if (showGames) save.matches.map { it.date }.distinct().size else valid.map { it.date }.distinct().size).toString()))
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
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FilterChip(!showGames, { showGames = false }, label = { Text("Training") }, modifier = Modifier.heightIn(min = 48.dp))
                    FilterChip(showGames, { showGames = true }, label = { Text("Games") }, modifier = Modifier.heightIn(min = 48.dp))
                }
                if (showGames) {
                    CourtHeading("Game history")
                    if (save.matches.isEmpty()) {
                        CourtQuestPremiumSurface(Modifier.fillMaxWidth(), PaddingValues(24.dp)) {
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
                    CourtQuestPremiumSurface(Modifier.fillMaxWidth(), PaddingValues(24.dp)) {
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
                    sessionHistory.take(3).forEach { result -> TrainingSessionCard(result, drills) }
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

@Composable
private fun TrainingSessionCard(result: DrillResult, catalog: List<Drill>, modifier: Modifier = Modifier) {
    CourtQuestPremiumSurface(modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            CourtEyebrow(if (result.missionSessionId?.startsWith("mission-session_") == true) "${result.date} · Mission" else if (result.practice) "${result.date} · Extra practice" else result.date)
            Text(result.drillSnapshot?.title ?: catalog.firstOrNull { it.id == result.drillId }?.title ?: "Saved drill",
                color = CourtQuestDesign.textPrimary, style = MaterialTheme.typography.titleMedium)
            Text("${result.completed} successful / ${result.attempted} attempts",
                color = CourtQuestDesign.accent, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
