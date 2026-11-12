package com.courtquestpro

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun MeScreen(save: SaveState, onEdit: () -> Unit, onProgress: () -> Unit) {
    val days = (save.trainingDays ?: listOf(1, 3, 5)).filter { it in 1..7 }.distinct().sorted()
    val dayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp)) {
        item { AppHeader("Your game. Your way.", "Your player profile and training setup.") }
        item {
            Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                CourtQuestPremiumSurface(Modifier.fillMaxWidth(), PaddingValues(24.dp), highlighted = true) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        CourtEyebrow("PLAYER PROFILE")
                        Text(save.playerName.ifBlank { "Your player profile" },
                            color = CourtQuestDesign.textPrimary, style = MaterialTheme.typography.headlineLarge)
                        Text("${save.difficulty} · ${save.weeklyPlan} focus",
                            color = CourtQuestDesign.textSecondary, style = MaterialTheme.typography.bodyLarge)
                        CourtQuestPrimaryButton(onEdit, Modifier.fillMaxWidth()) {
                            Icon(Icons.Default.Edit, null); Spacer(Modifier.width(8.dp)); Text("Edit profile")
                        }
                    }
                }
                CourtHeading("Your routine")
                Text(if (days.isEmpty()) "No training days selected. Set your routine on Court."
                    else days.joinToString(" · ") { dayLabels[it - 1] },
                    color = CourtQuestDesign.textPrimary, style = MaterialTheme.typography.titleLarge)
                Text("Your focus and weekly schedule can be adjusted on Court. Your level applies to new daily workouts.",
                    color = CourtQuestDesign.textSecondary, style = MaterialTheme.typography.bodyMedium)
                CourtRule()
                CourtHeading("The work you put in")
                CourtMetricStrip(listOf("Total XP" to save.totalXp.toString(),
                    "Recorded drills" to save.drillResults.orEmpty().size.toString(),
                    "Games" to save.matches.size.toString()))
                Text("XP tracks participation. Drill results show what you recorded; they are not an independently verified skill rating.",
                    color = CourtQuestDesign.textSecondary, style = MaterialTheme.typography.bodyMedium)
                OutlinedButton(onProgress, Modifier.fillMaxWidth().heightIn(min = 52.dp)) { Text("Open training history") }
                CourtRule()
                Icon(Icons.Default.PhoneAndroid, null, tint = CourtQuestDesign.accent)
                CourtHeading("Saved on this phone")
                Text("Your profile, Missions and results stay on this device. Cloud backup and team sharing are not connected.",
                    color = CourtQuestDesign.textSecondary, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
