package com.courtquestpro

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
internal fun CareerScreen(save: SaveState, onHome: () -> Unit) {
    var showGames by remember { mutableStateOf(false) }
    var showAllSessions by remember { mutableStateOf(false) }
    val recorded = save.drillResults.orEmpty()
    val valid = recorded.filter { it.attempted > 0 && it.completed in 0..it.attempted }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp)) {
        item { AppHeader("Your work, recorded.", "${rankFor(save.totalXp)} · ${save.playerName.ifBlank { "Player development" }}") }
        item {
            Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    CourtMetric("Total XP", save.totalXp.toString(), Modifier.weight(1f), accent = true)
                    CourtMetric("Sessions", recorded.size.toString(), Modifier.weight(1f))
                    CourtMetric("Training days", valid.map { it.date }.distinct().size.toString(), Modifier.weight(1f))
                }
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
                    save.matches.forEach { match ->
                        CourtQuestPremiumSurface(Modifier.fillMaxWidth()) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                CourtEyebrow(match.date)
                                Text("${match.player} vs ${match.opponent}", color = CourtQuestDesign.textPrimary,
                                    style = MaterialTheme.typography.titleMedium)
                                Text("${match.playerScore} — ${match.opponentScore}", color = CourtQuestDesign.accent,
                                    style = MaterialTheme.typography.displaySmall)
                                Text("${match.winner} won · +${match.xp} XP", color = CourtQuestDesign.textSecondary,
                                    style = MaterialTheme.typography.bodyMedium)
                            }
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
                    CourtHeading("Training activity")
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatCard("CURRENT STREAK", "${save.currentStreak} days", Modifier.weight(1f))
                        StatCard("LONGEST STREAK", "${save.longestStreak} days", Modifier.weight(1f))
                    }
                    CourtHeading("Recorded sessions")
                    val sessionHistory = recorded.asReversed()
                    (if (showAllSessions) sessionHistory else sessionHistory.take(3)).forEach { result ->
                        CourtQuestPremiumSurface(Modifier.fillMaxWidth()) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                CourtEyebrow(result.date)
                                Text(drills.firstOrNull { it.id == result.drillId }?.title ?: result.drillId,
                                    color = CourtQuestDesign.textPrimary, style = MaterialTheme.typography.titleMedium)
                                Text("${result.completed} successful / ${result.attempted} attempts",
                                    color = CourtQuestDesign.accent, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                    if (sessionHistory.size > 3) {
                        TextButton(onClick = { showAllSessions = !showAllSessions }, modifier = Modifier.heightIn(min = 48.dp)) {
                            Text(if (showAllSessions) "Show recent sessions" else "Show all ${sessionHistory.size} sessions")
                        }
                    }
                    CourtRule()
                    CourtHeading("By skill")
                    listOf("Shooting", "Ball Handling", "Finishing", "Defense", "Rebounding", "Passing").forEach { category ->
                        val results = valid.filter { result -> drills.any { it.id == result.drillId && it.category == category } }
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
                TextButton(onClick = onHome, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("Back to Court") }
            }
        }
    }
}
